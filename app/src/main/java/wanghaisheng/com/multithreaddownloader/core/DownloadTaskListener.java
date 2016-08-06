package wanghaisheng.com.multithreaddownloader.core;

import wanghaisheng.com.multithreaddownloader.beans.DownloadException;

/**
 * Created by sheng on 2016/8/5.
 */
public interface DownloadTaskListener {

    void onPrepare(DownloadTask downloadTask);
    void onStart(DownloadTask downloadTask);
    void onDownloading(DownloadTask downloadTask);
    void onPause(DownloadTask downloadTask);
    void onCancel(DownloadTask downloadTask);
    void onCompleted(DownloadTask downloadTask);
    void onError(DownloadTask downloadTask, DownloadException e);
    void onProgress(DownloadTask downloadTask);

    int DOWNLOAD_ERROR_TYPE_FILE_NOT_FOUND = -1;
    int DOWNLOAD_ERROR_TYPE_IO_ERROR = -2;
}
