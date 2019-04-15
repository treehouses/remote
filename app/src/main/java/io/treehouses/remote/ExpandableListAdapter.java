package io.treehouses.remote;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

import io.treehouses.remote.Fragments.HomeFragment;
import io.treehouses.remote.Fragments.NetworkFragment;
import io.treehouses.remote.MiscOld.Constants;
import io.treehouses.remote.Network.BluetoothChatService;
import io.treehouses.remote.bases.BaseFragment;
import io.treehouses.remote.callback.HomeInteractListener;

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
    Bundle bundle = new Bundle();
    ViewHolder holder = new ViewHolder();

    public ExpandableListAdapter(Context context, ArrayList<String> groups, String[][] children, BluetoothChatService mChatService) {
        this.groups = groups;
        this.children = children;
        inf = LayoutInflater.from(context);
        this.mChatService = mChatService;
        this.context = context;
    }

    @Override
    public int getGroupCount() {
        return groups.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return children[groupPosition].length;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groups.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return children[groupPosition][childPosition];
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inf.inflate(R.layout.list_group, parent, false);
            holder.setListGroup(convertView);
            holder.setTextViewGroup((TextView) convertView.findViewById(R.id.lblListHeader));
            holder.getListGroup().setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.getTextViewGroup().setText(getGroup(groupPosition).toString());
        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        final String child = getChild(groupPosition, childPosition).toString().trim();
        Log.e("TAG", "getChildView was called");

        if (child.contains("Reset") || child.contains("Reboot")) {
            convertView = inf.inflate(R.layout.list_item, parent, false);
            holder.setListItem(convertView);
            holder.setTextView((TextView) convertView.findViewById(R.id.listItemStatic));
            convertView.setTag(holder);
            holder.getTextView().setText(child);
        } else {
            convertView = group1(child, convertView, parent, groupPosition, childPosition);

            if (child1) {
                child1(childPosition);
                child1 = false;
            }
            if (child2) {
                child2(childPosition);
                child2 = false;
            }
            if (child3) {
                child3(childPosition);
                child3 = false;
            }
            if (child4) {
                child4(childPosition);
                child4 = false;
            }

        }
        return convertView;
    }


    public View group1(String child, View convertView, ViewGroup parent, int groupPosition, int childPosition) {

        if (child.equals("ESSID") || child.equals("DNS")) {
            convertView = inf.inflate(R.layout.list_child, parent, false);
            holder.setListChild1(convertView);
            holder.setEditText1((TextInputEditText) convertView.findViewById(R.id.lblListItem));
            convertView.setTag(holder);
            holder.getEditText1().setHint(child);
            child1 = true;
        } else if (child.equals("Password") || child.equals("Gateway")) {
            convertView = inf.inflate(R.layout.list_child2, parent, false);
            holder.setListChild2(convertView);
            holder.setEditText2((TextInputEditText) convertView.findViewById(R.id.lblListItem2));
            convertView.setTag(holder);
            holder.getEditText2().setHint(child);
            child2 = true;
        } else if (child.equals("Hotspot ESSID") || child.equals("Subnet")) {
            convertView = inf.inflate(R.layout.list_child3, parent, false);
            holder.setListChild3(convertView);
            holder.setEditText3((TextInputEditText) convertView.findViewById(R.id.lblListItem3));
            convertView.setTag(holder);
            holder.getEditText3().setHint(child);
            child3 = true;
        } else if (child.equals("Hotspot Password")) {
            convertView = inf.inflate(R.layout.list_child3, parent, false);
            holder.setListChild3(convertView);
            holder.setEditText3((TextInputEditText) convertView.findViewById(R.id.lblListItem3));
            convertView.setTag(holder);
            holder.getEditText3().setHint(child);
            child4 = true;
        } else if (child.contains("Start")) {
            convertView = buttonLayout(parent, child, groupPosition, childPosition);
        }
        return convertView;
    }

    private View buttonLayout(final ViewGroup parent, final String child, final int groupPosition, final int childPosition) {
        View convertView = inf.inflate(R.layout.list_button, parent, false);
        holder.setListButton(convertView);
        holder.setButton((Button) convertView.findViewById(R.id.listButton));
        convertView.setTag(holder);
        holder.getButton().setText(child);

        holder.getButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getContext(), "Child clicked", Toast.LENGTH_LONG).show();
                BaseFragment baseFragment = new BaseFragment();
                baseFragment.listener = (HomeInteractListener) context;

                //storeData(child, groupPosition, parent);

                if (groupPosition == 4 && childPosition == 0) {
                    //childData(child);
                    baseFragment.listener.sendMessage("treehouses default network");
                } else if (groupPosition == 5 && childPosition == 0) {
                    baseFragment.listener.sendMessage("reboot");
                    try {
                        Thread.sleep(1000);
                        if (mChatService.getState() != Constants.STATE_CONNECTED) {
                            Toast.makeText(context, "Bluetooth Disconnected: Reboot in progress", Toast.LENGTH_LONG).show();
                            baseFragment.listener.openCallFragment(new HomeFragment());
                        } else {
                            Toast.makeText(context, "Reboot Unsuccessful", Toast.LENGTH_LONG).show();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        return convertView;
    }

    public void child1(final int childPosition) {
        holder.getEditText1().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                if (childPosition == 0) {
                    String essid = s.toString();
                    Toast.makeText(context, "ESSID: "+ essid, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void child2(final int childPosition) {
        holder.getEditText2().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                if (childPosition == 1) {
                    String pw = s.toString();
                    Toast.makeText(context, "Password: " + pw, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void child3(final int childPosition) {
        holder.getEditText3().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                if (childPosition == 2) {
                    String test = s.toString();
                    Toast.makeText(context, "IDK: "+ test, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void child4(final int childPosition) {
        holder.getEditText3().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                if (childPosition == 3) {
                    String test = s.toString();
                    Toast.makeText(context, "IDK: "+ test, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

//    public void storeData(String child, int groupPosition, ViewGroup parent) {
//        View convertView = inf.inflate(R.layout.list_child2, parent, false);
//        ViewHolder holder =  new ViewHolder();
//        holder.editText = convertView.findViewById(R.id.lblListItem);
//        convertView.setTag(holder);
//
//       // if (!holder.editText.getText().toString().equals("")) {
//            switch (groupPosition) {
//                case 0:
//                    if (child.equals("DNS")) {
//                        String dns = holder.editText.getText().toString();
//                        bundle.putString("dns", dns);
//                    } else if (child.equals("Gateway")) {
//                        String gateway = holder.editText.getText().toString();
//                        bundle.putString("gateway", gateway);
//                    } else if (child.equals("Subnet")) {
//                        String subnet = holder.editText.getText().toString();
//                        bundle.putString("subnet", subnet);
//                    }
//            }
//       // }
//    }

    public void childData() {

        //String data = getArguments().getString("data");
       // String type = groups.get()
        Log.e("TAG", "method called ");
//        switch (type) {
//            case "wifi":
//                wifiOn(data);
//                break;
//            case "hotspot":
//                hotspotOn(data);
//                break;
//            case "ethernet":
//                ethernetOn(bundle);
//                break;
//            case "bridge":
//                bridgeOn(bundle);
//                break;
//            default:
//                break;
//        }
    }

//    private void sendData(String child) {
//        Bundle bundle = new Bundle();
//        bundle.putString("data", child);
//        NetworkFragment networkFragment = new NetworkFragment();
//        networkFragment.setArguments(bundle);
//    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


}


