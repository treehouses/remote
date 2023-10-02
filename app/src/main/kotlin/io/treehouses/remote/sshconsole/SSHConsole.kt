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
package io.treehouses.remote.sshconsole

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.os.StrictMode
import android.text.ClipboardManager
import android.view.*
import android.widget.HorizontalScrollView
import androidx.core.view.MenuItemCompat
import androidx.preference.PreferenceManager
import io.treehouses.remote.InitialActivity
import io.treehouses.remote.PreferenceConstants
import io.treehouses.remote.R
import io.treehouses.remote.ssh.terminal.TerminalBridge
import io.treehouses.remote.ssh.terminal.TerminalManager
import io.treehouses.remote.ssh.terminal.TerminalManager.TerminalBinder
import io.treehouses.remote.ssh.interfaces.BridgeDisconnectedListener
import io.treehouses.remote.databinding.ActivitySshConsoleBinding
import io.treehouses.remote.utils.logD
import io.treehouses.remote.utils.logE

open class SSHConsole : DerivedSSHConsole(), BridgeDisconnectedListener {

    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            bound = (service as TerminalBinder).service

            // let manager know about our event handling services
            bound!!.disconnectListener = this@SSHConsole
            bound!!.isResizeAllowed = true
            val requestedNickname = if (requested != null) requested!!.fragment else null
            var requestedBridge = bound!!.mNicknameBridgeMap[requestedNickname]?.get()

            // If we didn't find the requested connection, try opening it
            if (requestedNickname != null && requestedBridge == null) {
                try {
                    logD("${String.format("We couldnt find an existing bridge with URI=%s (nickname=%s), so creating one now", requested.toString(), requestedNickname)}")
                    requestedBridge = bound!!.openConnection(requested)
                } catch (e: Exception) {
                    logE("Problem while trying to create new requested bridge from URI $e")
                }
            }

