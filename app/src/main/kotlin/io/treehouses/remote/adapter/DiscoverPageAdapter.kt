package io.treehouses.remote.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import io.treehouses.remote.Fragments.DiscoverNetworkFragment
import io.treehouses.remote.Fragments.TunnelSSHFragment

class DiscoverPageAdapter : FragmentStatePagerAdapter {

    constructor(fm: FragmentManager) : super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)

    override fun getItem(position: Int): Fragment {
        var fragment : Fragment? = null
        when(position) {
            0 -> fragment = TunnelSSHFragment()
//            1 -> TODO("ADD Discover Gateway Fragment")
            1 -> fragment = DiscoverNetworkFragment()
        }
        return fragment!!
    }

    override fun getCount(): Int {
        return 2
    }


}