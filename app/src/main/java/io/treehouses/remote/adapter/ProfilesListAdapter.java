package io.treehouses.remote.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import io.treehouses.remote.R;
import io.treehouses.remote.pojo.NetworkProfile;
import io.treehouses.remote.utils.SaveUtils;

public class ProfilesListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<String> titles;
    private HashMap<String, List<NetworkProfile>> data;
    public ProfilesListAdapter(Context context, List<String> titles, HashMap<String, List<NetworkProfile>> data) {
        this.context = context;
        this.data = data;
        this.titles = titles;
    }
    @Override
    public int getGroupCount() {
        return titles.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (isChildEmpty(groupPosition)) {
            return 1;
        }
        else {
            return data.get(titles.get(groupPosition)).size();
        }
    }

    @Override
    public Object getGroup(int groupPosition) {
        return "YEEY";
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return data.get(titles.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = layoutInflater.inflate(R.layout.list_group, null);
        TextView label = convertView.findViewById(R.id.lblListHeader);
        label.setText(titles.get(groupPosition));
        return convertView;
    }

    private boolean isChildEmpty(int groupPosition) {
        return data.get(titles.get(groupPosition)) == null || data.get(titles.get(groupPosition)).size() == 0;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = layoutInflater.inflate(R.layout.row_profile, null);
        TextView label = convertView.findViewById(R.id.label);
        Button deleteProfile = convertView.findViewById(R.id.delete_profile);

        if (isChildEmpty(groupPosition)) {
            label.setText("Please configure in the Network screen");
            label.setTextColor(context.getResources().getColor(R.color.md_grey_700));
            deleteProfile.setVisibility(View.GONE);
            return convertView;
        }

        label.setText(data.get(titles.get(groupPosition)).get(childPosition).ssid);

        deleteProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveUtils.deleteProfile(context, groupPosition, childPosition);
                data.get(titles.get(groupPosition)).remove(childPosition);
                ProfilesListAdapter.this.notifyDataSetChanged();
            }
        });
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
