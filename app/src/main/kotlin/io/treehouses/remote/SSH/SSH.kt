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

import android.util.Log
import com.trilead.ssh2.*
import com.trilead.ssh2.crypto.Base64
import com.trilead.ssh2.signature.DSASHA1Verify
import com.trilead.ssh2.signature.ECDSASHA2Verify
import com.trilead.ssh2.signature.Ed25519Verify
import com.trilead.ssh2.signature.RSASHA1Verify
import io.treehouses.remote.Fragments.DialogFragments.EditHostDialog.Companion.NO_KEY
import io.treehouses.remote.R
import io.treehouses.remote.SSH.Ed25519Provider.Companion.insertIfNeeded
import io.treehouses.remote.SSH.Terminal.TerminalBridge
import io.treehouses.remote.SSH.Terminal.TerminalManager
import io.treehouses.remote.SSH.beans.HostBean
import io.treehouses.remote.SSH.beans.PubKeyBean
import io.treehouses.remote.utils.KeyUtils
import net.i2p.crypto.eddsa.EdDSAPrivateKey
import net.i2p.crypto.eddsa.EdDSAPublicKey
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyPair
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.PublicKey
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

//        fun getUri(input: String?): Uri? {
//            val matcher = hostmask.matcher(input)
//            if (!matcher.matches()) return null
//            val sb = StringBuilder()
//            sb.append(protocolName)
//                    .append("://")
//                    .append(Uri.encode(matcher.group(1)))
//                    .append('@')
//                    .append(Uri.encode(matcher.group(2)))
//            val portString = matcher.group(6)
//            var port = defaultPort
//            if (portString != null) {
//                try {
//                    port = portString.toInt()
//                    if (port < 1 || port > 65535) {
//                        port = defaultPort
//                    }
//                } catch (nfe: NumberFormatException) {
//                    // Keep the default port
//                }
//            }
//            if (port != defaultPort) {
//                sb.append(':')
//                        .append(port)
//            }
//            sb.append("/#")
//                    .append(Uri.encode(input))
//            return Uri.parse(sb.toString())
//        }

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
            val hosts = KeyUtils.getAllKnownHosts(manager!!.applicationContext)
            Log.e("ALL HOSTS", hosts.toString())
            val matchName = String.format(Locale.US, "%s:%d", hostname, port)
            val fingerprint = KnownHosts.createHexFingerprint(serverHostKeyAlgorithm, serverHostKey)
            val algorithmName: String = if ("ssh-rsa" == serverHostKeyAlgorithm) "RSA" else if ("ssh-dss" == serverHostKeyAlgorithm) "DSA" else if (serverHostKeyAlgorithm.startsWith("ecdsa-")) "EC" else if ("ssh-ed25519" == serverHostKeyAlgorithm) "Ed25519" else serverHostKeyAlgorithm
            return when (hosts.verifyHostkey(matchName, serverHostKeyAlgorithm, serverHostKey)) {
                KnownHosts.HOSTKEY_IS_OK -> onKeyOk(algorithmName, fingerprint)
                KnownHosts.HOSTKEY_IS_NEW -> {
                    bridge!!.outputLine(manager!!.res!!.getString(R.string.host_authenticity_warning, hostname))
                    bridge!!.outputLine(manager!!.res!!.getString(R.string.host_fingerprint, algorithmName, fingerprint))
                    Log.e("HOST KEY", Arrays.toString(serverHostKey))
                    //                    if (result) {
//                        // save this key in known database
//                        manager.hostdb.saveKnownHost(hostname, port, serverHostKeyAlgorithm, serverHostKey);
//                    }
                    promptKeys(hostname, port, serverHostKeyAlgorithm, serverHostKey)
                }
                KnownHosts.HOSTKEY_HAS_CHANGED -> {
                    onHostKeyChanged(algorithmName, fingerprint)
                    promptKeys(hostname, port, serverHostKeyAlgorithm, serverHostKey)
                }
                else -> onKeyFailed()
            }
        }

        private fun onKeyOk(algorithmName: String, fingerprint: String): Boolean {
            bridge!!.outputLine(manager!!.res!!.getString(R.string.terminal_sucess, algorithmName, fingerprint))
            return true
        }

        private fun onKeyFailed(): Boolean {
            bridge!!.outputLine(manager!!.res!!.getString(R.string.terminal_failed))
            return false
        }

        private fun onHostKeyChanged(algorithmName: String, fingerprint: String) {
            val header = String.format("@   %s   @",
                    manager!!.res!!.getString(R.string.host_verification_failure_warning_header))
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
            // Users have no way to delete keys, so we'll prompt them for now.
            if (continueConnecting()) {
                KeyUtils.saveKnownHost(manager!!.applicationContext, "$hostname:$port", serverHostKeyAlgorithm, serverHostKey)
                return true
            } else return false
        }

        private fun continueConnecting() : Boolean = bridge!!.promptHelper!!.requestBooleanPrompt(null, manager!!.res!!.getString(R.string.prompt_continue_connecting))!!

        override fun getKnownKeyAlgorithmsForHost(host: String, port: Int): List<String> {
            val hostBean = KeyUtils.getKnownHost(manager!!.applicationContext, "$host:$port")
            return if (hostBean == null) ArrayList() else arrayListOf(hostBean.algorithm)
        }

        override fun removeServerHostKey(host: String, port: Int, algorithm: String, hostKey: ByteArray) {
            KeyUtils.removeKnownHost(manager!!.applicationContext, "$host:$port", algorithm, hostKey)
            Log.e("REMOVING HOST KEY", "For: $host:$port with algorithm: $algorithm")
        }

        override fun addServerHostKey(host: String, port: Int, algorithm: String, hostKey: ByteArray) {
            KeyUtils.saveKnownHost(manager!!.applicationContext, "$host:$port", algorithm, hostKey)
        }
    }

    private fun tryNoneAuthentication() {
        try {
            if (connection!!.authenticateWithNone(host!!.username)) {
                finishConnection()
                return
            }
        } catch (e: Exception) {
            Log.d(TAG, "Host does not support 'none' authentication.")
        }
    }

    private fun tryInMemKeys(pubkeyId: String) {
        // try each of the in-memory keys
        Log.e("HERE", "YAY: $pubkeyId")
        outputLine(R.string.terminal_auth_pubkey_any)
        for ((key, value) in manager!!.loadedKeypairs) {
            if (value?.bean!!.isConfirmUse && !promptForPubkeyUse(key)) continue
            if (this.tryPublicKey(host!!.username, key, value.pair)) {
                finishConnection()
                break
            }
        }
    }

    private fun promptForPw(pubkeyId: String) {
        outputLine(R.string.terminal_auth_pubkey_specific)
        // use a specific key for this host, as requested
        val pubkey = KeyUtils.getKey(manager!!.applicationContext, pubkeyId)

        if (pubkey == null) bridge?.outputLine(manager!!.res!!.getString(R.string.terminal_auth_pubkey_invalid));
        else if (tryPublicKey(pubkey)) finishConnection()
    }

    private fun tryInteractiveAuth() {
        // this auth method will talk with us using InteractiveCallback interface
        // it blocks until authentication finishes
        outputLine(R.string.terminal_auth_ki)
        interactiveCanContinue = false
        if (connection!!.authenticateWithKeyboardInteractive(host!!.username, this)) finishConnection()
        else outputLine(R.string.terminal_auth_ki_fail)
    }

    private fun authenticate() {
        tryNoneAuthentication()
        outputLine(R.string.terminal_auth)
        try {
            val pubkeyId = host!!.keyName
            if (!pubkeysExhausted &&
                    connection!!.isAuthMethodAvailable(host!!.username, AUTH_PUBLICKEY)) {

                // if explicit pubkey defined for this host, then prompt for password as needed
                // otherwise just try all in-memory keys held in terminalmanager
                if (pubkeyId.isEmpty() || pubkeyId == NO_KEY) tryInMemKeys(pubkeyId)
                else promptForPw(pubkeyId)
                pubkeysExhausted = true
            } else if (interactiveCanContinue && connection!!.isAuthMethodAvailable(host!!.username, AUTH_KEYBOARDINTERACTIVE)) {
                tryInteractiveAuth()
            } else if (connection!!.isAuthMethodAvailable(host!!.username, AUTH_PASSWORD)) {
                outputLine(R.string.terminal_auth_pass)
                val password = bridge!!.promptHelper!!.requestStringPrompt(null, manager!!.res!!.getString(R.string.prompt_password))
                if (password != null && connection!!.authenticateWithPassword(host!!.username, password)) finishConnection()
                else outputLine(R.string.terminal_auth_pass_fail)
            } else outputLine(R.string.terminal_auth_pass_fail)
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Connection went away while we were trying to authenticate", e)
            return
        } catch (e: Exception) {
            Log.e(TAG, "Problem during handleAuthentication()", e)
        }
    }

    private fun outputLine(stringResource: Int) {
        bridge!!.outputLine(manager!!.res!!.getString(stringResource))
    }

    /**
     * Attempt connection with given `pubkey`.
     *
     * @return `true` for successful authentication
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws IOException
     */
