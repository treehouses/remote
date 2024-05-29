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
package io.treehouses.remote.ssh.terminal

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.res.Configuration
import android.content.res.Resources
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.os.Vibrator
import androidx.preference.PreferenceManager
import io.treehouses.remote.PreferenceConstants
import io.treehouses.remote.ssh.PubKeyUtils
import io.treehouses.remote.ssh.beans.HostBean
import io.treehouses.remote.ssh.beans.PubKeyBean
import io.treehouses.remote.ssh.interfaces.BridgeDisconnectedListener
import io.treehouses.remote.ssh.interfaces.OnHostStatusChangedListener
import io.treehouses.remote.utils.SaveUtils
import java.io.IOException
import java.lang.ref.WeakReference
import java.security.KeyPair
import java.util.*

/**
 * Manager for SSH connections that runs as a service. This service holds a list
 * of currently connected SSH bridges that are ready for connection up to a GUI
 * if needed.
 *
 * @author jsharkey
 */
class TerminalManager : Service(), BridgeDisconnectedListener, OnSharedPreferenceChangeListener {
    val bridges = ArrayList<TerminalBridge>()
    var mHostBridgeMap: MutableMap<HostBean, WeakReference<TerminalBridge>> = HashMap()
    var mNicknameBridgeMap: MutableMap<String, WeakReference<TerminalBridge>> = HashMap()
    var defaultBridge: TerminalBridge? = null
    var disconnected: MutableList<HostBean> = ArrayList()
    var disconnectListener: BridgeDisconnectedListener? = null
    val hostStatusChangedListeners = ArrayList<OnHostStatusChangedListener>()

    @JvmField
    var loadedKeypairs: MutableMap<String, KeyHolder?> = HashMap()

    @JvmField
    var res: Resources? = null

    //	public HostStorage hostdb;
    //	public ColorStorage colordb;
    //	public PubkeyDatabase pubkeydb;
    var prefs: SharedPreferences? = null
    private val binder: IBinder = TerminalBinder()

    //	private ConnectivityReceiver connectivityManager;
    private var pubkeyTimer: Timer? = null
    private var idleTimer: Timer? = null
    private val IDLE_TIMEOUT: Long = 300000 // 5 minutes
    private var vibrator: Vibrator? = null

    @Volatile
    private var wantKeyVibration = false

    /**
     * Allow [TerminalBridge] to resize when the parent has changed.
     *
     * @param resizeAllowed
     */
    var isResizeAllowed = true
    private var savingKeys = false
    protected var mPendingReconnect: MutableList<WeakReference<TerminalBridge>> = ArrayList()
    var hardKeyboardHidden = false
    override fun onCreate() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this); prefs!!.registerOnSharedPreferenceChangeListener(this)
        res = resources
        pubkeyTimer = Timer("pubkeyTimer", true)

//		hostdb = HostDatabase.get(this);
//		colordb = HostDatabase.get(this);
//		pubkeydb = PubkeyDatabase.get(this);

        // load all marked pubkeys into memory
        savingKeys = prefs!!.getBoolean(PreferenceConstants.MEMKEYS, true)
        //		List<PubkeyBean> pubkeys = pubkeydb.getAllStartPubkeys();

//		for (PubkeyBean pubkey : pubkeys) {
//			try {
//				KeyPair pair = PubkeyUtils.convertToKeyPair(pubkey, null);
//				addKey(pubkey, pair);
//			} catch (Exception e) {
//				Log.d(TAG, String.format("Problem adding key '%s' to in-memory cache", pubkey.getNickname()), e);
//			}
//		}
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        wantKeyVibration = prefs!!.getBoolean(PreferenceConstants.BUMPY_ARROWS, true)

//		enableMediaPlayer();
        hardKeyboardHidden = res!!.getConfiguration().hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES
        //val lockingWifi = prefs!!.getBoolean(PreferenceConstants.WIFI_LOCK, true)

