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
import io.treehouses.remote.databinding.ActivitySshConsoleBinding
import java.lang.ref.WeakReference

open class SSHConsole : AppCompatActivity(), BridgeDisconnectedListener {
//    protected var pager: TerminalViewPager? = null
    protected var tabs: TabLayout? = null
    protected var toolbar: Toolbar? = null
    protected var bound: TerminalManager? = null
    protected var adapter: TerminalPagerAdapter? = null
    private var prefs: SharedPreferences? = null

    // determines whether or not menuitem accelerators are bound
    // otherwise they collide with an external keyboard's CTRL-char
    private var hardKeyboard = false
    protected var requested: Uri? = null
    private var clipboard: ClipboardManager? = null
//    private var keyboardGroup: LinearLayout? = null
    private var keyboardGroupHider: Runnable? = null
    private var empty: TextView? = null
    private var fadeOutDelayed: Animation? = null
    private var keyboardFadeIn: Animation? = null
    private var keyboardFadeOut: Animation? = null
    private var disconnect: MenuItem? = null
    private var paste: MenuItem? = null
    private var resize: MenuItem? = null
    private var urlScan: MenuItem? = null
    private var forcedOrientation = false
    private val handler = Handler()
    private var mContentView: View? = null
    private var actionBar: ActionBar? = null
    private var inActionBarMenu = false
    private var titleBarHide = false
    private var keyboardAlwaysVisible = false

    private lateinit var bind: ActivitySshConsoleBinding
    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            bound = (service as TerminalBinder).service

            // let manager know about our event handling services
            bound!!.disconnectListener = this@SSHConsole
            bound!!.isResizeAllowed = true
            val requestedNickname = if (requested != null) requested!!.fragment else null
            var requestedBridge = bound!!.getConnectedBridge(requestedNickname)

            // If we didn't find the requested connection, try opening it
            if (requestedNickname != null && requestedBridge == null) {
                try {
                    Log.d(TAG, String.format("We couldnt find an existing bridge with URI=%s (nickname=%s), so creating one now", requested.toString(), requestedNickname))
                    requestedBridge = bound!!.openConnection(requested)
                } catch (e: Exception) {
                    Log.e(TAG, "Problem while trying to create new requested bridge from URI", e)
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
    protected var promptHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            // someone below us requested to display a prompt
            updatePromptVisible()
        }
    }

    override fun onDisconnected(bridge: TerminalBridge) {
        synchronized(adapter!!) {
            adapter!!.notifyDataSetChanged()
            Log.d(TAG, "Someone sending HANDLE_DISCONNECT to parentHandler")
            if (bridge.isAwaitingClose) {
                closeBridge()
            }
        }
    }

    private var emulatedKeysListener = View.OnClickListener { v: View -> onEmulatedKeyClicked(v) }
    private var keyRepeatHandler = Handler()

