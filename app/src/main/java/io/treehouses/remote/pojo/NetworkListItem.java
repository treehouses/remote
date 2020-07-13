package io.treehouses.remote.pojo;

import java.util.ArrayList;
import java.util.List;

import io.treehouses.remote.R;

public class NetworkListItem {
    private String title;
    private int layout;

    public NetworkListItem(String title, int layout) {
        this.title = title;
        this.layout = layout;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getLayout() {
        return layout;
    }

    public void setLayout(int layout) {
        this.layout = layout;
    }

    public static List<NetworkListItem> getSystemList() {
        List<NetworkListItem> systemList = new ArrayList();
        systemList.add(new NetworkListItem("Open VNC", R.layout.open_vnc));
        systemList.add(new NetworkListItem("Configure Tethering (beta)", R.layout.configure_tethering));
        systemList.add(new NetworkListItem("Add SSH Key", R.layout.configure_ssh_key));
        systemList.add(new NetworkListItem("Toggle Camera", R.layout.configure_camera));
        systemList.add(new NetworkListItem("Wifi Country", R.layout.configure_wificountry));
        systemList.add(new NetworkListItem("Blocker Level", R.layout.configure_blocker));
        systemList.add(new NetworkListItem("Discover", R.layout.configure_discover));
        return systemList;
    }
}
