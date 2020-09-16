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

    fun createAlertDialog(context: Context?, title: String, msg: String, mView: View?): AlertDialog.Builder {
        return createAlertDialog(context, title).setMessage(msg).setView(mView)
    }

    fun createAlertDialog(context: Context?, mView: View?, icon: Int): AlertDialog.Builder {
        return createAlert(context).setView(mView).setIcon(icon)
    }

    fun createAlertDialog(context: Context?, title: String, mView: View?, icon: Int): AlertDialog.Builder {
        return createAlert(context).setView(mView).setIcon(icon).setTitle(title)
    }

    fun createAlertDialog(context: Context?, title: String, myFunc: () -> Unit) {
        createAdvancedDialog(createAlertDialog(context, title), Pair("YES", "NO"), myFunc)
    }

    fun createAlertDialog(context: Context?, title: String, msg: String, myFunc: () -> Unit) {
        createAdvancedDialog(createAlertDialog(context, title, msg), Pair("YES", "NO"), myFunc)
    }

    fun createAlertDialog(context: Context?, metaData: Pair<String, String>, buttonTitles: Pair<String, String>, myFunc: () -> Unit) {
        val title = metaData.first
        val msg = metaData.second
        val posLabel = buttonTitles.first
        val negLabel = buttonTitles.second
        createAdvancedDialog(createAlertDialog(context, title, msg), Pair(posLabel, negLabel), myFunc)
    }

    fun exec(dialog: DialogInterface?, myFunc: () -> Unit) {
        myFunc()
        dialog?.dismiss()
    }

    fun createAdvancedDialog(builder: AlertDialog.Builder, labels: Pair<String, String>, posFunc: () -> Unit, negFunc: () -> Unit = {}) {
        val posLabel = labels.first
        val negLabel = labels.second
        val posCallBack = { d: DialogInterface?, _: Int -> exec(d, posFunc) }
        val negCallback = { d: DialogInterface?, _: Int -> exec(d, negFunc)}
        builder.setPositiveButton(posLabel, posCallBack)
                .setNegativeButton(negLabel, negCallback)
                .show().window!!.setBackgroundDrawableResource(android.R.color.transparent)
    }
}