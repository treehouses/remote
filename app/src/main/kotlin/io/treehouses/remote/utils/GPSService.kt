package io.treehouses.remote.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import io.treehouses.remote.InitialActivity
import io.treehouses.remote.R

open class GPSService : Service(), LocationListener {
    private var locationManager: LocationManager? = null
    private var isGPSEnabled = false
    private lateinit var pref: SharedPreferences
    private lateinit var notificationManager: NotificationManager

    private val NOTIFICATION_ID = 101
    private val CHANNEL_ID = "ForegroundServiceChannel"

    override fun onCreate() {
        super.onCreate()
        pref = PreferenceManager.getDefaultSharedPreferences(this)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            createNotificationChannel()
            val notificationIntent = Intent(this, InitialActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
            val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this, CHANNEL_ID)
            val notification: Notification = notificationBuilder.setOngoing(true)
                .setContentTitle("GPS Service")
                .setContentText("Treehouses Remote is currently estimating your location")
                .setSmallIcon(R.drawable.treehouses2)
                .setContentIntent(pendingIntent)
                .build()

            startForeground(NOTIFICATION_ID, notification)

            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            isGPSEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
            getLocation()
        } else {
            stopSelf()
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_DISTANCE_CHANGE_FOR_UPDATES, MIN_TIME_BW_UPDATES.toFloat(), this)
        if (locationManager != null) {
            val location = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (location != null) {
                onLocationChanged(location)
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude
        Log.d("GPSService", "Location changed: Lat: $latitude, Lng: $longitude")
        pref.edit().putString("last_lat", "$latitude").apply()
        pref.edit().putString("last_lng", "$longitude").apply()
    }

    override fun onProviderDisabled(provider: String) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "Foreground Service Channel", NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 10 // 10 meters
        private const val MIN_TIME_BW_UPDATES = 1000 * 60 * 5.toLong() // 5 minutes
    }
}
