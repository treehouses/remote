package io.treehouses.remote.interfaces

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.ContextThemeWrapper
import android.view.View
import io.treehouses.remote.R

interface FragmentDialogInterface {
    fun showDialog(context: Context?, title: String, message: String) {
        val alertDialog = createAlertDialog(context, R.style.CustomAlertDialogStyle,title,message)
                .setPositiveButton("OK") { dialog: DialogInterface, _: Int -> dialog.dismiss() }.create()
        alertDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.show()
    }

    fun createAlertDialog(context: Context?, id:Int, title:String, message:String): AlertDialog.Builder {
        val alertDialog = AlertDialog.Builder(ContextThemeWrapper(context, id))
                .setTitle(title)
                .setMessage(message)

        return alertDialog

    }

    fun createAlertDialog(ctw:ContextThemeWrapper, view: View?, title:Int, icon:Int):AlertDialog.Builder{
        return AlertDialog.Builder(ctw)
                .setView(view)
                .setTitle(title)
                .setIcon(icon)
    }

    fun createRemoteReverseDialog(context: Context?): AlertDialog? {
        val a  = createAlertDialog(context, R.style.CustomAlertDialogStyle, "Reverse Lookup", "Calling...")
                .setNegativeButton("Dismiss") { dialog: DialogInterface, _: Int -> dialog.dismiss() }.create()
        a.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        return a
    }


}