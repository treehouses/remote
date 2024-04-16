package io.treehouses.remote.fragments.dialogfragments

import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import io.treehouses.remote.interfaces.FragmentDialogInterface
import io.treehouses.remote.R
import io.treehouses.remote.ssh.beans.HostBean
import io.treehouses.remote.bases.BaseTerminalBridge
import io.treehouses.remote.bases.FullScreenDialogFragment
import io.treehouses.remote.databinding.EditHostBinding
import io.treehouses.remote.utils.KeyUtils
import io.treehouses.remote.utils.SaveUtils
import io.treehouses.remote.utils.logD

class EditHostDialogFragment : FullScreenDialogFragment(), FragmentDialogInterface {
    private lateinit var bind : EditHostBinding
    private lateinit var host: HostBean
    private lateinit var initialHostUri: String

    private lateinit var allKeys: List<String>
    private lateinit var dismissListener : DialogInterface.OnDismissListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        bind = EditHostBinding.inflate(inflater, container, false)
        logD("${BaseTerminalBridge.TAG} ARGUMENT: ${arguments?.getString(SELECTED_HOST_URI, "")!!}")
        host = SaveUtils.getHost(requireContext(), arguments?.getString(SELECTED_HOST_URI, "")!!)!!
        initialHostUri = host.uri.toString()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bind.cancelButton.setOnClickListener { dismiss() }

        bind.saveHost.setOnClickListener { saveHost() }

        setUpKeys()

        bind.uriInput.setText(host.getPrettyFormat())

        bind.selectFontSize.minValue = 5
        bind.selectFontSize.maxValue = 15
        bind.selectFontSize.value = host.fontSize
        bind.deleteButton.setOnClickListener {
            deleteHost()
        }
    }

    private fun deleteHost() {
        val a = createAlertDialog(context, R.style.CustomAlertDialogStyle, "Delete Host",
                "Are you sure you want to delete this host?")
                .setPositiveButton("Yes") { dialog: DialogInterface, _: Int ->
                    SaveUtils.deleteHost(requireContext(), host)
                    dialog.dismiss()
                    dismiss()
                }.setNegativeButton("No") { dialog: DialogInterface, _: Int -> dialog.dismiss() }.create()
        a.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        a.show()
    }

    private fun setUpKeys() {
        allKeys = mutableListOf(NO_KEY).plus(KeyUtils.getAllKeyNames(requireContext()))
        bind.selectKey.adapter = ArrayAdapter(
                requireContext(),
                R.layout.key_type_spinner_item,
                R.id.itemTitle,
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
                SaveUtils.updateHost(requireContext(), host.uri.toString(), host.apply { keyName = "" })
                bind.selectKey.setSelection(0)
            }
        }
    }

    private fun saveHost() {
        var uriString = bind.uriInput.text.toString()
        if (!uriString.startsWith("ssh://")) uriString = "ssh://$uriString"
        val uri = Uri.parse(uriString)
        if (uri == null) {
            bind.uriInputLayout.error = "Invalid Uri"
            return
        }
        host.setHostFromUri(uri)
        val keyName = bind.selectKey.selectedItem.toString()
        host.keyName = if (keyName == NO_KEY) "" else keyName
        host.fontSize = bind.selectFontSize.value
        SaveUtils.updateHost(requireContext(), initialHostUri, host)
        dismiss()
    }

    fun setOnDismissListener(dl: DialogInterface.OnDismissListener) {
        dismissListener = dl
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissListener.onDismiss(dialog)
    }

    companion object {
        const val SELECTED_HOST_URI = "SELECTEDHOST"
        const val NO_KEY = "No Key"
    }


}