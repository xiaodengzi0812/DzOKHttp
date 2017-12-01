package com.dengzi.dzokhttp.download;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.dengzi.dzokhttp.BuildConfig;
import com.dengzi.dzokhttp.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @Title: 下载
 * @Author: djk
 * @Time: 2017/11/28
 * @Version:1.0.0
 */
public class DownloadActivity extends AppCompatActivity {
    String mApkUrl = "http://acj3.pc6.com/pc6_soure/2017-11/com.ss.android.essay.joke_664.apk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
    }

    /**
     * 多线程下载
     *
     * @param view
     */
    public void multipleDownloadClick(View view) {
        startActivity(new Intent(DownloadActivity.this, MultipleDownloadActivity.class));
    }

    /**
     * 单线程下载
     *
     * @param view
     */
    public void singleDownloadClick(View view) {
        OkHttpManager.getInstance().asyncCall(mApkUrl)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        File apkFile = new File(Environment.getExternalStorageDirectory(), "内涵段子.apk");
                        InputStream inputStream = response.body().byteStream();
                        OutputStream outputStream = new FileOutputStream(apkFile);
                        int len = 0;
                        byte[] buffer = new byte[1024 * 10];
                        while ((len = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, len);
                        }
                        inputStream.close();
                        outputStream.close();
                        installFile(apkFile);
                    }
                });
    }

    private void installFile(File file) {
        // 核心是下面几句代码
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //判断是否是AndroidN以及更高的版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileProvider", file);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        startActivity(intent);
    }

}
