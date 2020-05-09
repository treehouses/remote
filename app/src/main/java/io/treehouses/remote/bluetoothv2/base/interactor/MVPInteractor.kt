package io.treehouses.remote.bluetoothv2.base.interactor

interface MVPInteractor {

    fun isBluetoothConnected(): Boolean

    fun disconnect() : Unit?

}