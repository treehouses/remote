/*
 * ConnectBot: simple, powerful, open-source SSH client for Android
 * Copyright 2007 Kenny Root, Jeffrey Sharkey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.treehouses.remote.SSH

import android.net.Uri
import android.util.Log
import com.trilead.ssh2.*
import com.trilead.ssh2.crypto.PEMDecoder
import com.trilead.ssh2.signature.DSASHA1Verify
import com.trilead.ssh2.signature.ECDSASHA2Verify
import com.trilead.ssh2.signature.Ed25519Verify
import com.trilead.ssh2.signature.RSASHA1Verify
import io.treehouses.remote.R
import io.treehouses.remote.SSH.Ed25519Provider.Companion.insertIfNeeded
import io.treehouses.remote.SSH.Terminal.TerminalBridge
import io.treehouses.remote.SSH.Terminal.TerminalManager
import io.treehouses.remote.SSH.beans.HostBean
import io.treehouses.remote.SSH.beans.PubKeyBean
import net.i2p.crypto.eddsa.EdDSAPrivateKey
import net.i2p.crypto.eddsa.EdDSAPublicKey
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyPair
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.interfaces.*
import java.security.spec.InvalidKeySpecException
import java.util.*
import java.util.regex.Pattern

/**
 * @author Kenny Root
 */
class SSH : ConnectionMonitor, InteractiveCallback, AuthAgentCallback {
    var host: HostBean? = null
    var bridge: TerminalBridge? = null
    var manager: TerminalManager? = null

    var emulation: String? = null

    companion object {
        const val protocolName = "ssh"
        private const val TAG = "CB.SSH"
        const val defaultPort = 22
        private const val AUTH_PUBLICKEY = "publickey"
        private const val AUTH_PASSWORD = "password"
        private const val AUTH_KEYBOARDINTERACTIVE = "keyboard-interactive"
        private const val AUTH_TRIES = 20
        private val hostmask = Pattern.compile(
                "^(.+)@(([0-9a-z.-]+)|(\\[[a-f:0-9]+\\]))(:(\\d+))?$", Pattern.CASE_INSENSITIVE)
        private const val conditions = (ChannelCondition.STDOUT_DATA
                or ChannelCondition.STDERR_DATA
                or ChannelCondition.CLOSED
                or ChannelCondition.EOF)

        fun getUri(input: String?): Uri? {
            val matcher = hostmask.matcher(input)
            if (!matcher.matches()) return null
            val sb = StringBuilder()
            sb.append(protocolName)
                    .append("://")
                    .append(Uri.encode(matcher.group(1)))
                    .append('@')
                    .append(Uri.encode(matcher.group(2)))
            val portString = matcher.group(6)
            var port = defaultPort
            if (portString != null) {
                try {
                    port = portString.toInt()
                    if (port < 1 || port > 65535) {
                        port = defaultPort
                    }
                } catch (nfe: NumberFormatException) {
                    // Keep the default port
                }
            }
            if (port != defaultPort) {
                sb.append(':')
                        .append(port)
            }
            sb.append("/#")
                    .append(Uri.encode(input))
            return Uri.parse(sb.toString())
        }

        init {
            // Since this class deals with EdDSA keys, we need to make sure this is available.
            insertIfNeeded()
        }
    }

    constructor() : super() {}

    /**
     * @param host
     * @param bridge
     * @param manager
     */
    constructor(host: HostBean?, bridge: TerminalBridge?, manager: TerminalManager?) {
        this.host = host
        this.bridge = bridge
        this.manager = manager
    }

    private var compression = false

    @Volatile
    private var authenticated = false

    @Volatile
    var isConnected = false
        private set

    @Volatile
    var isSessionOpen = false
        private set
    private var pubkeysExhausted = false
    private var interactiveCanContinue = true
    private var connection: Connection? = null
    private var session: Session? = null
    private var stdin: OutputStream? = null
    private var stdout: InputStream? = null
    private var stderr: InputStream? = null

    //    private List<PortForwardBean> portForwards = new ArrayList<>();
    private var columns = 0
    private var rows = 0
    private val width = 0
    private val height = 0
    private var useAuthAgent = "no"
    private var agentLockPassphrase: String? = null

