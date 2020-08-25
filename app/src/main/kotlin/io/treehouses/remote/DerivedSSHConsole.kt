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
import androidx.core.app.ActivityCompat
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
            val view = viewModel.adapter!!.currentTerminalView ?: return null
            return view.bridge.promptHelper
        }

    protected var promptHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            // someone below us requested to display a prompt
            updatePromptVisible()
        }
    }

    inner class TerminalPagerAdapter : PagerAdapter() {
        override fun getCount(): Int {
            return if (viewModel.bound != null) viewModel.bound!!.bridges.size
            else 0
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            if (viewModel.bound == null || viewModel.bound!!.bridges.size <= position) Log.w(TAG, "Activity not bound when creating TerminalView.")
            val bridge = viewModel.bound!!.bridges[position]
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
            terminalNameOverlay.startAnimation(viewModel.fadeOutDelayed)
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            val view = `object` as View
            container.removeView(view)
        }

        override fun getItemPosition(`object`: Any): Int {
            if (viewModel.bound == null) return POSITION_NONE
            val view = `object` as View
            val terminal: TerminalView = view.findViewById(R.id.terminal_view)
            val host = terminal.bridge.host
            var itemIndex = POSITION_NONE
            var i = 0
            for (bridge in viewModel.bound!!.bridges) {
                if (bridge.host == host) {
                    itemIndex = i
                    break
                }
                i++
            }
            return itemIndex
        }

        fun getBridgeAtPosition(position: Int): TerminalBridge? {
            if (viewModel.bound == null) return null
            val bridges = viewModel.bound!!.bridges
            return if (position < 0 || position >= bridges.size) null
            else bridges[position]
        }

        override fun notifyDataSetChanged() {
            super.notifyDataSetChanged()
            if (viewModel.tabs != null) {
                viewModel.toolbar!!.visibility = if (this.count > 1) View.VISIBLE else View.GONE
                viewModel.tabs!!.setTabsFromPagerAdapter(this)
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

    protected fun setUrlItemListener() {
        viewModel.urlScan!!.setOnMenuItemClickListener {
            val terminalView = viewModel.adapter!!.currentTerminalView
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
                        title = viewModel.adapter!!.getPageTitle(position)
                        onTerminalChanged()
                    }
                })
        viewModel.adapter = TerminalPagerAdapter()
        bind.pager.adapter = viewModel.adapter
        if (viewModel.tabs != null) setupTabLayoutWithViewPager()
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
        viewModel.tabs!!.setTabsFromPagerAdapter(viewModel.adapter)
        bind.pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(viewModel.tabs))
        viewModel.tabs!!.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(bind.pager))
        if (viewModel.adapter!!.count > 0) {
            val curItem = bind.pager.currentItem
            if (viewModel.tabs!!.selectedTabPosition != curItem) viewModel.tabs!!.getTabAt(curItem)!!.select()
        }
    }

    protected fun showEmulatedKeys(showActionBar: Boolean) {
        if (bind.keyboard.keyboardGroup.visibility == View.GONE) setAnimation(viewModel.keyboardFadeIn, View.VISIBLE)
        if (showActionBar) viewModel.actionBar!!.show()
        autoHideEmulatedKeys()
    }

    protected fun setUpActionBar() {
        viewModel.actionBar = supportActionBar
        if (viewModel.actionBar != null) {
            viewModel.actionBar!!.setDisplayHomeAsUpEnabled(true)
            if (viewModel.titleBarHide) viewModel.actionBar!!.hide()
            viewModel.actionBar!!.addOnMenuVisibilityListener { isVisible: Boolean ->
                viewModel.inActionBarMenu = isVisible
                if (!isVisible) hideEmulatedKeys()
            }
        }
    }

    protected fun detectSoftVisibility() {
        // Change keyboard button image according to soft keyboard visibility
        // How to detect keyboard visibility: http://stackoverflow.com/q/4745988
        viewModel.mContentView = findViewById(android.R.id.content)
        viewModel.mContentView!!.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            viewModel.mContentView!!.getWindowVisibleDisplayFrame(r)
            val screenHeight = viewModel.mContentView!!.rootView.height
            val keypadHeight = screenHeight - r.bottom
            // keyboard is opened
            if (keypadHeight > screenHeight * 0.15) bind.keyboard.buttonKeyboard.setImageResource(R.drawable.ic_keyboard_hide)
            else bind.keyboard.buttonKeyboard.setImageResource(R.drawable.ic_keyboard)
            // keyboard is closed
        }
    }

    protected fun setUpKeyboard() {
        viewModel.fadeOutDelayed = AnimationUtils.loadAnimation(this, R.anim.fade_out_delayed)

        // Preload animation for keyboard button
        viewModel.keyboardFadeIn = AnimationUtils.loadAnimation(this, R.anim.keyboard_fade_in)
        viewModel.keyboardFadeOut = AnimationUtils.loadAnimation(this, R.anim.keyboard_fade_out)
        viewModel.keyboardAlwaysVisible = viewModel.prefs!!.getBoolean(PreferenceConstants.KEY_ALWAYS_VISIBLE, false)
        if (viewModel.keyboardAlwaysVisible) {
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
            val terminal = viewModel.adapter!!.currentTerminalView ?: return@setOnClickListener
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
}