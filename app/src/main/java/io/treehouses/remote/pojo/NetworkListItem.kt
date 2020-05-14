package io.treehouses.remote.pojo

import io.treehouses.remote.R
import java.util.*

class NetworkListItem(var title: String, var layout: Int) {

    companion object {
        val networkList: List<NetworkListItem>
            get() {
                val list: MutableList<NetworkListItem> = ArrayList()
                list.add(NetworkListItem("Ethernet: Automatic", R.layout.dialog_ethernet))
                list.add(NetworkListItem("WiFi", R.layout.dialog_wifi))
                list.add(NetworkListItem("Hotspot", R.layout.dialog_hotspot))
                list.add(NetworkListItem("Bridge", R.layout.dialog_bridge))
                list.add(NetworkListItem("Reset", R.layout.dialog_reset))
                list.add(NetworkListItem("Reboot", R.layout.dialog_reboot))
                list.add(NetworkListItem("Network Mode: ", -1))
                return list
            }

        val systemList: List<NetworkListItem?>
            get() {
                val systemList: MutableList<NetworkListItem?> = ArrayList<Any?>()
                systemList.add(NetworkListItem("Open VNC", R.layout.open_vnc))
                systemList.add(NetworkListItem("Configure Tethering (beta)", R.layout.configure_tethering))
                return systemList
            }
    }

}