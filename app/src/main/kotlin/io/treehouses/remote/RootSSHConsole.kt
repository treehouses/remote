package io.treehouses.remote

import android.content.SharedPreferences
import android.net.Uri
import android.os.Handler
import android.text.ClipboardManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.tabs.TabLayout
import io.treehouses.remote.SSH.Terminal.TerminalKeyListener
import io.treehouses.remote.SSH.Terminal.TerminalManager
import io.treehouses.remote.SSH.Terminal.TerminalView
import io.treehouses.remote.Views.terminal.vt320
import io.treehouses.remote.bases.BaseTerminalKeyListener
import io.treehouses.remote.databinding.ActivitySshConsoleBinding

open class RootSSHConsole: AppCompatActivity() {
    //    protected var pager: TerminalViewPager? = null
    protected var tabs: TabLayout? = null
    protected var toolbar: Toolbar? = null
    protected var bound: TerminalManager? = null
    protected var adapter: TerminalPagerAdapter? = null
    protected var prefs: SharedPreferences? = null

    // determines whether or not menuitem accelerators are bound
    // otherwise they collide with an external keyboard's CTRL-char
    protected var hardKeyboard = false
    protected var requested: Uri? = null
    protected var clipboard: ClipboardManager? = null
    //    protected var keyboardGroup: LinearLayout? = null
    protected var keyboardGroupHider: Runnable? = null
    protected var empty: TextView? = null
    protected var fadeOutDelayed: Animation? = null
    protected var keyboardFadeIn: Animation? = null
    protected var keyboardFadeOut: Animation? = null
    protected var disconnect: MenuItem? = null
    protected var paste: MenuItem? = null
    protected var resize: MenuItem? = null
    protected var urlScan: MenuItem? = null
    protected var forcedOrientation = false
    protected var handler = Handler()
    protected var mContentView: View? = null
    protected var actionBar: ActionBar? = null
    protected var inActionBarMenu = false
    protected var titleBarHide = false
    protected var keyboardAlwaysVisible = false
    protected lateinit var bind: ActivitySshConsoleBinding
    protected val currentTerminalView: TerminalView?
        get() {
            val currentView = bind.pager.findViewWithTag<View>(adapter?.getBridgeAtPosition(bind.pager.currentItem))
                    ?: return null
            return currentView.findViewById<View>(R.id.terminal_view) as TerminalView
        }

    protected fun isSpecialButton(v: View, handler: TerminalKeyListener) : Boolean {
        var flag = true
        when (v.id) {
            R.id.button_ctrl -> handler.metaPress(BaseTerminalKeyListener.OUR_CTRL_ON, true)
            R.id.button_esc -> handler.sendEscape()
            R.id.button_tab -> handler.sendTab()
            else -> flag = false
        }
        return flag
    }

    protected fun checkButtons(v: View, handler: TerminalKeyListener) {
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

    protected fun setDisconnectItemListener() {
        disconnect!!.setOnMenuItemClickListener {
            // disconnect or close the currently visible session
            val terminalView = currentTerminalView
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
    protected fun updateDefault() {
        // update the current default terminal
        val view = currentTerminalView
        if (view == null || bound == null) {
            return
        }
        bound!!.defaultBridge = view.bridge
    }

    private fun pasteIntoTerminal() {
        // force insert of clipboard text into current console
        val terminalView = currentTerminalView
        val bridge = terminalView!!.bridge

        // pull string from clipboard and generate all events to force down
        var clip = ""
        if (clipboard!!.hasText()) {
            clip = clipboard!!.text.toString()
        }
        bridge.injectString(clip)
    }

    protected fun setPasteItemListener() {
        paste!!.setOnMenuItemClickListener {
            pasteIntoTerminal()
            true
        }
    }
}