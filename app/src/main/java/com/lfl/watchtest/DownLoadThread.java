package com.lfl.watchtest;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownLoadThread implements Runnable {
    private Context mContext;
    private MainActivity mainActivity;
    DownLoadThread(MainActivity mainActivity, Context context){
        mContext = context;
        this.mainActivity = mainActivity;
    }
    @Override
    public void run() {
        Toast.makeText(mContext, "开始下载", Toast.LENGTH_SHORT).show();
        mainActivity.file = getApkFile(mainActivity.downLoadUrl);
        if (mainActivity.file.exists() && !mainActivity.cancelTest) {
            Toast.makeText(mContext, "下载成功", Toast.LENGTH_SHORT).show();
        }
        if (mainActivity.cancelTest){
            mainActivity.myHanler.sendEmptyMessage(2);
        }
    }

    private File getApkFile(String url) {
        try {
            URL u = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) u.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            int contentLength = conn.getContentLength();
            Log.i(MainActivity.TAG, "getContentLength: " + contentLength);
            if (conn.getResponseCode() == 200) {
                InputStream is = conn.getInputStream();
                FileOutputStream os = new FileOutputStream(mainActivity.file);
                byte[] buffer = new byte[4096];
                int len;
                long totalReaded = 0;
                while ((len = is.read(buffer)) != -1) {
                    if (mainActivity.cancelTest){
                        Log.i(MainActivity.TAG, "interrupted: ");
                        break;
                    }
                    totalReaded += len;
                    long progress = totalReaded * 100 / contentLength;
                    int i = Long.valueOf(progress).intValue();
                    mainActivity.seekBar.setProgress(i);
                    os.write(buffer, 0, len);
                }
                os.flush();
                os.close();
                is.close();
                mainActivity.myHanler.sendEmptyMessage(2);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mainActivity.file;
    }
}
