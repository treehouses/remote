package io.treehouses.remote.sshconsole

import android.annotation.TargetApi
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.animation.Animation
import androidx.core.app.ActivityCompat
import io.treehouses.remote.PreferenceConstants
import io.treehouses.remote.R
import io.treehouses.remote.ssh.PromptHelper
import io.treehouses.remote.ssh.terminal.TerminalKeyListener
import io.treehouses.remote.ssh.terminal.TerminalView

open class BaseSSHConsole: RootSSHConsole() {

    @TargetApi(11)
    protected fun requestActionBar() {
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY)
    }

    protected fun updatePrompt(b:Boolean, helper: PromptHelper) {
        helper.setResponse(b)
        updatePromptVisible()
    }

    /**
     * Show any prompts requested by the currently visible [TerminalView].
     */
    protected fun updatePromptVisible() {
        // check if our currently-visible terminalbridge is requesting any prompt services
        val view = adapter?.currentTerminalView

        // Hide all the prompts in case a prompt request was canceled
        hideAllPrompts()
        // we dont have an active view, so hide any prompts
        if (view == null) return
        val prompt = view.bridge.promptHelper
        when {
            String::class.java == prompt!!.promptRequested -> {
                hideEmulatedKeys()
                setConsolePassword(prompt)
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

    protected fun hideEmulatedKeys() {
        if (!keyboardAlwaysVisible) {
            if (keyboardGroupHider != null) handler.removeCallbacks(keyboardGroupHider!!)
            bind.keyboard.keyboardGroup.visibility = View.GONE
        }
        hideActionBarIfRequested()
    }

    private fun hideActionBarIfRequested() {
        if (titleBarHide && actionBar != null) actionBar!!.hide()
    }

    private fun setConsolePassword(prompt: PromptHelper) {
        bind.consolePasswordGroup.visibility = View.VISIBLE
        val instructions = prompt.promptInstructions
        if (!instructions.isNullOrEmpty()) {
            bind.consolePasswordInstructions.visibility = View.VISIBLE
            bind.consolePasswordInstructions.text = instructions
        } else bind.consolePasswordInstructions.visibility = View.GONE
        bind.consolePassword.setText("")
        bind.consolePassword.hint = prompt.promptHint
        bind.consolePassword.requestFocus()
    }

    private fun hideAllPrompts() {
        bind.consolePasswordGroup.visibility = View.GONE
        bind.consoleBooleanGroup.visibility = View.GONE
    }

    protected fun updateEmptyVisible() {
        // update visibility of empty status message
        empty!!.visibility = if (bind.pager.childCount == 0) View.VISIBLE else View.GONE
    }

    /**
     * @param bridge
     */
    protected fun closeBridge() {
        updateEmptyVisible()
        updatePromptVisible()
        // If we just closed the last bridge, go back to the previous activity.
        if (bind.pager.childCount == 0) {
            finish()
        }
    }

    protected fun onEmulatedKeyClicked(v: View) {
        val terminal = adapter!!.currentTerminalView ?: return
        val handler = terminal.bridge.keyHandler
        val hideKeys = sendKeys(v, handler)
        if (hideKeys) hideEmulatedKeys()
        else autoHideEmulatedKeys()

        terminal.bridge.tryKeyVibrate()
        hideActionBarIfRequested()
    }

    private fun sendKeys(v: View, handler: TerminalKeyListener) : Boolean {
        return if (isSpecialButton(v, handler)) true
        else {
            checkButtons(v, handler)
            false
        }
    }

    protected fun autoHideEmulatedKeys() {
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

    protected fun setAnimation(animation: Animation?, visibility: Int) {
        bind.keyboard.keyboardGroup.startAnimation(animation)
        bind.keyboard.keyboardGroup.visibility = visibility
    }

    protected fun checkSession(view: TerminalView?, activeTerminal: Boolean): Pair<Boolean, Boolean> {
        var sessionOpen = false
        var disconnected = false
        if (activeTerminal) {
            val bridge = view!!.bridge
            sessionOpen = bridge.isSessionOpen
            disconnected = bridge.isDisconnected
        }
        return Pair(sessionOpen, disconnected)
    }

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

    /**
     *
     */
    protected fun configureOrientation() {
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

    protected fun setIntentRequested() {
        val requestedBridge = bound!!.mNicknameBridgeMap[requested!!.fragment]?.get()
        var requestedIndex = 0
        synchronized(bind.pager) {
            if (requestedBridge == null) {
                // If we didn't find the requested connection, try opening it
                try {
                    bound!!.openConnection(requested)
                } catch (e: Exception) {
                    e.printStackTrace()
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

    /**
     * Displays the child in the ViewPager at the requestedIndex and updates the prompts.
     *
     * @param requestedIndex the index of the terminal view to display
     */
    protected fun setDisplayedTerminal(requestedIndex: Int) {
        bind.pager.currentItem = requestedIndex
        // set activity title
        title = adapter!!.getPageTitle(requestedIndex)
        onTerminalChanged()
    }

    private fun findCurrentView(id: Int): View? {
        val view = bind.pager.findViewWithTag<View>(adapter!!.getBridgeAtPosition(bind.pager.currentItem))
                ?: return null
        return view.findViewById(id)
    }

    /**
     * Called whenever the displayed terminal is changed.
     */
    protected fun onTerminalChanged() {
        val terminalNameOverlay = findCurrentView(R.id.terminal_name_overlay)
        terminalNameOverlay?.startAnimation(fadeOutDelayed)
        updateDefault()
        updatePromptVisible()
        ActivityCompat.invalidateOptionsMenu(this@BaseSSHConsole)
    }

    companion object {
        const val TAG = "CB.ConsoleActivity"
        const val KEYBOARD_DISPLAY_TIME = 3000
        const val KEYBOARD_REPEAT_INITIAL = 500
        const val KEYBOARD_REPEAT = 100
        const val STATE_SELECTED_URI = "selectedUri"
    }
}