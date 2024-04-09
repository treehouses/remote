package io.treehouses.remote.pojo

data class StatusData(val status: String = "",
                        val hostname: String = "",
                        val arm: String = "",
                        val internet: String = "",
                        val memory_total: String = "",
                        val memory_used: String = "",
                        val temperature: String = "",
                        val networkmode: String = "",
                        val info: String = "",
                        val storage: String = "")