    inner class HostKeyVerifier : ExtendedServerHostKeyVerifier() {
        @Throws(IOException::class)
        override fun verifyServerHostKey(hostname: String, port: Int,
                                         serverHostKeyAlgorithm: String, serverHostKey: ByteArray): Boolean {

            // read in all known hosts from hostdb
            val hosts = KnownHosts()
            val result: Boolean
            val matchName = String.format(Locale.US, "%s:%d", hostname, port)
            val fingerprint = KnownHosts.createHexFingerprint(serverHostKeyAlgorithm, serverHostKey)
            val algorithmName: String
            algorithmName = if ("ssh-rsa" == serverHostKeyAlgorithm) "RSA" else if ("ssh-dss" == serverHostKeyAlgorithm) "DSA" else if (serverHostKeyAlgorithm.startsWith("ecdsa-")) "EC" else if ("ssh-ed25519" == serverHostKeyAlgorithm) "Ed25519" else serverHostKeyAlgorithm
            return when (hosts.verifyHostkey(matchName, serverHostKeyAlgorithm, serverHostKey)) {
                KnownHosts.HOSTKEY_IS_OK -> {
                    bridge!!.outputLine(manager!!.res!!.getString(R.string.terminal_sucess, algorithmName, fingerprint))
                    true
                }
                KnownHosts.HOSTKEY_IS_NEW -> {
                    // prompt user
                    bridge!!.outputLine(manager!!.res!!.getString(R.string.host_authenticity_warning, hostname))
                    bridge!!.outputLine(manager!!.res!!.getString(R.string.host_fingerprint, algorithmName, fingerprint))
                    result = bridge!!.promptHelper!!.requestBooleanPrompt(null, manager!!.res!!.getString(R.string.prompt_continue_connecting))!!
                    if (result == null) {
                        return false
                    }
                    Log.e("HOST KEY", Arrays.toString(serverHostKey))
                    //                    if (result) {
//                        // save this key in known database
//                        manager.hostdb.saveKnownHost(hostname, port, serverHostKeyAlgorithm, serverHostKey);
//                    }
                    result
                }
                KnownHosts.HOSTKEY_HAS_CHANGED -> {
                    val header = String.format("@   %s   @",
                            manager!!.res!!.getString(R.string.host_verification_failure_warning_header))
                    val atsigns = CharArray(header.length)
                    Arrays.fill(atsigns, '@')
                    val border = String(atsigns)
                    bridge!!.outputLine(border)
                    bridge!!.outputLine(header)
                    bridge!!.outputLine(border)
                    bridge!!.outputLine(manager!!.res!!.getString(R.string.host_verification_failure_warning))
                    bridge!!.outputLine(String.format(manager!!.res!!.getString(R.string.host_fingerprint),
                            algorithmName, fingerprint))

                    // Users have no way to delete keys, so we'll prompt them for now.
                    result = bridge!!.promptHelper!!.requestBooleanPrompt(null, manager!!.res!!.getString(R.string.prompt_continue_connecting))!!
                    result
                }
                else -> {
                    bridge!!.outputLine(manager!!.res!!.getString(R.string.terminal_failed))
                    false
                }
            }
        }

        override fun getKnownKeyAlgorithmsForHost(host: String, port: Int): List<String> {
//            return manager.hostdb.getHostKeyAlgorithmsForHost(host, port);
            return ArrayList()
        }

        override fun removeServerHostKey(host: String, port: Int, algorithm: String, hostKey: ByteArray) {
//            manager.hostdb.removeKnownHost(host, port, algorithm, hostKey);
        }

        override fun addServerHostKey(host: String, port: Int, algorithm: String, hostKey: ByteArray) {
//            manager.hostdb.saveKnownHost(host, port, algorithm, hostKey);
        }
    }

