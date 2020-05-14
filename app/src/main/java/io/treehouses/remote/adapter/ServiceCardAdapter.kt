package io.treehouses.remote.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import io.treehouses.remote.Fragments.ServiceCardFragment
import io.treehouses.remote.pojo.ServiceInfo
import java.util.*

class ServiceCardAdapter(fm: FragmentManager?, data: ArrayList<ServiceInfo>) : FragmentStatePagerAdapter(fm!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private val data: ArrayList<ServiceInfo>
    private fun removeHeaders(data: ArrayList<ServiceInfo>): ArrayList<ServiceInfo> {
        val tmp: ArrayList<ServiceInfo> = ArrayList<Any?>(data)
        val iterator: MutableIterator<ServiceInfo> = tmp.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().isHeader()) iterator.remove()
        }
        return tmp
    }

    override fun getCount(): Int {
        return data.size
    }

    fun getData(): ArrayList<ServiceInfo> {
        return data
    }

    override fun getItem(position: Int): Fragment {
        return ServiceCardFragment(data[position])
    }

    override fun getItemPosition(o: Any): Int {
        return PagerAdapter.POSITION_NONE
    }

    override fun notifyDataSetChanged() {
        Collections.sort(data)
        super.notifyDataSetChanged()
    }

    init {
        this.data = removeHeaders(data)
    }
}