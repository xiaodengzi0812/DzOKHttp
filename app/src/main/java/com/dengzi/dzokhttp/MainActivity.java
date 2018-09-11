package com.dengzi.dzokhttp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.dengzi.dzokhttp.cache.CacheActivity;
import com.dengzi.dzokhttp.download.DownloadActivity;
import com.dengzi.dzokhttp.progress_download.ProgressDownloadActivity;
import com.dengzi.dzokhttp.progress_upload.ProgressUploadActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void uploadProgressClick(View view) {
        startActivity(new Intent(MainActivity.this, ProgressUploadActivity.class));
    }

    public void downloadProgressClick(View view) {
        startActivity(new Intent(MainActivity.this, ProgressDownloadActivity.class));
    }

    public void cacheClick(View view) {
        startActivity(new Intent(MainActivity.this, CacheActivity.class));
    }

    public void downloadClick(View view) {
        startActivity(new Intent(MainActivity.this, DownloadActivity.class));
    }

    public void dcustomClick(View view) {
        startActivity(new Intent(MainActivity.this, CustomActivity.class));
    }

}
