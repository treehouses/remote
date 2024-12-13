package io.treehouses.remote.bases

import com.trilead.ssh2.*
import com.trilead.ssh2.signature.DSASHA1Verify
import com.trilead.ssh2.signature.ECDSASHA2Verify
import com.trilead.ssh2.signature.Ed25519Verify
import com.trilead.ssh2.signature.RSASHA1Verify
import io.treehouses.remote.R
import io.treehouses.remote.ssh.Ed25519Provider.Companion.insertIfNeeded
import io.treehouses.remote.ssh.beans.HostBean
import io.treehouses.remote.ssh.beans.PubKeyBean
import io.treehouses.remote.ssh.terminal.TerminalBridge
import io.treehouses.remote.ssh.terminal.TerminalManager
import io.treehouses.remote.utils.KeyUtils
import net.i2p.crypto.eddsa.EdDSAPrivateKey
import net.i2p.crypto.eddsa.EdDSAPublicKey
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyPair
import java.security.interfaces.*
import java.util.*
import java.util.regex.Pattern

open class BaseSSH : ConnectionMonitor, InteractiveCallback, AuthAgentCallback {
    var host: HostBean? = null
    var bridge: TerminalBridge? = null
    var manager: TerminalManager? = null

    var emulation: String? = null

    companion object {
        const val TAG = "CB.SSH"
        const val AUTH_PUBLICKEY = "publickey"
        const val AUTH_PASSWORD = "password"
        const val AUTH_KEYBOARDINTERACTIVE = "keyboard-interactive"
        const val AUTH_TRIES = 20
        private val hostmask = Pattern.compile(
                "^(.+)@(([0-9a-z.-]+)|(\\[[a-f:0-9]+\\]))(:(\\d+))?$", Pattern.CASE_INSENSITIVE)
        const val conditions = (ChannelCondition.STDOUT_DATA
                or ChannelCondition.STDERR_DATA
                or ChannelCondition.CLOSED
                or ChannelCondition.EOF)

        init {
            insertIfNeeded()
        }
    }

    @Volatile
    private var authenticated = false

    @Volatile
    var isConnected = false
        protected set

    @Volatile
    var isSessionOpen = false
        private set
    protected var pubkeysExhausted = false
    protected var interactiveCanContinue = true
    protected var connection: Connection? = null
    protected var session: Session? = null
    protected var stdin: OutputStream? = null
    protected var stdout: InputStream? = null
    protected var stderr: InputStream? = null
    protected var columns = 0
    protected var rows = 0
    private val width = 0
    private val height = 0
    private var useAuthAgent = "no"
    private var agentLockPassphrase: String? = null

