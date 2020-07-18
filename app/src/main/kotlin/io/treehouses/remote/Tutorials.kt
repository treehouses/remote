package io.treehouses.remote

import android.util.TypedValue
import android.view.View
import androidx.fragment.app.FragmentActivity
import io.treehouses.remote.databinding.*
import io.treehouses.remote.utils.SaveUtils
import me.toptas.fancyshowcase.FancyShowCaseQueue
import me.toptas.fancyshowcase.FancyShowCaseView
import me.toptas.fancyshowcase.FocusShape

object Tutorials {
    fun homeTutorials(bind: ActivityHomeFragmentBinding, activity: FragmentActivity) {
        if (!SaveUtils.getFragmentFirstTime(activity, SaveUtils.Screens.HOME)) return
        SaveUtils.setFragmentFirstTime(activity, SaveUtils.Screens.HOME, false)
        //Put animations here
        val a = fancyShowCaseViewBuilder(activity, bind.testConnection, "Test Bluetooth Connection to RPI", 750)
                .build()

        val b = fancyShowCaseViewBuilder(activity, bind.networkProfiles, "Configure Network Profiles in the Network Screen to quickly switch between network configurations", 500)
                .titleSize(18, TypedValue.COMPLEX_UNIT_SP)
                .focusCircleRadiusFactor(1.25)
                .build()
        show(a,b)
    }

    fun networkTutorials(bind: NewNetworkBinding, activity: FragmentActivity) {
        if (!SaveUtils.getFragmentFirstTime(activity, SaveUtils.Screens.NETWORK)) return
        SaveUtils.setFragmentFirstTime(activity, SaveUtils.Screens.NETWORK, false)
        //Put animations here
        val a = fancyShowCaseViewBuilder(activity, bind.networkWifi, "Touch Here to Connect to a WiFi Network", 750, FocusShape.CIRCLE).build()

        val b = fancyShowCaseViewBuilder(activity, bind.networkHotspot, "Touch Here to Connect to Start a Hotspot", 500, FocusShape.CIRCLE).build()

        val c = fancyShowCaseViewBuilder(activity, bind.networkBridge, "Touch Here to Configure a Bridge Connection", 500, FocusShape.CIRCLE).build()

        val d = fancyShowCaseViewBuilder(activity, bind.networkEthernet, "Touch Here to Configure an Ethernet Connection", 500, FocusShape.CIRCLE).build()

        val e = fancyShowCaseViewBuilder(activity, bind.buttonNetworkMode, "Use this button to Refresh Network Mode Info", 500, FocusShape.ROUNDED_RECTANGLE).build()

        val f = fancyShowCaseViewBuilder(activity, bind.rebootRaspberry, "Use this to Reboot RPi", 500, FocusShape.ROUNDED_RECTANGLE).build()

        val g = fancyShowCaseViewBuilder(activity, bind.resetNetwork, "Use this to Reset Network back to Default", 500, FocusShape.ROUNDED_RECTANGLE).build()

        show(a,b,c,d,e,f,g)
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
        val a = fancyShowCaseViewBuilder(activity, bind.editTextOut, "Enter Commands here to run on Pi Remotely", 750, FocusShape.ROUNDED_RECTANGLE).build()

        val b = fancyShowCaseViewBuilder(activity, bind.terminalList, "You can Save your Commands here to use them without typing again", 500, FocusShape.ROUNDED_RECTANGLE).build()

        val c = fancyShowCaseViewBuilder(activity, bind.btnPrevious, "Access Recently used Commands on Successive taps of this button", 500, FocusShape.CIRCLE).build()

        val d = fancyShowCaseViewBuilder(activity, bind.infoButton, "Get Information on what Treehouses Commands are Available and how to use them", 500, FocusShape.CIRCLE).build()

        show(a,b,c,d)
    }

