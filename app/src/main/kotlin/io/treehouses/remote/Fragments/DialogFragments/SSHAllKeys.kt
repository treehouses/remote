package io.treehouses.remote.Fragments.DialogFragments

import android.R.attr.label
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.RecyclerView
import io.treehouses.remote.SSH.PubKeyUtils
import io.treehouses.remote.SSH.beans.PubKeyBean
import io.treehouses.remote.adapter.ViewHolderSSHAllKeyRow
import io.treehouses.remote.bases.FullScreenDialogFragment
import io.treehouses.remote.callback.RVButtonClick
import io.treehouses.remote.databinding.DialogViewKeysBinding
import io.treehouses.remote.databinding.RowKeyBinding
import io.treehouses.remote.utils.KeyUtils


class SSHAllKeys : FullScreenDialogFragment() {
    private lateinit var bind : DialogViewKeysBinding

    private lateinit var allKeys: List<PubKeyBean>
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = DialogViewKeysBinding.inflate(inflater, container, false)
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind.doneBtn.setOnClickListener { dismiss() }
        setUpKeys()
    }

    private fun setUpKeys() {
        allKeys = KeyUtils.getAllKeys(requireContext())
        if (allKeys.isNullOrEmpty()) {
            bind.noHosts.visibility = View.VISIBLE
            bind.allKeys.visibility = View.GONE
        } else {
            bind.noHosts.visibility = View.GONE
            bind.allKeys.visibility = View.VISIBLE
        }

        setUpAdapter()
    }

    private fun setUpAdapter() {
        bind.allKeys.adapter = object : RecyclerView.Adapter<ViewHolderSSHAllKeyRow>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderSSHAllKeyRow {
                val binding = RowKeyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return ViewHolderSSHAllKeyRow(binding, object : RVButtonClick {
                    override fun onButtonClick(position: Int) {
                        copyToClipboard(allKeys[position])
                    }
                })
            }

            override fun getItemCount(): Int {
                return allKeys.size
            }

            override fun onBindViewHolder(holder: ViewHolderSSHAllKeyRow, position: Int) {
                holder.bind(allKeys[position])
            }
        }
    }

    private fun copyToClipboard(pubkey: PubKeyBean) {
        val decodedPublic = PubKeyUtils.decodePublic(pubkey.publicKey!!, pubkey.type)
        val openSSH = PubKeyUtils.convertToOpenSSHFormat(decodedPublic, pubkey.nickname)
        val clipboard: ClipboardManager? = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clip = ClipData.newPlainText(pubkey.getDescription(requireContext()), openSSH)
        clipboard?.setPrimaryClip(clip)?.let {
            Toast.makeText(requireContext(), "Copied ${pubkey.nickname} public key to clipboard", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        const val SELECTED_HOST_URI = "SELECTEDHOST"
        const val NO_KEY = "No Key"
    }


}