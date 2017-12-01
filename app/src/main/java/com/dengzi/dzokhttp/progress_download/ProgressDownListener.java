package com.dengzi.dzokhttp.progress_download;

/**
 * @author Djk
 * @Title: 下载进度条
 * @Time: 2017/11/27.
 * @Version:1.0.0
 */
public interface ProgressDownListener {
    void onProgress(long totalLength, long currentLength);
}
