package com.dengzi.dzokhttp.download;

import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;

/**
 * @author Djk
 * @Title: 文件下载task
 * @Time: 2017/11/29.
 * @Version:1.0.0
 */
class DownloadTask {
    public String mUrl;// 下载url
    private long mTotalLength;// 下载文件总长度
    private volatile long mCurrentLength;// 当前下载文件的长度
    private List<DownloadRunnable> mRunnableList = new ArrayList<>();// Runnable集合,里面存放下载线程
    private static final int DEFAULT_TIME = 50;// 50ms回调一次
    private DownloadCallback mCallback;// 上级传来的回调
    private File mDownFile;// 当前下载的文件
    private boolean mIsSuccess;
    private boolean mIsStop;
    private ProgressThread mProgressThread;

    DownloadTask(String url, long totalLength) {
        this.mUrl = url;
        this.mTotalLength = totalLength;
        init();
    }

    /**
     * 初始化线程
     */
    private void init() {
        // 获取当前手机最优开启的线程数
        int threadSize = DownloadDispatcher.THREAD_SIZE;
        mProgressThread = new ProgressThread();
        for (int i = 0; i < threadSize; i++) {
            // 根据线程数来计算每个线程所要下载的长度
            long threadLength = mTotalLength / threadSize;
            long start = i * threadLength;
            long end = (i + 1) * threadLength - 1;
            // 如果是最后一个线程,下载剩下所有的长度
            if (i == threadSize - 1) {
                end = mTotalLength - 1;
            }
            // 创建一个下载线程,并添加到线程集合
            DownloadRunnable downloadRunnable = new DownloadRunnable(start, end, i, mUrl, new TaskCallback(i));
            mRunnableList.add(downloadRunnable);
        }
    }

    /**
     * 停止下载
     */
    void stop() {
        mIsStop = true;
        mCallback = null;
        if (mRunnableList != null) {
            for (DownloadRunnable downloadRunnable : mRunnableList) {
                downloadRunnable.stop();
                downloadRunnable = null;
            }
        }
        mRunnableList.clear();
        mRunnableList = null;
        mProgressThread = null;
    }

    /**
     * 自己类的回调
     */
    private class TaskCallback implements DownloadCallback {
        int threadId;

        TaskCallback(int threadId) {
            this.threadId = threadId;
        }

        @Override
        public void onState(int state) {

        }

        @Override
        public void onFailure(IOException e) {
            // 只要有一个线程失败,就将全部线程停止
            for (DownloadRunnable downloadRunnable : mRunnableList) {
                downloadRunnable.stop();
            }
        }

        @Override
        public void onProgress(long currentLength, long totalLength) {
        }

        @Override
        public void onSucceed(File file) {
        }
    }

    /**
     * 添加回调
     *
     * @param callback 回调
     */
    void addCallback(DownloadCallback callback) {
        this.mCallback = callback;
        this.mCallback.onState(DownloadCallback.STATE_PREPARE);
    }

    /**
     * 开始下载
     */
    void start() {
        this.mCallback.onState(DownloadCallback.STATE_DOWNLOADING);
        // 遍历线程集合并添加到线程池中开始下载
        for (DownloadRunnable downloadRunnable : mRunnableList) {
            DownloadDispatcher.getInstance().executorService().execute(downloadRunnable);
        }
        DownloadDispatcher.getInstance().executorService().execute(mProgressThread);
    }

    /**
     * 进度条回调线程,单独开一个线程来处理进度条的回调
     */
    private class ProgressThread extends Thread {
        @Override
        public void run() {
            long startTime = SystemClock.currentThreadTimeMillis();
            // 未下载结束就一直循环
            while (mCurrentLength < mTotalLength) {
                if (mIsStop) return;
                long currentTime = SystemClock.currentThreadTimeMillis();
                // 为了性能,50ms回调一次
                if (currentTime - startTime > DEFAULT_TIME) {
                    startTime = currentTime;
                    if (mCallback != null) {
                        mCallback.onProgress(getCurrentLength(), mTotalLength);
                        if (mIsSuccess) {
                            mCallback.onSucceed(mDownFile);
                        }
                    }
                }
            }
        }
    }

    /**
     * 遍历获取子类集合的当前下载数
     *
     * @return 当前下载数
     */
    private long getCurrentLength() {
        int successState = 0;
        mCurrentLength = 0;
        if (mRunnableList == null) return 0;
        for (DownloadRunnable downloadRunnable : mRunnableList) {
            mCurrentLength += downloadRunnable.getCurrentLength();
            successState += downloadRunnable.getSuccessState();
            if (mDownFile == null && downloadRunnable.getSuccessState() == 1) {
                mDownFile = downloadRunnable.getDownFile();
            }
        }
        if (DownloadDispatcher.THREAD_SIZE == successState && !mIsSuccess && mCurrentLength == mTotalLength) {
            mIsSuccess = true;
            downSuccess();
        }
        return mCurrentLength;
    }

    private void downSuccess() {
        for (int i = 0; i < mRunnableList.size(); i++) {
            String key = Utils.md5UrlThreadId(mUrl, i);
            SPUtils.getInstance("DOWN").put(key, 0L);
        }
    }

}
