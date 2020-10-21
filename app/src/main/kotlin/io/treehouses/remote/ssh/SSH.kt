package io.treehouses.remote.ssh

import com.trilead.ssh2.ChannelCondition
import com.trilead.ssh2.Connection
import com.trilead.ssh2.ConnectionInfo
import io.treehouses.remote.fragments.dialogFragments.EditHostDialog
import io.treehouses.remote.R
import io.treehouses.remote.ssh.terminal.TerminalBridge
import io.treehouses.remote.ssh.terminal.TerminalManager
import io.treehouses.remote.ssh.beans.HostBean
import io.treehouses.remote.ssh.beans.PubKeyBean
import io.treehouses.remote.bases.BaseSSH
import io.treehouses.remote.utils.KeyUtils
import io.treehouses.remote.utils.logD
import io.treehouses.remote.utils.logE
import java.io.IOException
import java.security.KeyPair
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.InvalidKeySpecException

class SSH: BaseSSH {
    private var compression = false

    constructor() : super()

    /**
     * @param host
     * @param bridge
     * @param manager
     */
    constructor(host: HostBean?, bridge: TerminalBridge?, manager: TerminalManager?) : super() {
        this.host = host
        this.bridge = bridge
        this.manager = manager
    }

    fun connect() {
        connection = Connection(host!!.hostname, host!!.port)
        connection!!.addConnectionMonitor(this)
        try {
            connection!!.setCompression(compression)
        } catch (e: IOException) {
            logE("Could not enable compression! $e")
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
            logE("Problem in SSH connection thread during authentication $e")

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

        if (manager!!.loadedKeypairs.containsKey(pubkey.nickname)) {
            // load this key from memory if its already there
            logD("${String.format("Found unlocked key '%s' already in-memory", pubkey.nickname)}")
            if (pubkey.isConfirmUse) {
                if (!promptForPubkeyUse(pubkey.nickname)) return false
            }
            pair = manager!!.getKey(pubkey.nickname)
        } else {
            // otherwise load key from database and prompt for password as needed
            var password: String? = null

            if (pubkey.isEncrypted) {
                password = bridge!!.promptHelper!!.requestPrompt<String>(null, manager!!.res!!.getString(R.string.prompt_pubkey_password, pubkey.nickname), isBool = false)

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
            val pubKey = PubKeyUtils.decodeKey(pubkey.publicKey!!, pubkey.type, "public")

            pair = convertAndSaveKey(pubKey as PublicKey, privKey, pubkey)
        }
        return tryPublicKey(host!!.username, pubkey.nickname, pair)
    }

    private fun onBadPassword(pubkey: PubKeyBean, e: Exception): Boolean {
        val message = String.format("Bad password for key '%s'. Authentication failed.", pubkey.nickname)
        logD("message $e")
        bridge!!.outputLine(message)
        return false
    }

    private fun convertAndSaveKey(pubKey: PublicKey, privKey: PrivateKey, pubkey: PubKeyBean): KeyPair {
        // convert key to trilead format
        val pair = KeyPair(pubKey, privKey)
        logD("Unlocked key ${PubKeyUtils.formatKey(pubKey)}")
        logD("${String.format("Unlocked key '%s'", pubkey.nickname)}")

        // save this key in memory
        manager!!.addKey(pubkey, pair)
        return pair
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
                if (pubkeyId.isEmpty() || pubkeyId == EditHostDialog.NO_KEY) tryInMemKeys(pubkeyId)
                else promptForPw(pubkeyId)
                pubkeysExhausted = true
            } else if (interactiveCanContinue && connection!!.isAuthMethodAvailable(host!!.username, AUTH_KEYBOARDINTERACTIVE)) {
                tryInteractiveAuth()
            } else if (connection!!.isAuthMethodAvailable(host!!.username, AUTH_PASSWORD)) {
                outputLine(R.string.terminal_auth_pass)
                val password = bridge!!.promptHelper!!.requestPrompt<String>(null, manager!!.res!!.getString(R.string.prompt_password), isBool = false)
                if (password != null && connection!!.authenticateWithPassword(host!!.username, password)) finishConnection()
                else outputLine(R.string.terminal_auth_pass_fail)
            } else outputLine(R.string.terminal_auth_pass_fail)
        } catch (e: IllegalStateException) {
            logE("Connection went away while we were trying to authenticate $e")
            return
        } catch (e: Exception) {
            logE("Problem during handleAuthentication() $e")
        }
    }

    private fun tryInteractiveAuth() {
        // this auth method will talk with us using InteractiveCallback interface
        // it blocks until authentication finishes
        outputLine(R.string.terminal_auth_ki)
        interactiveCanContinue = false
        if (connection!!.authenticateWithKeyboardInteractive(host!!.username, this)) finishConnection()
        else outputLine(R.string.terminal_auth_ki_fail)
    }

    private fun tryNoneAuthentication() {
        try {
            if (connection!!.authenticateWithNone(host!!.username)) {
                finishConnection()
                return
            }
        } catch (e: Exception) {
            logE("Host does not support 'none' authentication.")
        }
    }

    private fun tryInMemKeys(pubkeyId: String) {
        // try each of the in-memory keys
        logD("HERE YAY: $pubkeyId")
        outputLine(R.string.terminal_auth_pubkey_any)
        for ((key, value) in manager!!.loadedKeypairs) {
            if (value?.bean!!.isConfirmUse && !promptForPubkeyUse(key)) continue
            if (this.tryPublicKey(host!!.username, key, value.pair)) {
                finishConnection()
                break
            }
        }
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
            logE("Problem in SSH connection thread during authentication $e")
        }
    }

    private fun promptForPw(pubkeyId: String) {
        outputLine(R.string.terminal_auth_pubkey_specific)
        // use a specific key for this host, as requested
        val pubkey = KeyUtils.getKey(manager!!.applicationContext, pubkeyId)

        if (pubkey == null) bridge?.outputLine(manager!!.res!!.getString(R.string.terminal_auth_pubkey_invalid));
        else if (tryPublicKey(pubkey)) finishConnection()
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

    private fun outputCryptoAlgo(connectionInfo: ConnectionInfo, stringRes: Int) {
        bridge!!.outputLine(manager!!.res!!.getString(stringRes,
                connectionInfo.clientToServerCryptoAlgorithm,
                connectionInfo.clientToServerMACAlgorithm))
    }

    fun setCompression(compression: Boolean) {
        this.compression = compression
    }

    /* (non-Javadoc)
     * @see org.connectbot.transport.AbsTransport#usesNetwork()
     */
    fun usesNetwork(): Boolean {
        return true
    }

    fun setDimensions(columns: Int, rows: Int, width: Int, height: Int) {
        this.columns = columns
        this.rows = rows
        if (isSessionOpen) {
            try {
                session!!.resizePTY(columns, rows, width, height)
            } catch (e: IOException) {
                logE("Couldn't send resize PTY packet $e")
            }
        }
    }

    @Throws(IOException::class)
    fun write(buffer: ByteArray?) {
        if (stdin != null) stdin!!.write(buffer)
    }

    @Throws(IOException::class)
    fun write(c: Int) {
        if (stdin != null) stdin!!.write(c)
    }
}