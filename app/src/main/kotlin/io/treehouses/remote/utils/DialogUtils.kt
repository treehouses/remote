package io.treehouses.remote.utils

import android.app.AlertDialog
import android.content.Context
import android.view.ContextThemeWrapper
import android.view.View
import io.treehouses.remote.R

object DialogUtils {

    fun createAlert(context: Context?): AlertDialog.Builder {
        return AlertDialog.Builder(ContextThemeWrapper(context, R.style.CustomAlertDialogStyle))
    }

    fun createAlertDialog(context: Context?, title: String): AlertDialog.Builder {
        return createAlert(context).setTitle(title)
    }

    fun createAlertDialog2(context: Context?, title: String, msg: String): AlertDialog.Builder {
        return createAlertDialog(context, title).setMessage(msg)
    }

    fun createAlertDialog3(context: Context?, mView: View?, icon: Int): AlertDialog.Builder {
        return createAlert(context).setView(mView).setIcon(icon)
    }
}