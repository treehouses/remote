package io.treehouses.remote.callback

import androidx.fragment.app.Fragment
import io.treehouses.remote.network.BluetoothChatService

interface HomeInteractListener {
    fun sendMessage(s: String)
    fun openCallFragment(f: Fragment)
    fun redirectHome()
    fun getChatService(): BluetoothChatService
    fun setChatService(service: BluetoothChatService)
}