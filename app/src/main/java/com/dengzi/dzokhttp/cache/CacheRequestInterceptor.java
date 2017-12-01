package com.dengzi.dzokhttp.cache;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author Djk
 * @Title: 设置无网络读缓存，设置到Request里面去
 * @Time: 2017/11/27.
 * @Version:1.0.0
 */
public class CacheRequestInterceptor implements Interceptor {

    private Context mContext;

    public CacheRequestInterceptor(Context context) {
        this.mContext = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        // 如果没网,就只读缓存
        if (!isNetworkAvailable(mContext)) {
            request = request.newBuilder().cacheControl(CacheControl.FORCE_CACHE).build();
        }
        return chain.proceed(request);
    }

    /**
     * 检查当前网络是否可用
     *
     * @return
     */
    private static boolean isNetworkAvailable(Context context) {
        // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            // 获取NetworkInfo对象
            NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();
            if (networkInfo != null && networkInfo.length > 0) {
                for (int i = 0; i < networkInfo.length; i++) {
                    // 判断当前网络状态是否为连接状态
                    if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}