package io.treehouses.remote.pojo

import java.io.Serializable

class ServiceInfo : Comparable<ServiceInfo>, Serializable {
    @JvmField
    var name: String
    @JvmField
    var serviceStatus: Int
    @JvmField
    var icon: String? = null
    @JvmField
    var info: String? = null
    @JvmField
    var autorun: String? = null

    //Headers
    constructor(n: String, status: Int) {
        name = n
        serviceStatus = status
    }

    //Services
    constructor(name: String, serviceStatus: Int, icon: String?, info: String?, autorun: String?) {
        this.name = name
        this.serviceStatus = serviceStatus
        this.icon = icon
        this.info = info
        this.autorun = autorun
    }

    override fun compareTo(other: ServiceInfo): Int {
        return serviceStatus - other.serviceStatus
    }

    val isHeader: Boolean
        get() = serviceStatus == SERVICE_HEADER_AVAILABLE || serviceStatus == SERVICE_HEADER_INSTALLED

    companion object {
        const val SERVICE_HEADER_INSTALLED = 0
        const val SERVICE_RUNNING = 1
        const val SERVICE_INSTALLED = 2
        const val SERVICE_HEADER_AVAILABLE = 3
        const val SERVICE_AVAILABLE = 4
    }
}