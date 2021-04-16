package io.treehouses.remote.pojo

import java.io.Serializable
import java.util.HashMap

data class ReverseData(val ip: String = "",
                       val postal: String = "",
                       val city: String = "",
                       val country: String = "",
                       val org: String = "",
                       val timezone: String = "")
