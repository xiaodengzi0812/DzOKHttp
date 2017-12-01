package com.dengzi.dzokhttp.download;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author Djk
 * @Title: okhttp管理类
 * @Time: 2017/11/28.
 * @Version:1.0.0
 */
public class OkHttpManager {
    private static volatile OkHttpManager mInstance;
    private OkHttpClient mOkHttpClient;

    public static OkHttpManager getInstance() {
        if (mInstance == null) {
            synchronized (OkHttpManager.class) {
                if (mInstance == null) {
                    mInstance = new OkHttpManager();
                }
            }
        }
        return mInstance;
    }

    private OkHttpManager() {
        mOkHttpClient = new OkHttpClient.Builder().build();
    }

    /**
     * 异步执行
     *
     * @param url 下载路径
     * @return call
     */
    public Call asyncCall(String url) {
        Request request = new Request.Builder().url(url).build();
        return mOkHttpClient.newCall(request);
    }

    /**
     * 同步执行
     */
    public Response syncResponse(String url, long start, long end) throws IOException {
        Request request = new Request.Builder().url(url)
                // 分块下载
                .addHeader("Range", "bytes=" + start + "-" + end)
                .build();
        return mOkHttpClient.newCall(request).execute();
    }
}
