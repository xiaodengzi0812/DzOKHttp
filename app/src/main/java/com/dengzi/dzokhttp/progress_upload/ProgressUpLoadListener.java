package com.dengzi.dzokhttp.progress_upload;

/**
 * @author Djk
 * @Title: 上传进度监听
 * @Time: 2017/11/27.
 * @Version:1.0.0
 */
public interface ProgressUpLoadListener {
    void onProgress(long totalLength, long currentLength);
}
