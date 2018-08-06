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

    private String result;

    private byte[] responseBody;

    private Map<String, List<String>> responseHeaders;

    private InputStream inputStream;

    public Response(InputStream inputStream, Map<String, List<String>> responseHeaders) {
        this.responseHeaders = responseHeaders;
        this.inputStream = inputStream;
    }

    public byte[] getResponseBody() {
        return responseBody;
    }

    public String string() {
        try {
            result = Util.convertStream2String(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Util.close(inputStream);
            inputStream = null;
        }
        return result;
    }

    public long getContentLength() {
        return Long.parseLong(responseHeaders.get("Content-Length").get(0));
    }
}
