package io.treehouses.remote.bluetoothv2.di.module

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable
import io.treehouses.remote.bluetoothv2.services.BluetoothConnectionService
import io.treehouses.remote.bluetoothv2.services.BluetoothHelper
import io.treehouses.remote.bluetoothv2.services.BluetoothReadWriteService
import io.treehouses.remote.bluetoothv2.services.BluetoothService
import io.treehouses.remote.bluetoothv2.util.SchedulerProvider
import javax.inject.Singleton

@Module
class AppModule {

    @Provides
    @Singleton
    internal fun provideContext(application: Application): Context = application


    @Provides
    internal fun provideCompositeDisposable(): CompositeDisposable = CompositeDisposable()

    @Provides
    internal fun provideSchedulerProvider(): SchedulerProvider = SchedulerProvider()

    @Provides
    @Singleton
    internal fun provideBluetoothService(bluetoothHelper: BluetoothService): BluetoothHelper = bluetoothHelper

}