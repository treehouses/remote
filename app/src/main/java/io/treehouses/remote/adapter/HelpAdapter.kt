package io.treehouses.remote.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import io.treehouses.remote.Views.HelpViewHolder
import io.treehouses.remote.databinding.RowHelpBinding
import io.treehouses.remote.pojo.HelpCommand

class HelpAdapter : RecyclerView.Adapter<HelpViewHolder>() {

    private val sortedList: SortedList<HelpCommand> = SortedList(HelpCommand::class.java, object : SortedList.Callback<HelpCommand>() {
        override fun areItemsTheSame(item1: HelpCommand?, item2: HelpCommand?): Boolean {
            return item1?.title == item2?.title && item1?.preview == item2?.preview
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) { notifyItemMoved(fromPosition, toPosition)}

        override fun onChanged(position: Int, count: Int) { notifyItemRangeChanged(position, count) }

        override fun onInserted(position: Int, count: Int) { notifyItemRangeInserted(position, count) }

        override fun onRemoved(position: Int, count: Int) { notifyItemRangeRemoved(position, count) }

        override fun compare(o1: HelpCommand, o2: HelpCommand): Int { return o1.title.compareTo(o2.title) }

        override fun areContentsTheSame(oldItem: HelpCommand?, newItem: HelpCommand?): Boolean { return areItemsTheSame(oldItem, newItem) }
    })

    fun add(model: HelpCommand?) { sortedList.add(model) }

    fun remove(model: HelpCommand?) { sortedList.remove(model) }

    fun add(models: List<HelpCommand>) { sortedList.addAll(models) }

    fun getitem(position: Int) : HelpCommand { return sortedList.get(position) }

    fun remove(models: List<HelpCommand?>) {
        sortedList.beginBatchedUpdates()
        for (model in models) sortedList.remove(model)
        sortedList.endBatchedUpdates()
    }

    fun replaceAll(models: List<HelpCommand?>) {
        sortedList.beginBatchedUpdates()
        for (i in sortedList.size() - 1 downTo 0) {
            val model: HelpCommand = sortedList.get(i)
            if (!models.contains(model)) sortedList.remove(model)
        }
        sortedList.addAll(models)
        sortedList.endBatchedUpdates()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HelpViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return HelpViewHolder(RowHelpBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: HelpViewHolder, position: Int) {
        val value = sortedList.get(position)
        holder.bind(value)
    }

    override fun getItemCount(): Int = sortedList.size()

}