package io.treehouses.remote.callback

import io.treehouses.remote.pojo.ServiceInfo

interface ServiceActionListener {
    fun onClickInstall(s: ServiceInfo?)
    fun onClickStart(s: ServiceInfo?)
    fun onClickLink(s: ServiceInfo?)
    fun onClickAutorun(s: ServiceInfo?, newAutoRun: Boolean)
    fun onClickEditEnvVar(s: ServiceInfo?)
}