package com.dengzi.dzokhttp.download;

import java.io.File;
import java.io.IOException;

/**
 * @Title: 下载回调
 * @Author: djk
 * @Time: 2017/11/29
 * @Version:1.0.0
 */
public interface DownloadCallback {
    int STATE_PREPARE = 0x0011;
    int STATE_DOWNLOADING = 0x0012;
    int STATE_SUCCESS = 0x0013;
    int STATE_FAIL = 0x0014;
    int STATE_PAUSE = 0x0015;

    void onState(int state);

    void onFailure(IOException e);

    void onProgress(long currentLength, long totalLength);

    void onSucceed(File file);
}
