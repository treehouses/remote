package io.treehouses.remote.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import io.treehouses.remote.Fragments.TorTabFragment
import io.treehouses.remote.Fragments.TunnelSSHFragment
import io.treehouses.remote.pojo.ServiceInfo
import java.util.*

class TunnelPageAdapter : FragmentStatePagerAdapter {
    var data: ArrayList<ServiceInfo>? = null
        private set

    constructor(fm: FragmentManager?) : super(fm!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {}
    constructor(fm: FragmentManager?, data: ArrayList<ServiceInfo>) : super(fm!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        this.data = removeHeaders(data)
    }

    private fun removeHeaders(data: ArrayList<ServiceInfo>): ArrayList<ServiceInfo> {
        val tmp = ArrayList(data)
        val iterator = tmp.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().isHeader) iterator.remove()
        }
        return tmp
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getItem(position: Int): Fragment {
        var fragment: Fragment? = null
        if (position == 0) {
            fragment = TorTabFragment()
        } else if (position == 1) {
            fragment = TunnelSSHFragment()
        }
        return fragment!!
    }

    override fun getItemPosition(o: Any): Int {
        return PagerAdapter.POSITION_NONE
    }

    override fun notifyDataSetChanged() {
        Collections.sort(data)
        super.notifyDataSetChanged()
    }
}