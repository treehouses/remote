package io.treehouses.remote.ui.services

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.treehouses.remote.pojo.ServiceInfo

class ServicesViewModel : ViewModel() {
    val servicesData = MutableLiveData<ArrayList<ServiceInfo>>(ArrayList())
}