package io.treehouses.remote

import android.content.SharedPreferences
import android.net.Uri
import android.os.Handler
import android.text.ClipboardManager
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModel
import com.google.android.material.tabs.TabLayout
import io.treehouses.remote.SSH.Terminal.TerminalManager

class SSHConsoleViewModel: ViewModel() {
    //    protected var pager: TerminalViewPager? = null
    protected var tabs: TabLayout? = null
    protected var toolbar: Toolbar? = null
    protected var bound: TerminalManager? = null
    protected var adapter: SSHConsole.TerminalPagerAdapter? = null
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
}