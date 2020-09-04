package io.treehouses.remote.callback

interface KeyMenuListener {
    fun onCopyPub(position: Int)
    fun onDelete(position: Int)
    fun onSendToRaspberry(position: Int)
}