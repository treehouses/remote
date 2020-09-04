package io.treehouses.remote.utils

import android.app.AlertDialog
import android.content.Context
import android.view.ContextThemeWrapper

object DialogUtils {
    fun createAlertDialog(context: Context?, id:Int, title: String): AlertDialog.Builder {
        return AlertDialog.Builder(ContextThemeWrapper(context, id)).setTitle(title)
    }
}