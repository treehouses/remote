package io.treehouses.remote.bluetoothv2.ui.home.presenter

import com.polidea.rxandroidble2.RxBleDevice
import io.treehouses.remote.bluetoothv2.base.presenter.MVPPresenter
import io.treehouses.remote.bluetoothv2.ui.home.interactor.HomeMVPInterator
import io.treehouses.remote.bluetoothv2.ui.home.view.HomeMVPView

interface HomeMVPPresenter<V : HomeMVPView, I : HomeMVPInterator> : MVPPresenter<V, I> {
    fun onConnectClicked(bleDevice: RxBleDevice)
    fun onDisconnectClicked(bleDevice: RxBleDevice)
    fun onScanClicked()
}