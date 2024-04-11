package io.treehouses.remote.utils

import java.util.*


enum class RESULTS {
    //Errors
    ERROR,
    RESULT_NOT_FOUND,
    //Command returns
    BOOLEAN,
    VERSION,
    REMOTE_CHECK,
    UPGRADE_CHECK,
    HOTSPOT_CONNECTED,
    WIFI_CONNECTED,
    BRIDGE_CONNECTED,
    DEFAULT_CONNECTED,
    DEFAULT_NETWORK,
    NETWORKMODE,
    NETWORKMODE_INFO,
    START_JSON,
    SPEED_TEST,
    END_JSON,
    END_JSON_SERVICES,
    END_JSON_COMMANDS,
    PING_OUTPUT,
    END_HELP,
    REVERSE_LOOKUP
}

fun match (output: String) : RESULTS {
    when {
        Matcher.isSpeedTest(output) -> return RESULTS.SPEED_TEST
        Matcher.isEndHelpJson(output) -> return RESULTS.END_HELP
        Matcher.isError(output) ->  return RESULTS.ERROR
        Matcher.isBoolean(output) -> return RESULTS.BOOLEAN
        Matcher.isVersion(output) -> return RESULTS.VERSION
        Matcher.isReverseLookup(output) -> return RESULTS.REVERSE_LOOKUP
        Matcher.isEndHelpJson(output) -> return RESULTS.END_HELP
        Matcher.isRemoteCheck(output) -> return RESULTS.REMOTE_CHECK
        Matcher.isBridgeConnected(output) -> return RESULTS.BRIDGE_CONNECTED
        Matcher.isHotspotConnected(output) -> return RESULTS.HOTSPOT_CONNECTED
        Matcher.isWifiConnected(output) -> return RESULTS.WIFI_CONNECTED
        Matcher.isUpgradeCheck(output) -> return RESULTS.UPGRADE_CHECK
        Matcher.isDefaultNetwork(output) -> return RESULTS.DEFAULT_NETWORK
        Matcher.isDefaultConnected(output) -> return RESULTS.DEFAULT_CONNECTED
        Matcher.isNetworkModeReturned(output) -> return RESULTS.NETWORKMODE
        Matcher.isStartJSON(output) -> return RESULTS.START_JSON
        Matcher.isEndAllServicesJson(output) -> return RESULTS.END_JSON_SERVICES
        Matcher.isEndCommandsJson(output) -> return RESULTS.END_JSON_COMMANDS
        Matcher.isPingOutput(output) -> return RESULTS.PING_OUTPUT
        Matcher.isEndJSON(output) -> return RESULTS.END_JSON
        Matcher.isNetworkModeInfoReturned(output) -> return RESULTS.NETWORKMODE_INFO }
    return RESULTS.RESULT_NOT_FOUND
}

class Output (val output: String) {
    fun isOneOf(vararg param : String) : Boolean {
        for (p in param) {
            if (output.contains(p)) return true
        }
        return false
    }
}
//Could remove IDs and simply use these functions
object Matcher {
    fun toLC(string: String) : String {return string.lowercase(Locale.ROOT).trim(); }

    fun isError(output: String): Boolean {
        val keys = listOf("error ", "unknown command", "usage: ", "not a valid option", "error: ", "not found")
        if (output.contains("{") || output.contains("}")) return false
        for (k in keys) if (toLC(output).contains(k)) return true
        return false
    }

    fun isValidOutput(output : String, str1: String, str2: String) : Boolean {
        return toLC(output) == str1 || toLC(output) == str2
    }

    fun isBoolean(output: String): Boolean { return isValidOutput(output, "true", "false")}

    fun isVersion(output: String): Boolean {
        return isValidOutput(output,"version: false", "version: true")
    }

    fun isRemoteCheck(output: String): Boolean {
        return output.contains(" ") && toLC(output).split(" ").size == 4
                && !output.contains("network") && !output.contains("pirateship") && !output.contains("bridge")
    }

    fun isUpgradeCheck(output: String): Boolean {
        val regexTrue = """(true|false) \d{1,2}[.]\d{1,2}[.]\d{1,2}""".toRegex()
        return regexTrue.matches(toLC(output))
    }
    fun isDefaultConnected(output: String): Boolean {return output.startsWith("Success: the network mode has")}
    fun isBridgeConnected(output: String): Boolean {return output.contains("bridge has been built")}

    fun isHotspotConnected (output: String): Boolean {return toLC(output).contains("pirateship has anchored successfully")}

    fun isWifiConnected (output: String): Boolean {return output.contains("open network") || output.contains("password network")}

    fun isDefaultNetwork(output: String): Boolean {return toLC(output).contains("the network mode has been reset to default")}

    fun isNetworkModeReturned(output: String): Boolean {
        return when (toLC(output)) {
            "wifi", "bridge", "ap local", "ap internet", "static wifi", "static ethernet", "default" -> true
            else -> false
        }
    }

    fun isNetworkModeInfoReturned(output: String): Boolean {
        return output.startsWith("essid:")
    }

    fun isStartJSON(output: String): Boolean { return toLC(output).startsWith("{") }

    fun isEndJSON(output: String): Boolean { return toLC(output).trim().endsWith("}")}

    fun isEndHelpJson(output: String): Boolean { return toLC(output).trim().endsWith("\" }")}

    fun isEndCommandsJson(output: String): Boolean { return toLC(output).endsWith("]}") }

    fun isPingOutput(output: String): Boolean {return toLC(output).contains("google.com") || toLC(output).contains("remote")}

    fun isEndAllServicesJson(output: String): Boolean { return toLC(output).endsWith("}}") }

    fun isSpeedTest(output: String): Boolean {return toLC(output).contains("mbit/s")  }

    fun isReverseLookup(output: String): Boolean { return toLC(output).trim().startsWith("{\"ip") && toLC(output).trim().endsWith("}")}


}