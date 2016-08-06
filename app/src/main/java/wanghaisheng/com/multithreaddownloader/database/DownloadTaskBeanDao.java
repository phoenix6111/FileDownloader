package wanghaisheng.com.multithreaddownloader.database;

import java.util.List;

import wanghaisheng.com.multithreaddownloader.beans.DownloadTaskBean;

/**
 * Created by sheng on 2016/8/4.
 */
public interface DownloadTaskBeanDao {

    /**
     * 根据taskId查询指定的task
     * @param taskId
     * @return
     */
    DownloadTaskBean query(String taskId);

    /**
     * 加载所有的task
     * @return
     */
    List<DownloadTaskBean> loadAll();

    /**
     * 查询是否有指定的taskId的task存在数据库
     * @param taskId
     * @return
     */
    boolean ifExists(String taskId);

    /**
     * 保存或更新
     * @param task
     */
    void udpateOrInsert(DownloadTaskBean task);

    /**
     * 更新task的状态
     * @param task
     */
    void updateTask(DownloadTaskBean task);

    /**
     * 保存新的task
     * @param task
     */
    void createTask(DownloadTaskBean task);

    /**
     * 根据taskId删除task
     * @param taskId
     */
    void deleteTask(String taskId);

    /**
     * 删除指定的task
     * @param task
     */
    void deleteTask(DownloadTaskBean task);
}
