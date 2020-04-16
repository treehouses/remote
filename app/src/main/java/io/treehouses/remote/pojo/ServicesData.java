package io.treehouses.remote.pojo;

import java.util.HashMap;
import java.util.List;

public class ServicesData {
    private List<String> available;
    private List<String> installed;
    private List<String> running;

    private HashMap<String, String> icon;
    private HashMap<String, String> info;
    private HashMap<String, String> autorun;


    public ServicesData() {}

    public List<String> getAvailable() {
        return available;
    }

    public List<String> getInstalled() {
        return installed;
    }

    public List<String> getRunning() {
        return running;
    }

    public HashMap<String, String> getIcon() {
        return icon;
    }

    public HashMap<String, String> getInfo() {
        return info;
    }

    public HashMap<String, String> getAutorun() {
        return autorun;
    }
}
