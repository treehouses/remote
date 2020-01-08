package io.treehouses.remote.utils;
/**
 * Created by rowsun on 9/28/16.
 */

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.ActivityCompat;

public class GPSService extends Service implements LocationListener {

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 5; // 1 minute
    private final Context mContext;
    protected LocationManager locationManager;
    boolean isGPSEnabled = false;
    boolean canGetLocation = false;
    Location location; // location
    double latitude; // latitude
    double longitude; // longitude
    SharedPreferences pref;


    public GPSService(Context context) {
        this.mContext = context;
        pref = PreferenceManager.getDefaultSharedPreferences(context);
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);
            getLocation();
        }
    }

    @SuppressLint("MissingPermission")
    private Location getLocation() {
        try {

            if (!isGPSEnabled) {
              //  showSettingsAlert();
            } else {
                this.canGetLocation = true;
                if (location == null) {
                    getLastKnownLocation();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    @SuppressLint("MissingPermission")
    private void getLastKnownLocation() {
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_DISTANCE_CHANGE_FOR_UPDATES,MIN_TIME_BW_UPDATES, this);
        if (locationManager != null) {
            location = locationManager
                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                Log.d("LOCATION", "getLastKnownLocation: " + latitude + " " + longitude);
                pref.edit().putString("last_lat", String.valueOf(latitude)).commit();
                pref.edit().putString("last_lng", String.valueOf(longitude)).commit();
            }
        }
    }

    public void stopUsingGPS() {
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.removeUpdates(GPSService.this);
        }
    }

    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }
        return latitude;
    }

    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }
        return longitude;
    }

    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle("GPS is settings");
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
        alertDialog.setPositiveButton("Settings", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            mContext.startActivity(intent);
        });
        alertDialog.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        alertDialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {

        if(location!=null) {
            this.location = location;
            Log.d("", "onLocationChanged: " + location.getLongitude() + " " + location.getLatitude());
            pref.edit().putString("last_lat", String.valueOf(location.getLatitude())).commit();
            pref.edit().putString("last_lng", String.valueOf(location.getLongitude())).commit();
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}

