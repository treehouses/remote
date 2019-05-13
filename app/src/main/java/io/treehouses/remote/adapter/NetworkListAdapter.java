package io.treehouses.remote.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.treehouses.remote.Fragments.HomeFragment;
import io.treehouses.remote.MiscOld.Constants;
import io.treehouses.remote.Network.BluetoothChatService;
import io.treehouses.remote.R;
import io.treehouses.remote.bases.BaseFragment;
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
        if (i>3 && i<6) {
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (i == 4) {
                        listener.sendMessage("treehouses default network");
                    } else if (i == 5) {
                        reboot();
                    }
                }
            });
        }

        return convertView;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(list.get(i).getLayout(), parent, false);
        switch (i) {
            case 0:
                new ViewHolderEthernet(convertView, listener);
                break;
            case 1:
                new ViewHolderWifi(convertView, listener);
                break;
            case 2:
                new ViewHolderHotspot(convertView, listener);
                break;
            case 3:
                new ViewHolderBridge(convertView, listener);
                break;
        }
        return convertView;
    }


    private void reboot() {
        try {
            Log.d("", "reboot: ");
            listener.sendMessage("reboot");
            Thread.sleep(1000);
            if (chatService.getState() != Constants.STATE_CONNECTED) {
                Toast.makeText(context, "Bluetooth Disconnected: Reboot in progress", Toast.LENGTH_LONG).show();
                listener.openCallFragment(new HomeFragment());
            } else {
                Toast.makeText(context, "Reboot Unsuccessful", Toast.LENGTH_LONG).show();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }

    public void setNetworkMode(String s) {
        NetworkListItem.changeGroup(s);
        notifyDataSetChanged();
    }


}