    inner class HostKeyVerifier : ExtendedServerHostKeyVerifier() {
        @Throws(IOException::class)
        override fun verifyServerHostKey(hostname: String, port: Int, serverHostKeyAlgorithm: String, serverHostKey: ByteArray): Boolean {
            val hosts = KeyUtils.getAllKnownHosts(manager!!.applicationContext)
            val matchName = String.format(Locale.US, "%s:%d", hostname, port)
            val print = KnownHosts.createHexFingerprint(serverHostKeyAlgorithm, serverHostKey)
            val algo: String = if ("ssh-rsa" == serverHostKeyAlgorithm) "RSA" else if ("ssh-dss" == serverHostKeyAlgorithm) "DSA" else if (serverHostKeyAlgorithm.startsWith("ecdsa-")) "EC" else if ("ssh-ed25519" == serverHostKeyAlgorithm) "Ed25519" else serverHostKeyAlgorithm
            return when (hosts.verifyHostkey(matchName, serverHostKeyAlgorithm, serverHostKey)) {
                KnownHosts.HOSTKEY_IS_OK -> onKeyOk(algo, print)
                KnownHosts.HOSTKEY_IS_NEW -> {
                    val id = R.string.host_fingerprint
                    val hostPrint = manager!!.res!!.getString(id, algo, print)

                    val id2 = R.string.host_authenticity_warning
                    val hostWarn = manager!!.res!!.getString(id2, hostname)
                    bridge!!.outputLine(hostWarn)
                    bridge!!.outputLine(hostPrint)
                    promptKeys(hostname, port, serverHostKeyAlgorithm, serverHostKey)
                }
                KnownHosts.HOSTKEY_HAS_CHANGED -> {
                    onHostKeyChanged(algo, print)
                    promptKeys(hostname, port, serverHostKeyAlgorithm, serverHostKey)
                }
                else -> onKeyFailed()
            }
        }

        private fun onKeyOk(algo: String, print: String): Boolean {
            val id = R.string.terminal_sucess
            val line = manager!!.res!!.getString(id, algo, print)
            bridge!!.outputLine(line)
            return true
        }

        private fun onKeyFailed(): Boolean {
            bridge!!.outputLine(manager!!.res!!.getString(R.string.terminal_failed))
            return false
        }

        private fun onHostKeyChanged(algorithmName: String, fingerprint: String) {
            val header = String.format("@   %s   @", manager!!.res!!.getString(R.string.host_verification_failure_warning_header))
            val atsigns = CharArray(header.length)
            Arrays.fill(atsigns, '@')
            val border = String(atsigns)
            bridge!!.outputLine(border)
            bridge!!.outputLine(header)
            bridge!!.outputLine(border)
            outputLine(R.string.host_verification_failure_warning)
            bridge!!.outputLine(String.format(manager!!.res!!.getString(R.string.host_fingerprint), algorithmName, fingerprint))
        }

        private fun promptKeys(hostname: String, port: Int, serverHostKeyAlgorithm: String, serverHostKey: ByteArray): Boolean {
            if (continueConnecting()) {
                KeyUtils.saveKnownHost(manager!!.applicationContext, "$hostname:$port", serverHostKeyAlgorithm, serverHostKey)
                return true
            } else return false
        }

        private fun continueConnecting(): Boolean {
            val prompt = manager!!.res!!.getString(R.string.prompt_continue_connecting)
            return bridge!!.promptHelper!!.requestPrompt<Boolean>(null, prompt, isBool = true)!!
        }

        override fun getKnownKeyAlgorithmsForHost(host: String, port: Int): List<String> {
            val hostBean = KeyUtils.getKnownHost(manager!!.applicationContext, "$host:$port")
            return if (hostBean == null) ArrayList() else arrayListOf(hostBean.algorithm)
        }

        override fun removeServerHostKey(host: String, port: Int, algorithm: String, hostKey: ByteArray) {
            KeyUtils.removeKnownHost(manager!!.applicationContext, "$host:$port")
        }

        override fun addServerHostKey(host: String, port: Int, algorithm: String, hostKey: ByteArray) {
            KeyUtils.saveKnownHost(manager!!.applicationContext, "$host:$port", algorithm, hostKey)
        }
    }

    protected fun outputLine(stringResource: Int) {
        bridge!!.outputLine(manager!!.res!!.getString(stringResource))
    }

    @Throws(IOException::class)
    protected fun tryPublicKey(username: String?, keyNickname: String?, pair: KeyPair?): Boolean {
        val success = connection!!.authenticateWithPublicKey(username, pair)
        if (!success) bridge!!.outputLine(manager!!.res!!.getString(R.string.terminal_auth_pubkey_fail, keyNickname))
        return success
    }

    protected fun finishConnection() {
        authenticated = true

        if (!host!!.wantSession) {
            outputLine(R.string.terminal_no_session)
            bridge!!.onConnected()
            return
        }
        try {
            session = connection!!.openSession()
            session?.let {
                if (useAuthAgent != "no") it.requestAuthAgentForwarding(this)
                it.requestPTY(emulation, columns, rows, width, height, null)
                it.startShell()
                stdin = it.stdin
                stdout = it.stdout
                stderr = it.stderr

            }
            isSessionOpen = true
            bridge!!.onConnected()
        } catch (e1: IOException) {
            e1.printStackTrace()
        }
    }

