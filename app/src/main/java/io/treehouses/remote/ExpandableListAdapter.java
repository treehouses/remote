package io.treehouses.remote;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import io.treehouses.remote.Fragments.HomeFragment;
import io.treehouses.remote.MiscOld.Constants;
import io.treehouses.remote.Network.BluetoothChatService;
import io.treehouses.remote.bases.BaseFragment;
import io.treehouses.remote.callback.HomeInteractListener;
import static io.treehouses.remote.MiscOld.Constants.getGroups;

public class ExpandableListAdapter extends BaseExpandableListAdapter {
    private Boolean child1 = false;
    private Boolean child2 = false;
    private Boolean child3 = false;
    private Boolean child4 = false;
    private final LayoutInflater inf;
    private ArrayList<String> groups;
    private String[][] children;
    private BluetoothChatService mChatService;
    private Context context;
    private Bundle bundle = new Bundle();
    private ViewHolder holder = new ViewHolder();
    private BaseFragment baseFragment = new BaseFragment();
    int layout;
    int layout2;
    int layout3;
    int layout4;

    int item;
    int item2;
    int item3;
    int item4;

    public ExpandableListAdapter(Context context, ArrayList<String> groups, String[][] children, BluetoothChatService mChatService) {
        this.groups = groups;
        this.children = children;
        inf = LayoutInflater.from(context);
        this.mChatService = mChatService;
        this.context = context;
    }
    public Context getContext() { return context; }
    @Override
    public int getGroupCount() { return groups.size(); }
    @Override
    public int getChildrenCount(int groupPosition) { return children[groupPosition].length; }
    @Override
    public Object getGroup(int groupPosition) { return groups.get(groupPosition); }
    @Override
    public Object getChild(int groupPosition, int childPosition) { return children[groupPosition][childPosition]; }
    @Override
    public long getGroupId(int groupPosition) { return groupPosition; }
    @Override
    public long getChildId(int groupPosition, int childPosition) { return childPosition; }
    @Override
    public boolean hasStableIds() { return true; }
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        convertView = inf.inflate(R.layout.list_group, parent, false);
        TextView listHeader = convertView.findViewById(R.id.lblListHeader);
        convertView.setTag(holder);
        holder = (ViewHolder) convertView.getTag();
        listHeader.setText(getGroup(groupPosition).toString());
        return convertView;
    }
    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final String child = getChild(groupPosition, childPosition).toString().trim();
        if (child.contains("Reset") || child.contains("Reboot")) {
            convertView = buttonLayout(parent, child, groupPosition, childPosition);
        } else {
            convertView = group(child, convertView, parent, groupPosition, childPosition);
            if (child1) {
                child(childPosition, holder.getEditText1());
                child1 = false;
            } else if (child2) {
                child(childPosition, holder.getEditText2());
                child2 = false;
            } else if (child3) {
                child(childPosition, holder.getEditText3());
                child3 = false;
            } else if (child4) {
                child(childPosition, holder.getEditText4());
                child4 = false;
            }
        }
        return convertView;
    }

    private View group(String child, View convertView, ViewGroup parent, int groupPosition, int childPosition) {
        setVariables();
        if (child.equals("ESSID") || child.equals("IP Address")) {
            child1 = true;
            convertView = getConvertView(layout, parent, child, item);
        } else if (child.equals("Password") || child.equals("DNS")) {
            child2 = true;
            convertView = getConvertView(layout2, parent, child, item2);
        } else if (child.equals("Hotspot ESSID") || child.equals("Gateway")) {
            child3 = true;
            convertView = getConvertView(layout3, parent, child, item3);
        } else if (child.equals("Hotspot Password") || child.equals("Mask")) {
            child4 = true;
            convertView = getConvertView(layout4, parent, child, item4);
        } else if (child.equals("Spinner")) {
            convertView = inf.inflate(R.layout.list_spinner, parent, false);
            Spinner spinnerValue = populateSpinner(convertView);
            holder.setListSpinner(spinnerValue.getSelectedItem().toString());
            convertView.setTag(holder);
        } else if (child.contains("Start")) { convertView = buttonLayout(parent, child, groupPosition, childPosition); }
        return convertView;
    }
    private void setVariables() {
        layout = R.layout.list_child;
        layout2 = R.layout.list_child2;
        layout3 = R.layout.list_child3;
        layout4 = R.layout.list_child4;
        item = R.id.lblListItem;
        item2 = R.id.lblListItem2;
        item3 = R.id.lblListItem3;
        item4 = R.id.lblListItem4;
    }

    private View getConvertView(int layout, ViewGroup parent, String child, int item) {
        View convertView = inf.inflate(layout, parent, false);
        TextInputEditText itemView = convertView.findViewById(item);
        if (child1) {
            holder.setEditText1(itemView);
            holder.getEditText1().setHint(child); }
        else if (child2) {
            holder.setEditText2(itemView);
            holder.getEditText2().setHint(child); }
        else if (child3) {
            holder.setEditText3(itemView);
            holder.getEditText3().setHint(child); }
        else if (child4) {
            holder.setEditText4(itemView);
            holder.getEditText4().setHint(child); }
        return convertView;
    }

    private Spinner populateSpinner(View convertView) {
        ArrayList<String> spinnerArray =  new ArrayList<>();
        spinnerArray.add("internet");
        spinnerArray.add("local");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, spinnerArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        final Spinner sItems = convertView.findViewById(R.id.ListSpinner);

        sItems.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                bundle.putString("spinner", sItems.getSelectedItem().toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
        sItems.setAdapter(adapter);
        return sItems;
    }

    private View buttonLayout(final ViewGroup parent, final String child, final int groupPosition, final int childPosition) {
        final String type = getGroups().get(groupPosition);
        View convertView = inf.inflate(R.layout.list_button, parent, false);
        holder.setButton((Button) convertView.findViewById(R.id.listButton));
        convertView.setTag(holder);
        holder.getButton().setText(child);
        holder.getButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                baseFragment.listener = (HomeInteractListener) context;
                if (groupPosition == 4 && childPosition == 0) { baseFragment.listener.sendMessage("treehouses default network"); }
                else if (groupPosition == 5 && childPosition == 0) { reboot(baseFragment); }
                if (!bundle.equals("")) { childData(type); }
            }
        });
        return convertView;
    }

    private void reboot(BaseFragment baseFragment) {
        try {
            baseFragment.listener.sendMessage("reboot");
            Thread.sleep(1000);
            if (mChatService.getState() != Constants.STATE_CONNECTED) {
                Toast.makeText(context, "Bluetooth Disconnected: Reboot in progress", Toast.LENGTH_LONG).show();
                baseFragment.listener.openCallFragment(new HomeFragment());
            } else { Toast.makeText(context, "Reboot Unsuccessful", Toast.LENGTH_LONG).show(); }
        } catch (InterruptedException e) { e.printStackTrace(); }
    }

    private void child(final int childPosition, EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                if (childPosition == 0) { bundle.putString("first", s.toString()); }
                else if (childPosition == 1) { bundle.putString("second", s.toString()); }
                else if (childPosition == 2) { bundle.putString("third", s.toString()); }
                else if (childPosition == 3) { bundle.putString("forth", s.toString()); }
            }
        });
    }
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) { return true; }

    private void childData(String type) {
        switch (type) {
            case "WiFi":
                baseFragment.listener.sendMessage("treehouses wifi \"" + bundle.getString("first") + "\" \"" + bundle.getString("second") + "\"");
                Toast.makeText(getContext(), "Connecting...", Toast.LENGTH_LONG).show();
                break;
            case "Hotspot":
                if (bundle.getString("second").equals("")) { baseFragment.listener.sendMessage("treehouses ap \"" + bundle.getString("spinner") + "\" \"" + bundle.getString("first") + "\""); }
                else { baseFragment.listener.sendMessage("treehouses ap \"" + bundle.getString("spinner") + "\" \"" + bundle.getString("first") + "\" \"" + bundle.getString("second") + "\""); }
                break;
            case "Ethernet: Automatic":
                baseFragment.listener.sendMessage("treehouses ethernet \"" + bundle.getString("first") + "\" \"" + bundle.getString("forth") + "\" \"" + bundle.getString("third") + "\" \"" + bundle.getString("second") + "\"");
                break;
            case "Bridge":
                String temp = "treehouses bridge \"" + (bundle.getString("first")) + "\" \"" + bundle.getString("third") + "\" ";
                String overallMessage = TextUtils.isEmpty(bundle.getString("second")) ? (temp += "\"\"") : (temp += "\"") + bundle.getString("second") + "\"";
                overallMessage += " ";
                if (!TextUtils.isEmpty(bundle.getString("forth"))) { overallMessage += "\"" + bundle.getString("forth") + "\""; }
                baseFragment.listener.sendMessage(overallMessage);
                break;
            default:
                break;
        }
    }
}