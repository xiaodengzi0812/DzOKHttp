package com.dengzi.dzokhttp.download;

import android.os.Environment;

import java.io.File;

/**
 * @author Djk
 * @Title: 文件管理类
 * @Time: 2017/11/29.
 * @Version:1.0.0
 */
public class FileManager {
    private String mRootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "okdown" + File.separator;
    private static final FileManager mFileManager = new FileManager();

    private FileManager() {
        // 文件夹不存在则创建文件夹
        File pathFile = new File(mRootPath);
        if (!pathFile.exists()) {
            pathFile.mkdir();
        }
    }

    static FileManager getInstance() {
        return mFileManager;
    }

    /**
     * 通过url来获取本的一个文件
     *
     * @param url 下载路径
     * @return 文件
     */
    File getFile(String url) {
        // 用url的md值做为文件名
        String filePath = mRootPath + Utils.md5Url(url);
        return new File(filePath);
    }

}