            // create views for all bridges on this service
            adapter!!.notifyDataSetChanged()
            val requestedIndex = bound!!.bridges.indexOf(requestedBridge)
            requestedBridge?.promptHelper?.setHandler(promptHandler)
            if (requestedIndex != -1) {
                bind.pager.post { setDisplayedTerminal(requestedIndex) }
            }
        }

        override fun onServiceDisconnected(className: ComponentName) {
            bound = null
            adapter!!.notifyDataSetChanged()
            updateEmptyVisible()
        }
    }

    override fun onDisconnected(bridge: TerminalBridge) {
        synchronized(adapter!!) {
            adapter!!.notifyDataSetChanged()
            logD("Someone sending HANDLE_DISCONNECT to parentHandler")
            if (bridge.isAwaitingClose) {
                closeBridge()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX)
        hardKeyboard = resources.configuration.keyboard == Configuration.KEYBOARD_QWERTY
        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        titleBarHide = prefs!!.getBoolean(PreferenceConstants.TITLEBARHIDE, false)
        if (titleBarHide) requestActionBar()
        bind = ActivitySshConsoleBinding.inflate(layoutInflater)
        setContentView(bind.root)

        // handle requested console from incoming intent
        if (icicle == null) { requested = intent.data
        } else {
            val uri = icicle.getString(STATE_SELECTED_URI)
            if (uri != null) requested = Uri.parse(uri)
        }
        setUpTerminalPager()
        empty = findViewById(android.R.id.empty)
        promptListeners()
        setUpKeyboard()
        addKeyboardListeners()
        keyboardScroll()
        setUpActionBar()

        detectSoftVisibility()

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun keyboardScroll() {
        val keyboardScroll = findViewById<HorizontalScrollView>(R.id.keyboard_hscroll)
        if (!hardKeyboard) {
            // Show virtual keyboard and scroll back and forth
            showEmulatedKeys(false)
            keyboardScroll.postDelayed({
                val xscroll = bind.keyboard.buttonF12.right
                keyboardScroll.smoothScrollBy(xscroll, 0)
                keyboardScroll.postDelayed({ keyboardScroll.smoothScrollBy(-xscroll, 0) }, 500)
            }, 500)
        }

        // Reset keyboard auto-hide timer when scrolling
        keyboardScroll.setOnTouchListener { v: View, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_MOVE -> autoHideEmulatedKeys()
                MotionEvent.ACTION_UP -> {
                    v.performClick()
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        val view = adapter!!.currentTerminalView
        val activeTerminal = view != null
        var (sessionOpen, disconnected) = checkSession(view, activeTerminal)
        menu.setQwertyMode(true)
        disconnect = menu.add("Disconnect")
        if (hardKeyboard) disconnect!!.alphabeticShortcut = 'w'
        if (!sessionOpen && disconnected) disconnect!!.title = "Close Console"
        disconnect!!.isEnabled = activeTerminal
        disconnect!!.setIcon(android.R.drawable.ic_menu_close_clear_cancel)
        setDisconnectItemListener()
        paste = menu.add("Paste")
        if (hardKeyboard) paste!!.alphabeticShortcut = 'v'
        MenuItemCompat.setShowAsAction(paste, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM)
        paste!!.isEnabled = activeTerminal
        setPasteItemListener()
        urlScan = menu.add("Scan for URLs")
        if (hardKeyboard) urlScan!!.alphabeticShortcut = 'u'
        urlScan!!.setIcon(android.R.drawable.ic_menu_search)
        urlScan!!.isEnabled = activeTerminal
        setUrlItemListener()
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        volumeControlStream = AudioManager.STREAM_NOTIFICATION
        val view = adapter?.currentTerminalView
        val activeTerminal = view != null
        var (sessionOpen, disconnected) = checkSession(view, activeTerminal)
        disconnect?.isEnabled = activeTerminal
        if (sessionOpen || !disconnected) disconnect?.title = "Disconnect" else disconnect?.title = "Close Console"
        paste?.isEnabled = activeTerminal
        //		portForward.setEnabled(sessionOpen && canForwardPorts);
        urlScan?.isEnabled = activeTerminal
        resize?.isEnabled = sessionOpen
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item == null) return false
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    public override fun onStart() {
        super.onStart()

        // connect with manager service to find all bridges
        // when connected it will insert all views
        bindService(Intent(this, TerminalManager::class.java), connection, Context.BIND_AUTO_CREATE)
    }

    public override fun onPause() {
        super.onPause()
        logD("onPause called")
        if (forcedOrientation && bound != null) {
            bound!!.isResizeAllowed = false
        }
    }

    public override fun onResume() {
        super.onResume()
        logD("onResume called")

        // Make sure we don't let the screen fall asleep.
        // This also keeps the Wi-Fi chipset from disconnecting us.
        if (prefs!!.getBoolean(PreferenceConstants.KEEP_ALIVE, true)) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        configureOrientation()
        if (forcedOrientation && bound != null) {
            bound!!.isResizeAllowed = true
        }
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onNewIntent(android.content.Intent)
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        requested = intent.data
        if (requested == null) {
            logD("Got null intent data in onNewIntent()")
            return
        }
        if (bound == null) {
            logD("We're not bound in onNewIntent()")
            return
        }
        setIntentRequested()
    }

    public override fun onStop() {
        super.onStop()
        unbindService(connection)
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        // Maintain selected host if connected.
        val currentTerminalView = adapter!!.currentTerminalView
        if (currentTerminalView != null
                && !currentTerminalView.bridge.isDisconnected) {
            requested = currentTerminalView.bridge.host!!.uri
            savedInstanceState.putString(STATE_SELECTED_URI, requested.toString())
        }
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        logD("${String.format("onConfigurationChanged; requestedOrientation=%d, newConfig.orientation=%d", requestedOrientation, newConfig.orientation)}")
        if (bound != null) {
            bound!!.isResizeAllowed = !(forcedOrientation &&
                    (newConfig.orientation != Configuration.ORIENTATION_LANDSCAPE &&
                            requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ||
                            newConfig.orientation != Configuration.ORIENTATION_PORTRAIT &&
                            requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT))
            bound!!.hardKeyboardHidden = newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES
            bind.keyboard.buttonKeyboard.visibility = if (bound!!.hardKeyboardHidden) View.VISIBLE else View.GONE
        }
    }
}
