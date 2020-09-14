package io.treehouses.remote.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
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

    fun createAlertDialog(context: Context?, title: String, msg: String): AlertDialog.Builder {
        return createAlertDialog(context, title).setMessage(msg)
    }

    fun createAlertDialog(context: Context?, mView: View?, icon: Int): AlertDialog.Builder {
        return createAlert(context).setView(mView).setIcon(icon)
    }

    fun createAlertDialog(context: Context?, title: String, myFunc: () -> Unit) {
        createAdvancedDialog(createAlertDialog(context, title), myFunc)
    }

    fun createAlertDialog(context: Context?, title: String, msg: String, myFunc: () -> Unit) {
        createAdvancedDialog(createAlertDialog(context, title, msg), myFunc)
    }

    private fun createAdvancedDialog(builder: AlertDialog.Builder, myFunc: () -> Unit) {
        builder.setPositiveButton("YES") { dialog: DialogInterface?, _: Int ->
            myFunc()
            dialog?.dismiss()
        }
                .setNegativeButton("NO") { dialog: DialogInterface?, _: Int -> dialog?.dismiss()}
                .show().window!!.setBackgroundDrawableResource(android.R.color.transparent)
    }
}