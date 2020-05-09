package io.treehouses.remote.bluetoothv2.ui.home.view

import com.polidea.rxandroidble2.scan.ScanResult
import io.treehouses.remote.bluetoothv2.base.view.MVPView

interface HomeMVPView : MVPView {

    fun showDevice(scanResult: ScanResult)
}