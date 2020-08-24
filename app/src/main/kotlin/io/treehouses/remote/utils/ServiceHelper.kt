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
                size = data.size[service]?.toInt() ?: 0,
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

fun isTorURL(output: String, received: Boolean): Boolean {
    return output.contains(".onion") && !received
}

fun isLocalUrl(output: String, received: Boolean): Boolean {
    return output.contains(".") && output.contains(":") && output.length < 25 && !received
}

fun containsServiceAction(output: String) : Boolean {
    return Output(output).isOneOf("started", "stopped and removed", "stopped", "installed")
}
