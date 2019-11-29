package io.treehouses.remote.pojo;

public class DeviceInfo {
    private Boolean paired;
    //private Boolean raspberryPI;
    private Boolean inRange;
    private String deviceName;

    public DeviceInfo (String deviceName, Boolean paired, Boolean inRange) {
        this.deviceName = deviceName;
        this.paired = paired;
        this.inRange = inRange;
    }
    public Boolean isPaired() {
        return paired;
    }

    public Boolean isInRange() {return inRange; }

//    public Boolean isRaspberryPI() {
//        return raspberryPI;
//    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setInRange(Boolean value) {
        this.inRange = value;
    }

}
