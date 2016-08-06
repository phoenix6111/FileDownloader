package wanghaisheng.com.multithreaddownloader.beans;

/**
 * Created by sheng on 2016/8/4.
 * 线程信息对象，保存在数据库中，以实点断点续传
 */
public class ThreadInfo {

    private int id;
    //下载地址
    private String url;
    //文件断点下载时的开始下载点
    private int start;
    //下载时的结束点
    private int end;
    //文件已经下载了多少
    private int finished;

    public ThreadInfo() {
    }

    public ThreadInfo(String url, int start, int end, int finished) {
        this.url = url;
        this.start = start;
        this.end = end;
        this.finished = finished;
    }

    public ThreadInfo(int id, String url, int start, int end, int finished) {
        this.id = id;
        this.url = url;
        this.start = start;
        this.end = end;
        this.finished = finished;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getFinished() {
        return finished;
    }

    public void setFinished(int finished) {
        this.finished = finished;
    }
}
