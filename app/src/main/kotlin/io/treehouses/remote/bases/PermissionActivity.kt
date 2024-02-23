package io.treehouses.remote.bases

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import android.view.ContextThemeWrapper
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.treehouses.remote.R

abstract class PermissionActivity : AppCompatActivity() {
    private fun checkPermission(strPermission: String): Boolean {
        val result = ContextCompat.checkSelfPermission(this, strPermission)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun statusCheck() {
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
        }
    }

    private fun buildAlertMessageNoGps() {
        val builder = AlertDialog.Builder(ContextThemeWrapper(this, R.style.CustomAlertDialogStyle))
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
            .setNegativeButton("No") { dialog, _ -> dialog.cancel() }
        val alert = builder.create()
        alert.window?.setBackgroundDrawableResource(android.R.color.transparent)
        alert.show()
    }

    fun requestPermission() {
        showLocationPermissionDisclosure()
    }

    private fun showLocationPermissionDisclosure() {
        val builder = AlertDialog.Builder(ContextThemeWrapper(this, R.style.CustomAlertDialogStyle))
        builder.setTitle("Location & GPS Usage")
            .setMessage("This app needs to collect location data in the background to estimate the radius " +
                    "from the nearest town, determining community users' general locations. " +
                    "This helps in targeting support and organizing events by understanding user " +
                    "distribution. To continue, you must enable Location.")
            .setPositiveButton("Yes") { _, _ -> proceedWithLocationPermission() }
            .setNegativeButton("No") { _, _ -> proceedWithoutLocationPermission() }
            .show()
    }

    private fun proceedWithLocationPermission() {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
        )

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU && !checkPermission(Manifest.permission.POST_NOTIFICATIONS)) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (permissionsToRequest.any { !checkPermission(it) }) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), PERMISSION_REQUEST_WIFI)
        } else {
            statusCheck()
        }
    }

    private fun proceedWithoutLocationPermission() {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
        )

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU && !checkPermission(Manifest.permission.POST_NOTIFICATIONS)) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (permissionsToRequest.any { !checkPermission(it) }) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), PERMISSION_REQUEST_WIFI)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_WIFI) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    statusCheck()
                }
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_WIFI = 111
    }
}