package io.treehouses.remote

import androidx.fragment.app.FragmentActivity
import io.treehouses.remote.databinding.*
import me.toptas.fancyshowcase.FancyShowCaseQueue
import me.toptas.fancyshowcase.FancyShowCaseView

object Tutorials {

    fun homeTutorials(bind: ActivityHomeFragmentBinding, activity: FragmentActivity) {
        val a = FancyShowCaseView.Builder(activity)
                .focusOn(bind.testConnection)
                .title("Test Bluetooth Connection to RPI")
                .delay(750)
                .enableAutoTextPosition()
                .build()

        val b = FancyShowCaseView.Builder(activity)
                .focusOn(bind.networkProfiles)
                .title("Configure Network Profiles in Network Screen")
                .delay(500)
                .focusCircleRadiusFactor(1.25)
                .enableAutoTextPosition()
                .build()
        val queue = FancyShowCaseQueue().add(a).add(b)
        queue.show()
    }

    fun networkTutorials(bind: NewNetworkBinding, activity: FragmentActivity) {

    }

    fun systemTutorials(bind: NewNetworkBinding, activity: FragmentActivity) {

    }

    fun terminalTutorials(bind: ActivityTerminalFragmentBinding, activity: FragmentActivity) {

    }

    fun servicesFirstTabTutorials(bind: ActivityServicesTabFragmentBinding, activity: FragmentActivity) {

    }

    fun servicesSecondTabTutorials(bind: ActivityServicesDetailsBinding, activity: FragmentActivity) {

    }

    fun tunnelTutorials(bind: ActivityTunnelFragmentBinding, activity: FragmentActivity) {

    }

    fun statusTutorials(bind: ActivityStatusFragmentBinding, activity: FragmentActivity) {

    }
}