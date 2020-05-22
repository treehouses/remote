package io.treehouses.remote

import android.app.Application
import com.parse.Parse
import com.polidea.rxandroidble2.RxBleClient
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import io.treehouses.remote.bluetoothv2.di.component.DaggerAppComponent
import io.treehouses.remote.bluetoothv2.services.BluetoothConnectionService_Factory
import io.treehouses.remote.utils.SaveUtils
import java.util.*
import javax.inject.Inject

class MainApplication : Application(), HasAndroidInjector {
    @Inject
    lateinit internal var activityDispatchingAndroidInjector: DispatchingAndroidInjector<Any>






    override fun onCreate() {
        super.onCreate()
       rxBleClient = RxBleClient.create(this)
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

        DaggerAppComponent.builder()
                .application(this)
                .build()
                .inject(this)
    }

    companion object {
        @JvmStatic
        lateinit var rxBleClient: RxBleClient

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

    override fun androidInjector(): AndroidInjector<Any> {
        return activityDispatchingAndroidInjector
    }


}