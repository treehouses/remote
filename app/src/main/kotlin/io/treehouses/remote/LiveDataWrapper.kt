package io.treehouses.remote

import androidx.lifecycle.MutableLiveData
import io.treehouses.remote.pojo.enum.Resource

class LiveDataWrapper<T>(val liveData: MutableLiveData<Resource<T>>)
