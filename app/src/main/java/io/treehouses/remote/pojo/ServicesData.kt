package io.treehouses.remote.pojo

import java.io.Serializable
import java.util.*

class ServicesData : Serializable {
    @JvmField
    val available: List<String>? = null
    @JvmField
    val installed: List<String>? = null
    @JvmField
    val running: List<String>? = null
    @JvmField
    val icon: HashMap<String, String>? = null
    @JvmField
    val info: HashMap<String, String>? = null
    @JvmField
    val autorun: HashMap<String, String>? = null

    fun getAvailable(): List<String?>? {
        return available
    }

    fun getInstalled(): List<String?>? {
        return installed
    }

    fun getRunning(): List<String?>? {
        return running
    }

    fun getIcon(): HashMap<String, String>? {
        return icon
    }

    fun getInfo(): HashMap<String, String>? {
        return info
    }

    fun getAutorun(): HashMap<String, String>? {
        return autorun
    }

}