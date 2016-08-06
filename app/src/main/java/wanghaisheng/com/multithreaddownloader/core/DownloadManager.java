package wanghaisheng.com.multithreaddownloader.core;

import android.content.Context;

import com.apkfuns.logutils.LogUtils;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import okhttp3.OkHttpClient;
import wanghaisheng.com.multithreaddownloader.beans.DownloadStatus;
import wanghaisheng.com.multithreaddownloader.beans.DownloadTaskBean;
import wanghaisheng.com.multithreaddownloader.database.DownloadTaskBeanDao;
import wanghaisheng.com.multithreaddownloader.database.DownloadTaskBeanDaoImpl;

/**
 * Created by sheng on 2016/8/5.
 */
public class DownloadManager {

    private static DownloadManager mInstance;

    private Context mContext;
    private OkHttpClient okHttpClient;
    private DownloadTaskBeanDao taskBeanDao;

    private int mPoolSize = 5;
    private ExecutorService executorService;
    private Map<String,Future> futureMap = new HashMap<>();

    private Map<String,DownloadTask> currentTaskList = new HashMap<>();

    public Map<String, DownloadTask> getCurrentTaskList() {
        return currentTaskList;
    }

    private DownloadManager(Context context,OkHttpClient okHttpClient, InputStream input) {
        this.mContext = context;

        executorService = Executors.newFixedThreadPool(mPoolSize);
        taskBeanDao = new DownloadTaskBeanDaoImpl(mContext);

        if(okHttpClient != null) {
            this.okHttpClient = okHttpClient;
        } else {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            if(input != null) {
                builder.sslSocketFactory(initCertificates(input));
            }

            this.okHttpClient = builder.build();
        }
    }

    public static SSLSocketFactory initCertificates(InputStream... certificates) {
        CertificateFactory certificateFactory;

        SSLContext sslContext = null;
        try {
            certificateFactory = CertificateFactory.getInstance("X.509");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            int index = 0;
            for (InputStream certificate : certificates) {
                String certificateAlias = Integer.toString(index++);
                keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(certificate));

                try {
                    if (certificate != null)
                        certificate.close();
                } catch (IOException e) {
                }
            }

            sslContext = SSLContext.getInstance("TLS");

            TrustManagerFactory trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

            trustManagerFactory.init(keyStore);
            sslContext.init(null, trustManagerFactory.getTrustManagers(),new SecureRandom());
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(sslContext!=null){
            return sslContext.getSocketFactory();
        }
        return null;

    }

    public static DownloadManager getInstance(Context context,OkHttpClient okHttpClient) {
        if(mInstance == null) {
            synchronized (DownloadManager.class) {
                if (mInstance == null) {
                    mInstance = new DownloadManager(context,okHttpClient,null);
                }
            }
        }

        return mInstance;
    }

    public static DownloadManager getInstance(Context context,OkHttpClient okHttpClient,InputStream certificateStream) {
        if(mInstance == null) {
            synchronized (DownloadManager.class) {
                if (mInstance == null) {
                    mInstance = new DownloadManager(context,okHttpClient,certificateStream);
                }
            }
        }

        return mInstance;
    }

    /**
     * 添加DownloadTask：
     * @param task
     * @param listener
     */
    public DownloadTask addDownloadTask(DownloadTask task, DownloadTaskListener listener) {
        //先判断该DownloadTask是否存在，如果存在则直接返回
        DownloadTask tTask = currentTaskList.get(task.getTaskId());
        if(tTask != null && tTask.getDownloadStatus()!= DownloadStatus.DOWNLOAD_STATUS_CANCEL) {
            LogUtils.d("Task already exists..");
            return tTask;
        }

        task.setDownloadStatus(DownloadStatus.DOWNLOAD_STATUS_PREPARE);
        task.addDownloadListener(listener);
        task.setTaskDao(taskBeanDao);
        task.setOkHttpClient(okHttpClient);
        currentTaskList.put(task.getTaskId(),task);

        //判断数据库中有没有此task数据，没有则添加一条
        if(getDownloadTaskFromDB(task.getTaskId()) == null) {
            DownloadTaskBean taskBean = new DownloadTaskBean(task.getTaskId(),task.getUrl()
                    ,task.getFileTotalSize(),task.getCompletedSize(),task.getSavePathDir(),task.getFileName(),task.getDownloadStatus());
            taskBeanDao.createTask(taskBean);
        }

        Future future = executorService.submit(task);
        futureMap.put(task.getTaskId(),future);

        return task;
    }

