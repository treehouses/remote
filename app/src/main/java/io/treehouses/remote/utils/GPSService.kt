package io.treehouses.remote.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Service
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat

/**
 * Created by rowsun on 9/28/16.
 */
class GPSService(private val mContext: Context) : Service(), LocationListener {
    protected var locationManager: LocationManager? = null
    var isGPSEnabled = false
    var canGetLocation = false
    var location // location
            : Location? = null
    var latitude // latitude = 0.0
    var longitude // longitude = 0.0
    var pref: SharedPreferences

    @SuppressLint("MissingPermission")
    private fun getLocation(): Location? {
        try {
            if (!isGPSEnabled) {
                //  showSettingsAlert();
            } else {
                canGetLocation = true
                if (location == null) {
                    lastKnownLocation
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return location
    }

    @get:SuppressLint("MissingPermission")
    private val lastKnownLocation: Unit
        private get() {
            locationManager!!.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, MIN_TIME_BW_UPDATES.toFloat(), this)
            if (locationManager != null) {
                location = locationManager!!
                        .getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (location != null) {
                    latitude = location!!.latitude
                    longitude = location!!.longitude
                    Log.d("LOCATION", "getLastKnownLocation: $latitude $longitude")
                    pref.edit().putString("last_lat", latitude.toString()).apply()
                    pref.edit().putString("last_lng", longitude.toString()).apply()
                }
            }
        }

    fun stopUsingGPS() {
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            locationManager!!.removeUpdates(this@GPSService)
        }
    }

    fun getLatitude(): Double {
        if (location != null) {
            latitude = location!!.latitude
        }
        return latitude
    }

    fun getLongitude(): Double {
        if (location != null) {
            longitude = location!!.longitude
        }
        return longitude
    }

    fun canGetLocation(): Boolean {
        return canGetLocation
    }

    fun showSettingsAlert() {
        val alertDialog = AlertDialog.Builder(mContext)
        alertDialog.setTitle("GPS is settings")
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?")
        alertDialog.setPositiveButton("Settings") { dialog: DialogInterface?, which: Int ->
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            mContext.startActivity(intent)
        }
        alertDialog.setNegativeButton("Cancel") { dialog: DialogInterface, which: Int -> dialog.cancel() }
        alertDialog.show()
    }

    override fun onLocationChanged(location: Location) {
        if (location != null) {
            this.location = location
            Log.d("", "onLocationChanged: " + location.longitude + " " + location.latitude)
            pref.edit().putString("last_lat", location.latitude.toString()).apply()
            pref.edit().putString("last_lng", location.longitude.toString()).apply()
        }
    }

    override fun onProviderDisabled(provider: String) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
    override fun onBind(arg0: Intent): IBinder {
        return null
    }

    companion object {
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 10 // 10 meters
        private const val MIN_TIME_BW_UPDATES = 1000 * 60 * 5 // 1 minute
                .toLong()
    }

    init {
        pref = PreferenceManager.getDefaultSharedPreferences(mContext)
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER)
            getLocation()
        }
    }
}