package io.treehouses.remote.bluetoothv2.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.polidea.rxandroidble2.RxBleDevice
import com.polidea.rxandroidble2.exceptions.BleScanException
import com.polidea.rxandroidble2.scan.ScanResult
import io.reactivex.disposables.Disposable
import io.treehouses.remote.MainApplication
import io.treehouses.remote.R
import io.treehouses.remote.bluetoothv2.ScanResultsAdapter
import io.treehouses.remote.bluetoothv2.services.BluetoothConnectionService
import io.treehouses.remote.bluetoothv2.ui.home.view.HomeFragment
import io.treehouses.remote.bluetoothv2.util.*
import kotlinx.android.synthetic.main.activity_remote.*


class RemoteActivity : AppCompatActivity(), (ScanResult) -> Unit {
    private var resultsAdapter =
            ScanResultsAdapter(this)

    private var hasClickedScan = false
    private var scanService = BluetoothConnectionService()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remote)
        supportFragmentManager.beginTransaction().replace(R.id.content, HomeFragment())
//        scanService.ScanDevices(object : BluetoothConnectionService.ScanBluetoothCallback {
//            override fun onDeviceFound(device: ScanResult) {
//                resultsAdapter.addScanResult(device)
//            }
//
//            override fun onScanFailure(message: String) {
//                debug(message)
//                showSnackbarShort(message, rv_devices)
//            }
//
//            override fun requestLocationPermission() {
//                (this@RemoteActivity).requestLocationPermission(MainApplication.rxBleClient)
//            }
//
//        })
//        rv_devices.apply {
//            layoutManager = LinearLayoutManager(this@RemoteActivity)
//            adapter = resultsAdapter
//        }
    }

    override fun invoke(p1: ScanResult) {
    }

//    private fun onScanFailure(throwable: Throwable) {
//        if (throwable is BleScanException) {
//            showError(throwable, btn_connect)
//        }
//    }
//
//
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
//        if (isLocationPermissionGranted(requestCode, grantResults) && hasClickedScan) {
//            hasClickedScan = false
//            //scan again
//        }
//    }
//
//
//    private var connectionDisposable: Disposable? = null
//    private var bleDevice: RxBleDevice? = null
//    override fun invoke(p1: ScanResult) {
//    }
//

//    private fun triggerDisconnect() = connectionDisposable?.dispose()
//
//    override fun onPause() {
//        super.onPause()
//        triggerDisconnect()
//        if (isScanning) scanDisposable?.dispose()
//        mtuDisposable.clear()
//    }
//
//
//    override fun onDestroy() {
//        super.onDestroy()
//    scanService.onPause()
//    }
}