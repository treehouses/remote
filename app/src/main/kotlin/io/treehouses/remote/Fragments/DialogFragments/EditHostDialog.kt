package io.treehouses.remote.Fragments.DialogFragments

import android.net.Uri
import android.os.Bundle
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

class EditHostDialog : FullScreenDialogFragment() {
    private lateinit var bind : EditHostBinding
    private lateinit var host: HostBean
    private lateinit var initialHostUri: String

    private lateinit var allKeys: List<String>
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = EditHostBinding.inflate(inflater, container, false)
        host = SaveUtils.getHost(requireContext(), arguments?.getString(SELECTED_HOST_URI, "")!!)!!
        initialHostUri = host.uri.toString()
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bind.cancelButton.setOnClickListener { dismiss() }

        bind.saveHost.setOnClickListener {
            try {
                val uri = Uri.parse(bind.uriInput.text.toString())
                host.setHostFromUri(uri)
            } catch (e: Exception) {
                bind.uriInputLayout.error = "Invalid Uri"
                return@setOnClickListener
            }
            val keyName = bind.selectKey.selectedItem.toString()
            host.keyName = if (keyName == NO_KEY) "" else keyName
            host.fontSize = bind.selectFontSize.value
            SaveUtils.updateHost(requireContext(), initialHostUri, host)
            dismiss()
        }

        setUpKeys()

        bind.uriInput.setText(host.getPrettyFormat())

        bind.selectFontSize.minValue = 5
        bind.selectFontSize.maxValue = 15
        bind.selectFontSize.value = host.fontSize

    }

    private fun setUpKeys() {
        allKeys = mutableListOf(NO_KEY).plus(KeyUtils.getAllKeyNames(requireContext()))
        bind.selectKey.adapter = ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                android.R.id.text1,
                allKeys)

        when {
            host.keyName in allKeys -> {
                bind.selectKey.setSelection(allKeys.indexOf(host.keyName))
            }
            host.keyName.isEmpty() -> {
                bind.selectKey.setSelection(0)
            }
            else -> {
                Toast.makeText(requireContext(), "Unknown Key", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        const val SELECTED_HOST_URI = "SELECTEDHOST"
        const val NO_KEY = "No Key"
    }


}