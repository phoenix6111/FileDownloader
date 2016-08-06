package wanghaisheng.com.multithreaddownloader;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.apkfuns.logutils.LogUtils;

import wanghaisheng.com.multithreaddownloader.beans.DownloadException;
import wanghaisheng.com.multithreaddownloader.core.DownloadManager;
import wanghaisheng.com.multithreaddownloader.core.DownloadTask;
import wanghaisheng.com.multithreaddownloader.core.DownloadTaskListenerAdapter;
import wanghaisheng.com.multithreaddownloader.utils.SecurityHelper;

/**
 * Created by sheng on 2016/8/4.
 */
public class DownloadService extends Service {

    public static final String ARG_URL = "arg_url";
    //调用端传来的action标识，“开始下载或停止下载
    public static final String ACTION_START = "action_start";
    public static final String ACTION_PAUSE = "action_stop";
    public static final String ACTION_RESUME = "action_resume";
    public static final String ACTION_CANCEL = "action_cancel";
    public static final String ACTION_UPDATE_PROGRESS = "action_update_progress";

    public static final String ARG_PROGRESS = "arg_progress";

    private DownloadManager downloadManager;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if(ACTION_START.equals(action)) {
            String url = intent.getStringExtra(ARG_URL);
            LogUtils.d("start  +"+url);
            download(url);
        } else if(ACTION_PAUSE.equals(action)) {
            String url = intent.getStringExtra(ARG_URL);
            pause(url);
        } else if(ACTION_RESUME.equals(action)) {
            String url = intent.getStringExtra(ARG_URL);
            resume(url);
        } else if(ACTION_CANCEL.equals(action)) {
            String url = intent.getStringExtra(ARG_URL);
            cancel(url);
        }

        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onCreate() {
        super.onCreate();
        downloadManager = DownloadManager.getInstance(getApplicationContext(),null);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void download(String url) {
        final DownloadTask task = new DownloadTask();
        String fileName = SecurityHelper.getMD5(url);

        task.setFileName(fileName);
        task.setTaskId(fileName);
        task.setSavePathDir(getExternalCacheDir().getPath() + "/");
        task.setUrl(url);

        downloadManager.addDownloadTask(task,new DownloadTaskListenerAdapter(){
            @Override
            public void onDownloading(DownloadTask downloadTask) {
                super.onDownloading(downloadTask);
                Intent intent = new Intent(ACTION_UPDATE_PROGRESS);
                intent.putExtra(ARG_PROGRESS,Math.round(downloadTask.getPercent()));
                intent.putExtra(ARG_URL,task.getUrl());
//                LogUtils.d("progress  "+downloadTask.getPercent());
                sendBroadcast(intent);
            }

            @Override
            public void onError(DownloadTask downloadTask, DownloadException e) {
                super.onError(downloadTask, e);
                LogUtils.d(e);
            }
        });

    }

    private void pause(String url){
        /*OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url("")
                .build();
        Response response = okHttpClient.newCall(request).execute();
        LogUtils.d(response);*/
        /*OkHttpClient.Builder builder = new OkHttpClient.Builder();

        final OkHttpClient okHttpClient = builder.build();
        //创建一个Request
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Request request = new Request.Builder()
                        .url("https://github.com/hongyangAndroid")
                        .build();
                try {
                    Response response = okHttpClient.newCall(request).execute();
                    LogUtils.d(response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();*/
        //new call
        /*Call call = mOkHttpClient.newCall(request);
        //请求加入调度
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtils.d(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                LogUtils.d("onresponse.......");
                LogUtils.d(response.body().string());
            }
        });*/
        String taskId = SecurityHelper.getMD5(url);
        downloadManager.pause(taskId);
    }

    private void resume(String url) {
        String taskId = SecurityHelper.getMD5(url);
        downloadManager.resume(taskId);
    }

    private void cancel(String url) {
        String taskId = SecurityHelper.getMD5(url);
        downloadManager.cancel(taskId);
    }
}
