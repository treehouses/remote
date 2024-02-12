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
    private fun areLocationPermissionsGranted(): Boolean {
        return checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) && checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    private fun checkPermission(strPermission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, strPermission) == PackageManager.PERMISSION_GRANTED
    }

    private fun statusCheck() {
        if (areLocationPermissionsGranted()) {
            val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                buildAlertMessageNoGps()
            }
        } else {
            showProminentDisclosure()
        }
    }

    private fun showProminentDisclosure() {
        AlertDialog.Builder(this)
            .setTitle("Permission & GPS Usage")
            .setMessage("This app collects location data in the background to estimate the radius " +
                    "from the nearest town, determining community users' general locations. " +
                    "This helps in targeting support and organizing events by understanding user " +
                    "distribution. To continue, you must enable GPS.")
            .setPositiveButton("Accept") { _, _ -> requestAllPermissions() }
            .setNegativeButton("Deny") { dialog, _ -> dialog.cancel() }
            .show()
    }

    private fun requestAllPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.POST_NOTIFICATIONS
        ), PERMISSION_REQUEST_WIFI)
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
        if (!areLocationPermissionsGranted()) {
            showProminentDisclosure()
        } else {
            statusCheck()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_WIFI) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                statusCheck()
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_WIFI = 111
    }
}
