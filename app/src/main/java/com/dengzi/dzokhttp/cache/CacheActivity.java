package com.dengzi.dzokhttp.cache;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.dengzi.dzokhttp.R;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @Title: 缓存
 * @Author: djk
 * @Time: 2017/11/27
 * @Version:1.0.0
 */
public class CacheActivity extends AppCompatActivity {
    String url = "https://api.saiwuquan.com/api/appv2/sceneModel?pageSize=1";
    OkHttpClient okHttpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom);
        initOkHttp();
    }

    private void initOkHttp() {
        String cachePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "dengzi_cache" + File.separator;
        File cacheFile = new File(cachePath);
        if (!cacheFile.exists()) {
            cacheFile.mkdirs();
        }
        Cache cache = new Cache(cacheFile, 10 * 1024 * 1024);

        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .cache(cache)
                .addInterceptor(new CacheRequestInterceptor(getApplicationContext()))
                .addNetworkInterceptor(new CacheResponseInterceptor())
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    public void okhttpClick(View view) {
        CacheControl cacheControl = new CacheControl.Builder().maxAge(100, TimeUnit.SECONDS).build();

        Request request = new Request.Builder()
                .url(url)
                // 每次请求的时候可以添加cacheControl去控制
                // CacheControl.FORCE_NETWORK : 强制使用网络
                // CacheControl.FORCE_CACHE : 强制使用缓存
//                .cacheControl(CacheControl.FORCE_NETWORK)
//                .cacheControl(cacheControl)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.e("dengzi", "onFailure");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ResponseBody body = response.body();
                String responseStr = body == null ? "" : body.string();
                Log.e("dengzi", "response -> " + responseStr);
                Log.e("dengzi", "缓存数据 -> " + response.cacheResponse());
                Log.e("dengzi", "网络数据 -> " + response.networkResponse());
                Log.e("dengzi", "-------------------------------------------------");
            }
        });
    }

}
