package io.treehouses.remote.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import io.treehouses.remote.R
import io.treehouses.remote.bases.BaseFragment
import io.treehouses.remote.callback.ServicesListener
import io.treehouses.remote.databinding.ActivitySshTunnelFragmentBinding
import io.treehouses.remote.pojo.ServiceInfo
import io.treehouses.remote.ui.socks.SocksFragment
import io.treehouses.remote.ui.tor.TorFragment
import io.treehouses.remote.ui.sshtunnel.SSHTunnelFragment

class TunnelFragment : BaseFragment(), ServicesListener, OnItemSelectedListener, OnPageChangeListener {

    private var tabLayout: TabLayout? = null
//    private var tunnelView: TunnelViewPager? = null
//    private var tunnelPageAdapter: TunnelPageAdapter? = null
    private lateinit var bind: ActivitySshTunnelFragmentBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = ActivitySshTunnelFragmentBinding.inflate(inflater, container, false)
        tabLayout = bind.tabLayout
        tabLayout!!.tabGravity = TabLayout.GRAVITY_FILL
        tabLayout!!.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                replaceFragment(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        childFragmentManager.beginTransaction().replace(R.id.tab_content, TorFragment()).commit()
        return bind.root
    }

    fun replaceFragment(position: Int) {
        var fragment: Fragment? = null
        when (position) {
            0 -> fragment = TorFragment()
            1 -> fragment = SSHTunnelFragment()
            2 -> fragment = SocksFragment()
        }
//        tunnelView!!.currentItem = position
        childFragmentManager.beginTransaction().replace(R.id.tab_content, fragment!!).commit()
    }

    //
    fun setTabEnabled(enabled: Boolean) {
        val tabStrip = tabLayout!!.getChildAt(0) as LinearLayout
        tabStrip.isEnabled = enabled
        for (i in 0 until tabStrip.childCount) {
            tabStrip.getChildAt(i).isClickable = enabled
        }
    }

    override fun onClick(s: ServiceInfo?) {
        tabLayout!!.getTabAt(1)!!.select()
        replaceFragment(1)
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        tabLayout!!.setScrollPosition(position, 0f, true)
    }

    override fun onPageSelected(position: Int) {
    }

    override fun onPageScrollStateChanged(state: Int) {}
}