package io.treehouses.remote.Fragments

import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout
import io.treehouses.remote.R

/**
 * This class is the hotspot and wifi dialog validator
 */
class TextBoxValidation {
    private var mDialog: AlertDialog? = null
    private var textWatcher: EditText? = null
    private var SSID: EditText? = null
    @JvmField
    var PWD: EditText? = null
    private var start: Button? = null
    private var addprofile: Button? = null
    private var textInputLayout: TextInputLayout? = null

    private var ESSIDEditText: EditText? = null
    private var HotspotESSIDEditText: EditText? = null
    fun setmDialog(mDialog: AlertDialog?) {
        this.mDialog = mDialog
    }

    fun setTextWatcher(textWatcher: EditText?) {
        this.textWatcher = textWatcher
    }

    constructor(context: Context, e1: EditText?, e2: EditText?, type: String) {
        if (type == "wifi") {
            SSID = e1
            PWD = e2
        } else if (type == "bridge") {
            ESSIDEditText = e1
            HotspotESSIDEditText = e2
        }
        if(type == "wifi" || type == "bridge") {
            textboxValidation(context, type, e1)
            textboxValidation(context, type, e2)
        }
    }

    fun setStart(start: Button?) {
        this.start = start
    }

    fun setAddprofile(addprofile: Button?) {
        this.addprofile = addprofile
    }

    fun setTextInputLayout(textInputLayout: TextInputLayout?) {
        this.textInputLayout = textInputLayout
    }

    constructor()

    /**
     * Textwatcher for most dialogs
     *
     */
    private fun textboxValidation(context: Context, type: String, toWatch: EditText?) {
        toWatch!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                when (type) {
                    "ethernet" -> {
        //                    validateETHERNET(context);
                    }
                    "wifi" -> {
                        validateWIFI(context)
                    }
                    "bridge" -> {
                        validateBridge()
                    }
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })
    }

    /**
     * Textwatcher for the change password dialog
     *
     */
    fun changePWValidation(confirmPWD: EditText, layout: TextInputLayout, context: Context) {
        addTextChangedListener(layout, textWatcher, confirmPWD, context)
        addTextChangedListener(layout, confirmPWD, confirmPWD, context)
    }

    private fun addTextChangedListener(layout: TextInputLayout, toWatch: EditText?, confirmPWD: EditText, context: Context) {
        toWatch!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                validateChangedPassword(layout, confirmPWD, context)
            }

            override fun afterTextChanged(editable: Editable) {}
        })
    }

    /**
     * Method that sets the dialog positive button to true or false
     */
    private fun dialogButtonTrueOrFalse(mDialog: AlertDialog?, button: Boolean?) {
        if (mDialog == null) return
        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).isClickable = button!!
        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = button
    }

    private fun dialogButtonTrueOrFalse(button1: Button?, button2: Button?, enabled: Boolean) {
        if (button1 != null) {
            button1.isClickable = enabled
            button1.isEnabled = enabled
        }
        if (button2 != null) {
            button2.isEnabled = enabled
            button2.isEnabled = enabled
        }
    }

    /**
     * WiFi dialog validator
     *
     */
    private fun validateWIFI(context: Context) {
        var flag = true
        if(SSID!!.length() == 0 || PWD!!.length() in 1..7){
            flag = false
            dialogButtonTrueOrFalse(start, addprofile, false)
            if (SSID!!.length() == 0) {
                SSID!!.error = context.getString(R.string.error_ssid_empty)
            }
            if (PWD!!.length() in 1..7) {
                textInputLayout!!.error = context.getString(R.string.error_pwd_length)
            }
        }
        if (flag) {
            dialogButtonTrueOrFalse(start, addprofile, true)
            textInputLayout!!.error = null
        }
    }

    /**
     * ETHERNET dialog validator
     *
     */
    private fun validateBridge() {
        var flag = true
        if(ESSIDEditText!!.length() == 0 || HotspotESSIDEditText!!.length() == 0){
            flag = false
            if (ESSIDEditText!!.length() == 0) {
                ESSIDEditText!!.error = "This field cannot be empty"
            }
            if (HotspotESSIDEditText!!.length() == 0) {
                HotspotESSIDEditText!!.error = "This field cannot be empty"
            }
        }

        dialogButtonTrueOrFalse(start, addprofile, flag)
    }

    /**
     * Change password validator
     *
     */
    private fun validateChangedPassword(layout: TextInputLayout, confirmPWD: EditText, context: Context) {
        if (confirmPWD.text.toString() == PWD!!.text.toString()) {
            dialogButtonTrueOrFalse(mDialog, true)
            layout.error = null
        } else {
            dialogButtonTrueOrFalse(mDialog, false)
            layout.error = context.getString(R.string.error_pwd_confirm)
        }
    }

    fun getListener(mDialog: AlertDialog) {
        mDialog.setOnShowListener { dialogButtonTrueOrFalse(mDialog, false) }
    }
}