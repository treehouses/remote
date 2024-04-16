package io.treehouses.remote.adapter

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import androidx.annotation.RequiresApi
import io.treehouses.remote.R
import io.treehouses.remote.callback.HomeInteractListener
import io.treehouses.remote.pojo.NetworkListItem

class SystemListAdapter(val context: Context, list: List<NetworkListItem>) : BaseExpandableListAdapter() {
    private val list: List<NetworkListItem>
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var listener: HomeInteractListener? = null
    private val views: MutableList<View>
    private val groupHeader: MutableList<View>
    fun setListener(listener: HomeInteractListener?) {
        this.listener = listener
        if (listener == null) {
            throw RuntimeException("Please implement home interact listener")
        }
    }

    fun getViews(): List<View?> {
        return groupHeader
    }

    override fun getGroupCount(): Int {
        return list.size
    }

    override fun getChildrenCount(position: Int): Int {
        return if (position > list.size - 1) 0 else 1
    }

    override fun getGroup(i: Int): Any {
        return list[i].title
    }

    override fun getChild(i: Int, i1: Int): Any {
        return list[i].layout
    }

    override fun getGroupId(i: Int): Long {
        return 0
    }

    override fun getChildId(i: Int, i1: Int): Long {
        return 0
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(i: Int, b: Boolean, convertView: View?, parent: ViewGroup): View? {
        val newView = inflater.inflate(R.layout.list_group, parent, false)
        val listHeader = newView?.findViewById<TextView>(R.id.lblListHeader)
        listHeader?.text = getGroup(i).toString()
        groupHeader.add(newView)
        return newView
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun getChildView(i: Int, i1: Int, b: Boolean, convertView: View?, parent: ViewGroup): View {

        // Needs to recycle views instead of creating new ones each time.
        // if (convertView == null) creating bugs
        if (views.size > i) {
            return views[i]
        }
        val newView: View = inflater.inflate(list[i].layout, parent, false)
        layout = list[i].layout
        position = i
        when (layout) {
            R.layout.configure_shutdown_reboot -> ViewHolderShutdownReboot(newView, context, listener!!)
            R.layout.open_vnc -> ViewHolderVnc(newView, context, listener!!)
            R.layout.configure_tethering -> ViewHolderTether(newView, listener!!, context)
            R.layout.configure_ssh_key -> ViewHolderSSHKey(newView, context, listener!!)
            R.layout.configure_camera -> ViewHolderCamera(newView, context, listener!!)
            R.layout.configure_blocker -> ViewHolderBlocker(newView, context, listener!!)
            R.layout.configure_ssh2fa -> ViewHolderSSH2FA(newView, context, listener!!)
        }
        views.add(newView)
        return newView
    }

    override fun isChildSelectable(i: Int, i1: Int): Boolean {
        return false
    }

    companion object {
        var layout = 0
            private set
        var position = 0
    }

    init {
        this.list = list
        views = mutableListOf()
        groupHeader = mutableListOf()
    }
}
