package io.treehouses.remote.SSH;

import android.net.Uri;

import java.io.IOException;
import java.util.Map;

import io.treehouses.remote.SSH.beans.HostBean;

public class SSH extends AbsTransport {
    @Override
    public void connect() {

    }

    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        return 0;
    }

    @Override
    public void write(byte[] buffer) throws IOException {

    }

    @Override
    public void write(int c) throws IOException {

    }

    @Override
    public void flush() throws IOException {

    }

    @Override
    public void close() {

    }

    @Override
    public void setDimensions(int columns, int rows, int width, int height) {

    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public boolean isSessionOpen() {
        return false;
    }

    @Override
    public int getDefaultPort() {
        return 0;
    }

    @Override
    public String getDefaultNickname(String username, String hostname, int port) {
        return null;
    }

    @Override
    public void getSelectionArgs(Uri uri, Map<String, String> selection) {

    }

    @Override
    public HostBean createHost(Uri uri) {
        return null;
    }

    @Override
    public boolean usesNetwork() {
        return false;
    }
}
