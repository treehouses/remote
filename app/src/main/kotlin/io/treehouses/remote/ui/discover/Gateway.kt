package io.treehouses.remote.ui.discover

class Gateway {
    var device = Device()
    lateinit var ssid: String

    fun isComplete(): Boolean {
        return device.isComplete() && this::ssid.isInitialized
    }
}