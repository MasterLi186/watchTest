package com.lfl.watchtest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.LocationManager;
import android.location.OnNmeaMessageListener;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {
    static final String TAG = "MainActivity";
    Spinner wifi_spinner;
    Spinner gps_spinner;
    Spinner modem_spinner;
    private ArrayAdapter<String> gpsAdapter;
    private ArrayAdapter<String> wifiAdapter;
    private ArrayAdapter<String> modemAdapter;
    int gpsTestInterval = 0;
    int wifiTestInterval = 0;
    int modemTestInterval = 0;
    protected LocationManager locationManager;
    SeekBar seekBar;
    String downLoadUrl = "https://down.qq.com/qqweb/QQ_1/android_apk/Android_8.3.9.4635_537064751.apk";
    File file = new File(Environment.getExternalStorageDirectory(),
            "test.apk");
    private int cancelTime = 60 * 1000 * 60 ;
    Context mContext;
    WifiManager wifiManager;
    MyHanler myHanler;
    Button wifi;
    Button gps;
    Button modem;
    Button cancel;
    TextView tv;
    boolean cancelTest = false;
    boolean isGpgTest;
    MyLocationListener locationListener;
    int useGps = 60 * 1000;
    int gpsTestCount = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        mContext = this;
        myHanler = new MyHanler(this,mContext);
    }
    protected void startGpsTest() {
        isGpgTest = true;
        Log.i(TAG, "gps_button: ");
        //设置不可点击
        wifi_spinner.setEnabled(false);
        gps_spinner.setEnabled(false);
        modem_spinner.setEnabled(false);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationListener == null){
            locationListener = new MyLocationListener(mContext,MainActivity.this);
        }
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
                wifi.setEnabled(false);
                gps.setEnabled(false);
                modem.setEnabled(false);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                locationManager.registerGnssStatusCallback(callback);
                locationManager.addNmeaListener(messageListener);
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                //工作usegps秒之后发消息停止gps
                myHanler.sendEmptyMessageDelayed(3,useGps);
                //locationManager.removeUpdates(locationListener);
                Log.i(TAG, "startGpsTest: ");
            } else {
                Log.i(TAG, "LocationManager.GPS_PROVIDER: false ");
            }
        }
    }


    GnssStatus.Callback callback = new GnssStatus.Callback() {
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

    OnNmeaMessageListener messageListener = new OnNmeaMessageListener() {
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
                if (!wifiEnabled){
                    //startActivity(new Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY));
                    Log.i(TAG, "!wifiEnabled");
                }
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
                tv.setText(getString(R.string.tv) + getString(R.string.gps));
                if (locationListener == null){
                    locationListener = new MyLocationListener(mContext,MainActivity.this);
                }
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
                cancelGpsTest();
                isGpgTest = false;
                tv.setText("");
                seekBar.setProgress(0);
                myHanler.sendEmptyMessage(6);
                break;
            default:
                break;
        }
    }

    private void cancelGpsTest() {
        try {
            if (isGpgTest){
                if (locationManager != null){
                    locationManager.removeUpdates(locationListener);
                    locationManager.removeNmeaListener(messageListener);
                    locationManager.unregisterGnssStatusCallback(callback);
                }else {
                    Log.i(TAG, "cancelGpsTest: locationManager == null");
                }
                locationManager = null;
                locationListener = null;
            }else {
                Log.i(TAG, "cancelGpsTest: isGpgTest == false");
            }
        } catch (Exception e) {
            e.printStackTrace();
            StackTraceElement[] stackTrace = e.getStackTrace();
            for (StackTraceElement stackTraceElement : stackTrace) {
                Log.e(TAG, "" + stackTraceElement);
            }
            Log.e(TAG, "cancelGpsTest Exception: " + e.getMessage());
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null){
            locationManager.removeUpdates(locationListener);
            locationManager.removeNmeaListener(messageListener);
            locationManager.unregisterGnssStatusCallback(callback);
            locationManager = null;
            locationListener = null;
        }
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        myHanler.removeCallbacksAndMessages(null);
        finish();
        System.exit(0);
    }

}
