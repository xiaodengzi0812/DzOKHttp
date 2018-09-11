package com.dengzi.dzokhttp.okhttp3;

import android.text.TextUtils;
import android.util.Log;


import com.dengzi.dzokhttp.util.FileIOUtils;
import com.dengzi.dzokhttp.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Djk
 * @Title:
 * @Time: 2017/11/24.
 * @Version:1.0.0
 */
public class FileRequestBody implements RequestBody {
    private String type;
    final Map<String, Object> params = new HashMap<>();
    private String boundary = createBoundary();
    final String NEWLINE = "\r\n"; // 换行，或者说是回车
    final String PREFIX = "--"; // 固定的前缀


    public FileRequestBody() {
    }

    public FileRequestBody setType(String type) {
        this.type = type;
        return this;
    }

    public FileRequestBody addFormDataPart(String key, String value) {
        params.put(key, value);
        return this;
    }

    public FileRequestBody addFile(File file) {
        return addFile(file, null);
    }

    public FileRequestBody addFile(File file, String fileName) {
        if (!FileUtils.isFile(file)) {// 文件不存在,则不添加此文件
            return this;
        }

        Binary fileBinary = create(file);
        if (TextUtils.isEmpty(fileName)) {
            fileName = file.getName();
        }
        params.put(fileName, fileBinary);
        return this;
    }

    final Map<String, Object> getParams() {
        return params;
    }

    public String getContentType() {
        return type + "; boundary=" + boundary;
    }

    private Binary create(final File file) {
        if (file == null) throw new NullPointerException("file == null");
        return new Binary() {
            @Override
            public String mimeType() {
                return "";
            }

            @Override
            public String fileName() {
                return file.getName();
            }

            @Override
            public void writeBinary(OutputStream outputStream) throws IOException {
                byte[] buffer = FileIOUtils.readFile2BytesByChannel(file);
                outputStream.write(buffer);
            }

            @Override
            public long getContentLength() {
                return file.length();
            }
        };
    }

    @Override
    public void addRequestProperty(HttpURLConnection connection) {
        connection.addRequestProperty("Content-Type", getContentType());
    }

    /**
     * 写入内容
     *
     * @param outputStream
     * @throws IOException
     */
    @Override
    public void onWriteBody(OutputStream outputStream) {
        try {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof Binary) {
                    writeFromBinary("file", (Binary) value, outputStream);
                } else {
                    writeFromString(key, (String) value, outputStream);
                }
                outputStream.write(NEWLINE.getBytes());
            }

            if (params.size() > 0) {
                outputStream.write((PREFIX + boundary + PREFIX).getBytes());
                outputStream.write(NEWLINE.getBytes());
            }
        } catch (IOException e) {
        }
    }

    public void writeFromString(String key, String value, OutputStream outputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(PREFIX + boundary + NEWLINE)
                .append("Content-Disposition: form-data; name = \"").append(key).append("\";")
                .append(NEWLINE)
                .append("Content-Type: text/plain")
                .append(NEWLINE)
                .append(NEWLINE);

        outputStream.write(sb.toString().getBytes());
        outputStream.write(value.getBytes());
    }

    public void writeFromBinary(String key, Binary binary, OutputStream outputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(PREFIX + boundary + NEWLINE)
                .append("Content-Disposition: form-data; name = \"").append(key).append("\";")
                .append("filename = \"").append(binary.fileName()).append("\"")
                .append(NEWLINE)
                .append("Content-Type: ").append(binary.mimeType())
                .append(NEWLINE)
                .append(NEWLINE);

        Log.e("TAG", "data = " + sb.toString());
        outputStream.write(sb.toString().getBytes());
        binary.writeBinary(outputStream);
    }

    private String createBoundary() {
        return "dzHttp" + UUID.randomUUID();
    }

}
