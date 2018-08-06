package com.dengzi.dzokhttp.okhttp3;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Djk
 * @Title:
 * @Time: 2017/11/24.
 * @Version:1.0.0
 */
public interface Binary {
    String mimeType();

    String fileName();

    void writeBinary(OutputStream outputStream) throws IOException;

    long getContentLength();
}
