package io.treehouses.remote.di

import io.treehouses.remote.network.BluetoothChatService
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { BluetoothChatService(applicationContext = androidContext()) }
}
