package wanghaisheng.com.multithreaddownloader.beans;

/**
 * Created by sheng on 2016/8/5.
 */
public interface DownloadStatus {

    //初始化状态
    int DOWNLOAD_STATUS_INIT = -1;
    //准备状态
    int DOWNLOAD_STATUS_PREPARE = 0;
    //开始状态
    int DOWNLOAD_STATUS_START = 1;
    //正在下载状态
    int DOWNLOAD_STATUS_DOWNLOADING = 2;
    //暂停状态
    int DOWNLOAD_STATUS_PAUSE = 3;
    //完成状态
    int DOWNLOAD_STATUS_COMPLETED = 4;
    //取消状态
    int DOWNLOAD_STATUS_CANCEL = 5;
    //错误状态
    int DOWNLOAD_STATUS_ERROR = 6;


}