    /**
     * Handle repeatable virtual keys and touch events
     */
    inner class KeyRepeater(private val mHandler: Handler, private val mView: View) : Runnable, View.OnTouchListener, View.OnClickListener {
        private var mDown = false
        override fun run() {
            mDown = true
            mHandler.removeCallbacks(this)
            mHandler.postDelayed(this, KEYBOARD_REPEAT.toLong())
            mView.performClick()
        }

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    mDown = false
                    mHandler.postDelayed(this, KEYBOARD_REPEAT_INITIAL.toLong())
                    mView.isPressed = true
                }
                MotionEvent.ACTION_CANCEL -> {
                    mHandler.removeCallbacks(this)
                    mView.isPressed = false
                }
                MotionEvent.ACTION_UP -> {
                    mHandler.removeCallbacks(this)
                    mView.isPressed = false
                    if (!mDown) mView.performClick()
                }
                else -> return false
            }
            return true
        }

        override fun onClick(view: View) = onEmulatedKeyClicked(view)

    }

    private fun onEmulatedKeyClicked(v: View) {
        val terminal = adapter!!.currentTerminalView ?: return
        val handler = terminal.bridge.keyHandler
        var hideKeys = sendKeys(v, handler)
        if (hideKeys) hideEmulatedKeys()
        else autoHideEmulatedKeys()

        terminal.bridge.tryKeyVibrate()
        hideActionBarIfRequested()
    }

    private fun isSpecialButton(v: View, handler: TerminalKeyListener) : Boolean {
        var flag = true
        when (v.id) {
            R.id.button_ctrl -> handler.metaPress(TerminalKeyListener.OUR_CTRL_ON, true)
            R.id.button_esc -> handler.sendEscape()
            R.id.button_tab -> handler.sendTab()
            else -> flag = false
        }
        return flag
    }

    private fun checkButtons(v: View, handler: TerminalKeyListener) {
        when (v.id) {
            R.id.button_up -> handler.sendPressedKey(vt320.KEY_UP)
            R.id.button_down -> handler.sendPressedKey(vt320.KEY_DOWN)
            R.id.button_left -> handler.sendPressedKey(vt320.KEY_LEFT)
            R.id.button_right -> handler.sendPressedKey(vt320.KEY_RIGHT)
            R.id.button_home -> handler.sendPressedKey(vt320.KEY_HOME)
            R.id.button_end -> handler.sendPressedKey(vt320.KEY_END)
            R.id.button_pgup -> handler.sendPressedKey(vt320.KEY_PAGE_UP)
            R.id.button_pgdn -> handler.sendPressedKey(vt320.KEY_PAGE_DOWN)
            R.id.button_f1 -> handler.sendPressedKey(vt320.KEY_F1)
            R.id.button_f2 -> handler.sendPressedKey(vt320.KEY_F2)
            R.id.button_f3 -> handler.sendPressedKey(vt320.KEY_F3)
            R.id.button_f4 -> handler.sendPressedKey(vt320.KEY_F4)
            R.id.button_f5 -> handler.sendPressedKey(vt320.KEY_F5)
            R.id.button_f6 -> handler.sendPressedKey(vt320.KEY_F6)
            R.id.button_f7 -> handler.sendPressedKey(vt320.KEY_F7)
            R.id.button_f8 -> handler.sendPressedKey(vt320.KEY_F8)
            R.id.button_f9 -> handler.sendPressedKey(vt320.KEY_F9)
            R.id.button_f10 -> handler.sendPressedKey(vt320.KEY_F10)
            R.id.button_f11 -> handler.sendPressedKey(vt320.KEY_F11)
            R.id.button_f12 -> handler.sendPressedKey(vt320.KEY_F12)
            else -> Log.e(TAG, "Unknown emulated key clicked: " + v.id)
        }
    }

    private fun sendKeys(v: View, handler: TerminalKeyListener) : Boolean {
        if (isSpecialButton(v, handler)) return true
        else {
            checkButtons(v, handler)
            return false
        }
    }

    private fun hideActionBarIfRequested() {
        if (titleBarHide && actionBar != null) actionBar!!.hide()
    }

    /**
     * @param bridge
     */
    private fun closeBridge() {
        updateEmptyVisible()
        updatePromptVisible()

        // If we just closed the last bridge, go back to the previous activity.
        if (bind.pager.childCount == 0) {
            Log.e("FINISHING SSH", "FINISH")
            finish()
        }
    }

    private fun findCurrentView(id: Int): View? {
        val view = bind.pager.findViewWithTag<View>(adapter!!.getBridgeAtPosition(bind.pager.currentItem))
                ?: return null
        return view.findViewById(id)
    }

    private val currentPromptHelper: PromptHelper?
        get() {
            val view = adapter!!.currentTerminalView ?: return null
            return view.bridge.promptHelper
        }

    private fun hideAllPrompts() {
        bind.consolePasswordGroup.visibility = View.GONE
        bind.consoleBooleanGroup.visibility = View.GONE
    }

    private fun showEmulatedKeys(showActionBar: Boolean) {
        if (bind.keyboard.keyboardGroup.visibility == View.GONE) setAnimation(keyboardFadeIn, View.VISIBLE)
        if (showActionBar) {
            actionBar!!.show()
        }
        autoHideEmulatedKeys()
    }

    private fun setAnimation(animation: Animation?, visibility: Int) {
        bind.keyboard.keyboardGroup.startAnimation(animation)
        bind.keyboard.keyboardGroup.visibility = visibility
    }

    private fun autoHideEmulatedKeys() {
        if (keyboardGroupHider != null) handler.removeCallbacks(keyboardGroupHider!!)
        keyboardGroupHider = Runnable {
            if (bind.keyboard.keyboardGroup.visibility == View.GONE || inActionBarMenu) {
                return@Runnable
            }
            if (!keyboardAlwaysVisible) setAnimation(keyboardFadeOut, View.GONE)
            hideActionBarIfRequested()
            keyboardGroupHider = null
        }
        handler.postDelayed(keyboardGroupHider!!, KEYBOARD_DISPLAY_TIME.toLong())
    }

    private fun hideEmulatedKeys() {
        if (!keyboardAlwaysVisible) {
            if (keyboardGroupHider != null) handler.removeCallbacks(keyboardGroupHider!!)
            bind.keyboard.keyboardGroup.visibility = View.GONE
        }
        hideActionBarIfRequested()
    }

    @TargetApi(11)
    private fun requestActionBar() {
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY)
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

    private fun setUpTerminalPager() {
        bind.pager.addOnPageChangeListener(
                object : SimpleOnPageChangeListener() {
                    override fun onPageSelected(position: Int) {
                        title = adapter!!.getPageTitle(position)
                        onTerminalChanged()
                    }
                })
        adapter = TerminalPagerAdapter()
        bind.pager.adapter = adapter
        if (tabs != null) setupTabLayoutWithViewPager()
        bind.pager.setOnClickListener { showEmulatedKeys(true) }
    }

    private fun setUpActionBar() {
        actionBar = supportActionBar
        if (actionBar != null) {
            actionBar!!.setDisplayHomeAsUpEnabled(true)
            if (titleBarHide) {
                actionBar!!.hide()
            }
            actionBar!!.addOnMenuVisibilityListener { isVisible: Boolean ->
                inActionBarMenu = isVisible
                if (!isVisible) {
                    hideEmulatedKeys()
                }
            }
        }
    }

    private fun detectSoftVisibility() {
        // Change keyboard button image according to soft keyboard visibility
        // How to detect keyboard visibility: http://stackoverflow.com/q/4745988
        mContentView = findViewById(android.R.id.content)
        mContentView!!.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            mContentView!!.getWindowVisibleDisplayFrame(r)
            val screenHeight = mContentView!!.rootView.height
            val keypadHeight = screenHeight - r.bottom
            if (keypadHeight > screenHeight * 0.15) {
                // keyboard is opened
                bind.keyboard.buttonKeyboard.setImageResource(R.drawable.ic_keyboard_hide)
            } else {
                // keyboard is closed
                bind.keyboard.buttonKeyboard.setImageResource(R.drawable.ic_keyboard)
            }
        }
    }

    private fun setUpKeyboard() {
        fadeOutDelayed = AnimationUtils.loadAnimation(this, R.anim.fade_out_delayed)

        // Preload animation for keyboard button
        keyboardFadeIn = AnimationUtils.loadAnimation(this, R.anim.keyboard_fade_in)
        keyboardFadeOut = AnimationUtils.loadAnimation(this, R.anim.keyboard_fade_out)
        keyboardAlwaysVisible = prefs!!.getBoolean(PreferenceConstants.KEY_ALWAYS_VISIBLE, false)
        if (keyboardAlwaysVisible) {
            // equivalent to android:layout_above=keyboard_group
            var layoutParams = RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT)
            layoutParams.addRule(RelativeLayout.ABOVE, R.id.keyboard_group)
            bind.pager.layoutParams = layoutParams
            layoutParams = RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)
            layoutParams.addRule(RelativeLayout.ABOVE, R.id.keyboard_group)
            findViewById<View>(R.id.console_password_group).layoutParams = layoutParams
            findViewById<View>(R.id.console_boolean_group).layoutParams = layoutParams

            // Show virtual keyboard
            bind.keyboard.keyboardGroup.visibility = View.VISIBLE
        }
    }

    private fun addKeyboardListeners() {
        bind.keyboard.buttonKeyboard.setOnClickListener {
            val terminal = adapter!!.currentTerminalView ?: return@setOnClickListener
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.toggleSoftInputFromWindow(terminal.applicationWindowToken,
                    InputMethodManager.SHOW_FORCED, 0)
            terminal.requestFocus()
            hideEmulatedKeys()
        }
        bind.keyboard.buttonCtrl.setOnClickListener(emulatedKeysListener)
        bind.keyboard.buttonEsc.setOnClickListener(emulatedKeysListener)
        bind.keyboard.buttonTab.setOnClickListener(emulatedKeysListener)
        addKeyRepeater(bind.keyboard.buttonUp)
        addKeyRepeater(bind.keyboard.buttonUp)
        addKeyRepeater(bind.keyboard.buttonDown)
        addKeyRepeater(bind.keyboard.buttonLeft)
        addKeyRepeater(bind.keyboard.buttonRight)
        setButtonListeners()
    }

    private fun setButtonListeners() {
        bind.keyboard.buttonHome.setOnClickListener(emulatedKeysListener)
        bind.keyboard.buttonEnd.setOnClickListener(emulatedKeysListener)
        bind.keyboard.buttonPgup.setOnClickListener(emulatedKeysListener)
        bind.keyboard.buttonPgdn.setOnClickListener(emulatedKeysListener)
        bind.keyboard.buttonF1.setOnClickListener(emulatedKeysListener)
        bind.keyboard.buttonF2.setOnClickListener(emulatedKeysListener)
        bind.keyboard.buttonF3.setOnClickListener(emulatedKeysListener)
        bind.keyboard.buttonF4.setOnClickListener(emulatedKeysListener)
        bind.keyboard.buttonF5.setOnClickListener(emulatedKeysListener)
        bind.keyboard.buttonF6.setOnClickListener(emulatedKeysListener)
        bind.keyboard.buttonF7.setOnClickListener(emulatedKeysListener)
        bind.keyboard.buttonF8.setOnClickListener(emulatedKeysListener)
        bind.keyboard.buttonF9.setOnClickListener(emulatedKeysListener)
        bind.keyboard.buttonF10.setOnClickListener(emulatedKeysListener)
        bind.keyboard.buttonF11.setOnClickListener(emulatedKeysListener)
        bind.keyboard.buttonF12.setOnClickListener(emulatedKeysListener)
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

    private fun promptListeners() {
        val onKeyListener = View.OnKeyListener {
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
        bind.consolePassword.setOnKeyListener(onKeyListener)

        fun update(b:Boolean, helper:PromptHelper) {
            helper.setResponse(b)
            updatePromptVisible()
        }

        bind.consolePromptYes.setOnClickListener {
            update(true, currentPromptHelper ?: return@setOnClickListener)
        }
        bind.consolePromptNo.setOnClickListener {
            update(false, currentPromptHelper ?: return@setOnClickListener)
        }

    }

    private fun addKeyRepeater(view: View) {
        val keyRepeater = KeyRepeater(keyRepeatHandler, view)
        view.setOnClickListener(keyRepeater)
        view.setOnTouchListener(keyRepeater)
    }

    /**
     * Ties the [TabLayout] to the [TerminalViewPager].
     *
     *
     * This method will:
     *
     *  * Add a [TerminalViewPager.OnPageChangeListener] that will forward events to
     * this TabLayout.
     *  * Populate the TabLayout's tabs from the ViewPager's [PagerAdapter].
     *  * Set our [TabLayout.OnTabSelectedListener] which will forward
     * selected events to the ViewPager
     *
     *
     */
    private fun setupTabLayoutWithViewPager() {
        tabs!!.setTabsFromPagerAdapter(adapter)
        bind.pager.addOnPageChangeListener(TabLayoutOnPageChangeListener(tabs))
        tabs!!.setOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(bind.pager))
        if (adapter!!.count > 0) {
            val curItem = bind.pager.currentItem
            if (tabs!!.selectedTabPosition != curItem) {
                tabs!!.getTabAt(curItem)!!.select()
            }
        }
    }

    /**
     *
     */
    private fun configureOrientation() {
        val rotateDefault: String = if (resources.configuration.keyboard == Configuration.KEYBOARD_NOKEYS) PreferenceConstants.ROTATION_PORTRAIT else PreferenceConstants.ROTATION_LANDSCAPE
        var rotate = prefs!!.getString(PreferenceConstants.ROTATION, rotateDefault)
        if (PreferenceConstants.ROTATION_DEFAULT == rotate) rotate = rotateDefault

        // request a forced orientation if requested by user
        when (rotate) {
            PreferenceConstants.ROTATION_LANDSCAPE -> {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                forcedOrientation = true
            }
            PreferenceConstants.ROTATION_PORTRAIT -> {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                forcedOrientation = true
            }
            else -> {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                forcedOrientation = false
            }
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
        disconnect!!.setOnMenuItemClickListener {
            // disconnect or close the currently visible session
            val terminalView = adapter!!.currentTerminalView
            val bridge = terminalView!!.bridge
            bridge.dispatchDisconnect(true)
            true
        }
        paste = menu.add("Paste")
        if (hardKeyboard) paste!!.alphabeticShortcut = 'v'
        MenuItemCompat.setShowAsAction(paste, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM)
        paste!!.isEnabled = activeTerminal
        paste!!.setOnMenuItemClickListener {
            pasteIntoTerminal()
            true
        }
        urlScan = menu.add("Scan for URLs")
        if (hardKeyboard) urlScan!!.alphabeticShortcut = 'u'
        urlScan!!.setIcon(android.R.drawable.ic_menu_search)
        urlScan!!.isEnabled = activeTerminal
        urlScan!!.setOnMenuItemClickListener {
            val terminalView = adapter!!.currentTerminalView
            val urls = terminalView!!.bridge.scanForURLs()
            val urlDialog = Dialog(this@SSHConsole)
            urlDialog.setTitle("Scan for URLs")
            val urlListView = ListView(this@SSHConsole)
            val urlListener = URLItemListener(this@SSHConsole)
            urlListView.onItemClickListener = urlListener
            urlListView.adapter = ArrayAdapter(this@SSHConsole, android.R.layout.simple_list_item_1, urls)
            urlDialog.setContentView(urlListView)
            urlDialog.show()
            true
        }
        return true
    }

    private fun checkSession(view: TerminalView?, activeTerminal: Boolean): Pair<Boolean, Boolean> {
        var sessionOpen = false
        var disconnected = false
        if (activeTerminal) {
            val bridge = view!!.bridge
            sessionOpen = bridge.isSessionOpen
            disconnected = bridge.isDisconnected
        }
        return Pair(sessionOpen, disconnected)
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
        if (forcedOrientation && bound != null) {
            bound!!.isResizeAllowed = false
        }
    }

    public override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called")

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
            Log.e(TAG, "Got null intent data in onNewIntent()")
            return
        }
        if (bound == null) {
            Log.e(TAG, "We're not bound in onNewIntent()")
            return
        }
        setIntentRequested()
    }

    private fun setIntentRequested() {
        val requestedBridge = bound!!.getConnectedBridge(requested!!.fragment)
        var requestedIndex = 0
        synchronized(bind.pager) {
            if (requestedBridge == null) {
                // If we didn't find the requested connection, try opening it
                try {
                    Log.d(TAG, String.format("We couldnt find an existing bridge with URI=%s (nickname=%s)," +
                            "so creating one now", requested.toString(), requested!!.fragment))
                    bound!!.openConnection(requested)
                } catch (e: Exception) {
                    Log.e(TAG, "Problem while trying to create new requested bridge from URI", e)
                    return
                }
                adapter!!.notifyDataSetChanged()
                requestedIndex = adapter!!.count
            } else {
                val flipIndex = bound!!.bridges.indexOf(requestedBridge)
                if (flipIndex > requestedIndex) {
                    requestedIndex = flipIndex
                }
            }
            setDisplayedTerminal(requestedIndex)
        }
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

    /**
     * Save the currently shown [TerminalView] as the default. This is
     * saved back down into [TerminalManager] where we can read it again
     * later.
     */
    private fun updateDefault() {
        // update the current default terminal
        val view = adapter!!.currentTerminalView
        if (view == null || bound == null) {
            return
        }
        bound!!.defaultBridge = view.bridge
    }

    protected fun updateEmptyVisible() {
        // update visibility of empty status message
        empty!!.visibility = if (bind.pager.childCount == 0) View.VISIBLE else View.GONE
    }

    /**
     * Show any prompts requested by the currently visible [TerminalView].
     */
    protected fun updatePromptVisible() {
        // check if our currently-visible terminalbridge is requesting any prompt services
        val view = adapter?.currentTerminalView

        // Hide all the prompts in case a prompt request was canceled
        hideAllPrompts()
        if (view == null) {
            // we dont have an active view, so hide any prompts
            return
        }
        val prompt = view.bridge.promptHelper
        Log.e("GOT", "HERE ")
        when {
            String::class.java == prompt!!.promptRequested -> {
                hideEmulatedKeys()
                bind.consolePasswordGroup.visibility = View.VISIBLE
                val instructions = prompt.promptInstructions
                if (instructions != null && instructions.isNotEmpty()) {
                    bind.consolePasswordInstructions.visibility = View.VISIBLE
                    bind.consolePasswordInstructions.text = instructions
                } else bind.consolePasswordInstructions.visibility = View.GONE
                bind.consolePassword.setText("")
                bind.consolePassword.hint = prompt.promptHint
                bind.consolePassword.requestFocus()
            }
            Boolean::class.java == prompt.promptRequested -> {
                hideEmulatedKeys()
                bind.consoleBooleanGroup.visibility = View.VISIBLE
                bind.consolePrompt.text = prompt.promptHint
                bind.consolePromptYes.requestFocus()
            }
            else -> {
                hideAllPrompts()
                view.requestFocus()
            }
        }
    }

    private class URLItemListener internal constructor(context: Context) : OnItemClickListener {
        private val contextRef: WeakReference<Context> = WeakReference(context)
        override fun onItemClick(arg0: AdapterView<*>?, view: View, position: Int, id: Long) {
            val context = contextRef.get() ?: return
            try {
                val urlView = view as TextView
                val url = urlView.text.toString()
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "couldn't open URL", e)
                // We should probably tell the user that we couldn't find a handler...
            }
        }

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d(TAG, String.format("onConfigurationChanged; requestedOrientation=%d, newConfig.orientation=%d", requestedOrientation, newConfig.orientation))
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

    /**
     * Called whenever the displayed terminal is changed.
     */
    private fun onTerminalChanged() {
        val terminalNameOverlay = findCurrentView(R.id.terminal_name_overlay)
        terminalNameOverlay?.startAnimation(fadeOutDelayed)
        updateDefault()
        updatePromptVisible()
        ActivityCompat.invalidateOptionsMenu(this@SSHConsole)
    }

    /**
     * Displays the child in the ViewPager at the requestedIndex and updates the prompts.
     *
     * @param requestedIndex the index of the terminal view to display
     */
    private fun setDisplayedTerminal(requestedIndex: Int) {
        bind.pager.currentItem = requestedIndex
        // set activity title
        title = adapter!!.getPageTitle(requestedIndex)
        onTerminalChanged()
    }

    private fun pasteIntoTerminal() {
        // force insert of clipboard text into current console
        val terminalView = adapter!!.currentTerminalView
        val bridge = terminalView!!.bridge

        // pull string from clipboard and generate all events to force down
        var clip = ""
        if (clipboard!!.hasText()) {
            clip = clipboard!!.text.toString()
        }
        bridge.injectString(clip)
    }

    inner class TerminalPagerAdapter : PagerAdapter() {
        override fun getCount(): Int {
            return if (bound != null) {
                bound!!.bridges.size
            } else {
                0
            }
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            if (bound == null || bound!!.bridges.size <= position) {
                Log.w(TAG, "Activity not bound when creating TerminalView.")
            }
            val bridge = bound!!.bridges[position]
            bridge.promptHelper!!.setHandler(promptHandler)

            // inflate each terminal view
            val view = layoutInflater.inflate(R.layout.item_terminal, container, false) as RelativeLayout

            // set the terminal name overlay text
            val terminalNameOverlay = view.findViewById<TextView>(R.id.terminal_name_overlay)
            terminalNameOverlay.text = bridge.host!!.nickname

            // and add our terminal view control, using index to place behind overlay
            val terminal = TerminalView(container.context, bridge, bind.pager)
            terminal.id = R.id.terminal_view
            view.addView(terminal, 0)

            // Tag the view with its bridge so it can be retrieved later.
            view.tag = bridge
            container.addView(view)
            terminalNameOverlay.startAnimation(fadeOutDelayed)
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            val view = `object` as View
            container.removeView(view)
        }

        override fun getItemPosition(`object`: Any): Int {
            if (bound == null) {
                return POSITION_NONE
            }
            val view = `object` as View
            val terminal: TerminalView = view.findViewById(R.id.terminal_view)
            val host = terminal.bridge.host
            var itemIndex = POSITION_NONE
            var i = 0
            for (bridge in bound!!.bridges) {
                if (bridge.host == host) {
                    itemIndex = i
                    break
                }
                i++
            }
            return itemIndex
        }

        fun getBridgeAtPosition(position: Int): TerminalBridge? {
            if (bound == null) {
                return null
            }
            val bridges = bound!!.bridges
            return if (position < 0 || position >= bridges.size) {
                null
            } else bridges[position]
        }

        override fun notifyDataSetChanged() {
            super.notifyDataSetChanged()
            if (tabs != null) {
                toolbar!!.visibility = if (this.count > 1) View.VISIBLE else View.GONE
                tabs!!.setTabsFromPagerAdapter(this)
            }
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        override fun getPageTitle(position: Int): CharSequence? {
            val bridge = getBridgeAtPosition(position) ?: return "???"
            return "Treehouses Remote: " + bridge.host!!.nickname
        }

        val currentTerminalView: TerminalView?
            get() {
                val currentView = bind.pager.findViewWithTag<View>(getBridgeAtPosition(bind.pager.currentItem))
                        ?: return null
                return currentView.findViewById<View>(R.id.terminal_view) as TerminalView
            }
    }

    companion object {
        const val TAG = "CB.ConsoleActivity"
        private const val KEYBOARD_DISPLAY_TIME = 3000
        private const val KEYBOARD_REPEAT_INITIAL = 500
        private const val KEYBOARD_REPEAT = 100
        private const val STATE_SELECTED_URI = "selectedUri"
    }
}
