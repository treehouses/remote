package io.treehouses.remote.adapter

import android.view.View
import android.widget.Button
import io.treehouses.remote.Fragments.DiscoverFragment
import io.treehouses.remote.R
import io.treehouses.remote.callback.HomeInteractListener

class ViewHolderDiscover internal constructor(v: View, listener: HomeInteractListener) {
    private val discoverBtn: Button = v.findViewById(R.id.discoverBtn)

    init {
        discoverBtn.setOnClickListener {
            listener.openCallFragment(DiscoverFragment())
        }
    }
}