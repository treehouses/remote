package io.treehouses.remote

import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.PagerAdapter
import com.google.android.material.tabs.TabLayout
import io.treehouses.remote.SSH.Terminal.TerminalBridge
import io.treehouses.remote.SSH.Terminal.TerminalManager
import io.treehouses.remote.SSH.Terminal.TerminalView
import io.treehouses.remote.databinding.ActivitySshConsoleBinding

class TerminalPagerAdapter(private val bound: TerminalManager?, private val promptHandler: Handler, private val layoutInflater: LayoutInflater,
                           private val bind: ActivitySshConsoleBinding, private val fadeOutDelayed: Animation?) : PagerAdapter() {
    private lateinit var terminalPager: TerminalPager
    override fun getCount(): Int {
        return if (bound != null) bound!!.bridges.size
        else 0
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        if (bound == null || bound!!.bridges.size <= position) Log.w(BaseSSHConsole.TAG, "Activity not bound when creating TerminalView.")
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
        if (bound == null) return POSITION_NONE
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
        if (bound == null) return null
        val bridges = bound!!.bridges
        return if (position < 0 || position >= bridges.size) null
        else bridges[position]
    }

    override fun notifyDataSetChanged() {
        super.notifyDataSetChanged()
        terminalPager.handleData()
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun getPageTitle(position: Int): CharSequence? {
        val bridge = getBridgeAtPosition(position) ?: return "???"
        return "Treehouses Remote: " + bridge.host!!.nickname
    }

    fun setTerminalPager(tp: TerminalPager) {
        terminalPager = tp
    }
}