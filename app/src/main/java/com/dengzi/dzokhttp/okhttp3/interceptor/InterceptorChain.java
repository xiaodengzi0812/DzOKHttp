package com.dengzi.dzokhttp.okhttp3.interceptor;

import com.dengzi.dzokhttp.okhttp3.Request;
import com.dengzi.dzokhttp.okhttp3.Response;

import java.util.List;

/**
 * @author Djk
 * @Title: 拦截器Chain实现类
 * @Time: 2017/11/24.
 * @Version:1.0.0
 */
public class InterceptorChain implements Interceptor.Chain {
    private List<Interceptor> interceptorList;
    private int index;
    private Request request;

    public InterceptorChain(List<Interceptor> interceptorList, int index, Request request) {
        this.interceptorList = interceptorList;
        this.index = index;
        this.request = request;
    }

    @Override
    public Request request() {
        return request;
    }

    @Override
    public Response next(Request request) throws Exception {
        // 调用下一个拦截器
        InterceptorChain next = new InterceptorChain(interceptorList, index + 1, request);
        Interceptor interceptor = interceptorList.get(index);
        Response response = interceptor.intercept(next);
        return response;
    }
}
