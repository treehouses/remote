package io.treehouses.remote.pojo;

public class NetworkProfile {
    public String essid;
    public String password;
    public String option;
    public NetworkProfile(String essid, String password, String option) {
        this.essid = essid;
        this.password = password;
        this.option = option;
    }
}
