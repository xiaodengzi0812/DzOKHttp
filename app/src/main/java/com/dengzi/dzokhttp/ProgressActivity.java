package com.dengzi.dzokhttp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import me.jessyan.progressmanager.ProgressListener;
import me.jessyan.progressmanager.ProgressManager;
import me.jessyan.progressmanager.body.ProgressInfo;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @Title: 第三方的Progress进度条实现demo
 * compile 'me.jessyan:progressmanager:1.5.0'
 * @Author: djk
 * @Time: 2017/11/27
 * @Version:1.0.0
 */
public class ProgressActivity extends AppCompatActivity {
    //    String url = "https://api.saiwuquan.com/api/appv2/sceneModel";
    String url = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1511414463265&di=b4f9eefd517d7deef59e16549a2c49d9&imgtype=0&src=http%3A%2F%2Fimg5q.duitang.com%2Fuploads%2Fitem%2F201501%2F10%2F20150110210743_xXcB8.gif";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom);
        initProgress();
    }

    private void initProgress() {
        ProgressManager.getInstance().addRequestListener(url, getUploadListener());
        ProgressManager.getInstance().addResponseListener(url, getDownloadListener());
    }

    @NonNull
    private ProgressListener getUploadListener() {
        return new ProgressListener() {
            @Override
            public void onProgress(ProgressInfo progressInfo) {
                Log.e("dengzi", "UploadListener->progress->" + progressInfo.getPercent());
            }

            @Override
            public void onError(long id, Exception e) {
                Log.e("dengzi", "UploadListener->onError");
            }
        };
    }

    @NonNull
    private ProgressListener getDownloadListener() {
        return new ProgressListener() {
            @Override
            public void onProgress(ProgressInfo progressInfo) {
                Log.e("dengzi", "DownloadListener->progress->" + progressInfo.getPercent());
            }

            @Override
            public void onError(long id, Exception e) {
                Log.e("dengzi", "DownloadListener->onError");
            }
        };
    }

    private static final MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");

    public void okhttpClick(View view) {

        MultipartBody requestBody = new MultipartBody.Builder()
                .addFormDataPart("pageNo", "1")
                .addFormDataPart("pageSize", "5")
                .addFormDataPart("platform", "android").build();

        RequestBody requestBody1 = new FormBody.Builder()
                .add("pageNo", "1")
                .add("pageSize", "5")
                .add("platform", "android").build();


        Request request = new Request.Builder()
                .url(url)
                .tag(this)
                .get()
//                .post(requestBody1)
                .build();

        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS);

        OkHttpClient okHttpClient = ProgressManager.getInstance().with(okHttpClientBuilder)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.e("dengzi", "出错了");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Log.e("dengzi", result);
            }
        });
    }

}
