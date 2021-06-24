package io.treehouses.remote.pojo

import java.io.Serializable

data class TunnelSSHKeyDialogData(
        var showHandlePhoneKeySaveDialog: Boolean = false,
        var showHandlePiKeySaveDialog: Boolean = false,
        var showHandleDifferentKeysDialog: Boolean = false,
        var piPrivateKey: String = "",
        var piPublicKey: String = "",
        var storedPublicKey: String = "",
        var storedPrivateKey: String = "",
        var profile: String = ""
) : Serializable