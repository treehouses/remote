package io.treehouses.remote.SSH.beans;

import android.net.Uri;

import java.nio.charset.Charset;

public class HostBean {
    private int fontSize = 10;
    private String nickname = "treehouses";
    private String hostname = "192.168.1.29";
    private String username = "pi";
    private long pubkeyId = -1;

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setPort(int port) {
        this.port = port;
    }

    private String protocol = "ssh";
    private int port = 22;

    private long id = -1;


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
        return true;
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

    public String getUsername() {return username; }

    public int getPort() {
        return port;
    }

    public long getPubkeyId() {return pubkeyId;}


    public String getProtocol() {
        return protocol;
    }

    public Uri getUri() {
        StringBuilder sb = new StringBuilder();
        sb.append("ssh://");

        if (username != null)
            sb.append(Uri.encode(username))
                    .append('@');

        sb.append(Uri.encode(getHostname()))
                .append(':')
                .append(getPort())
                .append("/#")
                .append(getNickname());
        return Uri.parse(sb.toString());
    }

    @Override
    public int hashCode() {
        int hash = 7;

        if (id != -1)
            return (int) id;

        hash = 31 * hash + (null == nickname ? 0 : nickname.hashCode());
        hash = 31 * hash + (null == protocol ? 0 : protocol.hashCode());
        hash = 31 * hash + (null == username ? 0 : username.hashCode());
        hash = 31 * hash + (null == hostname ? 0 : hostname.hashCode());
        hash = 31 * hash + port;

        return hash;
    }
}