    private fun authenticate() {
        try {
            if (connection!!.authenticateWithNone(host!!.username)) {
                finishConnection()
                return
            }
        } catch (e: Exception) {
            Log.d(TAG, "Host does not support 'none' authentication.")
        }
        bridge!!.outputLine(manager!!.res!!.getString(R.string.terminal_auth))
        try {
            val pubkeyId = host!!.pubkeyId
            if (!pubkeysExhausted && pubkeyId != -2L &&
                    connection!!.isAuthMethodAvailable(host!!.username, AUTH_PUBLICKEY)) {

                // if explicit pubkey defined for this host, then prompt for password as needed
                // otherwise just try all in-memory keys held in terminalmanager
                if (pubkeyId == -1L) {
                    // try each of the in-memory keys
                    Log.e("HERE", "YAY")
                    bridge!!.outputLine(manager!!.res!!.getString(R.string.terminal_auth_pubkey_any))
                    for ((key, value) in manager!!.loadedKeypairs) {
                        if (value?.bean!!.isConfirmUse
                                && !promptForPubkeyUse(key)) continue
                        if (this.tryPublicKey(host!!.username, key,
                                        value.pair)) {
                            finishConnection()
                            break
                        }
                    }
                } else {
                    bridge!!.outputLine(manager!!.res!!.getString(R.string.terminal_auth_pubkey_specific))
                    // use a specific key for this host, as requested
//                    PubKeyBean pubkey = manager.pubkeydb.findPubkeyById(pubkeyId);

//                    if (pubkey == null)
//                        bridge.outputLine(manager.res.getString(R.string.terminal_auth_pubkey_invalid));
//                    else
//                    if (tryPublicKey(pubkey))
                    finishConnection()
                }
                pubkeysExhausted = true
            } else if (interactiveCanContinue &&
                    connection!!.isAuthMethodAvailable(host!!.username, AUTH_KEYBOARDINTERACTIVE)) {
                // this auth method will talk with us using InteractiveCallback interface
                // it blocks until authentication finishes
                bridge!!.outputLine(manager!!.res!!.getString(R.string.terminal_auth_ki))
                interactiveCanContinue = false
                if (connection!!.authenticateWithKeyboardInteractive(host!!.username, this)) {
                    finishConnection()
                } else {
                    bridge!!.outputLine(manager!!.res!!.getString(R.string.terminal_auth_ki_fail))
                }
            } else if (connection!!.isAuthMethodAvailable(host!!.username, AUTH_PASSWORD)) {
                bridge!!.outputLine(manager!!.res!!.getString(R.string.terminal_auth_pass))
                val password = bridge!!.promptHelper!!.requestStringPrompt(null,
                        manager!!.res!!.getString(R.string.prompt_password))
                if (password != null
                        && connection!!.authenticateWithPassword(host!!.username, password)) {
                    finishConnection()
                } else {
                    bridge!!.outputLine(manager!!.res!!.getString(R.string.terminal_auth_pass_fail))
                }
            } else {
                bridge!!.outputLine(manager!!.res!!.getString(R.string.terminal_auth_fail))
            }
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Connection went away while we were trying to authenticate", e)
            return
        } catch (e: Exception) {
            Log.e(TAG, "Problem during handleAuthentication()", e)
        }
    }

