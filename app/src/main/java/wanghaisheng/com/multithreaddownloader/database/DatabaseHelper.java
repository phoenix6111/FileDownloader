package wanghaisheng.com.multithreaddownloader.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by sheng on 2016/8/4.
 * 数据库帮助类，单例对象
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper instance = null;

    private static final String DB_NAME = "file_downloader";
    private static final int VERSION = 1;
    public static final String T_DOWNLOAD_TASK = "download_task";

    private static final String CREATE_TABLE_SQL = "create table download_task(" +
            "task_id text," +
            "url text," +
            "file_total_size integer," +
            "completed_size integer," +
            "save_dir_path text," +
            "file_name text," +
            "download_status integer)";

    private static final String DROP_TABLE_SQL = "drop table if exists download_task";

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    public static DatabaseHelper getInstance(Context context) {
        if(instance == null) {
            synchronized (DatabaseHelper.class) {
                if(instance == null) {
                    instance = new DatabaseHelper(context);
                }
            }
        }

        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL(DROP_TABLE_SQL);
        db.execSQL(CREATE_TABLE_SQL);
    }
}
