package io.treehouses.remote.bluetoothv2.di.builder

import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.treehouses.remote.bluetoothv2.ui.RemoteActivity
import io.treehouses.remote.bluetoothv2.ui.home.HomeFragmentModule

@Module
abstract class ActivityBuilder {
    @ContributesAndroidInjector(modules = [(HomeFragmentModule::class)])
    abstract fun bindRemoteActivity(): RemoteActivity
}