package io.treehouses.remote.Fragments.DialogFragments.BottomSheetDialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.treehouses.remote.R
import io.treehouses.remote.bases.BaseBottomSheetDialog

class TorBottomSheet : BaseBottomSheetDialog() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.dialog_tor, container, false)
        val mobileArray = arrayOf("Android", "IPhone", "WindowsMobile", "Blackberry",
                "WebOS", "Ubuntu", "Windows7", "Max OS X")
        return v
    }
}