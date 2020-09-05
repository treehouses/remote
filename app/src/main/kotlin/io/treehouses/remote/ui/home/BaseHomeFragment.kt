package io.treehouses.remote.ui.home

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.AnimationDrawable
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.preference.PreferenceManager
import io.treehouses.remote.IntroActivity
import io.treehouses.remote.MainApplication
import io.treehouses.remote.R
import io.treehouses.remote.bases.BaseFragment
import io.treehouses.remote.utils.DialogUtils
import io.treehouses.remote.utils.Matcher
import io.treehouses.remote.utils.SaveUtils.Screens
import io.treehouses.remote.utils.Utils
import io.treehouses.remote.utils.logE
import java.util.*

open class BaseHomeFragment : BaseFragment() {
    protected var preferences: SharedPreferences? = null

    /**
     * ViewModel for the HomeFragment.
     */
    protected val viewModel : HomeViewModel by viewModels(ownerProducer = {this})

    /**
     * Loads the correct animation for the Test Connection Dialog.
     * @param green : ImageView = The imageView for the green LED
     * @param red : ImageView = The imageView for the red LED
     */
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

    /**
     * In the TestConnectionDialog, sets the correct background animation for each LED light
     */
    private fun setBackgrounds(green: ImageView, red: ImageView, greenDrawable: Int, redDrawable: Int) {
        green.setBackgroundResource(greenDrawable)
        red.setBackgroundResource(redDrawable)
    }

