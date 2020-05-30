package io.treehouses.remote.Views

import androidx.recyclerview.widget.RecyclerView
import io.treehouses.remote.databinding.RowHelpBinding
import io.treehouses.remote.pojo.HelpCommand

class HelpViewHolder(binding: RowHelpBinding) : RecyclerView.ViewHolder(binding.root) {
    private val mBinding: RowHelpBinding = binding
    fun bind(data: HelpCommand) {
        mBinding.helpCommand.text = data.title
        mBinding.commandPreview.text = data.preview
    }
}