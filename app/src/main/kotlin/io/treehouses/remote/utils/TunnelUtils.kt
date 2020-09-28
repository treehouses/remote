package io.treehouses.remote.utils

object TunnelUtils {
    fun getPortName(portsName: java.util.ArrayList<String>?, position: Int): String {
        return portsName!![position].split(":".toRegex(), 2).toTypedArray()[0]
    }
}