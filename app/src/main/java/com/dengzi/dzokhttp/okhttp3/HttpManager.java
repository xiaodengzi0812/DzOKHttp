package com.dengzi.dzokhttp.okhttp3;

import android.util.Log;

import com.dengzi.dzokhttp.util.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by djk on 2018/8/3.
 */
public class HttpManager {
    private static String mUrl = "http://log.aispeech.com/bus";//http://10.12.6.99:9090/bus

    private static volatile HttpManager mInstance;
    private OkHttpClient mHttpClient;

    public static HttpManager getInstance() {
        if (mInstance == null) {
            synchronized (HttpManager.class) {
                if (mInstance == null) {
                    mInstance = new HttpManager();
                }
            }
        }
        return mInstance;
    }

    private HttpManager() {
        mHttpClient = new OkHttpClient();
    }

    public void setHttpUrl(String url) {
        mUrl = url;
    }

    public void sendData(String jsonStr, final String filePath) {
        RequestBody requestBody = new ContextRequestBody().setContent(jsonStr);

        final Request request = new Request.Builder()
                .url(mUrl)
                .tag(this)
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build();

        mHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, Exception e) {
                Log.e("HttpManager", "http failed");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.e("HttpManager", "http success");
                FileUtils.deleteFile(filePath);
            }
        });
    }

    public void uploadFile(final String filePath) {
        File file = new File(filePath);
        if (!FileUtils.isFile(file)) {
            return;
        }

        RequestBody requestBody = new FileRequestBody().setType("multipart/form-data").addFile(file);

        final Request request = new Request.Builder()
                .url("http://10.12.6.99:9090/upfile")
                .tag(this)
                .post(requestBody)
                .build();

        mHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, Exception e) {
                String msg = e.getMessage();
                Log.e("HttpManager", "http failed --> "+ msg);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.e("HttpManager", "http success");
                FileUtils.deleteFile(filePath);
            }
        });
    }

    public void cancel() {
        mHttpClient.dispatcher().cancelAll();
    }
}
