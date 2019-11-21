package io.treehouses.remote.pojo;

public class DeviceInfo {
    private Boolean paired;
    //private Boolean raspberryPI;

    private String deviceName;

    public DeviceInfo (String deviceName, Boolean paired) {
        this.deviceName = deviceName;
        this.paired = paired;
    }
    public Boolean isPaired() {
        return paired;
    }

//    public Boolean isRaspberryPI() {
//        return raspberryPI;
//    }

    public String getDeviceName() {
        return deviceName;
    }

}
