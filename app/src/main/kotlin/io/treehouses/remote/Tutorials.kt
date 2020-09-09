package io.treehouses.remote

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import io.treehouses.remote.databinding.*
import io.treehouses.remote.utils.SaveUtils
import me.toptas.fancyshowcase.FancyShowCaseQueue
import me.toptas.fancyshowcase.FancyShowCaseView
import me.toptas.fancyshowcase.FocusShape
import me.toptas.fancyshowcase.listener.OnViewInflateListener

object Tutorials {
    private var queue = FancyShowCaseQueue()
    fun homeTutorials(bind: ActivityHomeFragmentBinding, activity: FragmentActivity) {
        if (!SaveUtils.getFragmentFirstTime(activity, SaveUtils.Screens.HOME)) return
        SaveUtils.setFragmentFirstTime(activity, SaveUtils.Screens.HOME, false)
        //Put animations here
        val a = fancyShowCaseViewRoundedRectSkippable(activity, bind.testConnection, "Test Bluetooth Connection to RPI")

        val b = fancyShowCaseViewRoundedRect(activity, bind.networkProfilesBack, "Configure Network Profiles in the Network Screen to quickly switch between network configurations")

        val c = fancyShowCaseView(activity, bind.btnConnect, "Disconnect with RPI", FocusShape.ROUNDED_RECTANGLE)

        val d = fancyShowCaseView(activity, bind.btnGetStarted, "Go to Terminal and Send Commands to RPI", FocusShape.ROUNDED_RECTANGLE)

        show(a,b,c,d)
    }

    fun networkTutorials(bind: ActivityNetworkFragmentBinding, activity: FragmentActivity) {
        if (!SaveUtils.getFragmentFirstTime(activity, SaveUtils.Screens.NETWORK)) return
        SaveUtils.setFragmentFirstTime(activity, SaveUtils.Screens.NETWORK, false)
        //Put animations here
        val network_wifi = fancyShowCaseViewBuilderSkippable(activity, bind.networkWifi, "Touch Here to Connect to a WiFi Network", FocusShape.CIRCLE).build()

        val network_hotspot = fancyShowCaseView(activity, bind.networkHotspot, "Touch Here to Connect to Start a Hotspot", FocusShape.CIRCLE)

        val network_bridge = fancyShowCaseView(activity, bind.networkBridge, "Touch Here to Configure a Bridge Connection", FocusShape.CIRCLE)

        val network_ethernet = fancyShowCaseView(activity, bind.networkEthernet, "Touch Here to Configure an Ethernet Connection", FocusShape.CIRCLE)

        val network_mode = fancyShowCaseView(activity, bind.buttonNetworkMode, "Use this button to Refresh Network Mode Info", FocusShape.ROUNDED_RECTANGLE)

        val reboot_raspberry = fancyShowCaseView(activity, bind.rebootRaspberry, "Use this to Reboot RPi", FocusShape.ROUNDED_RECTANGLE)

        val reset_network = fancyShowCaseView(activity, bind.resetNetwork, "Use this to Reset Network back to Default", FocusShape.ROUNDED_RECTANGLE)

        val scroll_down =  fancyShowCaseViewRoundedRect(activity, bind.discoverBtn, "Scroll down to the bottom of the page")

        val scan_button = fancyShowCaseView(activity, bind.rebootRaspberry, "Scan your Network to find other Connected Devices", FocusShape.ROUNDED_RECTANGLE)

        show(network_wifi, network_hotspot, network_bridge, network_ethernet, network_mode, reboot_raspberry, reset_network, scroll_down, scan_button)
    }

    fun systemTutorials(bind: ActivitySystemFragmentBinding, activity: FragmentActivity) {
        if (!SaveUtils.getFragmentFirstTime(activity, SaveUtils.Screens.SYSTEM)) return
        SaveUtils.setFragmentFirstTime(activity, SaveUtils.Screens.SYSTEM, false)
        //Put animations here

    }

    fun terminalTutorials(bind: ActivityTerminalFragmentBinding, activity: FragmentActivity) {
        if (!SaveUtils.getFragmentFirstTime(activity, SaveUtils.Screens.TERMINAL)) return
        SaveUtils.setFragmentFirstTime(activity, SaveUtils.Screens.TERMINAL, false)
        //Put animations here
        val a = fancyShowCaseViewBuilderSkippable(activity, bind.editTextOut, "Enter Commands here to run on Pi Remotely", FocusShape.ROUNDED_RECTANGLE).build()

        val b = fancyShowCaseView(activity, bind.infoButton, "Get Information on what Treehouses Commands are Available and how to use them", FocusShape.CIRCLE)

        val c = fancyShowCaseView(activity, bind.terminalList, "You can Save your Commands here to use them without typing again", FocusShape.ROUNDED_RECTANGLE)

        val d = fancyShowCaseView(activity, bind.btnPrevious, "Access Recently used Commands on Successive taps of this button", FocusShape.CIRCLE)

        val e = fancyShowCaseView(activity, bind.treehousesBtn, "Use this button for quick treehouses commands", FocusShape.ROUNDED_RECTANGLE)

        show(a,b,c,d,e)
    }

