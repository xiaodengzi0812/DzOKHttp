package com.dengzi.dzokhttp.progress_upload;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * @author Djk
 * @Title: 上传RequestBody
 * @Time: 2017/11/27.
 * @Version:1.0.0
 */
public class ProgressUpLoadRequestBody extends RequestBody {
    private RequestBody mRequestBody;
    private long mCurrentLength;
    private ProgressUpLoadListener mListener;

    public ProgressUpLoadRequestBody(RequestBody mRequestBody) {
        this.mRequestBody = mRequestBody;
    }

    public ProgressUpLoadRequestBody(RequestBody mRequestBody, ProgressUpLoadListener listener) {
        this.mRequestBody = mRequestBody;
        this.mListener = listener;
    }

    @Override
    public MediaType contentType() {
        return mRequestBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return mRequestBody.contentLength();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        MySkin mySkin = new MySkin(sink);
        BufferedSink bufferedSink = Okio.buffer(mySkin);
        mRequestBody.writeTo(bufferedSink);
        bufferedSink.flush();
    }

    private class MySkin extends ForwardingSink {

        public MySkin(Sink delegate) {
            super(delegate);
        }

        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            // 每次写都会来这里
            mCurrentLength += byteCount;
            if (mListener != null) {
                mListener.onProgress(contentLength(), mCurrentLength);
            }
            super.write(source, byteCount);
        }
    }
}
