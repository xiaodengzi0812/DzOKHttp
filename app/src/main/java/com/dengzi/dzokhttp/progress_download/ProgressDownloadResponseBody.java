package com.dengzi.dzokhttp.progress_download;

import android.util.Log;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * @author Djk
 * @Title: 下载监听的ResponseBody
 * @Time: 2017/11/27.
 * @Version:1.0.0
 */
public class ProgressDownloadResponseBody extends ResponseBody {
    // 当前下载的进度
    private long mCurrentLength;
    // 原始ResponseBody
    private ResponseBody mResponseBody;
    // 进度条
    private ProgressDownListener mListener;

    public ProgressDownloadResponseBody(ResponseBody responseBody, ProgressDownListener mListener) {
        this.mResponseBody = responseBody;
        this.mListener = mListener;
    }

    @Override
    public MediaType contentType() {
        return mResponseBody.contentType();
    }

    @Override
    public long contentLength() {
        return mResponseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        MySource mySource = new MySource(mResponseBody.source());
        BufferedSource bufferedSource = Okio.buffer(mySource);
        return bufferedSource;
    }

    private class MySource extends ForwardingSource {

        public MySource(Source delegate) {
            super(delegate);
        }

        @Override
        public long read(Buffer sink, long byteCount) throws IOException {
            // 读取Source
            long bytesRead = super.read(sink, byteCount);
            if (bytesRead != -1) {
                mCurrentLength += bytesRead;
                if (mListener != null) {
                    mListener.onProgress(contentLength(), mCurrentLength);
                }
            }
            sink.flush();
            return bytesRead;
        }
    }

}
