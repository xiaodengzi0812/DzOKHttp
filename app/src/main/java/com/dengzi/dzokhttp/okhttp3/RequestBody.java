package com.dengzi.dzokhttp.okhttp3;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;

/**
 * @author Djk
 * @Title:
 * @Time: 2017/11/24.
 * @Version:1.0.0
 */
public interface RequestBody {

    /**
     * 添加请求参数
     *
     * @return
     * @throws IOException
     */
    void addRequestProperty(HttpURLConnection connection);

    /**
     * 写入内容
     *
     * @param outputStream
     * @throws IOException
     */
    void onWriteBody(OutputStream outputStream);

}