//    @Throws(NoSuchAlgorithmException::class, InvalidKeySpecException::class, IOException::class)
    private fun tryPublicKey(pubkey: PubKeyBean): Boolean {
        var pair: KeyPair?

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
                password = bridge!!.promptHelper!!.requestStringPrompt(null, manager!!.res!!.getString(R.string.prompt_pubkey_password, pubkey.nickname))

                // Something must have interrupted the prompt.
                if (password == null) return false
            }
            // load using internal generated format
            val privKey: PrivateKey
            privKey = try {
                PubKeyUtils.decodePrivate(pubkey.privateKey!!, pubkey.type, password)
            } catch (e: Exception) {
                return onBadPassword(pubkey, e)
            }
            val pubKey = PubKeyUtils.decodePublic(pubkey.publicKey!!, pubkey.type)

            pair = convertAndSaveKey(pubKey, privKey, pubkey)
        }
        return tryPublicKey(host!!.username, pubkey.nickname, pair)
    }

    private fun onBadPassword(pubkey: PubKeyBean, e: Exception): Boolean {
        val message = String.format("Bad password for key '%s'. Authentication failed.", pubkey.nickname)
        Log.e(TAG, message, e)
        bridge!!.outputLine(message)
        return false
    }

    private fun convertAndSaveKey(pubKey: PublicKey, privKey: PrivateKey, pubkey: PubKeyBean): KeyPair {
        // convert key to trilead format
        val pair = KeyPair(pubKey, privKey)
        Log.d(TAG, "Unlocked key " + PubKeyUtils.formatKey(pubKey))
        Log.d(TAG, String.format("Unlocked key '%s'", pubkey.nickname))

        // save this key in memory
        manager!!.addKey(pubkey, pair)
        return pair
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

    private fun displayAlgorithms() {
        val connectionInfo = connection!!.connect(HostKeyVerifier())
        isConnected = true
        bridge!!.outputLine(manager!!.res!!.getString(R.string.terminal_kex_algorithm,
                connectionInfo.keyExchangeAlgorithm))
        if ((connectionInfo.clientToServerCryptoAlgorithm
                        == connectionInfo.serverToClientCryptoAlgorithm) && (connectionInfo.clientToServerMACAlgorithm
                        == connectionInfo.serverToClientMACAlgorithm)) {
            outputCryptoAlgo(connectionInfo, R.string.terminal_using_algorithm)
        } else {
            outputCryptoAlgo(connectionInfo, R.string.terminal_using_c2s_algorithm)
            outputCryptoAlgo(connectionInfo, R.string.terminal_using_s2c_algorithm)
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
            displayAlgorithms()
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
        tryAuthentication()
    }

    private fun tryAuthentication() {
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

    private fun outputCryptoAlgo(connectionInfo: ConnectionInfo, stringRes: Int) {
        bridge!!.outputLine(manager!!.res!!.getString(stringRes,
                connectionInfo.clientToServerCryptoAlgorithm,
                connectionInfo.clientToServerMACAlgorithm))
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

//    val options: Map<String, String>
//        get() {
//            val options: MutableMap<String, String> = HashMap()
//            options["compression"] = java.lang.Boolean.toString(compression)
//            return options
//        }
//
//    fun setOptions(options: Map<String?, String?>) {
//        if (options.containsKey("compression")) compression = java.lang.Boolean.parseBoolean(options["compression"])
//    }

    override fun connectionLost(reason: Throwable) {
        onDisconnect()
    }

//    fun canForwardPorts(): Boolean {
//        return true
//    }

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

//    fun createHost(uri: Uri): HostBean {
//        val host = HostBean()
//        host.protocol = protocolName
//        host.hostname = uri.host
//        var port = uri.port
//        if (port < 0) port = defaultPort
//        host.port = port
//        host.username = uri.userInfo
//        val nickname = uri.fragment
//        if (nickname == null || nickname.isEmpty()) {
//            host.nickname = getDefaultNickname(host.username,
//                    host.hostname, host.port)
//        } else {
//            host.nickname = uri.fragment
//        }
//        return host
//    }

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
                when (pair?.private) {
                    is RSAPrivateKey -> pubKeys[key] = RSASHA1Verify.encodeSSHRSAPublicKey(pair.public as RSAPublicKey)
                    is DSAPrivateKey -> pubKeys[key] = DSASHA1Verify.encodeSSHDSAPublicKey(pair.public as DSAPublicKey)
                    is ECPrivateKey -> pubKeys[key] = ECDSASHA2Verify.encodeSSHECDSAPublicKey(pair.public as ECPublicKey)
                    is EdDSAPrivateKey -> pubKeys[key] = Ed25519Verify.encodeSSHEd25519PublicKey(pair.public as EdDSAPublicKey)
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