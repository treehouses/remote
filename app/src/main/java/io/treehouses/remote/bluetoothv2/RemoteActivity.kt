package io.treehouses.remote.bluetoothv2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.polidea.rxandroidble2.exceptions.BleScanException
import io.treehouses.remote.bluetoothv2.util.showSnackbarShort
import com.polidea.rxandroidble2.scan.ScanFilter
import com.polidea.rxandroidble2.scan.ScanResult
import com.polidea.rxandroidble2.scan.ScanSettings
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.treehouses.remote.MainApplication
import io.treehouses.remote.R
import io.treehouses.remote.bluetoothv2.util.isLocationPermissionGranted
import io.treehouses.remote.bluetoothv2.util.requestLocationPermission
import kotlinx.android.synthetic.main.activity_remote.*

class RemoteActivity : AppCompatActivity() {

    private val rxBleClient = MainApplication.rxBleClient

    private var scanDisposable: Disposable? = null

//    private val resultsAdapter =
//            ScanResultsAdapter { startActivity(DeviceActivity.newInstance(this, it.bleDevice.macAddress)) }

    private var hasClickedScan = false

    private val isScanning: Boolean
        get() = scanDisposable != null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remote)
        onScanToggleClick()
    }

    private fun onScanToggleClick() {
        if (isScanning) {
            scanDisposable?.dispose()
        } else {
            if (rxBleClient.isScanRuntimePermissionGranted) {
                scanBleDevices()
                        .observeOn(AndroidSchedulers.mainThread())
                        .doFinally { dispose() }
                        .subscribe({
                            showSnackbarShort(it.bleDevice.macAddress, content)
                        }, { onScanFailure(it) })
                        .let { scanDisposable = it }
            } else {
                hasClickedScan = true
                requestLocationPermission(rxBleClient)
            }
        }
    }

    private fun scanBleDevices(): Observable<ScanResult> {
        val scanSettings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .build()

        val scanFilter = ScanFilter.Builder()
//            .setDeviceAddress("B4:99:4C:34:DC:8B")
                // add custom filters if needed
                .build()

        return rxBleClient.scanBleDevices(scanSettings, scanFilter)
    }

    private fun dispose() {
        scanDisposable = null
//        resultsAdapter.clearScanResults()
    }

    private fun onScanFailure(throwable: Throwable) {
        if (throwable is BleScanException) {
            showSnackbarShort(throwable.reason.toString(), content)
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (isLocationPermissionGranted(requestCode, grantResults) && hasClickedScan) {
            hasClickedScan = false
            scanBleDevices()
        }
    }

    public override fun onPause() {
        super.onPause()
        if (isScanning) scanDisposable?.dispose()
    }
}