    protected fun onDisconnect() {
        bridge!!.dispatchDisconnect(false)
    }

    override fun connectionLost(reason: Throwable) {
        onDisconnect()
    }

    override fun replyToChallenge(name: String, instruction: String, numPrompts: Int, prompt: Array<String>, echo: BooleanArray): Array<String?> {
        interactiveCanContinue = true
        val responses = arrayOfNulls<String>(numPrompts)
        for (i in 0 until numPrompts) {
            responses[i] = bridge!!.promptHelper!!.requestPrompt<String>(instruction, prompt[i], isBool = false)
        }
        return responses
    }

    fun setUseAuthAgent(useAuthAgent: String) {
        this.useAuthAgent = useAuthAgent
    }

    override fun retrieveIdentities(): Map<String, ByteArray> {
        val pubKeys: MutableMap<String, ByteArray> = HashMap(manager!!.loadedKeypairs.size)
        for ((key, value) in manager!!.loadedKeypairs) {
            val pair = value?.pair
            try {
                pubKeys[key] = when (pair?.private) {
                    is RSAPrivateKey -> RSASHA1Verify.get().encodePublicKey(pair.public as RSAPublicKey)
                    is DSAPrivateKey -> DSASHA1Verify.get().encodePublicKey(pair.public as DSAPublicKey)
                    is ECPrivateKey -> {
                        val verifier = ECDSASHA2Verify.getVerifierForKey(pair.public as ECPublicKey)
                        verifier.encodePublicKey(pair.public as ECPublicKey)
                    }
                    is EdDSAPrivateKey -> Ed25519Verify.get().encodePublicKey(pair.public as EdDSAPublicKey)
                    else -> ByteArray(0)
                }
            } catch (e: IOException) {
                continue
            }
        }
        return pubKeys
    }

    override fun getKeyPair(publicKey: ByteArray): KeyPair? {
        val nickname = manager!!.getKeyNickname(publicKey) ?: return null
        if (useAuthAgent == "no") {
            return null
        } else if (useAuthAgent == "confirm" ||
                manager!!.loadedKeypairs[nickname]!!.bean!!.isConfirmUse) {
            if (!promptForPubkeyUse(nickname)) return null
        }
        return manager!!.getKey(nickname)!!
    }

    protected fun promptForPubkeyUse(nickname: String?): Boolean {
        val result = bridge!!.promptHelper!!.requestPrompt<Boolean>(null,
                manager!!.res!!.getString(R.string.prompt_allow_agent_to_use_key, nickname), isBool = true)
        return result!!
    }

    override fun addIdentity(pair: KeyPair, comment: String, confirmUse: Boolean, lifetime: Int): Boolean {
        val pubkey = PubKeyBean()
        pubkey.nickname = comment
        pubkey.isConfirmUse = confirmUse
        pubkey.lifetime = lifetime
        manager!!.addKey(pubkey, pair)
        return true
    }

    override fun removeAllIdentities(): Boolean {
        manager!!.loadedKeypairs.clear()
        return true
    }

    override fun removeIdentity(publicKey: ByteArray): Boolean {
        return manager!!.removeKey(publicKey)
    }

    override fun isAgentLocked(): Boolean {
        return agentLockPassphrase != null
    }

    override fun requestAgentUnlock(unlockPassphrase: String): Boolean {
        if (agentLockPassphrase == null) return false
        if (agentLockPassphrase == unlockPassphrase) agentLockPassphrase = null
        return agentLockPassphrase == null
    }

    override fun setAgentLock(lockPassphrase: String): Boolean {
        if (agentLockPassphrase != null) return false
        agentLockPassphrase = lockPassphrase
        return true
    }

}