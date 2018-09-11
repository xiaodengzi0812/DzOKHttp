package com.dengzi.dzokhttp.okhttp3.interceptor;

import android.text.TextUtils;
import android.util.Log;


import com.dengzi.dzokhttp.okhttp3.Headers;
import com.dengzi.dzokhttp.okhttp3.Request;
import com.dengzi.dzokhttp.okhttp3.RequestBody;
import com.dengzi.dzokhttp.okhttp3.Response;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;

/**
 * @author Djk
 * @Title: 服务的请求
 * @Time: 2017/11/24.
 * @Version:1.0.0
 */
public class ServiceIntercepor implements Interceptor {
    private static final String TAG = "HTTP";

    @Override
    public Response intercept(Chain chain) throws Exception {
        Request request = chain.request();

        HttpURLConnection httpURLConnection = (HttpURLConnection) request.url().url().openConnection();
        if (httpURLConnection instanceof HttpsURLConnection) {
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) httpURLConnection;
            // 设置一些证书
            //httpsURLConnection.setSSLSocketFactory();
            //httpsURLConnection.setHostnameVerifier();
        }

        httpURLConnection.setRequestMethod(request.getMethod().value());
        httpURLConnection.setDoOutput(request.getMethod().isOut());
        httpURLConnection.setDoInput(true);
        httpURLConnection.setConnectTimeout(5000);
        httpURLConnection.setReadTimeout(5000);
        httpURLConnection.setRequestProperty("connection", "Keep-Alive");
        httpURLConnection.setRequestProperty("Charsert", "UTF-8");

        // 把头部提交一下
        Headers headers = request.getHeaders();
        int headerSize = headers.size();
        for (int i = 0; i < headerSize; i++) {
            httpURLConnection.setRequestProperty(headers.name(i), headers.value(i));
        }
        RequestBody requestBody = request.getRequestBody();
        if (requestBody != null) {
            requestBody.addRequestProperty(httpURLConnection);
        }

        // post 提交数据和文件数据
        httpURLConnection.connect();

        if (request.getMethod().isOut()) {
            requestBody.onWriteBody(httpURLConnection.getOutputStream());
        }

        int responseCode = httpURLConnection.getResponseCode();
        Log.e(TAG, "responseCode = " + responseCode);
        Map<String, List<String>> responseHeaders = httpURLConnection.getHeaderFields();

        if (hasBody(responseCode)) {
                    /*InputStream inputStream = getRealInputStream(responseCode, responseHeaders, httpURLConnection);
                    httpURLConnection.getInputStream();

                    // 把 InputStream 转成 String
                    String result = Util.convertStream2String(inputStream);*/
            InputStream inputStream = getRealInputStream(responseCode, responseHeaders, httpURLConnection);
            Response response = new Response(responseCode, inputStream, responseHeaders);
            return response;
        }
        return null;
    }

    private InputStream getRealInputStream(int responseCode, Map<String, List<String>> responseHeaders,
                                           HttpURLConnection httpURLConnection) throws IOException {
        InputStream inputStream;
        if (responseCode >= 400) {
            inputStream = httpURLConnection.getErrorStream();
        } else {
            inputStream = httpURLConnection.getInputStream();
        }
        String contentEncoding = httpURLConnection.getContentEncoding();
        if (!TextUtils.isEmpty(contentEncoding) && contentEncoding.contains("gzip")) {
            inputStream = new GZIPInputStream(inputStream);
        }
        return inputStream;
    }

    private boolean hasBody(int responseCode) {
        return !(responseCode >= 100 && responseCode < 200)
                || responseCode != 204 || responseCode != 205
                || !(responseCode >= 300 && responseCode < 400);
    }

}