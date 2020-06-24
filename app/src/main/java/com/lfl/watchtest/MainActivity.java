package com.lfl.watchtest;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private Button wifi;
    private Button gps;
    private Button modem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        wifi = findViewById(R.id.wifi_button);
        gps = findViewById(R.id.gps_button);
        modem = findViewById(R.id.modem_button);
        wifi.setOnClickListener(MainActivity.this);
        gps.setOnClickListener(MainActivity.this);
        modem.setOnClickListener(MainActivity.this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.wifi_button:
                Log.i(TAG, "wifi_button: ");
                break;
            case R.id.gps_button:
                Log.i(TAG, "gps_button: ");
                break;
            case R.id.modem_button:
                Log.i(TAG, "modem_button: ");
                break;
            default:
                break;
        }

    }
}
