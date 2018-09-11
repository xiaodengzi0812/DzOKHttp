package com.dengzi.dzokhttp.okhttp3;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;

public class ContextRequestBody implements RequestBody {

    private String mContext;

    public ContextRequestBody() {
    }

    public ContextRequestBody setContent(String content) {
        mContext = content;
        return this;
    }

    /**
     * 统计长度
     *
     * @return
     * @throws IOException
     */
    public long getContentLength() {
        long contentLength = mContext.getBytes().length;
        return contentLength;
    }

    @Override
    public void addRequestProperty(HttpURLConnection connection) {
        connection.addRequestProperty("Content-Length", getContentLength() + "");
    }

    /**
     * 写入内容
     *
     * @param outputStream
     * @throws IOException
     */
    public void onWriteBody(OutputStream outputStream) {
        try {
            outputStream.write(mContext.getBytes());
        } catch (IOException e) {
        }
    }
}
