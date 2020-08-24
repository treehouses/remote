package io.treehouses.remote

import android.annotation.TargetApi
import android.content.SharedPreferences
import android.net.Uri
import android.os.Handler
import android.text.ClipboardManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.animation.Animation
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModel
import com.google.android.material.tabs.TabLayout
import io.treehouses.remote.SSH.PromptHelper
import io.treehouses.remote.SSH.Terminal.TerminalBridge
import io.treehouses.remote.SSH.Terminal.TerminalKeyListener
import io.treehouses.remote.SSH.Terminal.TerminalManager
import io.treehouses.remote.SSH.Terminal.TerminalView
import io.treehouses.remote.Views.terminal.vt320
import io.treehouses.remote.bases.BaseTerminalKeyListener

class SSHConsoleViewModel: ViewModel() {
    //    var pager: TerminalViewPager? = null
    var tabs: TabLayout? = null
    var toolbar: Toolbar? = null
    var bound: TerminalManager? = null
    var adapter: SSHConsole.TerminalPagerAdapter? = null
    var prefs: SharedPreferences? = null

    // determines whether or not menuitem accelerators are bound
    // otherwise they collide with an external keyboard's CTRL-char
    var hardKeyboard = false
    var requested: Uri? = null
    var clipboard: ClipboardManager? = null
    //    var keyboardGroup: LinearLayout? = null
    var keyboardGroupHider: Runnable? = null
    var empty: TextView? = null
    var fadeOutDelayed: Animation? = null
    var keyboardFadeIn: Animation? = null
    var keyboardFadeOut: Animation? = null
    var disconnect: MenuItem? = null
    var paste: MenuItem? = null
    var resize: MenuItem? = null
    var urlScan: MenuItem? = null
    var forcedOrientation = false
    var handler = Handler()
    var mContentView: View? = null
    var actionBar: ActionBar? = null
    var inActionBarMenu = false
    var titleBarHide = false
    var keyboardAlwaysVisible = false

    fun isSpecialButton(v: View, handler: TerminalKeyListener) : Boolean {
        var flag = true
        when (v.id) {
            R.id.button_ctrl -> handler.metaPress(BaseTerminalKeyListener.OUR_CTRL_ON, true)
            R.id.button_esc -> handler.sendEscape()
            R.id.button_tab -> handler.sendTab()
            else -> flag = false
        }
        return flag
    }

    fun checkButtons(v: View, handler: TerminalKeyListener) {
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
            else -> Log.e(BaseSSHConsole.TAG, "Unknown emulated key clicked: " + v.id)
        }
    }

    fun setDisconnectItemListener() {
        disconnect!!.setOnMenuItemClickListener {
            // disconnect or close the currently visible session
            val terminalView = adapter!!.currentTerminalView
            val bridge = terminalView!!.bridge
            bridge.dispatchDisconnect(true)
            true
        }
    }

    /**
     * Save the currently shown [TerminalView] as the default. This is
     * saved back down into [TerminalManager] where we can read it again
     * later.
     */
    fun updateDefault() {
        // update the current default terminal
        val view = adapter!!.currentTerminalView
        if (view == null || bound == null) {
            return
        }
        bound!!.defaultBridge = view.bridge
    }

    fun pasteIntoTerminal() {
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

    fun setPasteItemListener() {
        paste!!.setOnMenuItemClickListener {
            pasteIntoTerminal()
            true
        }
    }

}