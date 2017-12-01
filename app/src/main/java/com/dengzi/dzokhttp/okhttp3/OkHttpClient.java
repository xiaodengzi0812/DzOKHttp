package com.dengzi.dzokhttp.okhttp3;

/**
 * description:
 * author: Darren on 2017/10/9 15:34
 * email: 240336124@qq.com
 * version: 1.0
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
