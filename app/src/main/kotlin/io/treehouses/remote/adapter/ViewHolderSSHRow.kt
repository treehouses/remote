package io.treehouses.remote.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import io.treehouses.remote.SSH.beans.HostBean
import io.treehouses.remote.callback.RVButtonClick
import io.treehouses.remote.databinding.RowSshBinding

class ViewHolderSSHRow(private val binding: RowSshBinding, private val listener: RVButtonClick) : RecyclerView.ViewHolder(binding.root) {
    fun bind(host: HostBean) {
        binding.title.text = host.getPrettyFormat()
        binding.editButton.setOnClickListener {
            listener.onButtonClick(adapterPosition)
        }
    }

    fun setConnected(isConnected: Boolean) {
        binding.status.visibility = if(isConnected) View.VISIBLE else View.INVISIBLE
    }
}