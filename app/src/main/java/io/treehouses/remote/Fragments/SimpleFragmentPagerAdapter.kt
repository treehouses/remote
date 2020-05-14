package io.treehouses.remote.Fragments

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class SimpleFragmentPagerAdapter(private val mContext: Context, fm: FragmentManager?, private val f1: Fragment, private val f2: Fragment) : FragmentPagerAdapter(fm!!) {

    // This determines the fragment for each tab
    override fun getItem(position: Int): Fragment {
        if (position == 0) {
            return f1
        } else if (position == 1) {
            return f2
        }
        return null
    }

    // This determines the number of tabs
    override fun getCount(): Int {
        return 2
    }

    // This determines the title for each tab
    override fun getPageTitle(position: Int): CharSequence? {
        // Generate title based on item position
        return when (position) {
            0 -> "Overview"
            1 -> "Details"
            else -> null
        }
    }

}