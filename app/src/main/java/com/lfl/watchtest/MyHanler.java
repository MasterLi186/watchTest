package com.lfl.watchtest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;

public class MyHanler extends Handler {
    private WeakReference<MainActivity> weakReference;
    private Context mContext;
    MyHanler(MainActivity mainActivity,Context context){
        weakReference = new WeakReference<>(mainActivity);
        mContext = context;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
        final MainActivity mainActivity = weakReference.get();
        switch (msg.what){
            case 1:
                if (mainActivity != null){
                    Log.i(MainActivity.TAG, "WifiEnabled: " + mainActivity.wifiManager.isWifiEnabled());
                    Toast.makeText(mainActivity,"开始下载",Toast.LENGTH_SHORT).show();
                    mainActivity.wifi.setClickable(false);
                    setAllEnabled(mainActivity, false);
                    new Thread(){
                        @Override
                        public void run() {
                            super.run();
                            Looper.prepare();
                            new DownLoadThread(mainActivity,mContext).run();
                            Looper.loop();
                        }
                    }.start();
                }else {
                    Toast.makeText(mContext,"mainActivity = null",Toast.LENGTH_SHORT).show();
                }
                break;
            case 2:
                if (mainActivity != null){
                    mainActivity.wifi.setClickable(true);
                    mainActivity.wifi.setEnabled(true);

                    if (mainActivity.file != null && mainActivity.file.length() > 0){
                        if (mainActivity.file.delete()){
                            Toast.makeText(mContext,"删除成功",Toast.LENGTH_SHORT).show();
                            if (!mainActivity.cancelTest){
                                sendEmptyMessageDelayed(1,mainActivity.wifiTestInterval * 1000);
                            }
                        }
                    }
                }else {
                    Toast.makeText(mContext,"mainActivity = null",Toast.LENGTH_SHORT).show();
                }
                break;
            case 3:
                //停止gps,经过gpsTestInterval时间再次启动gps
                Log.i(MainActivity.TAG, "handleMessage: 停止gps");
                if (mainActivity != null){
                    if (mainActivity.isGpgTest){
                        cancelGpsListener(mainActivity);
                        mainActivity.gpsTestCount = 0;
                        mainActivity.tv.setText(mContext.getString(R.string.tv)
                                + mContext.getString(R.string.gps) + " : " + mainActivity.gpsTestCount);
                    }else {
                        Log.i(MainActivity.TAG, "handleMessage: mainActivity.isGpgTest == false");
                    }
                    sendEmptyMessageDelayed(4,mainActivity.gpsTestInterval * 1000);
                }
                break;
            case 4:
                if (mainActivity != null ){
                    mainActivity.isGpgTest = true;
                    mainActivity.startGpsTest();
                }
                break;
            case 6:
                if (mainActivity != null){
                    setAllEnabled(mainActivity, true);
                    if (mainActivity.file != null && mainActivity.file.exists()){
                        boolean delete = mainActivity.file.delete();
                        Toast.makeText(mContext,"删除:" + delete,Toast.LENGTH_SHORT).show();
                    }
                }
                removeCallbacksAndMessages(null);
                break;
            default:
                break;
        }
    }

    private void cancelGpsListener(MainActivity mainActivity) {
        if (mainActivity.locationManager != null){
            mainActivity.locationManager.removeUpdates(mainActivity.locationListener);
            mainActivity.locationManager.removeNmeaListener(mainActivity.messageListener);
            mainActivity.locationManager.unregisterGnssStatusCallback(mainActivity.callback);
        }else {
            Log.i(MainActivity.TAG, "handleMessage: mainActivity.locationManager == null");
        }
        mainActivity.locationManager = null;
        mainActivity.locationListener = null;
    }

    private void setAllEnabled(MainActivity mainActivity, boolean b) {
        mainActivity.wifi.setEnabled(b);
        mainActivity.gps.setEnabled(b);
        mainActivity.modem.setEnabled(b);
        mainActivity.wifi_spinner.setEnabled(b);
        mainActivity.gps_spinner.setEnabled(b);
        mainActivity.modem_spinner.setEnabled(b);
    }


}
