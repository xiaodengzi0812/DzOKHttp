package com.dengzi.dzokhttp.progress_upload;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.dengzi.dzokhttp.R;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @Title: 上传进度demo
 * @Author: djk
 * @Time: 2017/11/27
 * @Version:1.0.0
 */
public class ProgressUploadActivity extends AppCompatActivity {
    String url = "https://api.saiwuquan.com/api/upload";
    private MyHandler mHandler = new MyHandler(this);
    private ProgressDialog progressDialog;

    private static class MyHandler extends Handler {
        private WeakReference<ProgressUploadActivity> activityReference;

        MyHandler(ProgressUploadActivity activity) {
            this.activityReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (activityReference.get() == null) return;
            switch (msg.what) {
                case 1:
                    Toast.makeText(activityReference.get(), "上传成功！", Toast.LENGTH_SHORT).show();
                    break;
                case -1:
                    Toast.makeText(activityReference.get(), "上传失败！", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom);
        initProgress();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    private void initProgress() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage("上传中...");
        progressDialog.setMax(100);
    }

    public void okhttpClick(View view) {
        progressDialog.show();
        String imagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "resource.apk";
        File file = new File(imagePath);

        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file1", file.getName(), RequestBody.create(MediaType.parse(guessMimeType(file.getAbsolutePath())), file))
                .addFormDataPart("platform", "android").build();

        ProgressUpLoadRequestBody progressUpLoadRequestBody = new ProgressUpLoadRequestBody(requestBody,
                new ProgressUpLoadListener() {
                    @Override
                    public void onProgress(long totalLength, long currentLength) {
                        Log.e("dengzi", currentLength + " / " + totalLength);
                        int progress = (int) (currentLength * 100 / totalLength);
                        progressDialog.setProgress(progress);
                    }
                });

        Request request = new Request.Builder()
                .url(url)
                .tag(this)
                .post(progressUpLoadRequestBody)
                .build();

        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS);
        OkHttpClient okHttpClient = okHttpClientBuilder.build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                progressDialog.dismiss();
                mHandler.sendEmptyMessage(-1);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ResponseBody body = response.body();
                String responseStr = body == null ? "" : body.string();
                Log.e("dengzi", "responseStr->" + responseStr);
                progressDialog.dismiss();
                mHandler.sendEmptyMessage(1);
            }
        });
    }

    private String guessMimeType(String filePath) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();

        String mimType = fileNameMap.getContentTypeFor(filePath);

        if (TextUtils.isEmpty(mimType)) {
            return "application/octet-stream";
        }
        return mimType;
    }

}
