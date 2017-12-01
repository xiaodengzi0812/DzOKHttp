package com.dengzi.dzokhttp.progress_download;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.dengzi.dzokhttp.R;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @Title: 下载进度条demo
 * @Author: djk
 * @Time: 2017/11/27
 * @Version:1.0.0
 */
public class ProgressDownloadActivity extends AppCompatActivity {
    String url = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1511414463265&di=b4f9eefd517d7deef59e16549a2c49d9&imgtype=0&src=http%3A%2F%2Fimg5q.duitang.com%2Fuploads%2Fitem%2F201501%2F10%2F20150110210743_xXcB8.gif";
    private ProgressDialog progressDialog;

    private MyHandler mHandler = new MyHandler(this);

    private static class MyHandler extends Handler {
        private WeakReference<ProgressDownloadActivity> activityReference;

        MyHandler(ProgressDownloadActivity activity) {
            this.activityReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (activityReference.get() == null) return;
            switch (msg.what) {
                case 1:
                    Toast.makeText(activityReference.get(), "下载成功！", Toast.LENGTH_SHORT).show();
                    break;
                case -1:
                    Toast.makeText(activityReference.get(), "下载失败！", Toast.LENGTH_SHORT).show();
                    break;
            }
            super.handleMessage(msg);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom);
        initProgress();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    private void initProgress() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage("下载中...");
        progressDialog.setMax(100);
    }

    private DownloadInterceptor getDownloadInterceptor() {
        return new DownloadInterceptor(new ProgressDownListener() {
            @Override
            public void onProgress(long totalLength, long currentLength) {
                Log.e("dengzi", currentLength + " / " + totalLength);
                int progress = (int) (currentLength * 100 / totalLength);
                progressDialog.setProgress(progress);
            }
        });
    }

    public void okhttpClick(View view) {
        progressDialog.show();
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                // 添加一个拦截器，用来拦截下载进度
                .addNetworkInterceptor(getDownloadInterceptor()).build();

        Request request = new Request.Builder()
                .url(url)
                .tag(this)
                .get()
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                progressDialog.dismiss();
                mHandler.sendEmptyMessage(-1);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ResponseBody body = response.body();
                String responseStr = body == null ? "" : body.string();
                Log.e("dengzi", "responseStr->" + responseStr);
                progressDialog.dismiss();
                mHandler.sendEmptyMessage(1);
            }
        });
    }

}
