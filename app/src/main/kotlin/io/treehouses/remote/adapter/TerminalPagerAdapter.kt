package io.treehouses.remote.adapter

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import io.treehouses.remote.sshconsole.BaseSSHConsole
import io.treehouses.remote.R
import io.treehouses.remote.ssh.terminal.TerminalBridge
import io.treehouses.remote.ssh.terminal.TerminalView
import io.treehouses.remote.callback.TerminalPager

class TerminalPagerAdapter : PagerAdapter() {
    private lateinit var terminalPager: TerminalPager

    override fun getCount(): Int {
        return if (terminalPager.getManager() != null) terminalPager.getManager()!!.bridges.size
        else 0
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        if (terminalPager.getManager() == null || terminalPager.getManager()!!.bridges.size <= position) Log.w(BaseSSHConsole.TAG, "Activity not bound when creating TerminalView.")
        val bridge = terminalPager.getManager()!!.bridges[position]
        bridge.promptHelper!!.setHandler(terminalPager.getHandler())

        // inflate each terminal view
        val view = terminalPager.getInflater().inflate(R.layout.item_terminal, container, false) as RelativeLayout

        // set the terminal name overlay text
        val terminalNameOverlay = view.findViewById<TextView>(R.id.terminal_name_overlay)
        terminalNameOverlay.text = bridge.host!!.nickname

        // and add our terminal view control, using index to place behind overlay
        val terminal = TerminalView(container.context, bridge, terminalPager.getPager())
        terminal.id = R.id.terminal_view
        view.addView(terminal, 0)

        // Tag the view with its bridge so it can be retrieved later.
        view.tag = bridge
        container.addView(view)
        terminalNameOverlay.startAnimation(terminalPager.getAnimation())
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val view = `object` as View
        container.removeView(view)
    }

    override fun getItemPosition(`object`: Any): Int {
        if (terminalPager.getManager() == null) return POSITION_NONE
        val view = `object` as View
        val terminal: TerminalView = view.findViewById(R.id.terminal_view)
        val host = terminal.bridge.host
        var itemIndex = POSITION_NONE
        var i = 0
        for (bridge in terminalPager.getManager()!!.bridges) {
            if (bridge.host == host) {
                itemIndex = i
                break
            }
            i++
        }
        return itemIndex
    }

    fun getBridgeAtPosition(position: Int): TerminalBridge? {
        if (terminalPager.getManager() == null) return null
        val bridges = terminalPager.getManager()!!.bridges
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

    val currentTerminalView: TerminalView?
        get() {
            val currentView = terminalPager.getPager().findViewWithTag<View>(getBridgeAtPosition(terminalPager.getPager().currentItem))
                    ?: return null
            return currentView.findViewById<View>(R.id.terminal_view) as TerminalView
        }

    fun setTerminalPager(tp: TerminalPager) {
        terminalPager = tp
    }
}