package com.dengzi.dzokhttp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.dengzi.dzokhttp.okhttp3.Call;
import com.dengzi.dzokhttp.okhttp3.Callback;
import com.dengzi.dzokhttp.okhttp3.MultipartRequestBody;
import com.dengzi.dzokhttp.okhttp3.OkHttpClient;
import com.dengzi.dzokhttp.okhttp3.Request;
import com.dengzi.dzokhttp.okhttp3.RequestBody;
import com.dengzi.dzokhttp.okhttp3.Response;

import java.io.IOException;

/**
 * @Title: 自己实现的一个okhttp demo
 * @Author: djk
 * @Time: 2017/11/27
 * @Version:1.0.0
 */
public class CustomActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom);
    }

    public void okhttpClick(View view) {
//        String imagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "dengzi.png";
//        File file = new File(imagePath);
//        Log.e("TAG", file.exists() + " ");
//
//        String url = "https://api.saiwuquan.com/api/upload";
//        RequestBody requestBody = new RequestBody()
//                .setType(RequestBody.FORM)
//                .addFormDataPart("file1", RequestBody.create(file))
//                .addFormDataPart("file2", RequestBody.create(file))
//                .addFormDataPart("platform", "android");


        String url = "https://gank.io/api/add2gank";
        RequestBody requestBody = new MultipartRequestBody()
                .addFormDataPart("pageNo", "1")
                .addFormDataPart("pageSize", "50")
                .addFormDataPart("platform", "android");
        final Request request = new Request.Builder()
                .url(url)
                .tag(this)
                .post(requestBody)
                .build();

        OkHttpClient okHttpClient = new OkHttpClient();

        okHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, Exception e) {
                e.printStackTrace();
                Log.e("dengzi", "出错了");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.string();
                Log.e("dengzi", result);
            }
        });
    }

}
