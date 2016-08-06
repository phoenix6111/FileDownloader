package wanghaisheng.com.multithreaddownloader.core;

/**
 * Created by sheng on 2016/8/6.
 */
public interface ProgressListener {

    /**
     * @param bytesRead     已下载字节数
     * @param contentLength 总字节数
     * @param done          是否下载完成
     */
    void update(long bytesRead, long contentLength, boolean done);

}
