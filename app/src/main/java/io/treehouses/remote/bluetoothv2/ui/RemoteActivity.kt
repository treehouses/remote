package io.treehouses.remote.bluetoothv2.ui

import android.os.Bundle
import com.polidea.rxandroidble2.scan.ScanResult
import io.treehouses.remote.MainApplication
import io.treehouses.remote.R
import io.treehouses.remote.bluetoothv2.base.view.BaseActivity
import io.treehouses.remote.bluetoothv2.services.BluetoothConnectionService
import io.treehouses.remote.bluetoothv2.ui.home.view.HomeFragment
import io.treehouses.remote.bluetoothv2.util.requestLocationPermission
import java.util.*


class RemoteActivity : BaseActivity(), BluetoothConnectionService.ScanBluetoothCallback {
//  var service =   BluetoothConnectionService()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remote)
        supportFragmentManager.beginTransaction().replace(R.id.content, HomeFragment()).commit()
        requestLocationPermission()
//        service.scanDevices(this)
    }

    override fun onFragmentAttached() {
    }

    override fun onFragmentDetached(tag: String) {
    }

    override fun onDeviceFound(device: ScanResult) {
//        startActivity(ReadWriteActivity.newInstance(this, device.bleDevice.macAddress, UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")))

    }

    override fun onPause() {
        super.onPause()
//        service.onPause()
    }

    override fun onScanFailure(message: String) {
    }

    override fun requestLocationPermission() {
        requestLocationPermission(MainApplication.rxBleClient)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        service.scanDevices(this)
    }
}