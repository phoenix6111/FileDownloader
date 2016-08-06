package wanghaisheng.com.multithreaddownloader.beans;

import java.io.Serializable;

/**
 * Created by sheng on 2016/8/4.
 */
public class FileInfo implements Serializable{

    private int id;
    //文件下载路径
    private String url;
    //文件名
    private String name;
    //文件是否下载完成
    private boolean finished;
    //文件长度
    private int length;

    public FileInfo() {
    }

    public FileInfo(int id, String url, String name, boolean finished, int length) {
        this.id = id;
        this.url = url;
        this.name = name;
        this.finished = finished;
        this.length = length;
    }

    public FileInfo(String url, String name, boolean finished, int length) {
        this.url = url;
        this.name = name;
        this.finished = finished;
        this.length = length;
    }

    public FileInfo(String url) {
        this.url = url;
    }

    public FileInfo(String url, String name) {
        this.url = url;
        this.name = name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
