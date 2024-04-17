package io.treehouses.remote.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import io.treehouses.remote.ssh.beans.HostBean
import io.treehouses.remote.callback.RVButtonClickListener
import io.treehouses.remote.databinding.RowSshBinding

class ViewHolderSSHRow(private val binding: RowSshBinding, private val listener: RVButtonClickListener) : RecyclerView.ViewHolder(binding.root) {
    fun bind(host: HostBean) {
        binding.title.text = host.getPrettyFormat()
        binding.editButton.setOnClickListener {
            val position = bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onButtonClick(position)
            }
        }
    }

    fun setConnected(isConnected: Boolean) {
        binding.status.visibility = if(isConnected) View.VISIBLE else View.INVISIBLE
    }
}