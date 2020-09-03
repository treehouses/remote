package io.treehouses.remote.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import androidx.preference.PreferenceManager
import android.util.Log
import androidx.core.app.ActivityCompat

/**
 * Created by rowsun on 9/28/16.
 */
open class GPSService(mContext: Context) : Service(), LocationListener {
    private var locationManager: LocationManager? = null
    private var isGPSEnabled = false
    private var canGetLocation = false
    @JvmField
    var location // location
            : Location? = null
    @JvmField
    var latitude:Double = 0.0 // latitude = 0.0
    @JvmField
    var longitude:Double = 0.0 // longitude = 0.0
    private var pref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext)

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
        get() {
            locationManager!!.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, MIN_TIME_BW_UPDATES.toFloat(), this)
            if (locationManager != null) {
                location = locationManager!!
                        .getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (location != null) {
                    latitude = location!!.latitude
                    longitude = location!!.longitude
                    pref.edit().putString("last_lat", latitude.toString()).apply()
                    pref.edit().putString("last_lng", longitude.toString()).apply()
                }
            }
        }

    override fun onLocationChanged(location: Location) {
        this.location = location
        pref.edit().putString("last_lat", location.latitude.toString()).apply()
        pref.edit().putString("last_lng", location.longitude.toString()).apply()
    }

    override fun onProviderDisabled(provider: String) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
    override fun onBind(arg0: Intent): IBinder? {
        return null
    }

    companion object {
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 10 // 10 meters
        private const val MIN_TIME_BW_UPDATES = 1000 * 60 * 5 // 1 minute
                .toLong()
    }

    init {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            isGPSEnabled = locationManager!!
                    .isProviderEnabled(LocationManager.GPS_PROVIDER)
            getLocation()
        }
    }
}