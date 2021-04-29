package io.treehouses.remote.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import io.treehouses.remote.fragments.SocksFragment
import io.treehouses.remote.ui.tortab.TorTabFragment
import io.treehouses.remote.fragments.TunnelSSHFragment
import io.treehouses.remote.pojo.ServiceInfo
import java.util.*

class TunnelPageAdapter : FragmentStatePagerAdapter {
    var data: ArrayList<ServiceInfo>? = null
        private set


    constructor(fm: FragmentManager?) : super(fm!!, BEHAVIOR_SET_USER_VISIBLE_HINT) {}


    override fun getCount(): Int {
        return 3
    }

    override fun getItem(position: Int): Fragment {
        var fragment: Fragment? = null
        if (position == 0) {
            fragment = TorTabFragment()
        } else if (position == 1) {
            fragment = TunnelSSHFragment()
        }
     else if (position == 2) {
        fragment = SocksFragment()
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