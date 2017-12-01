package com.dengzi.dzokhttp.okhttp3.interceptor;

import com.dengzi.dzokhttp.okhttp3.Request;
import com.dengzi.dzokhttp.okhttp3.Response;

/**
 * @author Djk
 * @Title: 拦截器接口
 * @Time: 2017/11/24.
 * @Version:1.0.0
 */
public interface Interceptor {

    Response intercept(Chain chain) throws Exception;

    interface Chain {
        Request request();

        Response next(Request request) throws Exception;
    }

}
