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

}

fun matchSshOutput(output: String): TUNNEL_SSH_RESULTS {
    when {
        Matcher.isError(output) -> return TUNNEL_SSH_RESULTS.ERROR
        Matcher.isBoolean(output) -> return TUNNEL_SSH_RESULTS.BOOLEAN
        Matcher.isStartJSON(output) -> return TUNNEL_SSH_RESULTS.START_JSON
        Matcher.isEndCommandsJson(output) -> return TUNNEL_SSH_RESULTS.END_JSON_COMMANDS
        Matcher.isEndJSON(output) -> return TUNNEL_SSH_RESULTS.END_JSON
    }
    return TUNNEL_SSH_RESULTS.RESULT_NOT_FOUND
}

object TunnelSSHMatcher {

}
