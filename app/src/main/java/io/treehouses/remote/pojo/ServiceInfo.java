package io.treehouses.remote.pojo;

public class ServiceInfo implements Comparable<ServiceInfo> {
    public final static int SERVICE_HEADER_INSTALLED = 0;
    public final static int SERVICE_RUNNING = 1;
    public final static int SERVICE_INSTALLED = 2;
    public final static int SERVICE_HEADER_AVAILABLE = 3;
    public final static int SERVICE_AVAILABLE = 4;

    public String name;
    public int serviceStatus;
    public String icon;
    public String info;
    public String autorun;

    //Headers
    public ServiceInfo(String n, int status) {
        this.name = n;
        this.serviceStatus = status;
    }

    //Services
    public ServiceInfo(String name, int serviceStatus, String icon, String info, String autorun) {
        this.name = name;
        this.serviceStatus = serviceStatus;
        this.icon = icon;
        this.info = info;
        this.autorun = autorun;
    }

    @Override
    public int compareTo(ServiceInfo o) {
        return this.serviceStatus - o.serviceStatus;
    }

    public boolean isHeader() {
        return serviceStatus == SERVICE_HEADER_AVAILABLE || serviceStatus == SERVICE_HEADER_INSTALLED;
    }
}
