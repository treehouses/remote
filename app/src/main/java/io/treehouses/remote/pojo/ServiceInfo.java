package io.treehouses.remote.pojo;

public class ServiceInfo {
    public final static int SERVICE_AVAILABLE = 0;
    public final static int SERVICE_INSTALLED = 1;
    public final static int SERVICE_RUNNING = 2;
    public String name;
    public int serviceStatus;

    public String url;

    public ServiceInfo(String n, int status) {
        this.name = n;
        this.serviceStatus = status;
        this.url = "";
    }
}
