package io.treehouses.remote.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.Button
import android.widget.TextView
import io.treehouses.remote.R
import io.treehouses.remote.pojo.NetworkProfile
import io.treehouses.remote.utils.SaveUtils
import java.util.*

class ProfilesListAdapter(private val context: Context, titles: List<String>, data: HashMap<String, List<NetworkProfile>?>) : BaseExpandableListAdapter() {
    private val titles: List<String>
    private val data: HashMap<String, List<NetworkProfile>?>
    override fun getGroupCount(): Int {
        return titles.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return if (isChildEmpty(groupPosition)) {
            1
        } else data[titles[groupPosition]]!!.size
    }

    override fun getGroup(groupPosition: Int): Any {
        return "YEEY"
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return data[titles[groupPosition]]!![childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return 0
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return 0
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View, parent: ViewGroup): View {
        var convertView = convertView
        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        convertView = layoutInflater.inflate(R.layout.list_group, null)
        val label = convertView.findViewById<TextView>(R.id.lblListHeader)
        label.text = titles[groupPosition]
        return convertView
    }

    private fun isChildEmpty(groupPosition: Int): Boolean {
        return data[titles[groupPosition]] == null || data[titles[groupPosition]]!!.size == 0
    }

    private fun setLabelText(label: TextView, s: String, delete: Button) {
        label.text = s
        label.setTextColor(context.resources.getColor(R.color.md_grey_700))
        delete.visibility = View.GONE
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View, parent: ViewGroup): View {
        var convertView = convertView
        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        convertView = layoutInflater.inflate(R.layout.row_profile, null)
        val label = convertView.findViewById<TextView>(R.id.label)
        val deleteProfile = convertView.findViewById<Button>(R.id.delete_profile)
        if (groupPosition == 3) {
            setLabelText(label, "Switch to Default Network", deleteProfile)
            return convertView
        }
        if (isChildEmpty(groupPosition)) {
            setLabelText(label, "Please configure in the Network screen", deleteProfile)
            return convertView
        }
        label.setText(data[titles[groupPosition]]!![childPosition].ssid)
        deleteProfile.setOnClickListener { showConfirmation(data[titles[groupPosition]]!![childPosition].ssid, groupPosition, childPosition) }
        return convertView
    }

    private fun showConfirmation(name: String, groupPosition: Int, childPosition: Int) {
        val alertDialog = AlertDialog.Builder(context)
                .setTitle("Delete Profile?")
                .setMessage("Are you sure you want to delete the following Network Profile: $name")
                .setPositiveButton("Delete") { dialog, which ->
                    SaveUtils.deleteProfile(context, groupPosition, childPosition)
                    data[titles[groupPosition]].removeAt(childPosition)
                    notifyDataSetChanged()
                }
                .setNegativeButton("Cancel") { dialog, which -> dialog.dismiss() }.create()
        alertDialog.show()
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

    init {
        this.data = data
        this.titles = titles
    }
}