    fun servicesOverviewTutorials(bind: ActivityServicesTabFragmentBinding, activity: FragmentActivity) {
        if (!SaveUtils.getFragmentFirstTime(activity, SaveUtils.Screens.SERVICES_OVERVIEW)) return
        SaveUtils.setFragmentFirstTime(activity, SaveUtils.Screens.SERVICES_OVERVIEW, false)
        //Put animations here
        val a = fancyShowCaseViewBuilder(activity, bind.listView, "Install and use a variety of services", 500, FocusShape.ROUNDED_RECTANGLE)
                .roundRectRadius(80)
                .disableFocusAnimation()
                .build()
        show(a)
    }

    fun servicesDetailsTutorials(bind: ActivityServicesDetailsBinding, activity: FragmentActivity) {
        if (!SaveUtils.getFragmentFirstTime(activity, SaveUtils.Screens.SERVICES_DETAILS)) return
        SaveUtils.setFragmentFirstTime(activity, SaveUtils.Screens.SERVICES_DETAILS, false)
        //Put animations here
        val a = fancyShowCaseViewBuilder(activity, bind.pickService, "Pick any service from this list", 500)
                .focusBorderSize(80)
                .build()
        show(a)
    }

    fun tunnelTutorials(bind: ActivityTunnelSshFragmentBinding, activity: FragmentActivity) {
        if (!SaveUtils.getFragmentFirstTime(activity, SaveUtils.Screens.TUNNEL)) return
        SaveUtils.setFragmentFirstTime(activity, SaveUtils.Screens.TUNNEL, false)
        //Put animations here
    }

    fun statusTutorials(bind: ActivityStatusFragmentBinding, activity: FragmentActivity) {
        if (!SaveUtils.getFragmentFirstTime(activity, SaveUtils.Screens.STATUS)) return
        SaveUtils.setFragmentFirstTime(activity, SaveUtils.Screens.STATUS, false)

        val a = fancyShowCaseViewBuilder(activity, bind.bluetoothBox, "Your Device's Bluetooth details are listed here", 750, FocusShape.ROUNDED_RECTANGLE)
                .roundRectRadius(80)
                .build()

        val b = fancyShowCaseViewBuilder(activity, bind.networkBox, "Network details can be found here", 500, FocusShape.ROUNDED_RECTANGLE)
                .roundRectRadius(80)
                .build()

        val c = fancyShowCaseViewBuilder(activity, bind.rpiDetailBox, "Some details like your Hostname, Image Version, CPU and Model are listed here", 500, FocusShape.ROUNDED_RECTANGLE)
                .roundRectRadius(80)
                .build()

        val d = fancyShowCaseViewBuilder(activity, bind.editName, "Edit your hostname here, new hostname will show up the next time you visit Status", 500, FocusShape.CIRCLE).build()

        val e = fancyShowCaseViewBuilder(activity, bind.cliVersionBox, "You can check your CLI Version here and Upgrade if a new Version is Available", 500, FocusShape.ROUNDED_RECTANGLE)
                .roundRectRadius(80)
                .build()

        val f = fancyShowCaseViewBuilder(activity, bind.measurablesBox, "RAM Usage and Temperature of CPU can be found here", 500, FocusShape.ROUNDED_RECTANGLE)
                .roundRectRadius(80)
                .build()

        show(a,b,c,d,e,f)
    }

    private fun fancyShowCaseViewBuilder(activity: FragmentActivity, view: View, title: String, delay: Int, focusShape: FocusShape = FocusShape.CIRCLE): FancyShowCaseView.Builder {
        return FancyShowCaseView.Builder(activity)
                .focusOn(view)
                .title(title)
                .enableAutoTextPosition()
                .backgroundColor(R.color.focusColor)
                .focusShape(focusShape)
                .fitSystemWindows(true)
                .delay(delay)
    }

    private fun show(vararg view: FancyShowCaseView) {
        val queue = FancyShowCaseQueue()
        for(v in view) {
            queue.add(v)
        }
        queue.show()
    }
}

