package com.dengzi.dzokhttp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

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
 * @Title: okHttp的使用demo
 * @Author: djk
 * @Time: 2017/11/27
 * @Version:1.0.0
 */
public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom);
    }

    private static final MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");

    public void okhttpClick(View view) {
        String url = "https://api.saiwuquan.com/api/appv2/sceneModel";
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
                .post(requestBody1)
                .build();

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
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