//		connectivityManager = new ConnectivityReceiver(this, lockingWifi);
    }

    override fun onDestroy() {
        disconnectAll(true, false)

//		hostdb = null;
//		pubkeydb = null;
        synchronized(this) {
            if (idleTimer != null) idleTimer!!.cancel()
            if (pubkeyTimer != null) pubkeyTimer!!.cancel()
        }

//		connectivityManager.cleanup();
//
//		ConnectionNotifier.getInstance().hideRunningNotification(this);

//		disableMediaPlayer();
    }

    /**
     * Disconnect all currently connected bridges.
     */
    fun disconnectAll(immediate: Boolean, excludeLocal: Boolean) {
        var tmpBridges: Array<TerminalBridge>? = null
        synchronized(bridges) { if (bridges.size > 0) tmpBridges = bridges.toTypedArray() }
        if (tmpBridges != null) {
            // disconnect and dispose of any existing bridges
            for (i in tmpBridges!!.indices) {
                if (excludeLocal && !tmpBridges!![i].isUsingNetwork) continue
                tmpBridges!![i].dispatchDisconnect(immediate)
            }
        }
    }

    /**
     * Open a new SSH session using the given parameters.
     */
    @Throws(IllegalArgumentException::class, IOException::class)
    private fun openConnection(host: HostBean): TerminalBridge {
        // throw exception if terminal already open
        require(mHostBridgeMap[host]?.get() == null) { "Connection already open for that nickname" }
        val bridge = TerminalBridge(this, host)
        bridge.setOnDisconnectedListener(this); bridge.startConnection()
        synchronized(bridges) {
            bridges.add(bridge)
            val wr = WeakReference(bridge)
            mHostBridgeMap[bridge.host!!] = wr
            mNicknameBridgeMap.put(bridge.host!!.nickname!!, wr)
        }
        synchronized(disconnected) { disconnected.remove(bridge.host) }

//		if (bridge.isUsingNetwork()) {
//			connectivityManager.incRef();
//		}
//
//		if (prefs.getBoolean(PreferenceConstants.CONNECTION_PERSIST, true)) {
//			ConnectionNotifier.getInstance().showRunningNotification(this);
//		}

        // also update database with new connected time
//		touchHost(host);
        notifyHostStatusChanged()
        return bridge
    }

    val emulation: String?
        get() = prefs!!.getString(PreferenceConstants.EMULATION, "xterm-256color")

    val scrollback: Int
        get() {
            var scrollback = 140
            try {
                scrollback = prefs!!.getString(PreferenceConstants.SCROLLBACK, "140")!!.toInt()
            } catch (e: Exception) { }
            return scrollback
        }

    /**
     * Open a new connection by reading parameters from the given URI. Follows
     * format specified by an individual transport.
     */
    @Throws(Exception::class)
    fun openConnection(uri: Uri?): TerminalBridge {
//		HostBean host = TransportFactory.findHost(hostdb, uri);
        var host = if (uri == null) null else SaveUtils.getHost(applicationContext, uri.toString())
        if (host == null) host = HostBean().apply { setHostFromUri(uri) }
        return openConnection(host)
    }
    //	/**
    //	 * Update the last-connected value for the given nickname by passing through
    //	 * to {@link HostDatabase}.
    //	 */
    //	private void touchHost(HostBean host) {
    //		hostdb.touchHost(host);
    //	}

    /**
     * Called by child bridge when somehow it's been disconnected.
     */
    override fun onDisconnected(bridge: TerminalBridge) {
        //var shouldHideRunningNotification = false
        synchronized(bridges) {
            // remove this bridge from our list
            bridges.remove(bridge); mHostBridgeMap.remove(bridge.host); mNicknameBridgeMap.remove(bridge.host!!.nickname)
//            if (bridge.isUsingNetwork) {
////				connectivityManager.decRef();
//            }
            if (bridges.isEmpty() && mPendingReconnect.isEmpty()) //shouldHideRunningNotification = true
            // pass notification back up to gui
                if (disconnectListener != null) disconnectListener!!.onDisconnected(bridge)
        }
        synchronized(disconnected) { disconnected.add(bridge.host!!) }
        notifyHostStatusChanged()

//		if (shouldHideRunningNotification) {
//			ConnectionNotifier.getInstance().hideRunningNotification(this);
//		}
    }

    @JvmOverloads
    fun addKey(pubkey: PubKeyBean, pair: KeyPair?, force: Boolean = false) {
        if (!savingKeys && !force) return
        loadedKeypairs.remove(pubkey.nickname)
        val sshPubKey = PubKeyUtils.extractOpenSSHPublic(pair)
        val keyHolder = KeyHolder()
        keyHolder.bean = pubkey; keyHolder.pair = pair; keyHolder.openSSHPubkey = sshPubKey
        loadedKeypairs[pubkey.nickname] = keyHolder
        if (pubkey.lifetime > 0) {
            val nickname = pubkey.nickname
            pubkeyTimer!!.schedule(object : TimerTask() {
                override fun run() {
                    loadedKeypairs.remove(pubkey.nickname)
                }
            }, pubkey.lifetime * 1000.toLong())
        }
    }

    fun removeKey(publicKey: ByteArray?): Boolean {
        var nickname: String? = null
        for ((key, value) in loadedKeypairs) {
            if (Arrays.equals(value!!.openSSHPubkey, publicKey)) {
                nickname = key
                break
            }
        }
        return if (nickname != null) {
            loadedKeypairs.remove(nickname) != null
        } else false
    }

    fun getKey(nickname: String?): KeyPair? {
        return if (loadedKeypairs.containsKey(nickname)) {
            val keyHolder = loadedKeypairs[nickname]
            keyHolder!!.pair
        } else null
    }

    fun getKeyNickname(publicKey: ByteArray?): String? {
        for ((key, value) in loadedKeypairs) {
            if (Arrays.equals(value!!.openSSHPubkey, publicKey)) return key
        }
        return null
    }

    inner class TerminalBinder : Binder() {
        val service: TerminalManager
            get() = this@TerminalManager
    }

    override fun onBind(intent: Intent): IBinder? {
        keepServiceAlive()
        isResizeAllowed = true
        return binder
    }

    /**
     * Make sure we stay running to maintain the bridges. Later [.stopNow] should be called to stop the service.
     */
    private fun keepServiceAlive() {
        if (idleTimer != null) {
            idleTimer!!.cancel()
            idleTimer = null
        }
        startService(Intent(this, TerminalManager::class.java))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        /*
         * We want this service to continue running until it is explicitly
         * stopped, so return sticky.
         */
        return START_STICKY
    }

    override fun onRebind(intent: Intent) {
        super.onRebind(intent)
        keepServiceAlive()
        isResizeAllowed = true
    }

    override fun onUnbind(intent: Intent): Boolean {
        isResizeAllowed = true
        if (bridges.size == 0) {
            if (loadedKeypairs.size > 0) {
                synchronized(this) {
                    if (idleTimer == null) idleTimer = Timer("idleTimer", true)
                    idleTimer!!.schedule(IdleTask(), IDLE_TIMEOUT)
                }
            } else {
                stopSelf()
            }
        } else {
            // tell each bridge to forget about their previous prompt handler
            for (bridge in bridges) {
                bridge.promptHelper?.setHandler(null)
            }
        }
        return true
    }

    private inner class IdleTask : TimerTask() {
        override fun run() {
            if (bridges.size == 0) {
                stopSelf()
            }
        }
    }

    fun tryKeyVibrate() {
            if (wantKeyVibration && vibrator != null) vibrator!!.vibrate(VIBRATE_DURATION)
    }

    /* (non-Javadoc)
     * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener#onSharedPreferenceChanged(android.content.SharedPreferences, java.lang.String)
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?,
                                           key: String?) {
        if (PreferenceConstants.BUMPY_ARROWS == key) wantKeyVibration = sharedPreferences!!.getBoolean(PreferenceConstants.BUMPY_ARROWS, true)
        else if (PreferenceConstants.WIFI_LOCK == key) { } else if (PreferenceConstants.MEMKEYS == key) savingKeys = prefs!!.getBoolean(PreferenceConstants.MEMKEYS, true)
    }

    class KeyHolder {
        @JvmField
        var bean: PubKeyBean? = null

        @JvmField
        var pair: KeyPair? = null
        var openSSHPubkey: ByteArray? = null
    }

    /**
     * Insert request into reconnect queue to be executed either immediately
     * or later when connectivity is restored depending on whether we're
     * currently connected.
     *
     * @param bridge the TerminalBridge to reconnect when possible
     */
    fun requestReconnect(bridge: TerminalBridge) {
        synchronized(mPendingReconnect) {
            mPendingReconnect.add(WeakReference(bridge))
            //			if (!bridge.isUsingNetwork() || connectivityManager.isConnected()) {
//				reconnectPending();
//			}
            if (!bridge.isUsingNetwork) {
                /**
                 * Reconnect all bridges that were pending a reconnect when connectivity
                 * was lost.
                 */
                synchronized(mPendingReconnect) {
                    for (ref in mPendingReconnect) {
                        val newBridge = ref.get() ?: continue
                        newBridge.startConnection()
                    }
                    mPendingReconnect.clear()
                }
            }
        }
    }

    private fun notifyHostStatusChanged() {
        for (listener in hostStatusChangedListeners) {
            listener.onHostStatusChanged()
        }
    }

    companion object {
        const val TAG = "CB.TerminalManager"
        const val VIBRATE_DURATION: Long = 30
    }
}