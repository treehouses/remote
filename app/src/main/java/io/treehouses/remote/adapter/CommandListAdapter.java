package io.treehouses.remote.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import io.treehouses.remote.R;
import io.treehouses.remote.pojo.CommandListItem;

public class CommandListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> expandableListTitle;
    private HashMap<String, List<CommandListItem>> expandableListDetail;

    public CommandListAdapter(Context context, List<String> expandableListTitle,
                              HashMap<String, List<CommandListItem>> expandableListDetail) {
        this.context = context;
        this.expandableListTitle = expandableListTitle;
        this.expandableListDetail = expandableListDetail;
    }

    @Override
    public Object getChild(int listPosition, int expandedListPosition) {
        if (expandedListPosition < expandableListDetail.get(this.expandableListTitle.get(listPosition)).size()) {
            return this.expandableListDetail.get(this.expandableListTitle.get(listPosition)).get(expandedListPosition).getTitle();
        }
        return "Add";
    }

    @Override
    public long getChildId(int listPosition, int expandedListPosition) {
        return expandedListPosition;
    }

    @Override
    public View getChildView(int listPosition, final int expandedListPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final String expandedListText = (String) getChild(listPosition, expandedListPosition);
        if (expandedListPosition == this.expandableListDetail.get(this.expandableListTitle.get(listPosition)).size()) {
            convertView = getConvertView(R.layout.list_add);
        }
        else {
            convertView = getConvertView(R.layout.list_item);
            TextView expandedListTextView = convertView.findViewById(R.id.expandedListItem);
            expandedListTextView.setText(expandedListText);
        }
        return convertView;
    }

    @Override
    public int getChildrenCount(int listPosition) {
        if (listPosition == 0) {
            return this.expandableListDetail.get(this.expandableListTitle.get(listPosition)).size() + 1;
        }
        return this.expandableListDetail.get(this.expandableListTitle.get(listPosition)).size();
    }

    @Override
    public Object getGroup(int listPosition) {
        return this.expandableListTitle.get(listPosition);
    }

    @Override
    public int getGroupCount() {
        return this.expandableListTitle.size();
    }

    @Override
    public long getGroupId(int listPosition) {
        return listPosition;
    }

    @Override
    public View getGroupView(int listPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String listTitle = (String) getGroup(listPosition);
        if (convertView == null) {
             convertView = getConvertView(R.layout.list_group);
        }
        TextView listTitleTextView = (TextView) convertView
                .findViewById(R.id.lblListHeader);
        listTitleTextView.setText(listTitle);
        return convertView;
    }

    public View getConvertView(int layout_id){
        LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return layoutInflater.inflate(layout_id, null);
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int listPosition, int expandedListPosition) {
        return true;
    }
}
