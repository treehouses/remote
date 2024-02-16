package io.treehouses.remote.utils

import io.treehouses.remote.pojo.ServiceInfo
import io.treehouses.remote.pojo.ServicesData

//val quickSearch = hashMapOf<String, ServiceInfo>()
fun constructServiceListFromData(data: ServicesData?) : HashMap<String, ServiceInfo> {
    if (data?.available == null) return hashMapOf()
    val services = hashMapOf<String, ServiceInfo>()
    data.available.forEach{ service ->
        services[service] = ServiceInfo(
                name = service,
                size = data.size[service]?.let { if (it.isNotBlank()) it.toInt() else 0 } ?: 0,
                serviceStatus = ServiceInfo.SERVICE_AVAILABLE,
                icon = data.icon[service],
                info = data.info[service],
                autorun = data.autorun[service],
                usesEnv = data.usesEnv[service]
        )
    }

    data.installed.forEach {
        services[it]?.serviceStatus = ServiceInfo.SERVICE_INSTALLED
    }

    data.running.forEach {
        services[it]?.serviceStatus = ServiceInfo.SERVICE_RUNNING
    }
    return services
}

fun formatServices(data: HashMap<String, ServiceInfo>) : MutableList<ServiceInfo> {
    val formattedServices = data.values.toMutableList()
    formattedServices.add(ServiceInfo("Installed", ServiceInfo.SERVICE_HEADER_INSTALLED))
    formattedServices.add(ServiceInfo("Available", ServiceInfo.SERVICE_HEADER_AVAILABLE))
    formattedServices.sort()
    return formattedServices
}

//fun isTorURL(output: String): Boolean {
//    return output.contains(".onion")
//}
//
//fun isLocalUrl(output: String): Boolean {
//    return output.contains(".") && output.contains(":") && output.length < 30
//}

fun isURL(output: String) : Boolean {
    return (output.contains(".") && output.contains(":") && output.length < 30) || output.contains(".onion")
}

fun containsServiceAction(output: String) : Boolean {
    return Output(output).isOneOf("started", "stopped and removed", "stopped", "installed")
}
fun indexOfService(name: String, services: MutableList<ServiceInfo>): Int {
    for (i in services.indices) {
        if (services[i].name == name) return i
    }
    return -1
}

/**
 * Helper function to count the number of headers before a specified position.
 * Mostly used to work with the spinner and the ViewPager, who have the same data, but
 * spinner has headers while ViewPager does not. Can be optimized (currently O(n), ideally O(1))
 * @param position = Interested position
 */
fun countHeadersBefore(position: Int, services: MutableList<ServiceInfo>): Int {
    var count = 0
    for (i in 0..position) { if (services[i].isHeader) count++ }
    return count
}