    /**
     * Attempt connection with given `pubkey`.
     *
     * @return `true` for successful authentication
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws IOException
     */
    @Throws(NoSuchAlgorithmException::class, InvalidKeySpecException::class, IOException::class)
    private fun tryPublicKey(pubkey: PubKeyBean): Boolean {
        var pair: KeyPair? = null
        if (manager!!.isKeyLoaded(pubkey.nickname)) {
            // load this key from memory if its already there
            Log.d(TAG, String.format("Found unlocked key '%s' already in-memory", pubkey.nickname))
            if (pubkey.isConfirmUse) {
                if (!promptForPubkeyUse(pubkey.nickname)) return false
            }
            pair = manager!!.getKey(pubkey.nickname)
        } else {
            // otherwise load key from database and prompt for password as needed
            var password: String? = null
            if (pubkey.isEncrypted) {
                password = bridge!!.promptHelper!!.requestStringPrompt(null,
                        manager!!.res!!.getString(R.string.prompt_pubkey_password, pubkey.nickname))

                // Something must have interrupted the prompt.
                if (password == null) return false
            }
            if (false) {
                // load specific key using pem format
                pair = PEMDecoder.decode(String(pubkey.getPrivateKey()!!, charset("UTF-8")).toCharArray(), password)
            } else {
                // load using internal generated format
                val privKey: PrivateKey
                privKey = try {
                    PubKeyUtils.decodePrivate(pubkey.getPrivateKey(),
                            pubkey.type, password)
                } catch (e: Exception) {
                    val message = String.format("Bad password for key '%s'. Authentication failed.", pubkey.nickname)
                    Log.e(TAG, message, e)
                    bridge!!.outputLine(message)
                    return false
                }
                val pubKey = PubKeyUtils.decodePublic(pubkey.getPublicKey(), pubkey.type)

                // convert key to trilead format
                pair = KeyPair(pubKey, privKey)
                Log.d(TAG, "Unlocked key " + PubKeyUtils.formatKey(pubKey))
            }
            Log.d(TAG, String.format("Unlocked key '%s'", pubkey.nickname))

            // save this key in memory
            manager!!.addKey(pubkey, pair)
        }
        return tryPublicKey(host!!.username, pubkey.nickname, pair)
    }

    @Throws(IOException::class)
    private fun tryPublicKey(username: String?, keyNickname: String?, pair: KeyPair?): Boolean {
        //bridge.outputLine(String.format("Attempting 'publickey' with key '%s' [%s]...", keyNickname, trileadKey.toString()));
        val success = connection!!.authenticateWithPublicKey(username, pair)
        if (!success) bridge!!.outputLine(manager!!.res!!.getString(R.string.terminal_auth_pubkey_fail, keyNickname))
        return success
    }

    /**
     * Internal method to request actual PTY terminal once we've finished
     * authentication. If called before authenticated, it will just fail.
     */
    private fun finishConnection() {
        authenticated = true
        Log.e("SHOULD BE AUTHENTICATED", "HERE")

//        for (PortForwardBean portForward : portForwards) {
//            try {
//                enablePortForward(portForward);
//                bridge.outputLine(manager.res.getString(R.string.terminal_enable_portfoward, portForward.getDescription()));
//            } catch (Exception e) {
//                Log.e(TAG, "Error setting up port forward during connect", e);
//            }
//        }
        if (!host!!.wantSession) {
            bridge!!.outputLine(manager!!.res!!.getString(R.string.terminal_no_session))
            bridge!!.onConnected()
            return
        }
        try {
            session = connection!!.openSession()
            session?.let {
                if (useAuthAgent != "no") it.requestAuthAgentForwarding(this)
                it.requestPTY(emulation, columns, rows, width, height, null)
                it.startShell()
                stdin = it.getStdin()
                stdout = it.getStdout()
                stderr = it.getStderr()

            }
            isSessionOpen = true
            bridge!!.onConnected()
        } catch (e1: IOException) {
            Log.e(TAG, "Problem while trying to create PTY in finishConnection()", e1)
        }
    }

