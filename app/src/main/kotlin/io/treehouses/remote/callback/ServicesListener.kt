package io.treehouses.remote.callback

import io.treehouses.remote.pojo.ServiceInfo

interface ServicesListener {
    fun onClick(s: ServiceInfo?)
}