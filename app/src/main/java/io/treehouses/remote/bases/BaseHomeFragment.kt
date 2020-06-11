package io.treehouses.remote.bases

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.AnimationDrawable
import android.net.Uri
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import io.treehouses.remote.Constants
import io.treehouses.remote.Fragments.DialogFragments.RPIDialogFragment
import io.treehouses.remote.MainApplication
import io.treehouses.remote.Network.ParseDbService
import io.treehouses.remote.R
import io.treehouses.remote.callback.SetDisconnect
import io.treehouses.remote.utils.LogUtils
import java.util.*

open class BaseHomeFragment : BaseFragment() {
    protected var preferences: SharedPreferences? = null
    private var imageVersion = ""
    private var tresshousesVersion = ""
    private var bluetoothMac = ""
    private var rpiVersion: String? = null
    private fun setAnimatorBackgrounds(green: ImageView, red: ImageView, option: Int) {
        when (option) {
            1 -> {
                green.setBackgroundResource(R.drawable.thanksgiving_anim_green)
                red.setBackgroundResource(R.drawable.thanksgiving_anim_red)
            }
            2 -> {
                green.setBackgroundResource(R.drawable.newyear_anim_green)
                red.setBackgroundResource(R.drawable.newyear_anim_red)
            }
            3 -> {
                green.setBackgroundResource(R.drawable.heavymetal_anim_green)
                red.setBackgroundResource(R.drawable.heavymetal_anim_red)
            }
            else -> {
                green.setBackgroundResource(R.drawable.dance_anim_green)
                red.setBackgroundResource(R.drawable.dance_anim_red)
            }
        }
    }

    protected fun showLogDialog(preferences: SharedPreferences) {
        val connectionCount = preferences.getInt("connection_count", 0)
        val lastDialogShown = preferences.getLong("last_dialog_shown", 0)
        val date = Calendar.getInstance()
        date.add(Calendar.DAY_OF_YEAR, -7)
        val v = layoutInflater.inflate(R.layout.alert_log, null)
        val emoji = String(Character.toChars(0x1F60A))
        if (lastDialogShown < date.timeInMillis && !preferences.getBoolean("send_log", false)) {
            if (connectionCount >= 3) {
                preferences.edit().putLong("last_dialog_shown", Calendar.getInstance().timeInMillis).apply()
                AlertDialog.Builder(ContextThemeWrapper(activity, R.style.CustomAlertDialogStyle)).setTitle("Sharing is Caring  $emoji").setCancelable(false).setMessage("Treehouses wants to collect your activities. " +
                        "Do you like to share it? It will help us to improve.")
                        .setPositiveButton("Continue") { _: DialogInterface?, _: Int -> preferences.edit().putBoolean("send_log", true).apply() }.setNegativeButton("Cancel") { _: DialogInterface?, _: Int -> MainApplication.showLogDialog = false }.setView(v).show()
            }
        }
    }

    protected fun rate(preferences: SharedPreferences) {
        val connectionCount = preferences.getInt("connection_count", 0)
        val ratingDialog = preferences.getBoolean("ratingDialog", true)
        LogUtils.log("$connectionCount  $ratingDialog")
        val lastDialogShown = preferences.getLong("last_dialog_shown", 0)
        val date = Calendar.getInstance()
        if (lastDialogShown < date.timeInMillis) {
            if (connectionCount >= 3 && ratingDialog) {
                AlertDialog.Builder(ContextThemeWrapper(activity, R.style.CustomAlertDialogStyle)).setTitle("Thank You").setCancelable(false).setMessage("We're so happy to hear that you love the Treehouses app! " +
                        "It'd be really helpful if you rated us. Thanks so much for spending some time with us.")
                        .setPositiveButton("RATE IT NOW") { _: DialogInterface?, _: Int ->
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = Uri.parse("https://play.google.com/store/apps/details?id=io.treehouses.remote")
                            startActivity(intent)
                            preferences.edit().putBoolean("ratingDialog", false).apply()
                        }.setNeutralButton("REMIND ME LATER") { _: DialogInterface?, _: Int -> MainApplication.ratingDialog = false }
                        .setNegativeButton("NO THANKS") { _: DialogInterface?, _: Int -> preferences.edit().putBoolean("ratingDialog", false).apply() }.show()
            }
        }
    }

    protected fun checkImageInfo(readMessage: List<String>, deviceName: String) {
        bluetoothMac = readMessage[0]
        imageVersion = readMessage[1]
        tresshousesVersion = readMessage[2]
        rpiVersion = readMessage[3]
        sendLog(deviceName)
    }

