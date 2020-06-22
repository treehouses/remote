package io.treehouses.remote

import android.util.TypedValue
import androidx.fragment.app.FragmentActivity
import io.treehouses.remote.databinding.*
import io.treehouses.remote.utils.SaveUtils
import me.toptas.fancyshowcase.FancyShowCaseQueue
import me.toptas.fancyshowcase.FancyShowCaseView

object Tutorials {

    fun homeTutorials(bind: ActivityHomeFragmentBinding, activity: FragmentActivity) {
        if (!SaveUtils.getFragmentFirstTime(activity, SaveUtils.Screens.HOME)) return
        SaveUtils.setFragmentFirstTime(activity, SaveUtils.Screens.HOME, false)
        //Put animations here
        val a = FancyShowCaseView.Builder(activity)
                .focusOn(bind.testConnection)
                .title("Test Bluetooth Connection to RPI")
                .delay(750)
                .enableAutoTextPosition()
                .build()

        val b = FancyShowCaseView.Builder(activity)
                .focusOn(bind.networkProfiles)
                .title("Configure Network Profiles in the Network Screen to quickly switch between network configurations")
                .titleSize(18, TypedValue.COMPLEX_UNIT_SP)
                .delay(500)
                .focusCircleRadiusFactor(1.25)
                .enableAutoTextPosition()
                .build()
        val queue = FancyShowCaseQueue().add(a).add(b)
        queue.show()
    }

    fun networkTutorials(bind: NewNetworkBinding, activity: FragmentActivity) {
        if (!SaveUtils.getFragmentFirstTime(activity, SaveUtils.Screens.NETWORK)) return
        SaveUtils.setFragmentFirstTime(activity, SaveUtils.Screens.NETWORK, false)
        //Put animations here
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

    }

    fun servicesOverviewTutorials(bind: ActivityServicesTabFragmentBinding, activity: FragmentActivity) {
        if (!SaveUtils.getFragmentFirstTime(activity, SaveUtils.Screens.SERVICES_OVERVIEW)) return
        SaveUtils.setFragmentFirstTime(activity, SaveUtils.Screens.SERVICES_OVERVIEW, false)
        //Put animations here


    }

    fun servicesDetailsTutorials(bind: ActivityServicesDetailsBinding, activity: FragmentActivity) {
        if (!SaveUtils.getFragmentFirstTime(activity, SaveUtils.Screens.SERVICES_DETAILS)) return
        SaveUtils.setFragmentFirstTime(activity, SaveUtils.Screens.SERVICES_DETAILS, false)
        //Put animations here

    }

    fun tunnelTutorials(bind: ActivityTunnelSshFragmentBinding, activity: FragmentActivity) {
        if (!SaveUtils.getFragmentFirstTime(activity, SaveUtils.Screens.TUNNEL)) return
        SaveUtils.setFragmentFirstTime(activity, SaveUtils.Screens.TUNNEL, false)
        //Put animations here


    }

    fun statusTutorials(bind: ActivityStatusFragmentBinding, activity: FragmentActivity) {
        if (!SaveUtils.getFragmentFirstTime(activity, SaveUtils.Screens.STATUS)) return
        SaveUtils.setFragmentFirstTime(activity, SaveUtils.Screens.STATUS, false)
        //Put animations here
        val a = FancyShowCaseView.Builder(activity)
                .focusCircleAtPosition(1225,2325, 200)
                .title("Tap to update CLI to newest version")
                .delay(500)
                .enableAutoTextPosition()
                .build()

        val b = FancyShowCaseView.Builder(activity)
                .focusCircleAtPosition(1325, 1300, 100)
                .title("Tap to change your Raspberry Pi name")
                .delay(50)
                .enableAutoTextPosition()
                .build()
        val queue = FancyShowCaseQueue().add(a).add(b)
        queue.show()
    }
}
