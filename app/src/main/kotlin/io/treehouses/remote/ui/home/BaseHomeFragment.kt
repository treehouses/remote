package io.treehouses.remote.ui.home

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.AnimationDrawable
import android.net.Uri
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import io.treehouses.remote.*
import io.treehouses.remote.Fragments.DialogFragments.RPIDialogFragment
import io.treehouses.remote.Network.ParseDbService
import io.treehouses.remote.bases.BaseFragment
import io.treehouses.remote.callback.SetDisconnect
import io.treehouses.remote.utils.LogUtils
import io.treehouses.remote.utils.SaveUtils.Screens
import java.util.*

open class BaseHomeFragment : BaseFragment() {
    protected var preferences: SharedPreferences? = null
    private var imageVersion = ""
    private var tresshousesVersion = ""
    private var bluetoothMac = ""
    private var rpiVersion: String? = null
    private fun setAnimatorBackgrounds(green: ImageView, red: ImageView, option: Int) {
        when (option) {
            1 -> setBackgrounds(green, red, R.drawable.thanksgiving_anim_green, R.drawable.thanksgiving_anim_red)
            2 -> setBackgrounds(green, red, R.drawable.newyear_anim_green, R.drawable.newyear_anim_red)
            3 -> setBackgrounds(green, red, R.drawable.heavymetal_anim_green, R.drawable.heavymetal_anim_red)
            4 -> setBackgrounds(green, red, R.drawable.lunarnewyear_anim_green, R.drawable.lunarnewyear_anim_red)
            5 -> setBackgrounds(green, red, R.drawable.valentine_anim_green, R.drawable.valentine_anim_red)
            6 -> setBackgrounds(green, red, R.drawable.carnival_anim_green, R.drawable.carnival_anim_red)
            7 -> green.setBackgroundResource(R.drawable.stpatricks_anim_green)
            8 -> setBackgrounds(green, red, R.drawable.onam_anim_green, R.drawable.onam_anim_red)
            9 -> setBackgrounds(green, red, R.drawable.easter_anim_green, R.drawable.easter_anim_red)
            10 -> setBackgrounds(green ,red, R.drawable.eid_anim_green, R.drawable.eid_anim_red)
            11 -> setBackgrounds(green, red, R.drawable.kecak_anim_green, R.drawable.kecak_anim_red)
            12 -> setBackgrounds(green, red, R.drawable.christmas_anim_green, R.drawable.christmas_anim_red)
            13 -> setBackgrounds(green, red, R.drawable.diwali_anim_green, R.drawable.diwali_anim_red)
            14 -> setBackgrounds(green, red, R.drawable.lantern_anim_green, R.drawable.lantern_anim_red)
            else -> setBackgrounds(green, red, R.drawable.dance_anim_green, R.drawable.dance_anim_red)
        }
    }

    private fun setBackgrounds(green: ImageView, red: ImageView, greenDrawable: Int, redDrawable: Int) {
        green.setBackgroundResource(greenDrawable)
        red.setBackgroundResource(redDrawable)
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
                CreateAlertDialog(activity, R.style.CustomAlertDialogStyle, "Sharing is Caring  $emoji").setCancelable(false).setMessage("Treehouses wants to collect your activities. " +
                        "Do you like to share it? It will help us to improve.")
                        .setPositiveButton("Continue") { _: DialogInterface?, _: Int -> preferences.edit().putBoolean("send_log", true).apply() }.setNegativeButton("Cancel") { _: DialogInterface?, _: Int -> MainApplication.showLogDialog = false }.setView(v).show().window!!.setBackgroundDrawableResource(android.R.color.transparent)
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
                val a = CreateAlertDialog(activity, R.style.CustomAlertDialogStyle, "Thank You").setCancelable(false).setMessage("We're so happy to hear that you love the Treehouses app! " +
                        "It'd be really helpful if you rated us. Thanks so much for spending some time with us.")
                        .setPositiveButton("RATE IT NOW") { _: DialogInterface?, _: Int ->
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = Uri.parse("https://play.google.com/store/apps/details?id=io.treehouses.remote")
                            startActivity(intent)
                            preferences.edit().putBoolean("ratingDialog", false).apply()
                        }.setNeutralButton("REMIND ME LATER") { _: DialogInterface?, _: Int -> MainApplication.ratingDialog = false }
                        .setNegativeButton("NO THANKS") { _: DialogInterface?, _: Int -> preferences.edit().putBoolean("ratingDialog", false).apply() }.create()
                a.window!!.setBackgroundDrawableResource(android.R.color.transparent)
                a.show()
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
            val preferences:SharedPreferences = preferences!!
            ParseDbService.sendLog(activity, deviceName, map, preferences)
            MainApplication.logSent = true
        }
    }

    protected fun showDialogOnce(preferences: SharedPreferences) {
        val firstTime = preferences.getBoolean(Screens.FIRST_TIME.name, true)
        Log.e("REACHED HERE", firstTime.toString())
        if (firstTime) {
//            showWelcomeDialog()
            Log.e("FIRST", "TIME")
            val i = Intent(activity, IntroActivity::class.java)
            startActivity(i)
            val editor = preferences.edit()
            editor.putBoolean(Screens.FIRST_TIME.name, false)
            editor.apply()
        }
    }

//    private fun showWelcomeDialog(): AlertDialog {
//        val s = SpannableString("""Treehouses Remote only works with our treehouses images, or a raspbian image enhanced by "control" and "cli". There is more information under "Get Started"
//
//https://treehouses.io/#!pages/download.md
//https://github.com/treehouses/control
//https://github.com/treehouses/cli""")
//        Linkify.addLinks(s, Linkify.ALL)
//        val d = CreateAlertDialog(context, R.style.CustomAlertDialogStyle, "Friendly Reminder" )
//                .setIcon(R.drawable.dialog_icon)
//                .setNegativeButton("OK") { dialog: DialogInterface, _: Int -> dialog.cancel() }
//                .setMessage(s)
//                .create()
//        d.show()
//        (d.findViewById<View>(android.R.id.message) as TextView).movementMethod = LinkMovementMethod.getInstance()
//        return d
//    }

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
        a.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        a.show()
        return a
    }

    private fun createTestConnectionDialog(mView: View, dismissable: Boolean, title: String, messageID: Int): AlertDialog {
        val d = CreateAlertDialog(context, R.style.CustomAlertDialogStyle,title).setView(mView).setIcon(R.drawable.bluetooth).setMessage(messageID)
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
        val alertDialog = CreateAlertDialog(context, R.style.CustomAlertDialogStyle, "Update Treehouses CLI")
                .setMessage("Treehouses CLI needs an upgrade to correctly function with Treehouses Remote. Please upgrade to the latest version!").setPositiveButton("Upgrade") { dialog: DialogInterface, _: Int ->
                    listener.sendMessage(getString(R.string.TREEHOUSES_UPGRADE))
                    Toast.makeText(context, "Upgraded", Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                }
                .setNegativeButton("Upgrade Later") { dialog: DialogInterface, _: Int -> dialog.dismiss() }
                .create()
        alertDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.show()
    }

    private fun CreateAlertDialog(context: Context?, id:Int, title: String): AlertDialog.Builder {
        return AlertDialog.Builder(ContextThemeWrapper(context, id)).setTitle(title)
    }
}