    fun connect() {
        connection = Connection(host!!.hostname, host!!.port)
        connection!!.addConnectionMonitor(this)
        try {
            connection!!.setCompression(compression)
        } catch (e: IOException) {
            Log.e(TAG, "Could not enable compression!", e)
        }
        try {
            /* Uncomment when debugging SSH protocol:
			DebugLogger logger = new DebugLogger() {

				public void log(int level, String className, String message) {
					Log.d("SSH", message);
				}

			};
			Logger.enabled = true;
			Logger.logger = logger;
			*/
            val connectionInfo = connection!!.connect(HostKeyVerifier())
            isConnected = true
            bridge!!.outputLine(manager!!.res!!.getString(R.string.terminal_kex_algorithm,
                    connectionInfo.keyExchangeAlgorithm))
            if ((connectionInfo.clientToServerCryptoAlgorithm
                            == connectionInfo.serverToClientCryptoAlgorithm) && (connectionInfo.clientToServerMACAlgorithm
                            == connectionInfo.serverToClientMACAlgorithm)) {
                bridge!!.outputLine(manager!!.res!!.getString(R.string.terminal_using_algorithm,
                        connectionInfo.clientToServerCryptoAlgorithm,
                        connectionInfo.clientToServerMACAlgorithm))
            } else {
                bridge!!.outputLine(manager!!.res!!.getString(
                        R.string.terminal_using_c2s_algorithm,
                        connectionInfo.clientToServerCryptoAlgorithm,
                        connectionInfo.clientToServerMACAlgorithm))
                bridge!!.outputLine(manager!!.res!!.getString(
                        R.string.terminal_using_s2c_algorithm,
                        connectionInfo.serverToClientCryptoAlgorithm,
                        connectionInfo.serverToClientMACAlgorithm))
            }
        } catch (e: IOException) {
            Log.e(TAG, "Problem in SSH connection thread during authentication", e)

            // Display the reason in the text.
            var t = e.cause
            do {
                bridge!!.outputLine(t!!.message!!)
                t = t.cause
            } while (t != null)
            close()
            onDisconnect()
            return
        }
        try {
            // enter a loop to keep trying until authentication
            var tries = 0
            while (isConnected && !connection!!.isAuthenticationComplete && tries++ < AUTH_TRIES) {
                authenticate()

                // sleep to make sure we dont kill system
                Thread.sleep(1000)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Problem in SSH connection thread during authentication", e)
        }
    }

    fun close() {
        isConnected = false
        if (session != null) {
            session!!.close()
            session = null
        }
        if (connection != null) {
            connection!!.close()
            connection = null
        }
    }

    private fun onDisconnect() {
        bridge!!.dispatchDisconnect(false)
    }

    @Throws(IOException::class)
    fun flush() {
        if (stdin != null) stdin!!.flush()
    }

    @Throws(IOException::class)
    fun read(buffer: ByteArray?, start: Int, len: Int): Int {
        var bytesRead = 0
        if (session == null) return 0
        val newConditions = session!!.waitForCondition(conditions, 0)
        if (newConditions and ChannelCondition.STDOUT_DATA != 0) {
            bytesRead = stdout!!.read(buffer, start, len)
        }
        if (newConditions and ChannelCondition.STDERR_DATA != 0) {
            val discard = ByteArray(256)
            while (stderr!!.available() > 0) {
                stderr!!.read(discard)
            }
        }
        if (newConditions and ChannelCondition.EOF != 0) {
            close()
            onDisconnect()
            throw IOException("Remote end closed connection")
        }
        return bytesRead
    }

    @Throws(IOException::class)
    fun write(buffer: ByteArray?) {
        if (stdin != null) stdin!!.write(buffer)
    }

    @Throws(IOException::class)
    fun write(c: Int) {
        if (stdin != null) stdin!!.write(c)
    }

    val options: Map<String, String>
        get() {
            val options: MutableMap<String, String> = HashMap()
            options["compression"] = java.lang.Boolean.toString(compression)
            return options
        }

    fun setOptions(options: Map<String?, String?>) {
        if (options.containsKey("compression")) compression = java.lang.Boolean.parseBoolean(options["compression"])
    }

    override fun connectionLost(reason: Throwable) {
        onDisconnect()
    }

    fun canForwardPorts(): Boolean {
        return true
    }

    //    @Override
    //    public List<PortForwardBean> getPortForwards() {
    //        return portForwards;
    //    }
    //
    //    @Override
    //    public boolean addPortForward(PortForwardBean portForward) {
    //        return portForwards.add(portForward);
    //    }
    //
    //    @Override
    //    public boolean removePortForward(PortForwardBean portForward) {
    //        // Make sure we don't have a phantom forwarder.
    //        disablePortForward(portForward);
    //
    //        return portForwards.remove(portForward);
    //    }
    //
    //    @Override
    //    public boolean enablePortForward(PortForwardBean portForward) {
    //        if (!portForwards.contains(portForward)) {
    //            Log.e(TAG, "Attempt to enable port forward not in list");
    //            return false;
    //        }
    //
    //        if (!authenticated)
    //            return false;
    //
    //        if (HostDatabase.PORTFORWARD_LOCAL.equals(portForward.getType())) {
    //            LocalPortForwarder lpf = null;
    //            try {
    //                lpf = connection.createLocalPortForwarder(
    //                        new InetSocketAddress(InetAddress.getLocalHost(), portForward.getSourcePort()),
    //                        portForward.getDestAddr(), portForward.getDestPort());
    //            } catch (Exception e) {
    //                Log.e(TAG, "Could not create local port forward", e);
    //                return false;
    //            }
    //
    //            if (lpf == null) {
    //                Log.e(TAG, "returned LocalPortForwarder object is null");
    //                return false;
    //            }
    //
    //            portForward.setIdentifier(lpf);
    //            portForward.setEnabled(true);
    //            return true;
    //        } else if (HostDatabase.PORTFORWARD_REMOTE.equals(portForward.getType())) {
    //            try {
    //                connection.requestRemotePortForwarding("", portForward.getSourcePort(), portForward.getDestAddr(), portForward.getDestPort());
    //            } catch (Exception e) {
    //                Log.e(TAG, "Could not create remote port forward", e);
    //                return false;
    //            }
    //
    //            portForward.setEnabled(true);
    //            return true;
    //        } else if (HostDatabase.PORTFORWARD_DYNAMIC5.equals(portForward.getType())) {
    //            DynamicPortForwarder dpf = null;
    //
    //            try {
    //                dpf = connection.createDynamicPortForwarder(
    //                        new InetSocketAddress(InetAddress.getLocalHost(), portForward.getSourcePort()));
    //            } catch (Exception e) {
    //                Log.e(TAG, "Could not create dynamic port forward", e);
    //                return false;
    //            }
    //
    //            portForward.setIdentifier(dpf);
    //            portForward.setEnabled(true);
    //            return true;
    //        } else {
    //            // Unsupported type
    //            Log.e(TAG, String.format("attempt to forward unknown type %s", portForward.getType()));
    //            return false;
    //        }
    //    }
    //    @Override
    //    public boolean disablePortForward(PortForwardBean portForward) {
    //        if (!portForwards.contains(portForward)) {
    //            Log.e(TAG, "Attempt to disable port forward not in list");
    //            return false;
    //        }
    //
    //        if (!authenticated)
    //            return false;
    //
    //        if (HostDatabase.PORTFORWARD_LOCAL.equals(portForward.getType())) {
    //            LocalPortForwarder lpf = null;
    //            lpf = (LocalPortForwarder) portForward.getIdentifier();
    //
    //            if (!portForward.isEnabled() || lpf == null) {
    //                Log.d(TAG, String.format("Could not disable %s; it appears to be not enabled or have no handler", portForward.getNickname()));
    //                return false;
    //            }
    //
    //            portForward.setEnabled(false);
    //
    //            lpf.close();
    //
    //            return true;
    //        } else if (HostDatabase.PORTFORWARD_REMOTE.equals(portForward.getType())) {
    //            portForward.setEnabled(false);
    //
    //            try {
    //                connection.cancelRemotePortForwarding(portForward.getSourcePort());
    //            } catch (IOException e) {
    //                Log.e(TAG, "Could not stop remote port forwarding, setting enabled to false", e);
    //                return false;
    //            }
    //
    //            return true;
    //        } else if (HostDatabase.PORTFORWARD_DYNAMIC5.equals(portForward.getType())) {
    //            DynamicPortForwarder dpf = null;
    //            dpf = (DynamicPortForwarder) portForward.getIdentifier();
    //
    //            if (!portForward.isEnabled() || dpf == null) {
    //                Log.d(TAG, String.format("Could not disable %s; it appears to be not enabled or have no handler", portForward.getNickname()));
    //                return false;
    //            }
    //
    //            portForward.setEnabled(false);
    //
    //            dpf.close();
    //
    //            return true;
    //        } else {
    //            // Unsupported type
    //            Log.e(TAG, String.format("attempt to forward unknown type %s", portForward.getType()));
    //            return false;
    //        }
    //    }
    fun setDimensions(columns: Int, rows: Int, width: Int, height: Int) {
        this.columns = columns
        this.rows = rows
        if (isSessionOpen) {
            try {
                session!!.resizePTY(columns, rows, width, height)
            } catch (e: IOException) {
                Log.e(TAG, "Couldn't send resize PTY packet", e)
            }
        }
    }

    fun getDefaultNickname(username: String?, hostname: String?, port: Int): String {
        return if (port == defaultPort) {
            String.format(Locale.US, "%s@%s", username, hostname)
        } else {
            String.format(Locale.US, "%s@%s:%d", username, hostname, port)
        }
    }

    /**
     * Handle challenges from keyboard-interactive authentication mode.
     */
    override fun replyToChallenge(name: String, instruction: String, numPrompts: Int, prompt: Array<String>, echo: BooleanArray): Array<String?> {
        interactiveCanContinue = true
        val responses = arrayOfNulls<String>(numPrompts)
        for (i in 0 until numPrompts) {
            // request response from user for each prompt
            responses[i] = bridge!!.promptHelper!!.requestStringPrompt(instruction, prompt[i])
        }
        return responses
    }

    fun createHost(uri: Uri): HostBean {
        val host = HostBean()
        host.protocol = protocolName
        host.hostname = uri.host
        var port = uri.port
        if (port < 0) port = defaultPort
        host.port = port
        host.username = uri.userInfo
        val nickname = uri.fragment
        if (nickname == null || nickname.length == 0) {
            host.nickname = getDefaultNickname(host.username,
                    host.hostname, host.port)
        } else {
            host.nickname = uri.fragment
        }
        return host
    }

    fun setCompression(compression: Boolean) {
        this.compression = compression
    }

    fun setUseAuthAgent(useAuthAgent: String) {
        this.useAuthAgent = useAuthAgent
    }

    override fun retrieveIdentities(): Map<String, ByteArray> {
        val pubKeys: MutableMap<String, ByteArray> = HashMap(manager!!.loadedKeypairs.size)
        for ((key, value) in manager!!.loadedKeypairs) {
            val pair = value?.pair
            try {
                val privKey = pair!!.private
                if (privKey is RSAPrivateKey) {
                    val pubkey = pair.public as RSAPublicKey
                    pubKeys[key] = RSASHA1Verify.encodeSSHRSAPublicKey(pubkey)
                } else if (privKey is DSAPrivateKey) {
                    val pubkey = pair.public as DSAPublicKey
                    pubKeys[key] = DSASHA1Verify.encodeSSHDSAPublicKey(pubkey)
                } else if (privKey is ECPrivateKey) {
                    val pubkey = pair.public as ECPublicKey
                    pubKeys[key] = ECDSASHA2Verify.encodeSSHECDSAPublicKey(pubkey)
                } else if (privKey is EdDSAPrivateKey) {
                    val pubkey = pair.public as EdDSAPublicKey
                    pubKeys[key] = Ed25519Verify.encodeSSHEd25519PublicKey(pubkey)
                } else continue
            } catch (e: IOException) {
                continue
            }
        }
        return pubKeys
    }

    override fun getKeyPair(publicKey: ByteArray): KeyPair? {
        val nickname = manager!!.getKeyNickname(publicKey) ?: return null
        if (useAuthAgent == "no") {
            Log.e(TAG, "")
            return null
        } else if (useAuthAgent == "confirm" ||
                manager!!.loadedKeypairs[nickname]!!.bean!!.isConfirmUse) {
            if (!promptForPubkeyUse(nickname)) return null
        }
        return manager!!.getKey(nickname)!!
    }

    private fun promptForPubkeyUse(nickname: String?): Boolean {
        val result = bridge!!.promptHelper!!.requestBooleanPrompt(null,
                manager!!.res!!.getString(R.string.prompt_allow_agent_to_use_key, nickname))
        return result!!
    }

    override fun addIdentity(pair: KeyPair, comment: String, confirmUse: Boolean, lifetime: Int): Boolean {
        val pubkey = PubKeyBean()
        //		pubkey.setType(PubkeyDatabase.KEY_TYPE_IMPORTED);
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

    /* (non-Javadoc)
     * @see org.connectbot.transport.AbsTransport#usesNetwork()
     */
    fun usesNetwork(): Boolean {
        return true
    }
}