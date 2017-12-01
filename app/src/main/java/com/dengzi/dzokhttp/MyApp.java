package com.dengzi.dzokhttp;

import android.app.Application;
import android.content.Context;

/**
 * @author Djk
 * @Title:
 * @Time: 2017/11/30.
 * @Version:1.0.0
 */

public class MyApp extends Application {
    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    @Override

    public void onCreate() {
        super.onCreate();
        mContext = this.getApplicationContext();
    }
}
