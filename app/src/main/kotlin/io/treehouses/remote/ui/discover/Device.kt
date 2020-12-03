package io.treehouses.remote.ui.discover

import io.treehouses.remote.fragments.DiscoverFragment

class Device {
        lateinit var ip: String
        lateinit var mac: String

        override fun equals(other: Any?): Boolean {
            return this.ip == (other as DiscoverFragment.Device).ip
        }

        fun isComplete(): Boolean {
            return this::ip.isInitialized && this::mac.isInitialized
        }
}