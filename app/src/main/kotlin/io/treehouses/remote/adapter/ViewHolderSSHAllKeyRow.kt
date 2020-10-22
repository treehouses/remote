package io.treehouses.remote.adapter

import android.view.ContextMenu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import io.treehouses.remote.R
import io.treehouses.remote.ssh.beans.PubKeyBean
import io.treehouses.remote.callback.KeyMenuListener
import io.treehouses.remote.databinding.RowKeyBinding


class ViewHolderSSHAllKeyRow(private val binding: RowKeyBinding, private val listener: KeyMenuListener) :
        RecyclerView.ViewHolder(binding.root),
        View.OnCreateContextMenuListener,
        MenuItem.OnMenuItemClickListener,
        PopupMenu.OnMenuItemClickListener {

    fun bind(host: PubKeyBean) {
        itemView.setOnCreateContextMenuListener(this)
        binding.keyName.text = host.nickname
        binding.keyType.text = host.getDescription(itemView.context)
        binding.actions.setOnClickListener {
            val popup = PopupMenu(itemView.context, binding.actions)
            popup.inflate(R.menu.keys_menu)
            popup.setOnMenuItemClickListener(this)
            popup.show()
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        val menuInflater = MenuInflater(itemView.context)
        menuInflater.inflate(R.menu.keys_menu, menu)
        menu?.children?.forEach { it.setOnMenuItemClickListener(this) }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.copy_public -> listener.onCopyPub(adapterPosition)
            R.id.delete_key -> listener.onDelete(adapterPosition)
            R.id.send_key -> listener.onSendToRaspberry(adapterPosition)
        }
        return false
    }
}