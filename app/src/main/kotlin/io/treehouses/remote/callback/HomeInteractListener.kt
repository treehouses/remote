package io.treehouses.remote.callback

import androidx.fragment.app.Fragment
import io.treehouses.remote.Network.BluetoothChatService

interface HomeInteractListener {
    fun sendMessage(s: String)
    fun openCallFragment(f: Fragment)
    fun getChatService(): BluetoothChatService
    fun setChatService(service: BluetoothChatService)
}