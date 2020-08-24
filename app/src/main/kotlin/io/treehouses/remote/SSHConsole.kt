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
package io.treehouses.remote

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Dialog
import android.content.*
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Rect
import android.media.AudioManager
import android.net.Uri
import android.os.*
import android.text.ClipboardManager
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.view.MenuItemCompat
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener
import io.treehouses.remote.Views.terminal.vt320
import io.treehouses.remote.SSH.PromptHelper
import io.treehouses.remote.SSH.Terminal.*
import io.treehouses.remote.SSH.Terminal.TerminalManager.TerminalBinder
import io.treehouses.remote.SSH.interfaces.BridgeDisconnectedListener
import io.treehouses.remote.bases.BaseTerminalKeyListener
import io.treehouses.remote.databinding.ActivitySshConsoleBinding
import io.treehouses.remote.ui.services.ServicesViewModel
import java.lang.ref.WeakReference

open class SSHConsole : DerivedSSHConsole(), BridgeDisconnectedListener {

    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            viewModel.bound = (service as TerminalBinder).service

            // let manager know about our event handling services
            viewModel.bound!!.disconnectListener = this@SSHConsole
            viewModel.bound!!.isResizeAllowed = true
            val requestedNickname = if (viewModel.requested != null) viewModel.requested!!.fragment else null
            var requestedBridge = viewModel.bound!!.mNicknameBridgeMap[requestedNickname]?.get()

            // If we didn't find the requested connection, try opening it
            if (requestedNickname != null && requestedBridge == null) {
                try {
                    Log.d(TAG, String.format("We couldnt find an existing bridge with URI=%s (nickname=%s), so creating one now", viewModel.requested.toString(), requestedNickname))
                    requestedBridge = viewModel.bound!!.openConnection(viewModel.requested)
                } catch (e: Exception) {
                    Log.e(TAG, "Problem while trying to create new requested bridge from URI", e)
                }
            }

