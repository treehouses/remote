package io.treehouses.remote.pojo

import io.treehouses.remote.R
import java.util.*

class NetworkListItem(var title: String, var layout: Int) {

    companion object {
        val systemList: List<NetworkListItem>
            get() {
                val systemList: MutableList<NetworkListItem> = ArrayList()
                systemList.add(NetworkListItem("Shutdown & Reboot", R.layout.configure_shutdown_reboot))
                systemList.add(NetworkListItem("Open VNC", R.layout.open_vnc))
                systemList.add(NetworkListItem("Configure Tethering (beta)", R.layout.configure_tethering))
                systemList.add(NetworkListItem("Add SSH Key", R.layout.configure_ssh_key))
                systemList.add(NetworkListItem("Toggle Camera", R.layout.configure_camera))
                systemList.add(NetworkListItem("Blocker Level", R.layout.configure_blocker))
                return systemList
            }
    }

}