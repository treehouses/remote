package io.treehouses.remote.pojo;

public class ServiceInfo implements Comparable<ServiceInfo> {
    public final static int SERVICE_AVAILABLE = 0;
    public final static int SERVICE_INSTALLED = 1;
    public final static int SERVICE_RUNNING = 2;

    public final static int SERVICE_HEADER = 3;

    public String name;
    public int serviceStatus;

    public ServiceInfo(String n, int status) {
        this.name = n;
        this.serviceStatus = status;
    }

    @Override
    public int compareTo(ServiceInfo o) {
        return o.serviceStatus - this.serviceStatus;
    }
}
