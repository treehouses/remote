package io.treehouses.remote.bluetoothv2.util

import android.app.Activity
import android.util.Log
import android.view.View
import com.polidea.rxandroidble2.exceptions.BleScanException


internal fun Activity.debug(messsage: String?) = Log.d("treeshouses", messsage)
internal fun Activity.error(exception: String?) = Log.e("treeshouses", exception)
