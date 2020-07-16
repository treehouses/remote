package io.treehouses.remote.SSH.beans

import android.net.Uri
import java.nio.charset.Charset

class HostBean {
    var fontSize = 8 //Default 10
    var nickname: String? = "treehouses"
    var hostname: String? = "192.168.1.29"
    var username: String? = "pi"
    val pubkeyId: Long = -1

    var protocol: String? = "ssh"
    var port = 22
    private val id: Long = -1
    val encoding: String
        get() = Charset.defaultCharset().name()

    val postLogin: String
        get() = ""

    val quickDisconnect: Boolean
        get() = false

    val stayConnected: Boolean
        get() = false

    val wantSession: Boolean
        get() = true

    val compression: Boolean
        get() = false

    val useAuthAgent: String
        get() = "no"

    val uri: Uri
        get() {
            val sb = StringBuilder()
            sb.append("ssh://")
            if (username != null) sb.append(Uri.encode(username))
                    .append('@')
            sb.append(Uri.encode(hostname))
                    .append(':')
                    .append(port)
                    .append("/#")
                    .append(nickname)
            return Uri.parse(sb.toString())
        }

    private fun log(s: String?): String {
        return s ?: "null"
    }

    @Throws(Exception::class)
    fun setHostFromUri(uri: Uri?) {
        if (uri == null) return
        try {
            protocol = uri.scheme
            username = uri.userInfo
            hostname = uri.host
            port = uri.port
            nickname = uri.fragment
        } catch (e: Exception) {
            throw Exception("Not A Valid URI")
        }
    }

    override fun hashCode(): Int {
        var hash = 7
        if (id != -1L) return id.toInt()
        hash = 31 * hash + if (null == nickname) 0 else nickname.hashCode()
        hash = 31 * hash + if (null == protocol) 0 else protocol.hashCode()
        hash = 31 * hash + if (null == username) 0 else username.hashCode()
        hash = 31 * hash + if (null == hostname) 0 else hostname.hashCode()
        hash = 31 * hash + port
        return hash
    }
}