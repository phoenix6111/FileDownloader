package wanghaisheng.com.multithreaddownloader.beans;

/**
 * Created by sheng on 2016/8/6.
 */
public class DownloadException extends Exception {

    private int errorCode;

    public DownloadException(Throwable cause, int errorCode) {
        super(cause);
        this.errorCode = errorCode;
    }

    public DownloadException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
