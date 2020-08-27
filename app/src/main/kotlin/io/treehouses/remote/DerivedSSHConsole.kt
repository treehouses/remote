package io.treehouses.remote

import android.app.Dialog
import android.content.Context
import android.graphics.Rect
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import io.treehouses.remote.SSH.PromptHelper
import io.treehouses.remote.SSH.Terminal.TerminalBridge
import io.treehouses.remote.SSH.Terminal.TerminalView
import io.treehouses.remote.SSH.Terminal.TerminalViewPager

open class DerivedSSHConsole: BaseSSHConsole() {
    private var emulatedKeysListener = View.OnClickListener { v: View -> onEmulatedKeyClicked(v) }
    private var keyRepeatHandler = Handler()
    private val currentPromptHelper: PromptHelper?
        get() {
            val view = currentTerminalView ?: return null
            return view.bridge.promptHelper
        }

    protected var promptHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            // someone below us requested to display a prompt
            updatePromptVisible()
        }
    }

    protected fun setUrlItemListener() {
        urlScan!!.setOnMenuItemClickListener {
            val terminalView = currentTerminalView
            val urls = terminalView!!.bridge.scanForURLs()
            val urlDialog = Dialog(this@DerivedSSHConsole)
            urlDialog.setTitle("Scan for URLs")
            val urlListView = ListView(this@DerivedSSHConsole)
            val urlListener = URLItemListener(this@DerivedSSHConsole)
            urlListView.onItemClickListener = urlListener
            urlListView.adapter = ArrayAdapter(this@DerivedSSHConsole, android.R.layout.simple_list_item_1, urls)
            urlDialog.setContentView(urlListView)
            urlDialog.show()
            true
        }
    }

    protected fun setUpTerminalPager() {
        bind.pager.addOnPageChangeListener(
                object : ViewPager.SimpleOnPageChangeListener() {
                    override fun onPageSelected(position: Int) {
                        title = adapter!!.getPageTitle(position)
                        onTerminalChanged()
                    }
                })
        adapter = TerminalPagerAdapter(bound, promptHandler, layoutInflater, bind, fadeOutDelayed)
        adapter!!.setTerminalPager(object : TerminalPager {
            override fun handleData() {
                handleData()
            }

        })
        bind.pager.adapter = adapter
        if (tabs != null) setupTabLayoutWithViewPager()
        bind.pager.setOnClickListener { showEmulatedKeys(true) }
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
        bind.pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs!!.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(bind.pager))
        if (adapter!!.count > 0) {
            val curItem = bind.pager.currentItem
            if (tabs!!.selectedTabPosition != curItem) tabs!!.getTabAt(curItem)!!.select()
        }
    }

    protected fun showEmulatedKeys(showActionBar: Boolean) {
        if (bind.keyboard.keyboardGroup.visibility == View.GONE) setAnimation(keyboardFadeIn, View.VISIBLE)
        if (showActionBar) actionBar!!.show()
        autoHideEmulatedKeys()
    }

    protected fun setUpActionBar() {
        actionBar = supportActionBar
        if (actionBar != null) {
            actionBar!!.setDisplayHomeAsUpEnabled(true)
            if (titleBarHide) actionBar!!.hide()
            actionBar!!.addOnMenuVisibilityListener { isVisible: Boolean ->
                inActionBarMenu = isVisible
                if (!isVisible) hideEmulatedKeys()
            }
        }
    }

    protected fun detectSoftVisibility() {
        // Change keyboard button image according to soft keyboard visibility
        // How to detect keyboard visibility: http://stackoverflow.com/q/4745988
        mContentView = findViewById(android.R.id.content)
        mContentView!!.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            mContentView!!.getWindowVisibleDisplayFrame(r)
            val screenHeight = mContentView!!.rootView.height
            val keypadHeight = screenHeight - r.bottom
            // keyboard is opened
            if (keypadHeight > screenHeight * 0.15) bind.keyboard.buttonKeyboard.setImageResource(R.drawable.ic_keyboard_hide)
            else bind.keyboard.buttonKeyboard.setImageResource(R.drawable.ic_keyboard)
            // keyboard is closed
        }
    }

    protected fun setUpKeyboard() {
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

    protected fun addKeyboardListeners() {
        bind.keyboard.buttonKeyboard.setOnClickListener {
            val terminal = currentTerminalView ?: return@setOnClickListener
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.toggleSoftInputFromWindow(terminal.applicationWindowToken,
                    InputMethodManager.SHOW_FORCED, 0)
            terminal.requestFocus()
            hideEmulatedKeys()
        }
        bind.keyboard.buttonCtrl.setOnClickListener(emulatedKeysListener)
        bind.keyboard.buttonEsc.setOnClickListener(emulatedKeysListener)
        bind.keyboard.buttonTab.setOnClickListener(emulatedKeysListener)
        addKeyRepeater(bind.keyboard.buttonUp); addKeyRepeater(bind.keyboard.buttonUp)
        addKeyRepeater(bind.keyboard.buttonDown); addKeyRepeater(bind.keyboard.buttonLeft)
        addKeyRepeater(bind.keyboard.buttonRight)
        setButtonListeners()
    }

    private fun addKeyRepeater(view: View) {
        val keyRepeater = KeyRepeater(keyRepeatHandler, view)
        view.setOnClickListener(keyRepeater); view.setOnTouchListener(keyRepeater)
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

    protected fun promptListeners() {
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

    private fun handleData() {
        if (tabs != null) {
            toolbar!!.visibility = if (adapter?.count!! > 1) View.VISIBLE else View.GONE
            tabs!!.setTabsFromPagerAdapter(adapter)
        }
    }
}