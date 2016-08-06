package wanghaisheng.com.multithreaddownloader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.daimajia.numberprogressbar.NumberProgressBar;

public class MainActivity extends AppCompatActivity {

    NumberProgressBar progressBar1;
    Button btnControl;
    Button btnCancel;

    NumberProgressBar progressBar2;
    Button btnControl2;
    Button btnCancel2;

    private boolean isDownloading1 = false;
    private boolean isDownloading2 = false;

    private String url1 = "https://apkb.mumayi.com/2016/08/02/29/296288/wangyiyunyinle_V3.6.0_mumayi_c6181.apk";
    private String url2 = "https://apkc.mumayi.com/2016/07/21/3/30752/youku_V5.7.3_mumayi_0210c.apk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar1 = (NumberProgressBar) findViewById(R.id.progress_bar1);
        btnControl = (Button) findViewById(R.id.btn_control);
        btnCancel = (Button) findViewById(R.id.btn_cancel);

        progressBar2 = (NumberProgressBar) findViewById(R.id.progress_bar2);
        btnControl2 = (Button) findViewById(R.id.btn_control2);
        btnCancel2 = (Button) findViewById(R.id.btn_cancel2);

        btnControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,DownloadService.class);
                if(!isDownloading1) {
                    if(progressBar1.getProgress()<=0) {
                        intent.setAction(DownloadService.ACTION_START);
                    } else {
                        intent.setAction(DownloadService.ACTION_RESUME);
                    }
                    btnControl.setText("暂停");
                    isDownloading1 = true;
                } else {
                    intent.setAction(DownloadService.ACTION_PAUSE);
                    btnControl.setText("继续下载");
                    isDownloading1 = false;
                }

                intent.putExtra(DownloadService.ARG_URL,url1);
                startService(intent);
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,DownloadService.class);

                intent.setAction(DownloadService.ACTION_CANCEL);
                intent.putExtra(DownloadService.ARG_URL,url1);
                startService(intent);
                progressBar1.setProgress(0);
            }
        });

        btnControl2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,DownloadService.class);

                if(!isDownloading2) {
                    if(progressBar2.getProgress()<=0) {
                        intent.setAction(DownloadService.ACTION_START);
                    } else {
                        intent.setAction(DownloadService.ACTION_RESUME);
                    }
                    btnControl2.setText("暂停");
                    isDownloading2 = true;
                } else {
                    intent.setAction(DownloadService.ACTION_PAUSE);
                    btnControl2.setText("继续下载");
                    isDownloading2 = false;
                }

                intent.putExtra(DownloadService.ARG_URL,url2);
                startService(intent);
            }
        });

        btnCancel2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,DownloadService.class);

                intent.setAction(DownloadService.ACTION_CANCEL);
                intent.putExtra(DownloadService.ARG_URL,url2);
                startService(intent);
                progressBar2.setProgress(0);
            }
        });

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadService.ACTION_UPDATE_PROGRESS);
        registerReceiver(progressReceiver,intentFilter);

    }

    BroadcastReceiver progressReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String url = intent.getStringExtra(DownloadService.ARG_URL);
            final int progress = intent.getIntExtra(DownloadService.ARG_PROGRESS,0);
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    if(url1.equals(url)) {
                        progressBar1.setProgress(progress);
                        if(progressBar1.getProgress()==100) {
                            btnControl.setText("下载完成");
                        }
                    } else if(url2.equals(url)) {
                        progressBar2.setProgress(progress);
                        if(progressBar2.getProgress()==100) {
                            btnControl2.setText("下载完成");
                        }
                    }
                }
            });
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(progressReceiver);
    }
}
