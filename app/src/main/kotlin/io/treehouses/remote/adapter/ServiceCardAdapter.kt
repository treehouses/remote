package io.treehouses.remote.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import io.treehouses.remote.Fragments.ServiceCardFragment
import io.treehouses.remote.pojo.ServiceInfo
import java.util.*

class ServiceCardAdapter(fm: FragmentManager?, data: MutableList<ServiceInfo>) : FragmentStatePagerAdapter(fm!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    var data: ArrayList<ServiceInfo>
    private fun removeHeaders(data: MutableList<ServiceInfo>): ArrayList<ServiceInfo> {
        val tmp = ArrayList(data)
        val iterator = tmp.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().isHeader) iterator.remove()
        }
        return tmp
    }

    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(position: Int): Fragment {
        val b = Bundle()
        b.putSerializable("serviceData", data[position])
        val f: Fragment = ServiceCardFragment()
        f.arguments = b
        return f
    }

    override fun getItemPosition(o: Any): Int {
        return PagerAdapter.POSITION_NONE
    }

    override fun notifyDataSetChanged() {
        this.data = removeHeaders(data)
        data.sort()
        super.notifyDataSetChanged()
    }

    init {
        this.data = removeHeaders(data)
    }
}