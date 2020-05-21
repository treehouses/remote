package io.treehouses.remote.utils

import java.util.*


enum class RESULTS {
    //Errors
    ERROR,
    RESULT_NOT_FOUND,
    UNKNOWN,
    //Command returns
    BOOLEAN,
    VERSION,
    REMOTE_CHECK,
    UPGRADE_CHECK,
    HOTSPOT_OR_WIFI,
    BRIDGE_CONNECTED,
    DEFAULT_NETWORK

}

fun match (output: String) : RESULTS {
    when {
        Matcher.isError(output) ->  return RESULTS.ERROR
        Matcher.isBoolean(output) -> return RESULTS.BOOLEAN
        Matcher.isVersion(output) -> return RESULTS.VERSION
        Matcher.isRemoteCheck(output) -> return RESULTS.REMOTE_CHECK
        Matcher.isBridge(output) -> return RESULTS.BRIDGE_CONNECTED
        Matcher.isUpgradeCheck(output) -> return RESULTS.UPGRADE_CHECK
        Matcher.isHotspotOrWifi(output) -> return RESULTS.HOTSPOT_OR_WIFI
        Matcher.isDefaultNetwork(output) -> return RESULTS.DEFAULT_NETWORK
    }
    return RESULTS.RESULT_NOT_FOUND
}

object Matcher {
    private fun toLC(string: String) : String {
        return string.toLowerCase(Locale.ROOT).trim();
    }

    fun isError(output: String): Boolean {
        val keys = listOf("error", "unknown", "usage", "command")
        for (k in keys) if (toLC(output).contains(k)) return true
        return false
    }

    fun isBoolean(output: String): Boolean {
        return toLC(output) == "true" || toLC(output) == "false"
    }

    fun isVersion(output: String): Boolean {
        return toLC(output) == "version: false" || toLC(output) == "version: true"
    }

    fun isRemoteCheck(output: String): Boolean {
        return output.contains(" ") && toLC(output).split(" ").size == 4
                && !output.contains("network") && !output.contains("pirateship") && !output.contains("bridge")
    }

    fun isBridge(output: String): Boolean {return output.contains("bridge has been built")}

    fun isUpgradeCheck(output: String): Boolean {
        val regexTrue = """true \d{1,2}[.]\d{1,2}[.]\d{1,2}""".toRegex()
        val regexFalse = """false \d{1,2}[.]\d{1,2}[.]\d{1,2}""".toRegex()

        return regexTrue.matches(toLC(output)) || regexFalse.matches(toLC(output))
    }

    fun isHotspotOrWifi(output: String): Boolean {
        return toLC(output).contains("connected") || output.contains("pirateship")
    }

    fun isDefaultNetwork(output: String): Boolean {
        return toLC(output).contains("default")
    }





}