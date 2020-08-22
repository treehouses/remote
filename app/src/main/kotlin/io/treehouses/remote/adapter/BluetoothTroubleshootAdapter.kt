package io.treehouses.remote.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import io.treehouses.remote.databinding.ListGroupBinding
import io.treehouses.remote.databinding.RowTroubleshootBinding

class BluetoothTroubleshootAdapter(private val context: Context, private val questions: List<String>, private val answers: List<String>) : BaseExpandableListAdapter() {
    override fun getGroupCount(): Int = questions.size

    override fun getChildrenCount(groupPosition: Int): Int = 1

    override fun getGroup(groupPosition: Int): Any = questions[groupPosition]

    override fun getChild(groupPosition: Int, childPosition: Int): Any = answers[groupPosition]

    override fun getGroupId(groupPosition: Int): Long = 0

    override fun getChildId(groupPosition: Int, childPosition: Int): Long = 0

    override fun hasStableIds(): Boolean = false

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val groupBind = ListGroupBinding.inflate(layoutInflater, parent, false)
        groupBind.lblListHeader.text = questions[groupPosition]
        return groupBind.root
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        val rowBinding = RowTroubleshootBinding.inflate(context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater, parent, false).apply {
            answer.text = answers[groupPosition]
        }
        return rowBinding.root
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }
}