    private fun sendLog(deviceName: String) {
        val connectionCount = preferences!!.getInt("connection_count", 0)
        val sendLog = preferences!!.getBoolean("send_log", true)
        preferences!!.edit().putInt("connection_count", connectionCount + 1).apply()
        if (connectionCount >= 3 && sendLog) {
            val map = HashMap<String, String?>()
            map["imageVersion"] = imageVersion
            map["treehousesVersion"] = tresshousesVersion
            map["bluetoothMacAddress"] = bluetoothMac
            map["rpiVersion"] = rpiVersion
            ParseDbService.sendLog(activity, deviceName, map, preferences)
            MainApplication.logSent = true
        }
    }

    protected fun showDialogOnce(preferences: SharedPreferences) {
        val dialogShown = preferences.getBoolean("dialogShown", false)
        if (!dialogShown) {
            showWelcomeDialog()
            val editor = preferences.edit()
            editor.putBoolean("dialogShown", true)
            editor.apply()
        }
    }

    private fun showWelcomeDialog(): AlertDialog {
        val s = SpannableString("""Treehouses Remote only works with our treehouses images, or a raspbian image enhanced by "control" and "cli". There is more information under "Get Started"

https://treehouses.io/#!pages/download.md
https://github.com/treehouses/control
https://github.com/treehouses/cli""")
        Linkify.addLinks(s, Linkify.ALL)
        val d = AlertDialog.Builder(ContextThemeWrapper(context, R.style.CustomAlertDialogStyle))
                .setTitle("Friendly Reminder")
                .setIcon(R.drawable.dialog_icon)
                .setNegativeButton("OK") { dialog: DialogInterface, _: Int -> dialog.cancel() }
                .setMessage(s)
                .create()
        d.show()
        (d.findViewById<View>(android.R.id.message) as TextView).movementMethod = LinkMovementMethod.getInstance()
        return d
    }

    protected fun showTestConnectionDialog(dismissable: Boolean, title: String, messageID: Int, selected_LED: Int): AlertDialog {
        val mView = layoutInflater.inflate(R.layout.dialog_test_connection, null)
        val mIndicatorGreen = mView.findViewById<ImageView>(R.id.flash_indicator_green)
        val mIndicatorRed = mView.findViewById<ImageView>(R.id.flash_indicator_red)
        if (!dismissable) {
            mIndicatorGreen.visibility = View.VISIBLE
            mIndicatorRed.visibility = View.VISIBLE
        } else {
            mIndicatorGreen.visibility = View.INVISIBLE
            mIndicatorRed.visibility = View.INVISIBLE
        }
        setAnimatorBackgrounds(mIndicatorGreen, mIndicatorRed, selected_LED)
        val animationDrawableGreen = mIndicatorGreen.background as AnimationDrawable
        val animationDrawableRed = mIndicatorRed.background as AnimationDrawable
        animationDrawableGreen.start()
        animationDrawableRed.start()
        val a = createTestConnectionDialog(mView, dismissable, title, messageID)
        a.show()
        return a
    }

    private fun createTestConnectionDialog(mView: View, dismissable: Boolean, title: String, messageID: Int): AlertDialog {
        val d = AlertDialog.Builder(ContextThemeWrapper(context, R.style.CustomAlertDialogStyle)).setView(mView).setTitle(title).setIcon(R.drawable.ic_action_device_access_bluetooth_searching).setMessage(messageID)
        if (dismissable) d.setNegativeButton("OK") { dialog: DialogInterface, _: Int -> dialog.dismiss() }
        return d.create()
    }

    protected fun showRPIDialog(s: SetDisconnect?) {
        val dialogFrag = RPIDialogFragment.newInstance(123)
        (dialogFrag as RPIDialogFragment).setCheckConnectionState(s)
        dialogFrag.setTargetFragment(this, Constants.REQUEST_DIALOG_FRAGMENT_HOTSPOT)
        dialogFrag.show(requireActivity().supportFragmentManager.beginTransaction(), "rpiDialog")
    }


    protected fun showUpgradeCLI() {
        val alertDialog = AlertDialog.Builder(ContextThemeWrapper(context, R.style.CustomAlertDialogStyle))
                .setTitle("Update Treehouses CLI")
                .setMessage("Treehouses CLI needs an upgrade to correctly function with Treehouses Remote. Please upgrade to the latest version!")
                .setPositiveButton("Upgrade") { dialog: DialogInterface, _: Int ->
                    listener.sendMessage(getString(R.string.TREEHOUSES_UPGRADE))
                    Toast.makeText(context, "Upgraded", Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                }
                .setNegativeButton("Upgrade Later") { dialog: DialogInterface, _: Int -> dialog.dismiss() }
                .create()
        alertDialog.show()
    }
}