package com.lfl.watchtest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class MyLocationListener implements LocationListener {
    private Context mContext;
    private MainActivity mainActivity;
    MyLocationListener(Context context,MainActivity mainActivity){
        this.mContext = context;
        this.mainActivity = mainActivity;
    }
    @SuppressLint("SetTextI18n")
    @Override
    public void onLocationChanged(Location location) {
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        double altitude = location.getAltitude();
        mainActivity.gpsTestCount ++;
        mainActivity.tv.setText(mContext.getString(R.string.tv)
                + mContext.getString(R.string.gps) + " : " + mainActivity.gpsTestCount);
        Log.i(MainActivity.TAG, "longitude : " + longitude);
        Log.i(MainActivity.TAG, "latitude : " + latitude);
        Log.i(MainActivity.TAG, "altitude : " + altitude);
        Toast toast = Toast.makeText(mContext, longitude + " , " + latitude + " , "
                + altitude, Toast.LENGTH_SHORT);
        if (mainActivity.isGpgTest){
            toast.show();
        }
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
}
