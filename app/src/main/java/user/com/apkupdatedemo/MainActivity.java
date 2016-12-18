package user.com.apkupdatedemo;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    // 更新版本要用到的一些信息
    private UpdateInfo info;
    private ProgressDialog pBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toast.makeText(MainActivity.this, "正在检查版本更新..", Toast.LENGTH_SHORT).show();
        // 自动检查有没有新版本 如果有新版本就提示更新
        new Thread() {
            public void run() {
                try {
                    UpdateInfoService updateInfoService = new UpdateInfoService(
                            MainActivity.this);
                    info = updateInfoService.getUpDateInfo();
                    handler1.sendEmptyMessage(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    @SuppressLint("HandlerLeak")
    private Handler handler1 = new Handler() {
        public void handleMessage(Message msg) {
            // 如果有更新就提示
            if (isNeedUpdate()) {   //在下面的代码段
                showUpdateDialog();  //下面的代码段
            }
        }
    };

    /**
     * 判断版本号  是否需要升级
     * @return true or false
     */
    private boolean isNeedUpdate() {
        String v = info.getVersion(); // 最新版本的版本号
        Log.i("update",v);
        Toast.makeText(MainActivity.this, v, Toast.LENGTH_SHORT).show();
        if (v.equals(getVersion())) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 获得当前的版本号
     * @return
     */
    private String getVersion() {
        try {
            PackageManager packageManager = getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(),0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "版本号未知";
        }
    }
    /**
     *提示更新版本
     */
    private void showUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle("请升级APP至版本" + info.getVersion());
        builder.setMessage(info.getDescription());
        builder.setCancelable(false);

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Environment.getExternalStorageState().equals(
                        Environment.MEDIA_MOUNTED)) {
                    downFile(info.getUrl());     //在下面的代码段
                } else {
                    Toast.makeText(MainActivity.this, "SD卡不可用，请插入SD卡",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
            }

        });
        builder.create().show();
    }

    /**
     * 下载文件。
     * @param url
     */
    public void downFile(final String url) {
        pBar = new ProgressDialog(MainActivity.this);    //进度条，在下载的时候实时更新进度，提高用户友好度
        pBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pBar.setTitle("正在下载");
        pBar.setMessage("请稍候...");
        pBar.setProgress(0);
        pBar.show();
        new Thread() {
            public void run() {
          /*      HttpClient client = new DefaultHttpClient();
                HttpGet get = new HttpGet(url);
                HttpResponse response;*/
                try {
                    URL urls = new URL(url);
                    HttpURLConnection urlConnection = (HttpURLConnection) urls.openConnection();
                    if(urlConnection.getResponseCode()==200){
                        Log.e("请求数据","------successful!");
                        int length = urlConnection.getContentLength(); //获取文件大小
                        InputStream is = urlConnection.getInputStream();
                        pBar.setMax(length);//设置进度条的总长度
                        FileOutputStream fileOutputStream = null;
                        if (is != null) {
                            Log.e("------","------is != null!");

                            File file = new File(Environment.getExternalStorageDirectory(),
                                    "NewUpdateDemo.apk");
                            Log.e("ch------1","------+is.read(buf)");
                            fileOutputStream = new FileOutputStream(file);
                            Log.e("ch------2","------+is.read(buf)");
                            byte[] buf = new byte[10];   //这个是缓冲区，即一次读取10个比特，我弄的小了点，因为在本地，所以数值太大一 下就下载完了，看不出progressbar的效果。
                            int ch = -1;
                            int process = 0;

                            while ((ch = is.read(buf)) != -1) {
                                fileOutputStream.write(buf, 0, ch);
                                process += ch;
                                pBar.setProgress(process);       //这里就是关键的实时更新进度了！
                            }
                        }
                        fileOutputStream.flush();
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                        down();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }.start();
    }

    void down() {
        handler1.post(new Runnable() {
            public void run() {
                pBar.cancel();
                update();
            }
        });
    }


    /**
     *   //安装文件，一般固定写法
     */

    void update() {
       /* Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(Environment
                        .getExternalStorageDirectory(), "NewUpdateDemo.apk")),
                "application/vnd.android.package-archive");
        startActivity(intent);*/
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setDataAndType(Uri.fromFile(new File(Environment
                        .getExternalStorageDirectory(), "NewUpdateDemo.apk")),
                        "application/vnd.android.package-archive");
        startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

}

