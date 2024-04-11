package io.treehouses.remote.utils

import java.util.*


enum class TUNNEL_SSH_RESULTS {
    //Errors
    ERROR,
    RESULT_NOT_FOUND,

    //Command returns
    BOOLEAN,
    START_JSON,
    END_JSON,
    END_JSON_COMMANDS,
    RESULT_HOST_NOT_FOUND,
    RESULT_NO_PORT,
    RESULT_STATUS_ON,
    RESULT_STATUS_OFF,
    RESULT_ADDED,
    RESULT_ALREADY_EXIST,
    RESULT_REMOVED,
    RESULT_MODIFIED_LIST,
    RESULT_SSH_PORT,
    RESULT_NO_TUNNEL;
}

fun matchSshOutput(output: String): TUNNEL_SSH_RESULTS {
    when {
        TunnelSSHMatcher.isHostNotFound(output) -> return TUNNEL_SSH_RESULTS.RESULT_HOST_NOT_FOUND
        TunnelSSHMatcher.checkNoTunnelSetup(output) -> return TUNNEL_SSH_RESULTS.RESULT_NO_TUNNEL
        Matcher.isError(output) -> return TUNNEL_SSH_RESULTS.ERROR
        Matcher.isBoolean(output) -> return TUNNEL_SSH_RESULTS.BOOLEAN
        TunnelSSHMatcher.isAdded(output) -> return TUNNEL_SSH_RESULTS.RESULT_ADDED
        TunnelSSHMatcher.isRemoved(output) -> return TUNNEL_SSH_RESULTS.RESULT_REMOVED
        TunnelSSHMatcher.isListModified(output) -> return TUNNEL_SSH_RESULTS.RESULT_MODIFIED_LIST
        TunnelSSHMatcher.contains(output, "@") -> return TUNNEL_SSH_RESULTS.RESULT_SSH_PORT
        TunnelSSHMatcher.contains(output, "the command 'treehouses sshtunnel ports' returns nothing") -> return TUNNEL_SSH_RESULTS.RESULT_NO_PORT
        TunnelSSHMatcher.contains(output, "Status: on") -> return TUNNEL_SSH_RESULTS.RESULT_STATUS_ON
        TunnelSSHMatcher.contains(output, "exists") -> return TUNNEL_SSH_RESULTS.RESULT_ALREADY_EXIST
        Matcher.isStartJSON(output) -> return TUNNEL_SSH_RESULTS.START_JSON
        Matcher.isEndJSON(output) -> return TUNNEL_SSH_RESULTS.END_JSON

    }
    return TUNNEL_SSH_RESULTS.RESULT_NOT_FOUND
}

object TunnelSSHMatcher {
    fun isHostNotFound(output: String): Boolean {
        return output.contains("Host / port not found")
    }

    fun checkNoTunnelSetup(output: String): Boolean {
        return output.lowercase(Locale.ROOT).trim().contains("no tunnel has been set up")
    }

    fun isListModified(output: String): Boolean {
        return (arrayOf("Added", "Removed").filter { it in output }).isNotEmpty()
    }

    fun isAdded(output: String): Boolean {
        return output.trim().contains("added")
    }

    fun isRemoved(output: String): Boolean {
        return Matcher.toLC(output).trim().contains("removed")
    }

    fun contains(output: String, key: String): Boolean {
        return output.contains(key)
    }
}