    /**
     * 顺序从CurrentTaskList和数据库中查询是否有指定的task存在，如果存在则让它继续执行
     * @param taskId
     * @return
     */
    public DownloadTask resume(String taskId) {
        DownloadTask task = currentTaskList.get(taskId);
        if(task != null) {
            if(task.getDownloadStatus()==DownloadStatus.DOWNLOAD_STATUS_PAUSE) {
                Future future = executorService.submit(task);
                futureMap.put(taskId,future);
            }
        } else {
            //从数据库中查询
            task = getDownloadTaskFromDB(taskId);
            if(task != null) {
                currentTaskList.put(taskId,task);
                Future future = executorService.submit(task);
                futureMap.put(taskId,future);
            }
        }

        return task;
    }

    /**
     * 添加DownloadListener
     * @param task
     * @param listener
     */
    public void addDownloadListener(DownloadTask task,DownloadTaskListener listener) {
        task.addDownloadListener(listener);
    }

    /**
     * 删除DownloadListener
     * @param task
     * @param listener
     */
    public void removeDownloadListener(DownloadTask task,DownloadTaskListener listener){
        task.removeDownloadListener(listener);
    }

    /**
     * 取消DownloadTask
     * @param task
     */
    public void cancel(DownloadTask task) {
        task.cancel();
        currentTaskList.remove(task.getTaskId());
        futureMap.remove(task.getTaskId());
        taskBeanDao.deleteTask(task.getTaskId());
    }

    /**
     * 取消DownloadTask
     * @param taskId
     */
    public void cancel(String taskId) {
        DownloadTask task = getDownloadTaskById(taskId);
        if(task != null) {
            cancel(task);
        }
    }

    /**
     * 暂停
     * @param task
     */
    public void pause(DownloadTask task) {
        if(task != null) {
            task.pause();
        }
    }

    /**
     * 暂停
     * @param taskId
     */
    public void pause(String taskId) {
        DownloadTask task = getDownloadTaskById(taskId);
        if(task != null) {
            task.pause();
        }
    }

    public List<DownloadTaskBean> loadAllDownloadTaskBeanFromDB() {
        return taskBeanDao.loadAll();
    }

    /**
     * 从数据库里面加载数据所有的DownloadTaskBean并转换成DownloadTask的List
     * @return
     */
    public List<DownloadTask> loadAllDownloadTaskFromDB() {
        List<DownloadTaskBean> taskBeans = loadAllDownloadTaskBeanFromDB();
        List<DownloadTask> downloadTasks = new ArrayList<>();
        if(taskBeans != null && taskBeans.size()>0) {
            for(DownloadTaskBean taskBean : taskBeans) {
                DownloadTask task = DownloadTask.parse(taskBean);
                downloadTasks.add(task);
            }
        }

        return downloadTasks;
    }

    /**
     * 获取所有的DownloadTask:包括内存和数据库
     * @return
     */
    public List<DownloadTask> loadAllDownloadTasks() {
        //合并数据库中和内存中的DownloadTask
        List<DownloadTask> tasks = new ArrayList<>();
        List<DownloadTask> memeryTask = (List<DownloadTask>) currentTaskList.values();
        if(memeryTask.size()>0) {
            tasks.addAll(memeryTask);
        }
        List<DownloadTask> dbTask = loadAllDownloadTaskFromDB();
        for(DownloadTask task : dbTask) {
            if(!tasks.contains(task)) {
                tasks.add(task);
            }
        }

        return tasks;
    }

    /**
     * 通过taskId获取DownloadTask，先从内存中获取，然后从数据库获取
     * @param taskId
     * @return
     */
    public DownloadTask getDownloadTaskById(String taskId) {
        DownloadTask task = null;
        task = currentTaskList.get(taskId);
        if(task == null) {
            task = getDownloadTaskFromDB(taskId);
        }

        return task;
    }

    /**
     * 从数据库从获取DownloadTaskBean并解析成DownloadTask
     * @param taskId
     * @return
     */
    private DownloadTask getDownloadTaskFromDB(String taskId) {
        DownloadTaskBean taskBean = taskBeanDao.query(taskId);
        if(taskBean != null) {
            return DownloadTask.parse(taskBean);
        }

        return null;
    }

}
