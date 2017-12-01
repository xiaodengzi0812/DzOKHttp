package com.dengzi.dzokhttp.progress_download;


import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * @author Djk
 * @Title: 下载拦截器
 * @Time: 2017/11/27.
 * @Version:1.0.0
 */
public class DownloadInterceptor implements Interceptor {
    // 下载进度条
    private ProgressDownListener mListener;

    public DownloadInterceptor(ProgressDownListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        // 获取下一个拦截器返回的Response
        Response response = chain.proceed(chain.request());
        // 创建一个我们自己的Response并返回
        return response.newBuilder()
                .body(new ProgressDownloadResponseBody(response.body(), mListener))
                .build();
    }

}
