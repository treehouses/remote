package io.treehouses.remote.pojo;

import java.util.ArrayList;
import java.util.List;

import io.treehouses.remote.Constants;
import io.treehouses.remote.R;

public class NetworkListItem {
    private   String title;
    private   int layout;

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


    public static List<NetworkListItem> getNetworkList() {
        List<NetworkListItem> list = new ArrayList<>();
        list.add(new NetworkListItem("Ethernet: Automatic", R.layout.dialog_ethernet));
        list.add(new NetworkListItem("WiFi", R.layout.dialog_wifi));
        list.add(new NetworkListItem("Hotspot", R.layout.dialog_hotspot));
        list.add(new NetworkListItem("Bridge", R.layout.dialog_bridge));
        list.add(new NetworkListItem("Reset", R.layout.dialog_reset));
        list.add(new NetworkListItem("Reboot", R.layout.dialog_reboot));
        list.add(new NetworkListItem("Network Mode: ", -1));
        return list;
    }

    public static List<NetworkListItem> getTerminalList() {
        List<NetworkListItem> terminalList = new ArrayList();
        terminalList.add(new NetworkListItem("Commands", R.layout.button_layout));
        return terminalList;
    }
}
