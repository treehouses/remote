package io.treehouses.remote

import android.annotation.TargetApi
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.ClipboardManager
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.animation.Animation
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayout
import io.treehouses.remote.SSH.PromptHelper
import io.treehouses.remote.SSH.Terminal.TerminalKeyListener
import io.treehouses.remote.SSH.Terminal.TerminalManager
import io.treehouses.remote.SSH.Terminal.TerminalView
import io.treehouses.remote.databinding.ActivitySshConsoleBinding
import io.treehouses.remote.ui.services.ServicesViewModel

open class BaseSSHConsole: AppCompatActivity() {

    protected lateinit var bind: ActivitySshConsoleBinding
    lateinit var viewModel: SSHConsoleViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[SSHConsoleViewModel::class.java]!!
    }

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
        val view = viewModel.adapter?.currentTerminalView

        // Hide all the prompts in case a prompt request was canceled
        hideAllPrompts()
        // we dont have an active view, so hide any prompts
        if (view == null) return
        val prompt = view.bridge.promptHelper
        Log.e("GOT", "HERE ")
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
        if (!viewModel.keyboardAlwaysVisible) {
            if (viewModel.keyboardGroupHider != null) viewModel.handler.removeCallbacks(viewModel.keyboardGroupHider!!)
            bind.keyboard.keyboardGroup.visibility = View.GONE
        }
        hideActionBarIfRequested()
    }

    protected fun hideActionBarIfRequested() {
        if (viewModel.titleBarHide && actionBar != null) actionBar!!.hide()
    }

    private fun setConsolePassword(prompt: PromptHelper) {
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

    private fun hideAllPrompts() {
        bind.consolePasswordGroup.visibility = View.GONE
        bind.consoleBooleanGroup.visibility = View.GONE
    }

    protected fun updateEmptyVisible() {
        // update visibility of empty status message
        viewModel.empty!!.visibility = if (bind.pager.childCount == 0) View.VISIBLE else View.GONE
    }

    /**
     * @param bridge
     */
    protected fun closeBridge() {
        updateEmptyVisible()
        updatePromptVisible()
        // If we just closed the last bridge, go back to the previous activity.
        if (bind.pager.childCount == 0) {
            Log.e("FINISHING SSH", "FINISH")
            finish()
        }
    }

    protected fun onEmulatedKeyClicked(v: View) {
        val terminal = viewModel.adapter!!.currentTerminalView ?: return
        val handler = terminal.bridge.keyHandler
        var hideKeys = sendKeys(v, handler)
        if (hideKeys) hideEmulatedKeys()
        else autoHideEmulatedKeys()

        terminal.bridge.tryKeyVibrate()
        hideActionBarIfRequested()
    }

    private fun sendKeys(v: View, handler: TerminalKeyListener) : Boolean {
        return if (viewModel.isSpecialButton(v, handler)) true
        else {
            viewModel.checkButtons(v, handler)
            false
        }
    }

    protected fun autoHideEmulatedKeys() {
        if (viewModel.keyboardGroupHider != null) viewModel.handler.removeCallbacks(viewModel.keyboardGroupHider!!)
        viewModel.keyboardGroupHider = Runnable {
            if (bind.keyboard.keyboardGroup.visibility == View.GONE || viewModel.inActionBarMenu) {
                return@Runnable
            }
            if (!viewModel.keyboardAlwaysVisible) setAnimation(viewModel.keyboardFadeOut, View.GONE)
            hideActionBarIfRequested()
            viewModel.keyboardGroupHider = null
        }
        viewModel.handler.postDelayed(viewModel.keyboardGroupHider!!, KEYBOARD_DISPLAY_TIME.toLong())
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
        var rotate = viewModel.prefs!!.getString(PreferenceConstants.ROTATION, rotateDefault)
        if (PreferenceConstants.ROTATION_DEFAULT == rotate) rotate = rotateDefault

        // request a forced orientation if requested by user
        when (rotate) {
            PreferenceConstants.ROTATION_LANDSCAPE -> {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                viewModel.forcedOrientation = true
            }
            PreferenceConstants.ROTATION_PORTRAIT -> {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                viewModel.forcedOrientation = true
            }
            else -> {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                viewModel.forcedOrientation = false
            }
        }
    }

    companion object {
        const val TAG = "CB.ConsoleActivity"
        const val KEYBOARD_DISPLAY_TIME = 3000
        const val KEYBOARD_REPEAT_INITIAL = 500
        const val KEYBOARD_REPEAT = 100
        const val STATE_SELECTED_URI = "selectedUri"
    }
}