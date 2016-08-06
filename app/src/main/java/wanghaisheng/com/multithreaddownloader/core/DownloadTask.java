package wanghaisheng.com.multithreaddownloader.core;

import android.text.TextUtils;

import com.apkfuns.logutils.LogUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import wanghaisheng.com.multithreaddownloader.beans.DownloadException;
import wanghaisheng.com.multithreaddownloader.beans.DownloadStatus;
import wanghaisheng.com.multithreaddownloader.beans.DownloadTaskBean;
import wanghaisheng.com.multithreaddownloader.database.DownloadTaskBeanDao;

/**
 * Created by sheng on 2016/8/5.
 * 下载任务类，继承自runnable接口
 */
public class DownloadTask implements Runnable,ProgressListener {

    private DownloadTaskBeanDao taskDao;
    private DownloadTaskBean taskBean;
    private OkHttpClient okHttpClient;

    private String taskId;
    //文件的总大小
    private long fileTotalSize;
    //文件已经下载的size
    private long completedSize;
    //文件下载的url
    private String url;
    //文件保存的目录
    private String savePathDir;
    //保存时的文件名
    private String fileName;
    private RandomAccessFile downloadFile;
    private int UPDATE_SIZE = 50 * 1024;//每下载50k则将记录保存进数据库
    //初始为init状态
    private int downloadStatus = DownloadStatus.DOWNLOAD_STATUS_INIT;

    private List<DownloadTaskListener> taskListeners = new ArrayList<>();

