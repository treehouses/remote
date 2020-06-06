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
    private View[] views;
    private static int layout;
    public static int position;

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
        this.views = new View[6];
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

        // Needs to recycle views instead of creating new ones each time.
        // if (convertView == null) creating bugs
        if (views[i] != null) {
            return views[i];
        }

        convertView = inflater.inflate(list.get(i).getLayout(), parent, false);

        layout = list.get(i).getLayout();

        position = i;

        if (layout == R.layout.open_vnc) {
            new ViewHolderVnc(convertView, context, listener);
        } else if (layout == R.layout.configure_tethering) {
            new ViewHolderTether(convertView, listener, context);
        } else if (layout == R.layout.configure_ssh_key) {
            new ViewHolderSSHKey(convertView, context, listener);
        } else if (layout == R.layout.configure_camera) {
            new ViewHolderCamera(convertView, context, listener);
        }

        views[i] = convertView;
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }

}
