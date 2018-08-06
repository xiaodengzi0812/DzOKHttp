package com.dengzi.dzokhttp.okhttp3;

/**
 * @author Djk
 * @Title:
 * @Time: 2017/11/24.
 * @Version:1.0.0
 */
public class OkHttpClient {
    final Dispatcher dispatcher;

    public OkHttpClient() {
        this(new Builder());
    }


    public OkHttpClient(Builder builder) {
        dispatcher = builder.dispatcher;
    }

    public Call newCall(Request request) {
        return new RealCall(this, request);
    }

    public Dispatcher dispatcher() {
        return dispatcher;
    }


    public static class Builder {
        Dispatcher dispatcher;

        public Builder() {
            this.dispatcher = new Dispatcher();
        }
    }
}
