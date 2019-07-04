package io.treehouses.remote.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.List;

import io.treehouses.remote.Network.BluetoothChatService;
import io.treehouses.remote.R;
import io.treehouses.remote.callback.HomeInteractListener;
import io.treehouses.remote.pojo.NetworkListItem;

public class NetworkListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<NetworkListItem> list;
    private LayoutInflater inflater;
    private HomeInteractListener listener;
    private BluetoothChatService chatService;

    public void setListener(HomeInteractListener listener) {
        this.listener = listener;
        if (listener == null) {
            throw new RuntimeException("Please implement home interact listener");
        }
    }

    public NetworkListAdapter(Context context, List<NetworkListItem> list, BluetoothChatService mChatService) {
        this.context = context;
        this.chatService = mChatService;
        inflater = LayoutInflater.from(context);
        this.list = list;
    }

    public BluetoothChatService getChatService() {
        return chatService;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public int getGroupCount() {
        return list.size();
    }

    @Override
    public int getChildrenCount(int position) {
        return position > 6 ? 0 : 1;
    }

    @Override
    public Object getGroup(int i) {
        return list.get(i).getTitle();
    }

    @Override
    public Object getChild(int i, int i1) {
        return list.get(i).getLayout();
    }

    @Override
    public long getGroupId(int i) {
        return 0;
    }

    @Override
    public long getChildId(int i, int i1) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(final int i, boolean b, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.list_group, parent, false);
        TextView listHeader = convertView.findViewById(R.id.lblListHeader);
        listHeader.setText(getGroup(i).toString());

        return convertView;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(list.get(i).getLayout(), parent, false);

        if (list.get(i).getLayout() != R.layout.button_layout) {
            switchStatement(i, convertView);
        } else {
            new ViewHolderCommands(convertView, listener);
        }

        return convertView;
    }

    private void switchStatement(int i, View convertView) {
        switch (i) {
            case 0:
                new ViewHolderEthernet(convertView, listener, getContext());
                break;
            case 1:
                new ViewHolderWifi(convertView, listener, getContext());
                break;
            case 2:
                new ViewHolderHotspot(convertView, listener, getContext());
                break;
            case 3:
                new ViewHolderBridge(convertView, listener, getContext());
                break;
            case 4:
                new ViewHolderReset(convertView, listener);
                break;
            case 5:
                new ViewHolderReboot(convertView, listener, getChatService(), getContext());
                break;
        }
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }

    public void setNetworkMode(String s) {
        list.get(6).setTitle(s);
        notifyDataSetChanged();
    }
}
