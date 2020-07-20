package io.treehouses.remote.adapter

import androidx.recyclerview.widget.RecyclerView
import io.treehouses.remote.SSH.beans.HostBean
import io.treehouses.remote.databinding.RowSshBinding

class ViewHolderSSHRow(private val binding: RowSshBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(host: HostBean) {
        binding.title.text = host.getPrettyFormat()
    }
}