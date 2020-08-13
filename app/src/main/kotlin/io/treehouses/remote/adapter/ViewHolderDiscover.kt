package io.treehouses.remote.adapter

import android.view.View
import android.widget.TextView
import io.treehouses.remote.Fragments.DiscoverFragment
import io.treehouses.remote.R
import io.treehouses.remote.callback.HomeInteractListener

class ViewHolderDiscover internal constructor(v: View, listener: HomeInteractListener) {
    private val discoverText: TextView = v.findViewById(R.id.discover_text)

    init {
        discoverText.setOnClickListener {
            listener.openCallFragment(DiscoverFragment())
        }
    }
}