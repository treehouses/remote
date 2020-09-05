package io.treehouses.remote.utils

import android.app.AlertDialog
import android.content.Context
import android.view.ContextThemeWrapper
import android.view.View
import io.treehouses.remote.R

object DialogUtils {
    fun createAlertDialog(context: Context?, title: String): AlertDialog.Builder {
        return AlertDialog.Builder(ContextThemeWrapper(context, R.style.CustomAlertDialogStyle)).setTitle(title)
    }

    fun createAlertDialog2(context: Context?, title: String, msg: String): AlertDialog.Builder {
        return AlertDialog.Builder(ContextThemeWrapper(context, R.style.CustomAlertDialogStyle)).setTitle(title).setMessage(msg)
    }

    fun createAlertDialog3(context: Context?, mView: View?, icon: Int): AlertDialog.Builder {
        return AlertDialog.Builder(ContextThemeWrapper(context, R.style.CustomAlertDialogStyle)).setView(mView).setIcon(icon)
    }
}