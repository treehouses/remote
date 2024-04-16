package io.treehouses.remote.ssh.beans

import android.net.Uri
import java.nio.charset.Charset

class HostBean {
    var fontSize = 8 //Default 10
    var nickname: String? = "treehouses"
    var hostname: String? = "192.168.1.29"
    var username: String? = "pi"
    var keyName: String = ""

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

    @Throws(Exception::class)
    fun setHostFromUri(uri: Uri?) {
        if (uri == null) return
        try {
            protocol = uri.scheme
            username = uri.userInfo
            hostname = uri.host
            port = if (uri.port == -1) 22 else uri.port
            nickname = if (uri.fragment == null) "" else uri.fragment
        } catch (e: Exception) {
            throw Exception("Not A Valid URI")
        }
    }

    fun getPrettyFormat() : String {
        var format = "$username@$hostname"
        if (port != 22) format += ":$port"
        return format
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as HostBean
        val flag: Boolean = when {
            nickname != other.nickname -> false
            hostname != other.hostname -> false
            username != other.username -> false
            port != other.port -> false
            else -> true
        }
        return flag
    }
}