    /**
     * Prompts user for data collection
     * Keeps track of
     * - connection count (only prompt after 3 connections)
     * - last time dialog was shows (only show after a week)
     * @param preferences : SharedPreferences = Preferences to save the user preferences to
     */
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
                DialogUtils.createAlertDialog(activity, "Sharing is Caring  $emoji").setCancelable(false).setMessage("Treehouses wants to collect your activities. " +
                        "Do you like to share it? It will help us to improve.")
                        .setPositiveButton("Continue") { _: DialogInterface?, _: Int -> preferences.edit().putBoolean("send_log", true).apply() }.setNegativeButton("Cancel") { _: DialogInterface?, _: Int -> MainApplication.showLogDialog = false }.setView(v).show().window!!.setBackgroundDrawableResource(android.R.color.transparent)
            }
        }
    }

    /**
     * Prompts the user to rate the application.
     * - Should show at the same time as the sharing data dialog
     * @param preferences : SharedPreferences = preferences to save user preferences to
     */
    protected fun rate(preferences: SharedPreferences) {
        val connectionCount = preferences.getInt("connection_count", 0)
        val ratingDialog = preferences.getBoolean("ratingDialog", true)
        logE("$connectionCount  $ratingDialog")
        val lastDialogShown = preferences.getLong("last_dialog_shown", 0)
        val date = Calendar.getInstance()
        if (lastDialogShown < date.timeInMillis) {
            if (connectionCount >= 3 && ratingDialog) {
                val a = DialogUtils.createAlertDialog(activity,"Thank You").setCancelable(false).setMessage("We're so happy to hear that you love the Treehouses app! " +
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

    protected fun showDialogOnce(preferences: SharedPreferences) {
        val firstTime = preferences.getBoolean(Screens.FIRST_TIME.name, true)
        if (firstTime) {
//            showWelcomeDialog()
            logE("FIRST TIME")
            val i = Intent(activity, IntroActivity::class.java)
            startActivity(i)
            val editor = preferences.edit()
            editor.putBoolean(Screens.FIRST_TIME.name, false)
            editor.apply()
        }
    }

    /**
     * Show the Test Connection Dialog that shows the state of the blinking LED pattern
     */
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

    /**
     * Utility function to create the Test Connection Dialog
     */
    private fun createTestConnectionDialog(mView: View, dismissable: Boolean, title: String, messageID: Int): AlertDialog {
        val d = DialogUtils.createAlertDialog(context,title).setView(mView).setIcon(R.drawable.bluetooth).setMessage(messageID)
        if (dismissable) d.setNegativeButton("OK") { dialog: DialogInterface, _: Int -> dialog.dismiss() }
        return d.create()
    }

    /**
     * Show that the Treehouses CLI may be out of date, and requires and upgrade. This is usally triggered by an unexpected error.
     */
    protected fun showUpgradeCLI() {
        val alertDialog = DialogUtils.createAlertDialog(context, "Update Treehouses CLI")
                .setMessage("Treehouses CLI needs an upgrade to correctly function with Treehouses Remote. Please upgrade to the latest version!").setPositiveButton("Upgrade") { dialog: DialogInterface, _: Int ->
                    viewModel.sendMessage(getString(R.string.TREEHOUSES_UPGRADE))
                    Toast.makeText(context, "Upgraded", Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                }
                .setNegativeButton("Upgrade Later") { dialog: DialogInterface, _: Int -> dialog.dismiss() }
                .create()
        alertDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.show()
    }

    /**
     * Called to sync Bluetooth file that is on the phone with the version on the Raspberry Pi
     * @param serverHash : String = the hash of the server Bluetooth File
     */
    protected fun syncBluetooth(serverHash: String) {
        logE("SERVER: $serverHash")
        //Get the local Bluetooth file on the app
        val inputStream = context?.assets?.open("bluetooth-server.txt")
        val localString = inputStream?.bufferedReader().use { it?.readText() }
        inputStream?.close()
        val hashed = Utils.hashString(localString!!)
        logE("LOCAL: $serverHash")
        //Bluetooth file is outdated, but RPI is connected to the internet
        if (Matcher.isError(serverHash) && viewModel.internetStatus.value == true) {
            askForBluetoothUpgradeOverInternet()
        }
        //Bluetooth file is outdated, and RPI is not connected to the internet
        else if (Matcher.isError(serverHash) && viewModel.internetStatus.value == false) {
            noInternetForBluetoothUpgrade()
        }
        //If there is no error, compare the server hashes to determine whether an upgrade is needed
        else if (hashed.trim() != serverHash.trim() && PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean("bluetooth_file_local_upgrade", false)) {
            askForBluetoothUpgradeStable(localString)
        }
    }

    /**
     * Called when there is no internet to allow for upgrade to the new Bluetooth versioning/upgrading system.
     * Error when running Bluetooth server sync, and there is no internet to allow download from
     * https://raw.githubusercontent.com/treehouses/control/master/server.py
     */
    private fun noInternetForBluetoothUpgrade() {
        val noInternetMsg = "There is a new version of bluetooth available, however, your Raspberry Pi is not connected to the Internet. Please connect to a network to upgrade your bluetooth."
        val dialog = DialogUtils.createAlertDialog(requireContext(), "No Internet!").setMessage(noInternetMsg)
                .setPositiveButton("Ok") { d, _ ->
                    d.dismiss()
                }.create()
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    /**
     * Called when the bluetooth server sync hash check returned an error, however, the RPI is connected to the internet,
     * allowing for a pull from master https://raw.githubusercontent.com/treehouses/control/master/server.py
     */
    private fun askForBluetoothUpgradeOverInternet() {
        val dialog = DialogUtils.createAlertDialog(requireContext(), "Upgrade Bluetooth").setMessage("There is a new version of bluetooth available. Please upgrade to receive the latest changes.")
                .setPositiveButton("Upgrade") { _, _ ->
                    viewModel.sendMessage(getString(R.string.TREEHOUSES_UPGRADE_BLUETOOTH_MASTER))
                }
                .setNegativeButton("Cancel") {dialog, _ -> dialog.dismiss()}.create()
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    /**
     * When user is on the right Bluetooth file for versioning, user is prompted to upgrade bluetooth server
     * @param localFile : String = String version of the local file
     */
    private fun askForBluetoothUpgradeStable(localFile : String) {
        val compressedLocalFile = Utils.compressString(localFile).replace("\n","" )
        val dialog = DialogUtils.createAlertDialog(context, "Re-sync Bluetooth Server")
                .setMessage("The bluetooth server on the Raspberry Pi does not match the one on your device. Would you like to update the CLI bluetooth server?")
                .setPositiveButton("Upgrade") { _, _ ->
                    logE("ENCODED $compressedLocalFile")
                    viewModel.sendMessage("remotesync $compressedLocalFile cnysetomer\n")
                    Toast.makeText(requireContext(), "Bluetooth Upgraded. Please reboot Raspberry Pi to apply the changes.", Toast.LENGTH_LONG).show()
                }.setNegativeButton("Cancel") { dialog: DialogInterface, _: Int ->
                    dialog.dismiss()
                }.create()
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    /**
     * If the version check does not satisfy the necessary requirements imposed by Treehouses CLI, alert the user
     * to upgrade their version of Treehouses Remote.
     */
    protected fun updateTreehousesRemote() {
        val alertDialog = DialogUtils.createAlertDialog2(context, "Update Required",
                "Please update Treehouses Remote, as it does not meet the required version on the Treehouses CLI.")
                .setPositiveButton("Update") { _: DialogInterface?, _: Int ->
                    val appPackageName = requireActivity().packageName // getPackageName() from Context or Activity object
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
                    } catch (anfe: ActivityNotFoundException) {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
                    }
                }.create()
        alertDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.show()
    }
}