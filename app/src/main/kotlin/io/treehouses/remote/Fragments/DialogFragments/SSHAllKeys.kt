package io.treehouses.remote.Fragments.DialogFragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import io.treehouses.remote.SSH.beans.HostBean
import io.treehouses.remote.bases.FullScreenDialogFragment
import io.treehouses.remote.databinding.EditHostBinding
import io.treehouses.remote.utils.KeyUtils
import io.treehouses.remote.utils.SaveUtils
import java.lang.Exception

class SSHAllKeys : FullScreenDialogFragment() {
    private lateinit var bind : EditHostBinding

    private lateinit var allKeys: List<String>
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = EditHostBinding.inflate(inflater, container, false)
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    private fun setUpKeys() {
        allKeys = mutableListOf(NO_KEY).plus(KeyUtils.getAllKeyNames(requireContext()))
        bind.selectKey.adapter = ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                android.R.id.text1,
                allKeys)

    }

    companion object {
        const val SELECTED_HOST_URI = "SELECTEDHOST"
        const val NO_KEY = "No Key"
    }


}