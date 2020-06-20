package io.treehouses.remote.pojo

import java.io.Serializable
import java.util.*

data class ServicesData(private val available: List<String>? = null,
                        private val installed: List<String>? = null,
                        private val running: List<String>? = null,
                        private val icon: HashMap<String, String>? = null,
                        private val info: HashMap<String, String>? = null,
                        private val autorun: HashMap<String, String>? = null) : Serializable {


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