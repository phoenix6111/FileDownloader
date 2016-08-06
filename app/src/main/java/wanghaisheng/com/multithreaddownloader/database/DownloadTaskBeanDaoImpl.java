package wanghaisheng.com.multithreaddownloader.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import wanghaisheng.com.multithreaddownloader.beans.DownloadTaskBean;

/**
 * Created by sheng on 2016/8/4.
 */
public class DownloadTaskBeanDaoImpl implements DownloadTaskBeanDao {

    private DatabaseHelper dbHelper;

    public DownloadTaskBeanDaoImpl(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    @Override
    public DownloadTaskBean query(String taskId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from "+DatabaseHelper.T_DOWNLOAD_TASK+" where task_id=?",new String[]{taskId});

        DownloadTaskBean taskBean = null;
        while (cursor.moveToNext()) {
            taskBean = new DownloadTaskBean();
            taskBean.setTaskId(taskId);
            taskBean.setUrl(cursor.getString(cursor.getColumnIndex("url")));
            taskBean.setFileName(cursor.getString(cursor.getColumnIndex("file_name")));
            taskBean.setSaveDirPath(cursor.getString(cursor.getColumnIndex("save_dir_path")));
            taskBean.setCompletedSize(cursor.getLong(cursor.getColumnIndex("completed_size")));
            taskBean.setFileTotalSize(cursor.getLong(cursor.getColumnIndex("file_total_size")));
            taskBean.setDownloadStatus(cursor.getInt(cursor.getColumnIndex("download_status")));
        }

        cursor.close();
        return taskBean;
    }

    @Override
    public List<DownloadTaskBean> loadAll() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql = "select * from "+DatabaseHelper.T_DOWNLOAD_TASK;
        Cursor cursor = db.rawQuery(sql,null);

        List<DownloadTaskBean> taskBeanList = new ArrayList<>();
        DownloadTaskBean taskBean = null;
        while (cursor.moveToNext()) {
            taskBean = new DownloadTaskBean();
            taskBean.setTaskId(cursor.getString(cursor.getColumnIndex("task_id")));
            taskBean.setUrl(cursor.getString(cursor.getColumnIndex("url")));
            taskBean.setFileName(cursor.getString(cursor.getColumnIndex("file_name")));
            taskBean.setSaveDirPath(cursor.getString(cursor.getColumnIndex("save_dir_path")));
            taskBean.setCompletedSize(cursor.getLong(cursor.getColumnIndex("completed_size")));
            taskBean.setFileTotalSize(cursor.getLong(cursor.getColumnIndex("file_total_size")));
            taskBean.setDownloadStatus(cursor.getInt(cursor.getColumnIndex("download_status")));
            taskBeanList.add(taskBean);
        }

        cursor.close();

        return taskBeanList;
    }

    @Override
    public boolean ifExists(String taskId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "select * from "+DatabaseHelper.T_DOWNLOAD_TASK+" where task_id=?";
        Cursor cursor = db.rawQuery(sql,new String[]{taskId});
        boolean exists = cursor.moveToNext();
        cursor.close();
        return exists;
    }

    @Override
    public void udpateOrInsert(DownloadTaskBean task) {
        if (ifExists(task.getTaskId())) {
            updateTask(task);
        } else {
            createTask(task);
        }
    }

    @Override
    public void updateTask(DownloadTaskBean task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "update "+DatabaseHelper.T_DOWNLOAD_TASK+" set url=?,file_total_size=?,completed_size=?," +
                "save_dir_path=?,file_name=?,download_status=? where task_id=?";
        db.execSQL(sql,new Object[]{task.getUrl(),task.getFileTotalSize(),task.getCompletedSize()
            ,task.getSaveDirPath(),task.getFileName(),task.getDownloadStatus(),task.getTaskId()});
    }

    @Override
    public void createTask(DownloadTaskBean task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "insert into "+DatabaseHelper.T_DOWNLOAD_TASK+"(task_id,url,file_total_size,completed_size" +
                ",save_dir_path,file_name,download_status) values (?,?,?,?,?,?,?)";
        db.execSQL(sql,new Object[]{task.getTaskId(),task.getUrl(),task.getFileTotalSize(),task.getCompletedSize()
                ,task.getSaveDirPath(),task.getFileName(),task.getDownloadStatus()});
    }

    @Override
    public void deleteTask(String taskId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "delete from "+DatabaseHelper.T_DOWNLOAD_TASK+" where task_id=?";
        db.execSQL(sql,new Object[]{taskId});
    }

    @Override
    public void deleteTask(DownloadTaskBean task) {
        if(task == null) {
            return;
        }
        String taskId = task.getTaskId();
        deleteTask(taskId);
    }
}
