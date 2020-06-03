package io.treehouses.remote.bluetoothv2.ui

import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.treehouses.remote.MainApplication
import io.treehouses.remote.R
import io.treehouses.remote.bluetoothv2.util.*
import io.treehouses.remote.utils.LogUtils.log
import kotlinx.android.synthetic.main.activity_read_write.*
import java.util.UUID

private const val EXTRA_MAC_ADDRESS = "extra_mac_address"

private const val EXTRA_CHARACTERISTIC_UUID = "extra_uuid"

class ReadWriteActivity : AppCompatActivity() {

    companion object {
        fun newInstance(context: Context, macAddress: String, uuid: UUID) =
                Intent(context, ReadWriteActivity::class.java).apply {
                    putExtra(EXTRA_MAC_ADDRESS, macAddress)
                    putExtra(EXTRA_CHARACTERISTIC_UUID, uuid)
                }
    }

    private lateinit var characteristicUuid: UUID

    private val disconnectTriggerSubject = PublishSubject.create<Unit>()

    private lateinit var connectionObservable: Observable<RxBleConnection>

    private val connectionDisposable = CompositeDisposable()

    private lateinit var bleDevice: RxBleDevice

    private val inputBytes: ByteArray
        get() = write_input.text.toString().toByteArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read_write)

        val macAddress = intent.getStringExtra(EXTRA_MAC_ADDRESS)
        characteristicUuid = intent.getSerializableExtra(EXTRA_CHARACTERISTIC_UUID) as UUID
        log(characteristicUuid.toString());
//        characteristicUuid =UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        bleDevice = MainApplication.rxBleClient.getBleDevice(macAddress)
        connectionObservable = prepareConnectionObservable()
//        supportActionBar!!.subtitle = "Mac address"

        connect.setOnClickListener { onConnectToggleClick() }
        read.setOnClickListener { onReadClick() }
        write.setOnClickListener { onWriteClick() }
        notify.setOnClickListener { onNotifyClick() }
    }

    private fun prepareConnectionObservable(): Observable<RxBleConnection> =
            bleDevice
                    .establishConnection(false)
                    .takeUntil(disconnectTriggerSubject)
                    .observeOn(Schedulers.io())
                    .subscribeOn(AndroidSchedulers.mainThread())

    private fun onConnectToggleClick() {
        if (bleDevice.isConnected) {
            triggerDisconnect()
        } else {
            connectionObservable
//                    .flatMapSingle { it.discoverServices() }
//                    .flatMapSingle { it.getCharacteristic(UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")) }
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe { connect.setText("Connecting") }
                    .subscribe(
                            { characteristic ->
                                updateUI(true)
                                Log.i(javaClass.simpleName, "Hey, connection has been established!")
                            },
                            { onConnectionFailure(it) },
                            { updateUI(false) }
                    )
                    .let { connectionDisposable.add(it) }
        }
    }

    private fun onReadClick() {
        if (bleDevice.isConnected) {
            connectionObservable
                    .firstOrError()
                    .flatMap { it.readCharacteristic(UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e")) }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ bytes ->
                        read_output.text = String(bytes)
                        read_hex_output.text = bytes?.toHex()
                        write_input.setText(bytes?.toHex())
                    }, { onReadFailure(it) })
                    .let { connectionDisposable.add(it) }
        }
    }

    private fun onWriteClick() {
        if (bleDevice.isConnected) {
            connectionObservable
                    .firstOrError()
                    .flatMap { it.writeCharacteristic(UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e"), inputBytes) }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ onWriteSuccess() }, { onWriteFailure(it) })
                    .let { connectionDisposable.add(it) }
        }
    }

    private fun onNotifyClick() {
        if (bleDevice.isConnected) {
            connectionObservable
                    .flatMap { it.setupNotification(characteristicUuid) }
                    .doOnNext { runOnUiThread { notificationHasBeenSetUp() } }
                    // we have to flatmap in order to get the actual notification observable
                    // out of the enclosing observable, which only performed notification setup
                    .flatMap { it }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ onNotificationReceived(it) }, { onNotificationSetupFailure(it) })
                    .let { connectionDisposable.add(it) }
        }
    }

    private fun onConnectionFailure(throwable: Throwable) {
        throwable.printStackTrace()
        showSnackbarShort("Connection error: $throwable", connect)
        updateUI(false)
    }

    private fun onReadFailure(throwable: Throwable) = showSnackbarShort("Read error: $throwable", connect)

    private fun onWriteSuccess() = showSnackbarShort("Write success", connect)

    private fun onWriteFailure(throwable: Throwable) = showSnackbarShort("Write error: $throwable", connect)

    private fun onNotificationReceived(bytes: ByteArray) = showSnackbarShort("Change: ${bytes.toHex()}", connect)

    private fun onNotificationSetupFailure(throwable: Throwable) =
            showSnackbarShort("Notifications error", connect)

    private fun notificationHasBeenSetUp() = showSnackbarShort("Notifications has been set up", connect)

    private fun triggerDisconnect() = disconnectTriggerSubject.onNext(Unit)


    private fun updateUI(boolean: Boolean) {
        if (!boolean) {
            connect.setText("Connect")
            read.isEnabled = false
            write.isEnabled = false
        }
        else {
            connect.setText("Diconnnect")
            read.isEnabled = true
            write.isEnabled = true
        }
//            with(characteristic) {
//                read.isEnabled = hasProperty(BluetoothGattCharacteristic.PROPERTY_READ)
//                write.isEnabled = hasProperty(BluetoothGattCharacteristic.PROPERTY_WRITE)
//                notify.isEnabled = hasProperty(BluetoothGattCharacteristic.PROPERTY_NOTIFY)
//            }
//        }
    }

    override fun onPause() {
        super.onPause()
        connectionDisposable.clear()
    }
}
