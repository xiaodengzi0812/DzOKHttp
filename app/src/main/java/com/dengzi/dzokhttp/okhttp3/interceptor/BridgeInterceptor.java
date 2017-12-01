package com.dengzi.dzokhttp.okhttp3.interceptor;

import android.util.Log;

import com.dengzi.dzokhttp.okhttp3.Request;
import com.dengzi.dzokhttp.okhttp3.Response;

/**
 * @author Djk
 * @Title:
 * @Time: 2017/11/24.
 * @Version:1.0.0
 */
public class BridgeInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws Exception {
        Log.e("dengzi", "BridgeInterceptor");
        Request request = chain.request();


        return chain.next(request);
    }
}
