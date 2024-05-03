package io.treehouses.remote.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import io.treehouses.remote.R
import io.treehouses.remote.databinding.DialogProgressBinding

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

    class CustomProgressDialog(context: Context) {
        private val binding: DialogProgressBinding = DialogProgressBinding.inflate(LayoutInflater.from(context))
        private val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(context)
        private val progressBar = binding.progressBar
        private val progressText = binding.progressText
        private val progressTitle = binding.progressTitle
        private var dialog: AlertDialog? = null
        private var positiveButtonAction: (() -> Unit)? = null
        private var negativeButtonAction: (() -> Unit)? = null

        init {
            dialogBuilder.setView(binding.root)
            dialogBuilder.setCancelable(false)
            binding.buttonPositive.setOnClickListener {
                positiveButtonAction?.invoke()
            }
            binding.buttonNegative.setOnClickListener {
                negativeButtonAction?.invoke()
            }
        }
        fun setPositiveButton(text: String, isVisible: Boolean = true, listener: () -> Unit) {
            binding.buttonPositive.text = text
            positiveButtonAction = listener
            binding.buttonPositive.visibility = if (isVisible) View.VISIBLE else View.GONE
        }

        fun setNegativeButton(text: String = "Cancel", isVisible: Boolean = true, listener: () -> Unit) {
            binding.buttonNegative.text = text
            negativeButtonAction = listener
            binding.buttonNegative.visibility = if (isVisible) View.VISIBLE else View.GONE
        }

        fun show() {
            if (dialog == null) {
                dialog = dialogBuilder.create()
                dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
            }
            dialog?.show()
            dialog?.show()
        }

        fun dismiss() {
            dialog?.dismiss()
        }

        fun setCancelable(state: Boolean) {
            dialog?.setCancelable(state)
        }

        fun setIndeterminate(isIndeterminate: Boolean) {
            progressBar.isIndeterminate = isIndeterminate
        }

        fun setMax(maxValue: Int) {
            progressBar.max = maxValue
        }

        fun setProgress(value: Int) {
            setIndeterminate(false)
            progressBar.progress = value
        }

        fun setMessage(text: String) {
            progressText.text = text
            progressText.visibility = View.VISIBLE
        }

        fun setTitle(text: String?) {
            progressTitle.visibility = View.VISIBLE
            progressTitle.text = text
        }
        fun isShowing(): Boolean {
            return dialog?.isShowing ?: false
        }

        fun disableNegativeButton() {
            binding.buttonNegative.isEnabled = false
        }
    }
}