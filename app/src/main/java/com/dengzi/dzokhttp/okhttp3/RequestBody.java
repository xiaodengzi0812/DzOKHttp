package com.dengzi.dzokhttp.okhttp3;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Djk
 * @Title:
 * @Time: 2017/11/24.
 * @Version:1.0.0
 */
public class RequestBody {
    public static final String FORM = "multipart/form-data";
    private String type;
    private String boundary = createBoundary();
    private String startBoundary = "--" + boundary;
    private String endBoundary = startBoundary + "--";
    final Map<String, Object> params;

    public RequestBody() {
        params = new HashMap<>();
    }

    public RequestBody setType(String type) {
        this.type = type;
        return this;
    }

    public RequestBody addFormDataPart(String key, String value) {
        params.put(key, value);
        return this;
    }

    public RequestBody addFormDataPart(String key, Binary binary) {
        params.put(key, binary);
        return this;
    }

    final Map<String, Object> getParams() {
        return params;
    }

    public String getContentType() {
        return type + "; boundary=" + boundary;
    }

    public static Binary create(final File file) {
        if (file == null) throw new NullPointerException("content == null");
        return new Binary() {
            @Override
            public String mimeType() {
                FileNameMap fileNameMap = URLConnection.getFileNameMap();
                String contentTypeFor = fileNameMap.getContentTypeFor(file.getAbsolutePath());
                if (contentTypeFor == null) {
                    contentTypeFor = "application/octet-stream";
                }
                return contentTypeFor;
            }

            @Override
            public String fileName() {
                return file.getName();
            }

            @Override
            public void writeBinary(OutputStream outputStream) throws IOException {
                InputStream inputStream = new FileInputStream(file);
                byte[] buffer = new byte[2048];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, len);
                }
                inputStream.close();
            }

            @Override
            public long getContentLength() {
                return file.length();
            }
        };
    }

    /**
     * 统计长度
     *
     * @return
     * @throws IOException
     */
    public long getContentLength() throws IOException {
        long contentLength = 0;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            // Log.e("TAG", "params" + entry.getValue());
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Binary) {
                Binary binary = (Binary) value;
                String data = startBoundary + "\r\n"
                        + "Content-Disposition: form-data; name = \""
                        + key + "\"; filename = \""
                        + binary.fileName() + "\"" + "\r\n"
                        + "Content-Type: " + binary.mimeType() + "\r\n\r\n";

                contentLength += data.getBytes().length;
                contentLength += binary.getContentLength();
            } else {
                String data = startBoundary + "\r\n"
                        + "Content-Disposition: form-data; name = \""
                        + key + "\"" + "\r\n"
                        + "Content-Type: text/plain" + "\r\n\r\n";

                contentLength += data.getBytes().length;
                contentLength += ((String) value).getBytes().length;
            }
            contentLength += "\r\n".getBytes().length;
        }

        if (params.size() > 0) {
            contentLength += endBoundary.getBytes().length;
        }

        return contentLength;
    }

    /**
     * 写入内容
     *
     * @param outputStream
     * @throws IOException
     */
    public void onWriteBody(OutputStream outputStream) throws IOException {
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Binary) {
                writeFromBinary(key, (Binary) value, outputStream);
            } else {
                writeFromString(key, (String) value, outputStream);
            }
            outputStream.write("\r\n".getBytes());
        }

        if (params.size() > 0) {
            outputStream.write(endBoundary.getBytes());
        }
    }

    public void writeFromString(String key, String value, OutputStream outputStream) throws IOException {
        String data = startBoundary + "\r\n"
                + "Content-Disposition: form-data; name = \""
                + key + "\"" + "\r\n"
                + "Content-Type: text/plain" + "\r\n\r\n";
        outputStream.write(data.getBytes());
        outputStream.write(value.getBytes());
    }

    public void writeFromBinary(String key, Binary binary, OutputStream outputStream) throws IOException {
        String data = startBoundary + "\r\n"
                + "Content-Disposition: form-data; name = \""
                + key + "\"; filename = \""
                + binary.fileName() + "\"" + "\r\n"
                + "Content-Type: " + binary.mimeType() + "\r\n\r\n";

        Log.e("TAG", "data = " + data);
        outputStream.write(data.getBytes());
        binary.writeBinary(outputStream);
    }

    private String createBoundary() {
        return "OkHttp" + UUID.randomUUID();
    }
}
