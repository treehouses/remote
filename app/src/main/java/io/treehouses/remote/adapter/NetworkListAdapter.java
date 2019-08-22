package io.treehouses.remote.adapter;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

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
    private static int layout;

    public static int getLayout() {
        return layout;
    }

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

    private BluetoothChatService getChatService() {
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View getChildView(int i, int i1, boolean b, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(list.get(i).getLayout(), parent, false);
        layout = list.get(i).getLayout();

        if (layout == R.layout.open_vnc) {
            new ViewHolderVnc(convertView, context, listener);
        } else if (layout == R.layout.configure_tethering) {
            new ViewHolderTether(convertView, listener, context);
        } else if (layout == R.layout.button_layout) {
            new ViewHolderCommands(convertView, listener);
        } else {
            switchStatement(i, convertView);
        }
        return convertView;

//         else if (layout == R.layout.profile_ethernet || layout == R.layout.profile_wifi || layout == R.layout.profile_hotspot|| layout == R.layout.profile_bridge) {
//            new ViewHolderProfile(context, convertView);
    }

    private void switchStatement(int i, View convertView) {
        switch (i) {
            case 0:
                new ViewHolderEthernet(convertView, listener, context);
                break;
            case 1:
                new ViewHolderWifi(convertView, listener, context);
                break;
            case 2:
                new ViewHolderHotspot(convertView, listener, context);
                break;
            case 3:
                new ViewHolderBridge(convertView, listener, context);
                break;
            case 4:
                new ViewHolderReset(convertView, listener);
                break;
            case 5:
                new ViewHolderReboot(convertView, listener, getChatService(), context);
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
