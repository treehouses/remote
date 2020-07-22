package io.treehouses.remote.adapter

import androidx.recyclerview.widget.RecyclerView
import io.treehouses.remote.SSH.beans.PubKeyBean
import io.treehouses.remote.callback.RVButtonClick
import io.treehouses.remote.databinding.RowKeyBinding

class ViewHolderSSHAllKeyRow(private val binding: RowKeyBinding, private val listener: RVButtonClick) : RecyclerView.ViewHolder(binding.root) {
    fun bind(host: PubKeyBean) {
        binding.copyBtn.setOnClickListener { listener.onButtonClick(adapterPosition) }
        binding.keyName.text = host.nickname
        binding.keyType.text = host.getDescription(itemView.context)
    }
}