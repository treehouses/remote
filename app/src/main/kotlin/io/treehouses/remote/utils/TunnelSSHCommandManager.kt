package io.treehouses.remote.utils


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
        Matcher.isError(output) -> return TUNNEL_SSH_RESULTS.ERROR
        Matcher.isBoolean(output) -> return TUNNEL_SSH_RESULTS.BOOLEAN
        TunnelSSHMatcher.isHostNotFound(output) -> TUNNEL_SSH_RESULTS.RESULT_HOST_NOT_FOUND
        TunnelSSHMatcher.checkTunnelSetupAddedOrRemoved(output, "no tunnel has been set up") -> TUNNEL_SSH_RESULTS.RESULT_NO_TUNNEL
        TunnelSSHMatcher.checkTunnelSetupAddedOrRemoved(output,"added") -> TUNNEL_SSH_RESULTS.RESULT_ADDED
        TunnelSSHMatcher.checkTunnelSetupAddedOrRemoved(output, "removed") -> TUNNEL_SSH_RESULTS.RESULT_REMOVED
        TunnelSSHMatcher.isListModified(output) -> TUNNEL_SSH_RESULTS.RESULT_MODIFIED_LIST
        TunnelSSHMatcher.contains(output, "@") -> TUNNEL_SSH_RESULTS.RESULT_MODIFIED_LIST
        TunnelSSHMatcher.contains(output, "the command 'treehouses sshtunnel ports' returns nothing") -> TUNNEL_SSH_RESULTS.RESULT_NO_PORT
        TunnelSSHMatcher.contains(output, "Status: on") -> TUNNEL_SSH_RESULTS.RESULT_STATUS_ON
        TunnelSSHMatcher.contains(output, "exists") -> TUNNEL_SSH_RESULTS.RESULT_ALREADY_EXIST
        Matcher.isStartJSON(output) -> return TUNNEL_SSH_RESULTS.START_JSON
        Matcher.isEndCommandsJson(output) -> return TUNNEL_SSH_RESULTS.END_JSON_COMMANDS
        Matcher.isEndJSON(output) -> return TUNNEL_SSH_RESULTS.END_JSON

    }
    return TUNNEL_SSH_RESULTS.RESULT_NOT_FOUND
}

object TunnelSSHMatcher {
    fun isHostNotFound(output: String): Boolean {
        return output.contains("Host / port not found")
    }

    fun checkTunnelSetupAddedOrRemoved(output: String, check: String): Boolean {
        return Matcher.toLC(output.trim()).contains(check)
    }

    fun isListModified(output: String): Boolean {
        return (arrayOf("Added", "Removed").filter { it in output }).isNotEmpty()
    }

    fun contains(output: String, key :String): Boolean {
        return output.contains(key)
    }
}
