package io.treehouses.remote.pojo

import android.text.SpannableString
import android.text.Spanned
import java.io.Serializable


data class TunnelSSHData(
        var checkSwitchNotification: Boolean = false,
        var enableSwitchNotification: Boolean = false,
        var enabledNotifyNow :Boolean =false,
        var addHostText: String = "Add Host",
        var addPortText: String = "Add Port",
        var enableAddHost: Boolean = false,
        var enableAddPort: Boolean = false,
        var enableSSHPort: Boolean = false,
        var publicKey: Spanned = SpannableString(""),
        var privateKey: Spanned = SpannableString(""),
        var hostNames: ArrayList<String> = ArrayList(),
        var portNames: ArrayList<String> = ArrayList(),
        var hostPosition: ArrayList<Int> = ArrayList()
) : Serializable
