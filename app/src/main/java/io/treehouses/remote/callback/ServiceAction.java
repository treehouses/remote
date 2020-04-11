package io.treehouses.remote.callback;

import io.treehouses.remote.pojo.ServiceInfo;

public interface ServiceAction {
    void onClickInstall(ServiceInfo s);
    void onClickStart(ServiceInfo s);
    void onClickLink(ServiceInfo s);
}
