package com.dengzi.dzokhttp.download;


import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.dengzi.dzokhttp.okhttp3.Util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @author Djk
 * @Title: 文件的下载线程
 * @Time: 2017/11/29.
 * @Version:1.0.0
 */
public class DownloadRunnable implements Runnable {
    private long mStart;// 开始位置
    private final long mEnd;// 结束位置
    private final String mUrl;// 下载地址
    private final int mThreadId;// 当前线程id
    private DownloadCallback mCallBack;// 回调
    private boolean mIsStop;// 是否要结束下载
    private long mCurrentLength = 0;// 当前已下载长度
    private int mIsSuccess = 0;// 当前线程是否下载成功
    private File mDownFile;// 当前下载的文件

    public DownloadRunnable(long start, long end, int threadId, String url, DownloadCallback callback) {
        this.mStart = start;
        this.mEnd = end;
        this.mThreadId = threadId;
        this.mUrl = url;
        this.mCallBack = callback;
        initCurrentLength();
    }

    @Override
    public void run() {
        RandomAccessFile accessFile = null;
        InputStream inputStream = null;
        try {
            Response response = OkHttpManager.getInstance().syncResponse(mUrl, mStart, mEnd);
            ResponseBody body = response == null ? null : response.body();
            inputStream = body == null ? null : body.byteStream();
            mDownFile = FileManager.getInstance().getFile(mUrl);
            accessFile = new RandomAccessFile(mDownFile, "rwd");
            accessFile.seek(mStart);
            int len;
            byte[] buffer = new byte[1024 * 10];
            while ((len = inputStream.read(buffer)) != -1) {
                if (mIsStop) {
                    saveCurrentLength();
                    break;
                }
                mCurrentLength += len;
                accessFile.write(buffer, 0, len);
            }
            mIsSuccess = 1;
        } catch (IOException e) {
            e.printStackTrace();
            mCallBack.onFailure(e);
        } finally {
            Utils.close(accessFile);
            Utils.close(inputStream);
        }
    }

    /**
     * 保存当前进度
     */
    private void saveCurrentLength() {
        String key = Utils.md5UrlThreadId(mUrl, mThreadId);
        SPUtils.getInstance("DOWN").put(key, mCurrentLength);
    }

    /**
     * 初始化当前进度
     */
    private void initCurrentLength() {
        String key = Utils.md5UrlThreadId(mUrl, mThreadId);
        mCurrentLength = SPUtils.getInstance("DOWN").getLong(key, 0);
        mStart = mStart + mCurrentLength;
    }

    /**
     * 停止
     */
    void stop() {
        mIsStop = true;
        saveCurrentLength();
    }

    /**
     * 获取当前下载长度
     *
     * @return 当前下载长度
     */
    long getCurrentLength() {
        return mCurrentLength;
    }

    int getSuccessState() {
        return mIsSuccess;
    }

    File getDownFile() {
        return mDownFile;
    }


}
