package wanghaisheng.com.multithreaddownloader.utils;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * Created by sheng on 2016/8/4.
 */
public class OkHttpUtils {

    private static OkHttpUtils instance;
    private OkHttpClient okHttpClient;

    private OkHttpUtils() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(20 * 1000, TimeUnit.MILLISECONDS)
                .readTimeout(20 * 1000, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true);

        okHttpClient = builder.build();
//        new RandomAccessFile();
    }

    public static OkHttpUtils getInstance() {
        if(instance == null) {
            synchronized (OkHttpUtils.class) {
                if(instance == null) {
                    instance = new OkHttpUtils();
                }
            }
        }

        return instance;
    }

}
