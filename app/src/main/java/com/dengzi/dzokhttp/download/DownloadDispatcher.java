package com.dengzi.dzokhttp.download;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @author Djk
 * @Title: 下载Dispatcher
 * @Time: 2017/11/29.
 * @Version:1.0.0
 */
public class DownloadDispatcher {
    // 单例创建一个DownloadDispatcher
    private static volatile DownloadDispatcher mInstance;
    private Handler mHandler;

    private DownloadDispatcher() {
        mHandler = new Handler(Looper.getMainLooper());
    }

    public static DownloadDispatcher getInstance() {
        if (mInstance == null) {
            synchronized (DownloadDispatcher.class) {
                if (mInstance == null) {
                    mInstance = new DownloadDispatcher();
                }
            }
        }
        return mInstance;
    }

    // 同时可以支持两个文件同时下载
    private static final int MAX_COUNT = 2;
    // 根据手机来判断同时开启多少个线程,从AsyncTask中复制来的源码
    static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    static final int THREAD_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));

    // 线程池,从OkHttp的Dispatcher类中复制来的源码
    private ExecutorService mExecutorService;
    // 准备执行的队列
    private final Deque<DownloadTask> mReadyTask = new ArrayDeque<>();
    // 正在执行中的队列
    private final Deque<DownloadTask> mRunningTask = new ArrayDeque<>();

    // 可回收的线程池
    public synchronized ExecutorService executorService() {
        if (mExecutorService == null) {
            mExecutorService = new ThreadPoolExecutor(0, 64, 30, TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>(), new ThreadFactory() {
                private AtomicInteger mCount = new AtomicInteger(1);

                @Override
                public Thread newThread(@NonNull Runnable r) {
                    Thread thread = new Thread(r, "DownloadTask->" + mCount.getAndAdd(1));
                    thread.setDaemon(false);
                    return thread;
                }
            });
        }
        return mExecutorService;
    }

    /**
     * 开始下载
     *
     * @param url      下载路径
     * @param callback 回调
     */
    public void startDownload(final String url, final DownloadCallback callback) {
        OkHttpManager.getInstance().asyncCall(url)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        boolean isMainLooper = Looper.myLooper() == Looper.getMainLooper();
                        // 获取文件最大的长度
                        ResponseBody body = response.body();
                        long totalLength = body == null ? 0 : body.contentLength();
                        // 判断一下是否能获取到最大的长度,如果获取不到文件的最大长度,则开启单线程下载
                        if (totalLength <= -1) {
                            singleDown(response, callback);
                            return;
                        }
                        // 创建一个下载Task,
                        DownloadTask downloadTask = new DownloadTask(url, totalLength);
                        downloadTask.addCallback(new DispatcherCallback(downloadTask, callback));
                        addTask(downloadTask);
                    }
                });
    }

    /**
     * 添加文件下载task
     *
     * @param downloadTask 下载task
     */
    private void addTask(DownloadTask downloadTask) {
        if (mRunningTask.size() >= MAX_COUNT) {
            mReadyTask.add(downloadTask);
        } else {
            mRunningTask.add(downloadTask);
            downloadTask.start();
        }
    }

    /**
     * 停止下载
     *
     * @param url
     */
    public void stopDownload(String url) {
        if (mRunningTask != null) {
            for (DownloadTask downloadTask : mRunningTask) {
                if (downloadTask.mUrl.equals(url)) {
                    downloadTask.stop();
                    finished(downloadTask);
                    return;
                }
            }
        }
    }

    /**
     * 自己类的回调
     */
    private class DispatcherCallback implements DownloadCallback {
        private WeakReference<DownloadTask> weakDownloadTask;
        private WeakReference<DownloadCallback> weakCallback;

        DispatcherCallback(DownloadTask downloadTask, DownloadCallback callback) {
            this.weakDownloadTask = new WeakReference<>(downloadTask);
            this.weakCallback = new WeakReference<>(callback);
        }

        @Override
        public void onState(final int state) {
            final DownloadCallback callback = weakCallback.get();
            if (callback != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onState(state);
                    }
                });
            }
        }

        @Override
        public void onFailure(final IOException e) {
            final DownloadCallback callback = weakCallback.get();
            DownloadTask downloadTask = weakDownloadTask.get();
            if (callback != null && downloadTask != null) {
                finished(downloadTask);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onFailure(e);
                    }
                });
            }
        }

        @Override
        public void onProgress(final long currentLength, final long totalLength) {
            final DownloadCallback callback = weakCallback.get();
            if (callback != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onProgress(currentLength, totalLength);
                    }
                });
            }
        }

        @Override
        public void onSucceed(final File file) {
            // 把回调转到主线程再回调回去
            final DownloadCallback callback = weakCallback.get();
            DownloadTask downloadTask = weakDownloadTask.get();
            if (callback != null && downloadTask != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSucceed(file);
                    }
                });
                finished(downloadTask);
            }
        }
    }

    /**
     * 执行结束
     *
     * @param downloadTask 下载task
     */
    private void finished(DownloadTask downloadTask) {
        // 从正在执行的队列中移除该task
        mRunningTask.remove(downloadTask);
        synchronized (DownloadDispatcher.class) {
            // 判断 正在执行的task数量小于MAX_COUNT && 准备执行的task数量大于0
            while (mRunningTask.size() < MAX_COUNT && mReadyTask.size() > 0) {
                // 读取准备执行队列中的第一个并赋值给newTask
                DownloadTask newTask = mReadyTask.getFirst();
                // 移除准备队列中的第一个
                mReadyTask.removeFirst();
                // 正在执行队列添加一个
                mRunningTask.add(newTask);
                // 开始执行
                newTask.start();
            }
        }
    }

    /**
     * 单线程下载
     *
     * @param response 回调内容
     */
    private void singleDown(Response response, DownloadCallback callback) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        long totalLength = 0;
        try {
            File apkFile = new File(Environment.getExternalStorageDirectory(), "内涵段子.apk");
            if (response != null && response.body() != null) {
                ResponseBody body = response.body();
                inputStream = body == null ? null : body.byteStream();
                totalLength = body == null ? 0 : body.contentLength();
            }
            if (inputStream == null) {
                return;
            }
            outputStream = new FileOutputStream(apkFile);
            long currentLength = 0;
            int len;
            byte[] buffer = new byte[1024 * 10];
            while ((len = inputStream.read(buffer)) != -1) {
                currentLength += len;
                outputStream.write(buffer, 0, len);
                callback.onProgress(currentLength, totalLength);
            }
            callback.onSucceed(apkFile);
        } catch (IOException e) {
            callback.onFailure(e);
        } finally {
            Utils.close(inputStream);
            Utils.close(outputStream);
        }
    }
}