    fun servicesOverviewTutorials(bind: ActivityServicesTabFragmentBinding, activity: FragmentActivity) {
        if (!SaveUtils.getFragmentFirstTime(activity, SaveUtils.Screens.SERVICES_OVERVIEW)) return
        SaveUtils.setFragmentFirstTime(activity, SaveUtils.Screens.SERVICES_OVERVIEW, false)
        //Put animations here
        val a = fancyShowCaseViewBuilder(activity, bind.linearLayout, "Search, Install and Use a Variety of Services", FocusShape.ROUNDED_RECTANGLE)
                .roundRectRadius(80)
                .disableFocusAnimation()
                .build()
        show(a)
    }

    fun servicesDetailsTutorials(bind: ActivityServicesDetailsBinding, activity: FragmentActivity) {
        if (!SaveUtils.getFragmentFirstTime(activity, SaveUtils.Screens.SERVICES_DETAILS)) return
        SaveUtils.setFragmentFirstTime(activity, SaveUtils.Screens.SERVICES_DETAILS, false)
        //Put animations here
        val a = fancyShowCaseViewBuilder(activity, bind.pickService, "Pick any service from this list")
                .focusBorderSize(80)
                .build()
        show(a)
    }

    fun tunnelTutorials(bind: ActivityTunnelSshFragmentBinding, activity: FragmentActivity) {
        //if (!SaveUtils.getFragmentFirstTime(activity, SaveUtils.Screens.TUNNEL)) return
        //SaveUtils.setFragmentFirstTime(activity, SaveUtils.Screens.TUNNEL, false)
        //Put animations here
    }

    fun statusTutorials(bind: ActivityStatusFragmentBinding, activity: FragmentActivity) {
        if (!SaveUtils.getFragmentFirstTime(activity, SaveUtils.Screens.STATUS)) return
        SaveUtils.setFragmentFirstTime(activity, SaveUtils.Screens.STATUS, false)

        val a = fancyShowCaseViewRoundedRectSkippable(activity, bind.bluetoothBox, "Your Device's Bluetooth details are listed here")

        val b = fancyShowCaseViewRoundedRect(activity, bind.networkBox, "Network details can be found here")

        val c = fancyShowCaseViewRoundedRect(activity, bind.rpiDetailBox, "Some details like your Hostname, Image Version, CPU and Model are listed here")

        val d = fancyShowCaseView(activity, bind.editName, "Edit your hostname here, new hostname will show up the next time you visit Status", FocusShape.CIRCLE)

        val e = fancyShowCaseViewRoundedRect(activity, bind.cliVersionBox, "You can check your CLI Version here and Upgrade if a new Version is Available")

        val f = fancyShowCaseViewRoundedRect(activity, bind.measurablesBox, "RAM Usage and Temperature of CPU can be found here")

        val g = fancyShowCaseViewRoundedRect(activity, bind.refreshBtn, "Refresh Anytime to Check Everything Again")

        show(a,b,c,d,e,f,g)
    }

    private fun fancyShowCaseViewBuilderSkippable(activity: FragmentActivity, view: View, title: String, focusShape: FocusShape = FocusShape.CIRCLE): FancyShowCaseView.Builder {
        return fancyShowCaseViewBuilder(activity, view, title, focusShape)
                .customView(R.layout.tutorial, object : OnViewInflateListener {
                    override fun onViewInflated(view: View) {
                       val skipButton = view.findViewById<Button>(R.id.skipBtn)
                        skipButton.setOnClickListener(mClickListener)
                        val text = view.findViewById<TextView>(R.id.text)
                        text.text = title
                    }
                })
    }

    private fun fancyShowCaseViewBuilder(activity: FragmentActivity, view: View, title: String, focusShape: FocusShape = FocusShape.CIRCLE): FancyShowCaseView.Builder {
        return FancyShowCaseView.Builder(activity)
                .focusOn(view)
                .title(title)
                .enableAutoTextPosition()
                .backgroundColor(R.color.focusColor)
                .focusShape(focusShape)
                .fitSystemWindows(true)
                .delay(750)
    }

    private fun fancyShowCaseView(activity: FragmentActivity, view: View, title: String, focusShape: FocusShape): FancyShowCaseView {
        return fancyShowCaseViewBuilder(activity, view, title, focusShape).build()
    }

    private fun fancyShowCaseViewRoundedRect(activity: FragmentActivity, view: View, title: String, focusShape: FocusShape = FocusShape.ROUNDED_RECTANGLE): FancyShowCaseView {
        return fancyShowCaseViewBuilder(activity, view, title, focusShape)
                .roundRectRadius(80)
                .build()
    }

    private fun fancyShowCaseViewRoundedRectSkippable(activity: FragmentActivity, view: View, title: String): FancyShowCaseView {
        return fancyShowCaseViewBuilderSkippable(activity, view, title, FocusShape.ROUNDED_RECTANGLE)
                .roundRectRadius(80)
                .build()
    }

    private fun show(vararg view: FancyShowCaseView) {
        queue = FancyShowCaseQueue()
        for(v in view) {
            queue.add(v)
        }
        queue.show()
    }

    private var mClickListener = View.OnClickListener { queue.cancel(true) }
}

