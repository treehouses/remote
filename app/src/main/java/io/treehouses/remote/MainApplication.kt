package io.treehouses.remote

import android.app.Application
import com.parse.Parse
import io.treehouses.remote.utils.SaveUtils
import java.util.*

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        terminalList = ArrayList()
        tunnelList = ArrayList()
        commandList = ArrayList()
        Parse.initialize(Parse.Configuration.Builder(this)
                .applicationId(Constants.PARSE_APPLICATION_ID)
                .clientKey(null)
                .server(Constants.PARSE_URL)
                .build()
        )
        SaveUtils.initCommandsList(applicationContext)
    }

    companion object {
        @JvmStatic
        var terminalList: ArrayList<String>? = null
            private set
        @JvmStatic
        var tunnelList: ArrayList<String>? = null
            private set
        @JvmStatic
        var commandList: ArrayList<String>? = null
            private set
        @JvmField
        var showLogDialog = true
        @JvmField
        var ratingDialog = true

    }
}