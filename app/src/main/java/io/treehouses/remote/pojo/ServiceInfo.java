package io.treehouses.remote.pojo;

public class ServiceInfo implements Comparable<ServiceInfo> {
    public final static int SERVICE_HEADER_INSTALLED = 0;
    public final static int SERVICE_RUNNING = 1;
    public final static int SERVICE_INSTALLED = 2;
    public final static int SERVICE_HEADER_AVAILABLE = 3;
    public final static int SERVICE_AVAILABLE = 4;


    public String name;
    public int serviceStatus;

    public ServiceInfo(String n, int status) {
        this.name = n;
        this.serviceStatus = status;
    }

    @Override
    public int compareTo(ServiceInfo o) {
        return this.serviceStatus - o.serviceStatus;
    }
}
