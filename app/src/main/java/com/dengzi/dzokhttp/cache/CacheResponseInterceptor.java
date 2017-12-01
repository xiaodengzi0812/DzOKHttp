package com.dengzi.dzokhttp.cache;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * @author Djk
 * @Title: 设置300秒读缓存，设置到Response里面去
 * @Time: 2017/11/27.
 * @Version:1.0.0
 */
public class CacheResponseInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        response = response.newBuilder()
                // 移除之前的缓存策略
//                .removeHeader("Cache-Control")
                .removeHeader("Pragma")
                // 添加我们自己的缓存策略（300秒之内读缓存）
                .addHeader("Cache-Control", "max-age=300").build();
        return response;
    }
}
