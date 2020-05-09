package io.treehouses.remote.bluetoothv2.ui.home.interactor

import io.treehouses.remote.bluetoothv2.base.interactor.MVPInteractor

interface HomeMVPInterator : MVPInteractor {


     fun scanDevices()
     fun connectToDevice(mac : String)
     fun disconnectDevice()
}