    @Override
    public void run() {
        LogUtils.d("on run  ");

        //准备状态
        downloadStatus = DownloadStatus.DOWNLOAD_STATUS_PREPARE;
        onPrepare();

        InputStream in = null;
        BufferedInputStream bufferedIn = null;

        try {
            //从数据库中获取该task的数据
            taskBean = taskDao.query(taskId);
            downloadFile = new RandomAccessFile(savePathDir+fileName,"rwd");
            if(taskBean != null) {
                completedSize = taskBean.getCompletedSize();
                fileTotalSize = taskBean.getFileTotalSize();
            }

            //比较文件已经下载的长度和数据库中保存的已下载的长度，因为有时文件数据还没保存进文件，但下载完的size已经保存进数据库了
            long fileLength = downloadFile.length();
            if(fileLength < completedSize) {
                completedSize = fileLength;
            }


            //如果已下载文件的大小等于文件的总大小，则表示文件已下载完成
            if(fileLength != 0 && fileTotalSize <= fileLength) {
                setDownloadStatus(DownloadStatus.DOWNLOAD_STATUS_COMPLETED);
                fileTotalSize = completedSize = fileLength;

                //保存内容进数据库
                taskBean = new DownloadTaskBean(taskId,url,fileTotalSize,completedSize,savePathDir,fileName,downloadStatus);
                taskDao.updateTask(taskBean);
                onCompleted();

                return;
            }

            LogUtils.d("start download ");

            //start 状态
            downloadStatus = DownloadStatus.DOWNLOAD_STATUS_START;
            onStart();

            //开始下载
            Request request = new Request.Builder()
                    .header("RANGE", "bytes=" + completedSize + "-")//断点续传
                    .url(url)
                    .build();
            LogUtils.d("before success ful1111");
//            LogUtils.d(okHttpClient);
            Response response = okHttpClient.newCall(request).execute();
            LogUtils.d(response);
            if(response != null && response.isSuccessful()) {

                ResponseBody responseBody = response.body();
                if(responseBody != null) {
                    downloadStatus = DownloadStatus.DOWNLOAD_STATUS_DOWNLOADING;
                    onDownloading();

                    LogUtils.d("into successful  ");

                    //如果fileTotalSize 小于或等于0 ，则表示是还没开始下载
                    if(fileTotalSize <= 0) {
                        fileTotalSize = responseBody.contentLength();
                        taskBean.setFileTotalSize(fileTotalSize);
                        String subType = responseBody.contentType().subtype();
                        LogUtils.d(response.headers());
                        LogUtils.d("subtype  "+subType);
                        LogUtils.d("contenttype  "+responseBody.contentType());
                        String resultName = fileName+"."+subType;
                        taskBean.setFileName(resultName);
                        taskDao.udpateOrInsert(taskBean);
                    }

                    //返回的没有Content-Range 不支持断点下载 需要重新下载
                    if(TextUtils.isEmpty(response.header("Content-Range"))) {
                        File alreadyDownloadFile = new File(savePathDir,fileName);
                        if(alreadyDownloadFile.exists()) {
                            alreadyDownloadFile.delete();
                        }

                        downloadFile = new RandomAccessFile(savePathDir+fileName,"rwd");
                        completedSize = 0;
                    }

                    downloadFile.seek(completedSize);

                    in = responseBody.byteStream();
                    bufferedIn = new BufferedInputStream(in);

                    byte[] buffer = new byte[2*1024];
                    int bufferOffset = 0;
                    int readLen = 0;

                    if(taskBean != null) {
                        taskBean = new DownloadTaskBean(taskId,url,fileTotalSize,0L,savePathDir,fileName,downloadStatus);
                        taskDao.udpateOrInsert(taskBean);
                    }

                    while ((readLen = bufferedIn.read(buffer))>0 && downloadStatus!=DownloadStatus.DOWNLOAD_STATUS_PAUSE
                            && downloadStatus != DownloadStatus.DOWNLOAD_STATUS_CANCEL ) {
                        downloadFile.write(buffer,0,readLen);
                        bufferOffset += readLen;
                        completedSize += readLen;

                        //如果下载的文件size大于规定可保存进数据库的size，则将下载进度保存进数据库
                        if(bufferOffset >= UPDATE_SIZE) {
                            //保存下载信息进数据库
                            taskBean.setCompletedSize(completedSize);
                            taskDao.updateTask(taskBean);

                            onDownloading();
                        }
                    }

                    //循环外再执行一次，防止文件总大小小于UPDATE_SIZE，或者最后一次的时候小于UPDATE_SIZE
                    taskBean.setCompletedSize(completedSize);
                    taskDao.updateTask(taskBean);
                    onDownloading();

                }

            } else {
                LogUtils.d("error......");
                //下载失败
                downloadStatus = DownloadStatus.DOWNLOAD_STATUS_ERROR;
                onError(DownloadTaskListener.DOWNLOAD_ERROR_TYPE_IO_ERROR,new IOException("网络错误"));
            }
        } catch (FileNotFoundException e) {
            LogUtils.d(e);
            downloadStatus = DownloadStatus.DOWNLOAD_STATUS_ERROR;
            onError(DownloadTaskListener.DOWNLOAD_ERROR_TYPE_FILE_NOT_FOUND,e);
            return;
        } catch (IOException e) {
            LogUtils.d(e);
            downloadStatus = DownloadStatus.DOWNLOAD_STATUS_ERROR;
            onError(DownloadTaskListener.DOWNLOAD_ERROR_TYPE_IO_ERROR,e);
            return;
        } finally {
            taskBean.setCompletedSize(completedSize);
            taskDao.udpateOrInsert(taskBean);
            //关闭流
            if(bufferedIn != null) {
                try {
                    bufferedIn.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(downloadFile != null) {
                try {
                    downloadFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if(fileTotalSize == completedSize) {
            downloadStatus = DownloadStatus.DOWNLOAD_STATUS_COMPLETED;
            taskBean.setCompletedSize(completedSize);
            taskBean.setDownloadStatus(downloadStatus);
            taskDao.updateTask(taskBean);
        }

        switch (downloadStatus) {
            case DownloadStatus.DOWNLOAD_STATUS_COMPLETED:
                onCompleted();
                break;
            case DownloadStatus.DOWNLOAD_STATUS_PAUSE:
                onPause();
                break;
            case DownloadStatus.DOWNLOAD_STATUS_CANCEL:
                taskDao.deleteTask(taskBean);
                File tFile = new File(savePathDir+fileName);
                if(tFile.exists()) {
                    tFile.delete();
                }
                onCancel();
                break;
        }
    }

    @Override
    public void update(long bytesRead, long contentLength, boolean done) {

    }

    public void setOkHttpClient(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    public void setTaskDao(DownloadTaskBeanDao taskDao) {
        this.taskDao = taskDao;
    }

    /**
     * 文件下载进度
     * @return
     */
    public float getPercent() {
        return fileTotalSize==0?0: completedSize* 100 / fileTotalSize;
    }

    /**
     * 取消下载
     */
    public void cancel() {
        setDownloadStatus(DownloadStatus.DOWNLOAD_STATUS_CANCEL);
        //删除已经下载的文件
        File downLoadFile = new File(savePathDir,fileName);
        if(downLoadFile.exists()) {
            downLoadFile.delete();
        }
    }

    /**
     * 暂停下载
     */
    public void pause() {
        setDownloadStatus(DownloadStatus.DOWNLOAD_STATUS_PAUSE);
    }

    /**
     * prepare状态回调
     */
    private void onPrepare() {
        for(DownloadTaskListener listener : taskListeners) {
            listener.onPrepare(this);
        }
    }

    private void onStart() {
        for(DownloadTaskListener listener : taskListeners) {
            listener.onStart(this);
        }
    }

    private void onDownloading() {
        for(DownloadTaskListener listener : taskListeners) {
            listener.onDownloading(this);
        }
    }

    /**
     * 更新进度
     */
    private void onProgress() {
        for(DownloadTaskListener listener : taskListeners) {
            listener.onProgress(this);
        }
    }

    private void onCompleted() {
        for(DownloadTaskListener listener : taskListeners) {
            listener.onCompleted(this);
        }
    }

    private void onPause() {
        for(DownloadTaskListener listener : taskListeners) {
            listener.onPause(this);
        }
    }

    private void onCancel() {
        for(DownloadTaskListener listener : taskListeners) {
            listener.onCancel(this);
        }
    }

    private void onError(int errorCode,Exception e) {
        for(DownloadTaskListener listener : taskListeners) {
            DownloadException exception = new DownloadException(e,errorCode);

            listener.onError(this,exception);
        }
    }

    public void addDownloadListener(DownloadTaskListener listener) {
        taskListeners.add(listener);
    }

    /**
     * if listener is null,clear all listener
     * @param listener
     */
    public void removeDownloadListener(DownloadTaskListener listener) {
        if(listener!=null){
            taskListeners.remove(listener);
        }
    }

    public void removeAllDownloadListener(){
        this.taskListeners.clear();
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Long getFileTotalSize() {
        return fileTotalSize;
    }

    public void setFileTotalSize(Long fileTotalSize) {
        this.fileTotalSize = fileTotalSize;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSavePathDir() {
        return savePathDir;
    }

    public void setSavePathDir(String savePathDir) {
        this.savePathDir = savePathDir;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getDownloadStatus() {
        return downloadStatus;
    }

    public void setDownloadStatus(int downloadStatus) {
        this.downloadStatus = downloadStatus;
    }

    public long getCompletedSize() {
        return completedSize;
    }

    public void setCompletedSize(long completedSize) {
        this.completedSize = completedSize;
    }

    public DownloadTaskBean getTaskBean() {
        return taskBean;
    }

    public void setTaskBean(DownloadTaskBean taskBean) {
        this.taskBean = taskBean;
    }

    public static DownloadTask parse(DownloadTaskBean taskBean) {
        DownloadTask task = new DownloadTask();
        task.setTaskId(taskBean.getTaskId());
        task.setUrl(taskBean.getUrl());
        task.setFileName(taskBean.getFileName());
        task.setSavePathDir(taskBean.getSaveDirPath());
        task.setCompletedSize(taskBean.getCompletedSize());
        task.setTaskBean(taskBean);
        task.setFileTotalSize(taskBean.getFileTotalSize());
        task.setDownloadStatus(taskBean.getDownloadStatus());

        return task;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof DownloadTask) {
            DownloadTask source = (DownloadTask) obj;
            return source.getTaskId().equals(getTaskId());
        }

        return super.equals(obj);
    }


}
