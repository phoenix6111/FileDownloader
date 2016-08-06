package wanghaisheng.com.multithreaddownloader.beans;

import java.io.Serializable;

/**
 * Created by sheng on 2016/8/5.
 */
public class DownloadTaskBean implements Serializable{

    //下载文件id，标识，以md5编码url
    private String taskId;
    //下载文件的url
    private String url;
    //需下载文件的总的大小
    private Long fileTotalSize;
    //上一次已下载的文件的大小
    private Long completedSize;
    //下载文件的保存dir
    private String saveDirPath;
    //下载文件的文件名
    private String fileName;
    //上一次下载时的状态：INIT,PREPARE,START,DOWNLOADING,CANCEL,PAUSE,COMPLETED,ERROR
    private int downloadStatus;

    public DownloadTaskBean(String taskId, String url, Long fileTotalSize, Long completedSize, String saveDirPath, String fileName, int downloadStatus) {
        this.taskId = taskId;
        this.url = url;
        this.fileTotalSize = fileTotalSize;
        this.completedSize = completedSize;
        this.saveDirPath = saveDirPath;
        this.fileName = fileName;
        this.downloadStatus = downloadStatus;
    }

    public DownloadTaskBean() {
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Long getFileTotalSize() {
        return fileTotalSize;
    }

    public void setFileTotalSize(Long fileTotalSize) {
        this.fileTotalSize = fileTotalSize;
    }

    public Long getCompletedSize() {
        return completedSize;
    }

    public void setCompletedSize(Long completedSize) {
        this.completedSize = completedSize;
    }

    public String getSaveDirPath() {
        return saveDirPath;
    }

    public void setSaveDirPath(String saveDirPath) {
        this.saveDirPath = saveDirPath;
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
}
