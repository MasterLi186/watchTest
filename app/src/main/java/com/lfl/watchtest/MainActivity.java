package com.lfl.watchtest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.OnNmeaMessageListener;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {
    private static final String TAG = "MainActivity";
    private Spinner wifi_spinner;
    private Spinner gps_spinner;
    private Spinner modem_spinner;
    private ArrayAdapter<String> gpsAdapter;
    private ArrayAdapter<String> wifiAdapter;
    private ArrayAdapter<String> modemAdapter;
    private int gpsTestInterval = 0;
    private int wifiTestInterval = 0;
    private int modemTestInterval = 0;
    private LocationManager locationManager;
    private SeekBar seekBar;
    final String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + new Date() + ".apk";
    private Button wifi;
    private String downLoadUrl = "https://d1.music.126.net/dmusic/CloudMusic_official_5.4.1.284637.apk";
    private File apkFile;
    private File file = new File(Environment.getExternalStorageDirectory(),
            "test.apk");
    private int cancelTime = 60 * 1000 * 60 ;
//    private Handler handler = new Handler(){
//
//        @Override
//        public void handleMessage(@NonNull Message msg) {
//            super.handleMessage(msg);
//            switch (msg.what){
//                case 1:
//                    Log.i(TAG, "WifiEnabled: " + wifiManager.isWifiEnabled());
//                    Toast.makeText(mContext,"开始下载",Toast.LENGTH_SHORT).show();
//                    wifi.setClickable(false);
//                    wifi.setEnabled(false);
//                    new downLoadThread().start();
//                    break;
//                case 2:
//                    wifi.setClickable(true);
//                    wifi.setEnabled(true);
//                    if (file != null && file.length() > 0){
//                        if (file.delete()){
//                            Toast.makeText(MainActivity.this,"删除成功",Toast.LENGTH_SHORT).show();
//                            handler.sendEmptyMessageDelayed(1,wifiTestInterval * 1000);
//                        }
//                    }
//                    break;
//            }
//        }
//    };
    private Context mContext;
    private WifiManager wifiManager;
    private MyHanler myHanler;
    private Button gps;
    private Button modem;
    private Button cancel;
    private TextView tv;
    private boolean cancelTest = false;
    private MainActivity.downLoadThread downLoadThread;
    private boolean isGpgTest;

    private class MyHanler extends Handler{
        WeakReference<MainActivity> weakReference;
        MyHanler(MainActivity mainActivity){
            weakReference = new WeakReference<>(mainActivity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            MainActivity mainActivity = weakReference.get();
            switch (msg.what){
                case 1:
                    if (mainActivity != null){
                        Log.i(TAG, "WifiEnabled: " + mainActivity.wifiManager.isWifiEnabled());
                        Toast.makeText(mainActivity,"开始下载",Toast.LENGTH_SHORT).show();
                        mainActivity.wifi.setClickable(false);
                        mainActivity.wifi.setEnabled(false);
                        mainActivity.gps.setEnabled(false);
                        mainActivity.modem.setEnabled(false);
                        new Thread(){
                            @Override
                            public void run() {
                                super.run();
                                Looper.prepare();
                                downLoadThread.run();
                                Looper.loop();
                            }
                        }.start();
                    }else {
                        Toast.makeText(mainActivity,"mainActivity = null",Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 2:
                    if (mainActivity != null){
                        wifi.setClickable(true);
                        wifi.setEnabled(true);
                        if (file != null && file.length() > 0){
                            if (file.delete()){
                                Toast.makeText(MainActivity.this,"删除成功",Toast.LENGTH_SHORT).show();
                                if (!cancelTest){
                                    sendEmptyMessageDelayed(1,wifiTestInterval * 1000);
                                }
                            }
                        }
                    }else {
                        Toast.makeText(mainActivity,"mainActivity = null",Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 6:
                    if (mainActivity != null){
                        mainActivity.wifi.setEnabled(true);
                        mainActivity.gps.setEnabled(true);
                        mainActivity.modem.setEnabled(true);
                        if (apkFile != null && apkFile.exists()){
                            boolean delete = apkFile.delete();
                            Toast.makeText(MainActivity.this,"删除成功",Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        mContext = this;
        myHanler = new MyHanler(this);
        downLoadThread = new downLoadThread();
    }

    private void startGpsTest() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Log.i(TAG, "checkSelfPermission: ");
                return;
            }
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                // 注册监听器，当地理位置变化时，发出通知给Listener。这个方法很关键。4个参数需要了解清楚：
                // 第1个参数：你所使用的provider名称，是个String
                // 第2个参数minTime：地理位置更新时发出通知的最小时间间隔
                // 第3个参数minDistance：地理位置更新发出通知的最小距离，第2和第3个参数的作用关系是“或”的关系，也就是满足任意一个条件都会发出通知。这里第2、3个参数都是0，意味着任何时间，只要位置有变化就会发出通知。
                // 第4个参数：你的监听器
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                locationManager.registerGnssStatusCallback(callback);
                locationManager.addNmeaListener(messageListener);
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                //locationManager.removeUpdates(locationListener);
                Log.i(TAG, "startGpsTest: ");
            } else {
                Log.i(TAG, "LocationManager.GPS_PROVIDER: false ");
            }
        }
    }
    private LocationListener locationListener = new LocationListener(){

        @Override
        public void onLocationChanged(Location location) {
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            double altitude = location.getAltitude();
            Log.i(TAG, "longitude : " + longitude);
            Log.i(TAG, "latitude : " + latitude);
            Log.i(TAG, "altitude : " + altitude);
            Toast.makeText(MainActivity.this,longitude + " , " + latitude + " , " + altitude,Toast.LENGTH_LONG).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private GnssStatus.Callback callback = new GnssStatus.Callback() {
        @Override
        public void onStarted() {
            super.onStarted();
        }

        @Override
        public void onStopped() {
            super.onStopped();
        }

        @Override
        public void onFirstFix(int ttffMillis) {
            super.onFirstFix(ttffMillis);
        }

        @Override
        public void onSatelliteStatusChanged(GnssStatus status) {
            super.onSatelliteStatusChanged(status);
        }
    };

    private OnNmeaMessageListener messageListener = new OnNmeaMessageListener() {
        @Override
        public void onNmeaMessage(String message, long timestamp) {

        }
    };
    private void initView() {
        tv = findViewById(R.id.tv);
        //4个按钮
        wifi = findViewById(R.id.wifi_button);
        gps = findViewById(R.id.gps_button);
        modem = findViewById(R.id.modem_button);
        cancel = findViewById(R.id.cancel);
        //初始化seekBar
        seekBar = findViewById(R.id.seekBar);
        seekBar.setMax(100);
        seekBar.setEnabled(false);
        //3个Spinner
        wifi_spinner = findViewById(R.id.wifi_spinner);
        gps_spinner = findViewById(R.id.gps_spinner);
        modem_spinner = findViewById(R.id.modem_spinner);
        //按钮点击事件
        wifi.setOnClickListener(MainActivity.this);
        gps.setOnClickListener(MainActivity.this);
        modem.setOnClickListener(MainActivity.this);
        cancel.setOnClickListener(MainActivity.this);
        //Spinner点击事件
        wifi_spinner.setOnItemSelectedListener(MainActivity.this);
        gps_spinner.setOnItemSelectedListener(MainActivity.this);
        modem_spinner.setOnItemSelectedListener(MainActivity.this);
    }

    private void initData() {
        gpsAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, getResources().getStringArray(R.array.gps));
        wifiAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, getResources().getStringArray(R.array.wifi));
        modemAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, getResources().getStringArray(R.array.modem));
        gps_spinner.setAdapter(gpsAdapter);
        wifi_spinner.setAdapter(wifiAdapter);
        modem_spinner.setAdapter(modemAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        checkPermission();
        if (apkFile != null){
            Log.i(TAG, "length: " + apkFile.length());
        }
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)== PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET)== PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE)== PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE)== PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE)== PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE)== PackageManager.PERMISSION_GRANTED

        ) {
            //拥有权限，做你想做的事情
            //ToDo
            Log.i(TAG, "有权限: ");
            //获取wifi状态
            wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                boolean wifiEnabled = wifiManager.isWifiEnabled();
//                if (!wifiEnabled){
//                    startActivity(new Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY));
//                }
            }
        }else{
            //没有开启权限，向系统申请权限
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE
            }, 1);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.wifi_button:
                cancelTest = false;
                Log.i(TAG, "wifi_button: ");
                tv.setText(getString(R.string.tv) + getString(R.string.wifi));
                myHanler.sendEmptyMessage(1);
                break;
            case R.id.gps_button:
                cancelTest = false;
                isGpgTest = true;
                tv.setText(getString(R.string.tv) + getString(R.string.gps));
                Log.i(TAG, "gps_button: ");
                startGpsTest();
                break;
            case R.id.modem_button:
                cancelTest = false;
                tv.setText(getString(R.string.tv) + getString(R.string.modem));
                Log.i(TAG, "modem_button: ");
                myHanler.sendEmptyMessage(1);
                break;
            case R.id.cancel:
                cancelTest = true;
                if (isGpgTest){
                    locationManager.removeUpdates(locationListener);
                    locationManager.removeNmeaListener(messageListener);
                    locationManager.unregisterGnssStatusCallback(callback);
                }
                isGpgTest = false;
                tv.setText("");
                seekBar.setProgress(0);
                myHanler.sendEmptyMessage(6);
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //选择列表项的操作
        switch (parent.getId()){
            case R.id.gps_spinner:
                String gpsItem = gpsAdapter.getItem(position);
                if (gpsItem != null) {
                    gpsTestInterval = Integer.parseInt(gpsItem.substring(2, gpsItem.indexOf("秒")));
                }
                Log.i(TAG, "gpsTestInterval: " + gpsTestInterval);
                break;
            case R.id.wifi_spinner:
                String wifiItem = wifiAdapter.getItem(position);
                if (wifiItem != null) {
                    wifiTestInterval = Integer.parseInt(wifiItem.substring(2, wifiItem.indexOf("秒")));
                    if (wifiTestInterval == 0){
                        wifiTestInterval = 1;
                    }
                }
                Log.i(TAG, "wifiTestInterval: " + wifiTestInterval);
                break;
            case R.id.modem_spinner:
                String modemItem = modemAdapter.getItem(position);
                if (modemItem != null) {
                    modemTestInterval = Integer.parseInt(modemItem.substring(2, modemItem.indexOf("秒")));
                    if (modemTestInterval == 0){
                        modemTestInterval = 1;
                    }
                }
                Log.i(TAG, "modemTestInterval: " + modemTestInterval);
                break;
            default:
                break;
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //未选中时候的操作
    }

    public File getApkFile(String url) {
        try {
            URL u = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) u.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            int contentLength = conn.getContentLength();
            Log.i(TAG, "getContentLength: " + contentLength);
            if (conn.getResponseCode() == 200) {
                InputStream is = conn.getInputStream();
                FileOutputStream os = new FileOutputStream(file);
                byte[] buffer = new byte[4096];
                int len;
                long totalReaded = 0;
                while ((len = is.read(buffer)) != -1) {
                    if (cancelTest){
                        Log.i(TAG, "interrupted: ");
                        break;
                    }
                    totalReaded += len;
                    long progress = totalReaded * 100 / contentLength;
                    int i = Long.valueOf(progress).intValue();
                    seekBar.setProgress(i);
                    os.write(buffer, 0, len);
                }
                os.flush();
                os.close();
                is.close();
                myHanler.sendEmptyMessage(2);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null){
            locationManager.unregisterGnssStatusCallback(callback);
            locationManager = null;
        }
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        myHanler.removeCallbacksAndMessages(null);
        finish();
        System.exit(0);
    }

     class downLoadThread implements Runnable{
        @Override
        public void run() {
//            Looper.prepare();
            Toast.makeText(MainActivity.this, "开始下载", Toast.LENGTH_SHORT).show();
            apkFile = getApkFile(downLoadUrl);
            if (apkFile.exists() && !cancelTest) {
                Toast.makeText(MainActivity.this, "下载成功", Toast.LENGTH_SHORT).show();
            }
            if (cancelTest){
                myHanler.sendEmptyMessage(2);
            }
//            Looper.loop();
        }
    }
}
