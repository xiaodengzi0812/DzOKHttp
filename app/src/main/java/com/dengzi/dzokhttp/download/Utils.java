package com.dengzi.dzokhttp.download;

import android.text.TextUtils;

import java.io.Closeable;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @Title: 工具类
 * @Author: djk
 * @Time: 2017/11/29
 * @Version:1.0.0
 */
public class Utils {

    /**
     * 获取一个url的md5值
     *
     * @param url
     * @return
     */
    public static String md5Url(String url) {
        if (TextUtils.isEmpty(url)) {
            return url;
        }
        StringBuffer sb = new StringBuffer();
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("md5");
            messageDigest.update(url.getBytes());
            byte[] cipher = messageDigest.digest();
            for (byte b : cipher) {
                // 转成了 16 机制
                String hexStr = Integer.toHexString(b & 0xff);
                // 不足还补 0
                sb.append(hexStr.length() == 1 ? "0" + hexStr : hexStr);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static String md5UrlThreadId(String url, int threadId) {
        return md5Url(url) + "_" + Integer.toString(threadId);
    }

    /**
     * 关闭
     *
     * @param closeable
     */
    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
