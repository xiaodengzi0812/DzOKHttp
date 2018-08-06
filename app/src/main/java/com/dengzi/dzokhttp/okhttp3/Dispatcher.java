package com.dengzi.dzokhttp.okhttp3;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Title:网络请求类
 * @Author: djk
 * @Time: 2017/11/16
 * @Version:1.0.0
 */
public class Dispatcher {
    // 同时执行的最大线程数
    private int maxRequests = 5;
    // 线程执行Executor
    private ExecutorService executorService;
    // 准备执行的队列
    private final Deque<RealCall.AsyncCall> readyAsyncCalls = new ArrayDeque<>();
    // 异步执行中的队列
    private final Deque<RealCall.AsyncCall> runningAsyncCalls = new ArrayDeque<>();
    // 同步执行中的队列
    private final Deque<RealCall> runningSyncCalls = new ArrayDeque<>();

    // 构造
    public Dispatcher() {
    }

    // 可以传一个线程执行类进来
    public Dispatcher(ExecutorService executorService) {
        this.executorService = executorService;
    }

    // 单例创建线程池
    public synchronized ExecutorService executorService() {
        if (executorService == null) {
            executorService = new ThreadPoolExecutor(
                    0, // 核心线程数
                    Integer.MAX_VALUE,// 最大线程数
                    60,// 线程存活的时间
                    TimeUnit.SECONDS,// 线程存活的时间的单位
                    new SynchronousQueue<Runnable>(), Util.threadFactory("OkHttp Dispatcher", false));// 线程创建工厂
        }
        return executorService;
    }

    /**
     * 同步执行
     *
     * @param call
     */
    synchronized void executed(RealCall call) {
        runningSyncCalls.add(call);
    }

    /**
     * 异步执行
     *
     * @param call
     */
    public void enqueue(RealCall.AsyncCall call) {
        // 当前执行的队列size < 最大执行线程数
        if (runningAsyncCalls.size() < maxRequests) {
            // 直接添加到执行中队列
            runningAsyncCalls.add(call);
            // 从线程池中获取一个空闲的线程来执行
            executorService().execute(call);
        } else { // 当前执行的队列size > 最大执行线程数
            // 将本次执行添加到准备执行的队列
            readyAsyncCalls.add(call);
        }
    }

    /**
     * 继续执行准备执行的队列中的线程
     */
    private synchronized void promoteCalls() {
        // 如果异步执行的总数 > 最大执行线程数,则不继续执行
        if (runningAsyncCalls.size() >= maxRequests) return;
        // 如果准备执行的队列为null,则不继续执行
        if (readyAsyncCalls.isEmpty()) return; // No ready calls to promote.
        // 获取准备执行的队列
        for (Iterator<RealCall.AsyncCall> i = readyAsyncCalls.iterator(); i.hasNext(); ) {
            // 从准备执行的队列中取一个,放到正在执行队列中
            RealCall.AsyncCall call = i.next();
            i.remove();
            runningAsyncCalls.add(call);
            executorService().execute(call);
            // 如果异步执行的总数 > 最大执行线程数,则跳出循环
            if (runningAsyncCalls.size() >= maxRequests) return;
        }
    }

    /**
     * 单次异步执行结束
     */
    public void finished(RealCall.AsyncCall call) {
        runningAsyncCalls.remove(call);
        promoteCalls();
    }

    /**
     * 获取正在执行的线程总数
     *
     * @return
     */
    public synchronized int runningCallsCount() {
        // 异步线程数 + 同步线程数
        return runningAsyncCalls.size() + runningSyncCalls.size();
    }

    /**
     * 取消所有的请求
     */
    public synchronized void cancelAll() {
        // 取消准备队列中的请求
        for (RealCall.AsyncCall call : readyAsyncCalls) {
            call.get().cancel();
        }
        readyAsyncCalls.clear();
//        // 取消异步执行中的队列
//        for (RealCall.AsyncCall call : runningAsyncCalls) {
//            call.get().cancel();
//        }
//        // 取消同步执行中的队列
//        for (RealCall call : runningSyncCalls) {
//            call.cancel();
//        }
    }
}
