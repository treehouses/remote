package io.treehouses.remote.pojo

import java.io.Serializable
import java.util.*


data class ServicesData(val available: List<String> = listOf(),
                        val installed: List<String> = listOf(),
                        val running: List<String> = listOf(),
                        val icon: HashMap<String, String> = hashMapOf(),
                        val info: HashMap<String, String> = hashMapOf(),
                        val autorun: HashMap<String, String> =hashMapOf()) : Serializable

