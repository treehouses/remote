package io.treehouses.remote.ssh.beans

data class KnownHostBean (
        val hostName:String = "",
        val port: Int = 22,
        val algorithm: String = "RSA",
        val pubKey: ByteArray = ByteArray(0)
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KnownHostBean
        var flag = true
        if (hostName != other.hostName) flag = false
        if (port != other.port) flag = false
        if (algorithm != other.algorithm) flag = false
        if (!pubKey.contentEquals(other.pubKey)) flag = false

        return flag
    }

    override fun hashCode(): Int {
        var result = hostName.hashCode()
        result = 31 * result + port
        result = 31 * result + algorithm.hashCode()
        result = 31 * result + pubKey.contentHashCode()
        return result
    }
}