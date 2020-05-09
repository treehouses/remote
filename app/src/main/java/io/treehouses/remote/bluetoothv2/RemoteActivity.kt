package io.treehouses.remote.bluetoothv2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice
import com.polidea.rxandroidble2.exceptions.BleScanException
import com.polidea.rxandroidble2.scan.ScanFilter
import com.polidea.rxandroidble2.scan.ScanResult
import com.polidea.rxandroidble2.scan.ScanSettings
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.treehouses.remote.MainApplication
import io.treehouses.remote.R
import io.treehouses.remote.bluetoothv2.util.*
import kotlinx.android.synthetic.main.activity_remote.*
import java.util.*

class RemoteActivity : AppCompatActivity(), (ScanResult) -> Unit {

    private val rxBleClient = MainApplication.rxBleClient

    private var stateDisposable: Disposable? = null

    private val mtuDisposable = CompositeDisposable()
    private var scanDisposable: Disposable? = null

    private var resultsAdapter =
            ScanResultsAdapter(this)

    private var hasClickedScan = false

    private val isScanning: Boolean
        get() = scanDisposable != null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remote)
        rv_devices.apply {
            layoutManager = LinearLayoutManager(this@RemoteActivity)
            adapter = resultsAdapter
        }
        btn_connect.setOnClickListener {
            onScanToggleClick()
        }
        var uuid =   UUID.fromString("")
        var s = "test"

        send_message.setOnClickListener {
            bleDevice!!.establishConnection(false)

                    .flatMap({ rxBleConnection -> rxBleConnection.writeCharacteristic(uuid, s.toByteArray(Charsets.UTF_8) ) })
                    .subscribe({ characteristicValue -> })

        }
    }

    private fun performLongWrite(
            connection: RxBleConnection,
            notifications: Pair<Observable<ByteArray>, Observable<ByteArray>>
    ): Observable<ByteArray> {
        val (deviceCallback0, deviceCallback1) = notifications

        return connection.createNewLongWriteBuilder() // create a new long write builder
                .setBytes(bytesToWrite) // REQUIRED - set the bytes to write
                .setCharacteristicUuid(UUID.randomUUID()) // set the UUID of the characteristic to write
                .setWriteOperationAckStrategy { bufferNonEmpty ->
                    Observables.zip(
                            deviceCallback0, // DEVICE_CALLBACK_0
                            deviceCallback1, // DEVICE_CALLBACK_1
                            bufferNonEmpty
                    ) { _, _, nonEmpty -> nonEmpty }
                }
                .build()
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
                            resultsAdapter.addScanResult(it)
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
                .build()

        return rxBleClient.scanBleDevices(scanSettings, scanFilter)
    }

    private fun dispose() {
        scanDisposable = null
//        resultsAdapter.clearScanResults()
    }

    private fun onScanFailure(throwable: Throwable) {
        if (throwable is BleScanException) {
            showError(throwable, btn_connect)
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (isLocationPermissionGranted(requestCode, grantResults) && hasClickedScan) {
            hasClickedScan = false
            scanBleDevices()
        }
    }


    override fun invoke(result: ScanResult) {
        debug("Invoke result")
        connectDevice(result.bleDevice)
    }

    private var connectionDisposable: Disposable? = null
    private var bleDevice: RxBleDevice? = null


    private fun connectDevice(bleDevice: RxBleDevice) {
        this.bleDevice = bleDevice

        if (bleDevice.isConnected) {
            showSnackbarShort("Disconnected ", btn_connect)
            triggerDisconnect()
        } else {
            bleDevice.establishConnection(true)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doFinally { dispose() }
                    .subscribe({
                        debug(it.readRssi().toString())
                        showSnackbarShort("Connection received", btn_connect)
                        debug("""Connected ${bleDevice.macAddress}""")
                    }, {
                        it.message?.let { it1 -> showSnackbarShort(it1, btn_connect) }
                    })
                    .let {
                        connectionDisposable = it
                    }
        }
    }

    private fun triggerDisconnect() = connectionDisposable?.dispose()

    override fun onPause() {
        super.onPause()
        triggerDisconnect()
        if (isScanning) scanDisposable?.dispose()
        mtuDisposable.clear()
    }


    override fun onDestroy() {
        super.onDestroy()
        stateDisposable?.dispose()
    }
}