package com.dengzi.dzokhttp.okhttp3;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @author Djk
 * @Title:网络响应
 * @Time: 2017/11/24.
 * @Version:1.0.0
 */
public class Response {

    private int responseCode;

    private byte[] responseBody;

    private Map<String, List<String>> responseHeaders;

    private InputStream inputStream;

    public Response(int responseCode, InputStream inputStream, Map<String, List<String>> responseHeaders) {
        this.responseCode = responseCode;
        this.responseHeaders = responseHeaders;
        this.inputStream = inputStream;
    }

    public byte[] getResponseBody() {
        return responseBody;
    }

    public String string() {
        String result = "";
        try {
            result = Util.convertStream2String(inputStream);
        } catch (IOException e) {
        } finally {
            Util.close(inputStream);
            inputStream = null;
        }
        return result;
    }

    public long getContentLength() {
        return Long.parseLong(responseHeaders.get("Content-Length").get(0));
    }

    public int getResponseCode() {
        return this.responseCode;
    }
}
