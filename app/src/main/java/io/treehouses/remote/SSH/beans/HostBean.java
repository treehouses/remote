package io.treehouses.remote.SSH.beans;

import java.nio.charset.Charset;

public class HostBean {
    private int fontSize = 10;
    private String nickname;
    private String hostname = "pi@@192.168.1.29";

    public String getEncoding() {
        return Charset.defaultCharset().name();
    }

    public String getPostLogin() {
        return "";
    }

    public boolean getQuickDisconnect() {
        return false;
    }

    public boolean getStayConnected() {
        return true;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int sizeDp) {
        this.fontSize = sizeDp;
    }

    public String getNickname() {
        return nickname;
    }

    public boolean getWantSession() {
        return false;
    }

    public boolean getCompression() {
        return false;
    }

    public String getUseAuthAgent() {
        return "no";
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return 22;
    }


    public String getProtocol() {
        return "ssh";
    }
}
