package com.lfl.watchtest;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.OnNmeaMessageListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
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
                locationManager.removeUpdates(locationListener);
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
        //3个按钮
        wifi = findViewById(R.id.wifi_button);
        Button gps = findViewById(R.id.gps_button);
        Button modem = findViewById(R.id.modem_button);
        //初始化seekBar
        seekBar = findViewById(R.id.seekBar);
        seekBar.setMax(100);
        //3个Spinner
        wifi_spinner = findViewById(R.id.wifi_spinner);
        gps_spinner = findViewById(R.id.gps_spinner);
        modem_spinner = findViewById(R.id.modem_spinner);
        //按钮点击事件
        wifi.setOnClickListener(MainActivity.this);
        gps.setOnClickListener(MainActivity.this);
        modem.setOnClickListener(MainActivity.this);
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

        ) {
            //拥有权限，做你想做的事情
            //ToDo
            Log.i(TAG, "有权限: ");
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
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.wifi_button:
                Log.i(TAG, "wifi_button: ");
//                download(path);
                wifi.setClickable(false);
                wifi.setEnabled(false);
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        Looper.prepare();
                        apkFile = getApkFile(downLoadUrl);
                        if (apkFile.exists()){
                            Toast.makeText(MainActivity.this,"下载成功",Toast.LENGTH_SHORT).show();
                        }
                        Looper.loop();
                    }
                }.start();
                break;
            case R.id.gps_button:
                Log.i(TAG, "gps_button: ");
                startGpsTest();
                break;
            case R.id.modem_button:
                Log.i(TAG, "modem_button: ");
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
                }
                Log.i(TAG, "wifiTestInterval: " + wifiTestInterval);
                break;
            case R.id.modem_spinner:
                String modemItem = modemAdapter.getItem(position);
                if (modemItem != null) {
                    modemTestInterval = Integer.parseInt(modemItem.substring(2, modemItem.indexOf("秒")));
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

            if (conn.getResponseCode() == 200) {
                InputStream is = conn.getInputStream();
                FileOutputStream os = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
                os.flush();
                os.close();
                is.close();
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "runOnUiThread: ");
                        SystemClock.sleep(2000);
                        wifi.setClickable(true);
                        wifi.setEnabled(true);
                        if (file != null && file.length() > 0){
                            if (file.delete()){
                                Toast.makeText(MainActivity.this,"删除成功",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
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
        finish();
        System.exit(0);
    }
}
