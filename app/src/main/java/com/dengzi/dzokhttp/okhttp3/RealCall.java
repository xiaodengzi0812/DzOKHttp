package com.dengzi.dzokhttp.okhttp3;



import com.dengzi.dzokhttp.okhttp3.interceptor.BridgeInterceptor;
import com.dengzi.dzokhttp.okhttp3.interceptor.CustomInterceptor;
import com.dengzi.dzokhttp.okhttp3.interceptor.Interceptor;
import com.dengzi.dzokhttp.okhttp3.interceptor.InterceptorChain;
import com.dengzi.dzokhttp.okhttp3.interceptor.ServiceIntercepor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Djk
 * @Title:
 * @Time: 2017/11/24.
 * @Version:1.0.0
 */
public class RealCall implements Call {

    private final OkHttpClient client;

    // Guarded by this.
    private boolean executed;

    private boolean isCancel = false;

    /**
     * The application's original request unadulterated by redirects or auth headers.
     */
    Request originalRequest;

    public RealCall(OkHttpClient client, Request originalRequest) {
        this.client = client;
        this.originalRequest = originalRequest;
    }

    @Override
    public Request request() {
        return originalRequest;
    }

    @Override
    public Response execute() throws IOException {
        synchronized (this) {
            if (executed) throw new IllegalStateException("Already Executed");
            executed = true;
        }
        try {
            client.dispatcher().executed(this);
            Response result = getResponseWithInterceptorChain();
            if (result == null) throw new IOException("Canceled");
            return result;
        } finally {
            return null;
        }
    }

    @Override
    public void enqueue(Callback responseCallback) {
        synchronized (this) {
            if (executed) throw new IllegalStateException("Already Executed");
            executed = true;
        }

        client.dispatcher().enqueue(new AsyncCall(responseCallback));
    }

    @Override
    public void cancel() {
        isCancel = true;
    }

    @Override
    public boolean isExecuted() {
        return executed;
    }

    @Override
    public boolean isCanceled() {
        return isCancel;
    }

    private Response getResponseWithInterceptorChain() throws IOException {
        // 主要是一些拦截器
        return null;
    }

    HttpUrl redactedUrl() {
        return originalRequest.url().resolve("/...");
    }

    final class AsyncCall extends NamedRunnable {
        private final Callback responseCallback;

        private AsyncCall(Callback responseCallback) {
            super("OkHttp %s", redactedUrl().toString());
            this.responseCallback = responseCallback;
        }

        Request request() {
            return originalRequest;
        }

        RealCall get() {
            return RealCall.this;
        }

        @Override
        protected void execute() {
            if (isCanceled()) {
                return;
            }
            Request request = request();

            // 模拟OkHttp的拦截器写法
            List<Interceptor> interceptors = new ArrayList<>();
            interceptors.add(new CustomInterceptor());
            interceptors.add(new BridgeInterceptor());
            interceptors.add(new ServiceIntercepor());
            InterceptorChain interceptorChain = new InterceptorChain(interceptors, 0, request);
            try {
                Response response = interceptorChain.next(request);
                if (isCanceled()) {
                    return;
                }
                if (response != null) {
                    if (response.getResponseCode() == 200) {
                        responseCallback.onResponse(RealCall.this, response);
                    } else {
                        responseCallback.onFailure(RealCall.this, new Exception(response.string()));
                    }
                } else {
                    responseCallback.onFailure(RealCall.this, new Exception("response == null"));
                }
            } catch (Exception e) {
                // e.printStackTrace();
                responseCallback.onFailure(RealCall.this, e);
            } finally {
                client.dispatcher().finished(this);
            }
        }
    }

}