            // create views for all bridges on this service
            viewModel.adapter!!.notifyDataSetChanged()
            val requestedIndex = viewModel.bound!!.bridges.indexOf(requestedBridge)
            requestedBridge?.promptHelper?.setHandler(promptHandler)
            if (requestedIndex != -1) {
                bind.pager.post { setDisplayedTerminal(requestedIndex) }
            }
        }

        override fun onServiceDisconnected(className: ComponentName) {
            viewModel.bound = null
            viewModel.adapter!!.notifyDataSetChanged()
            updateEmptyVisible()
        }
    }

    override fun onDisconnected(bridge: TerminalBridge) {
        synchronized(viewModel.adapter!!) {
            viewModel.adapter!!.notifyDataSetChanged()
            Log.d(TAG, "Someone sending HANDLE_DISCONNECT to parentHandler")
            if (bridge.isAwaitingClose) {
                closeBridge()
            }
        }
    }

    private val currentPromptHelper: PromptHelper?
        get() {
            val view = viewModel.adapter!!.currentTerminalView ?: return null
            return view.bridge.promptHelper
        }

    @SuppressLint("ClickableViewAccessibility")
    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX)
        viewModel.hardKeyboard = resources.configuration.keyboard == Configuration.KEYBOARD_QWERTY
        viewModel.clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        viewModel.prefs = PreferenceManager.getDefaultSharedPreferences(this)
        viewModel.titleBarHide = viewModel.prefs!!.getBoolean(PreferenceConstants.TITLEBARHIDE, false)
        if (viewModel.titleBarHide) requestActionBar()
        bind = ActivitySshConsoleBinding.inflate(layoutInflater)
        setContentView(bind.root)

        // handle requested console from incoming intent
        if (icicle == null) { viewModel.requested = intent.data
        } else {
            val uri = icicle.getString(STATE_SELECTED_URI)
            if (uri != null) viewModel.requested = Uri.parse(uri)
        }
        setUpTerminalPager()
        viewModel.empty = findViewById(android.R.id.empty)
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
        if (!viewModel.hardKeyboard) {
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

    private fun promptListeners() {
        val onKeyListener = createOnKeyListener()
        bind.consolePassword.setOnKeyListener(onKeyListener)

        bind.consolePromptYes.setOnClickListener {
            updatePrompt(true, currentPromptHelper ?: return@setOnClickListener)
        }
        bind.consolePromptNo.setOnClickListener {
            updatePrompt(false, currentPromptHelper ?: return@setOnClickListener)
        }

    }

    private fun createOnKeyListener(): View.OnKeyListener {
        return View.OnKeyListener {
            _: View?, keyCode: Int, event: KeyEvent ->
            if (event.action == KeyEvent.ACTION_UP || keyCode != KeyEvent.KEYCODE_ENTER) return@OnKeyListener false

            // pass collected password down to current terminal
            val value = bind.consolePassword.text.toString()
            val helper = currentPromptHelper ?: return@OnKeyListener false
            helper.setResponse(value)

            // finally clear password for next user
            bind.consolePassword.setText("")
            updatePromptVisible()
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        val view = viewModel.adapter!!.currentTerminalView
        val activeTerminal = view != null
        var (sessionOpen, disconnected) = checkSession(view, activeTerminal)
        menu.setQwertyMode(true)
        viewModel.disconnect = menu.add("Disconnect")
        if (viewModel.hardKeyboard) viewModel.disconnect!!.alphabeticShortcut = 'w'
        if (!sessionOpen && disconnected) viewModel.disconnect!!.title = "Close Console"
        viewModel.disconnect!!.isEnabled = activeTerminal
        viewModel.disconnect!!.setIcon(android.R.drawable.ic_menu_close_clear_cancel)
        viewModel.setDisconnectItemListener()
        viewModel.paste = menu.add("Paste")
        if (viewModel.hardKeyboard) viewModel.paste!!.alphabeticShortcut = 'v'
        MenuItemCompat.setShowAsAction(viewModel.paste, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM)
        viewModel.paste!!.isEnabled = activeTerminal
        viewModel.setPasteItemListener()
        viewModel.urlScan = menu.add("Scan for URLs")
        if (viewModel.hardKeyboard) viewModel.urlScan!!.alphabeticShortcut = 'u'
        viewModel.urlScan!!.setIcon(android.R.drawable.ic_menu_search)
        viewModel.urlScan!!.isEnabled = activeTerminal
        setUrlItemListener()
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        volumeControlStream = AudioManager.STREAM_NOTIFICATION
        val view = viewModel.adapter?.currentTerminalView
        val activeTerminal = view != null
        var (sessionOpen, disconnected) = checkSession(view, activeTerminal)
        viewModel.disconnect?.isEnabled = activeTerminal
        if (sessionOpen || !disconnected) viewModel.disconnect?.title = "Disconnect" else viewModel.disconnect?.title = "Close Console"
        viewModel.paste?.isEnabled = activeTerminal
        //		portForward.setEnabled(sessionOpen && canForwardPorts);
        viewModel.urlScan?.isEnabled = activeTerminal
        viewModel.resize?.isEnabled = sessionOpen
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item == null) return false
        return when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, InitialActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                true
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
        Log.d(TAG, "onPause called")
        if (viewModel.forcedOrientation && viewModel.bound != null) {
            viewModel.bound!!.isResizeAllowed = false
        }
    }

    public override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called")

        // Make sure we don't let the screen fall asleep.
        // This also keeps the Wi-Fi chipset from disconnecting us.
        if (viewModel.prefs!!.getBoolean(PreferenceConstants.KEEP_ALIVE, true)) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        configureOrientation()
        if (viewModel.forcedOrientation && viewModel.bound != null) {
            viewModel.bound!!.isResizeAllowed = true
        }
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onNewIntent(android.content.Intent)
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        viewModel.requested = intent.data
        if (viewModel.requested == null) {
            Log.e(TAG, "Got null intent data in onNewIntent()")
            return
        }
        if (viewModel.bound == null) {
            Log.e(TAG, "We're not bound in onNewIntent()")
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
        val currentTerminalView = viewModel.adapter!!.currentTerminalView
        if (currentTerminalView != null
                && !currentTerminalView.bridge.isDisconnected) {
            viewModel.requested = currentTerminalView.bridge.host!!.uri
            savedInstanceState.putString(STATE_SELECTED_URI, viewModel.requested.toString())
        }
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d(TAG, String.format("onConfigurationChanged; requestedOrientation=%d, newConfig.orientation=%d", requestedOrientation, newConfig.orientation))
        if (viewModel.bound != null) {
            viewModel.bound!!.isResizeAllowed = !(viewModel.forcedOrientation &&
                    (newConfig.orientation != Configuration.ORIENTATION_LANDSCAPE &&
                            requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ||
                            newConfig.orientation != Configuration.ORIENTATION_PORTRAIT &&
                            requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT))
            viewModel.bound!!.hardKeyboardHidden = newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES
            bind.keyboard.buttonKeyboard.visibility = if (viewModel.bound!!.hardKeyboardHidden) View.VISIBLE else View.GONE
        }
    }

}
