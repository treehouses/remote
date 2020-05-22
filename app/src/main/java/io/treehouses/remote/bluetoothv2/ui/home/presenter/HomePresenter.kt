package io.treehouses.remote.bluetoothv2.ui.home.presenter

import android.util.Log
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice
import io.reactivex.Observable
import javax.inject.Inject
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import io.treehouses.remote.bluetoothv2.base.presenter.BasePresenter
import io.treehouses.remote.bluetoothv2.ui.home.interactor.HomeMVPInterator
import io.treehouses.remote.bluetoothv2.ui.home.view.HomeMVPView
import io.treehouses.remote.bluetoothv2.util.SchedulerProvider
import io.treehouses.remote.bluetoothv2.util.isConnected
import io.treehouses.remote.bluetoothv2.util.log
import io.treehouses.remote.utils.LogUtils

class HomePresenter<V : HomeMVPView, I : HomeMVPInterator> @Inject internal constructor(interator: I, schedulerProvider: SchedulerProvider, compositeDisposable: CompositeDisposable) : BasePresenter<V, I>(interactor = interator, schedulerProvider = schedulerProvider, compositeDisposable = compositeDisposable), HomeMVPPresenter<V, I> {
    private val disconnectTriggerSubject = PublishSubject.create<Unit>()
    private lateinit var connectionObservable: Observable<RxBleConnection>
    override fun onConnectClicked(bleDevice: RxBleDevice) {

//        connectionObservable
//                .flatMapSingle { it.discoverServices() }
//                .flatMapSingle { it.getCharacteristic(characteristicUuid) }
//                .observeOn(AndroidSchedulers.mainThread())
//                .doOnSubscribe { connect.setText(R.string.connecting) }
//                .subscribe(
//                        { characteristic ->
//                            updateUI(characteristic)
//                            Log.i(javaClass.simpleName, "Hey, connection has been established!")
//                        },
//                        { onConnectionFailure(it) },
//                        { updateUI(null) }
//                )
//                .let { connectionDisposable.add(it) }
    }

    override fun onDisconnectClicked(bleDevice: RxBleDevice) {
        if (bleDevice.isConnected)
            disconnectTriggerSubject.onNext(Unit)
    }

    override fun onScanClicked() {
        getView()?.showProgress()
        LogUtils.log("on scan clicked")
        interactor?.let {
            compositeDisposable.add(it.scanDevices()
                    .compose(schedulerProvider.ioToMainObservableScheduler())
                    .subscribe({ result ->
                        getView()?.showDevice(result)
                        getView()?.hideProgress()
                    }, { err ->
                        getView()?.hideProgress()
                        err.message?.let {
                            getView()?.showError(it)
                        }
                        LogUtils.log("error " + err)
                    }))
        }

    }

}