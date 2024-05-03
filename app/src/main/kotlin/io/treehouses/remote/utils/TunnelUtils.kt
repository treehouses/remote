package io.treehouses.remote.utils

import android.content.Context
import io.treehouses.remote.adapter.TunnelPortAdapter
import java.util.ArrayList

object TunnelUtils {
    fun getPortName(portsName: ArrayList<String>?, position: Int): String {
        return portsName!![position].split(":".toRegex(), 2).toTypedArray()[0]
    }
    fun getPortAdapter(context: Context, portsName: ArrayList<String>?): TunnelPortAdapter? {
        var adapter: TunnelPortAdapter? = null
        try {
            adapter = TunnelPortAdapter(context, portsName!!)
            logE("adapter successful")
        } catch (e: Exception) {
            logE(e.toString())
        }
        return adapter
    }
}