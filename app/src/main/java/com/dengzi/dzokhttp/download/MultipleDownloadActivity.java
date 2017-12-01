package com.dengzi.dzokhttp.download;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.dengzi.dzokhttp.R;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * @Title: 多线程下载
 * @Author: djk
 * @Time: 2017/11/28
 * @Version:1.0.0
 */
public class MultipleDownloadActivity extends AppCompatActivity {
    String mApkUrl1 = "http://acj3.pc6.com/pc6_soure/2017-11/com.ss.android.essay.joke_664.apk";
    String mApkUrl2 = "http://gyxz.exmmw.cn/hk/rj_gyc1/dsqb.apk";
    String mApkUrl3 = "http://p3.exmmw.cn/p1/wn/zhuishushenqi.apk";
    String mApkUrl4 = "http://p3.exmmw.cn/p1/wn/zhuishushenqi.apk";

    private ProgressBar pb1, pb2, pb3, pb4;
    private Button btn1, btn2, btn3, btn4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiple_download);
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DownloadDispatcher.getInstance().stopDownload(mApkUrl1);
        DownloadDispatcher.getInstance().stopDownload(mApkUrl2);
        DownloadDispatcher.getInstance().stopDownload(mApkUrl3);
        DownloadDispatcher.getInstance().stopDownload(mApkUrl4);
    }

    private void initView() {
        pb1 = (ProgressBar) findViewById(R.id.pb1);
        pb2 = (ProgressBar) findViewById(R.id.pb2);
        pb3 = (ProgressBar) findViewById(R.id.pb3);
        pb4 = (ProgressBar) findViewById(R.id.pb4);
        btn1 = (Button) findViewById(R.id.btn1);
        btn2 = (Button) findViewById(R.id.btn2);
        btn3 = (Button) findViewById(R.id.btn3);
        btn4 = (Button) findViewById(R.id.btn4);
    }

    public void down1(View view) {
        commonDown(btn1, pb1, mApkUrl1);
    }

    public void down2(View view) {
        commonDown(btn2, pb2, mApkUrl2);
    }

    public void down3(View view) {
        commonDown(btn3, pb3, mApkUrl3);
    }

    public void down4(View view) {
        commonDown(btn4, pb4, mApkUrl4);
    }

    private void commonDown(Button btn, ProgressBar pb, String url) {
        if (btn.getText().toString().equals("下载中")) {
            btn.setText("暂停中");
            DownloadDispatcher.getInstance().stopDownload(url);
        } else if (btn.getText().toString().equals("暂停中")) {
            btn.setText("下载中");
            DownloadDispatcher.getInstance().startDownload(url, new MyDownCallback(pb, btn));
        } else if (btn.getText().toString().equals("开始")) {
            btn.setText("等待中");
            DownloadDispatcher.getInstance().startDownload(url, new MyDownCallback(pb, btn));
        }
    }

    private static class MyDownCallback implements DownloadCallback {
        private WeakReference<ProgressBar> weakProgressBar;
        private WeakReference<Button> weakButton;

        public MyDownCallback(ProgressBar progressBar, Button btn) {
            this.weakProgressBar = new WeakReference<>(progressBar);
            this.weakButton = new WeakReference<>(btn);
        }

        @Override
        public void onState(int state) {
            Button btn = weakButton.get();
            if (state == STATE_PREPARE) {
                btn.setText("等待中");
            } else if (state == STATE_DOWNLOADING) {
                btn.setText("下载中");
            } else if (state == STATE_SUCCESS) {
                btn.setText("已成功");
            }
        }

        @Override
        public void onFailure(IOException e) {
            Button btn = weakButton.get();
            btn.setText("失败");
        }

        @Override
        public void onProgress(long currentLength, long totalLength) {
            ProgressBar pb = weakProgressBar.get();
            int percent = (int) (100 * currentLength / totalLength);
            pb.setProgress(percent);
        }

        @Override
        public void onSucceed(File file) {
            Button btn = weakButton.get();
            btn.setText("成功");
        }
    }

}
