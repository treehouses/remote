package io.treehouses.remote.bluetoothv2.util

import android.app.Activity
import android.view.View
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar
import io.treehouses.remote.R

internal fun Activity.showSnackbarShort(text: String, view:View) {
    Snackbar.make(view, text, Snackbar.LENGTH_SHORT).show()
}

