package io.treehouses.remote.bluetoothv2.di.builder

import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.treehouses.remote.bluetoothv2.ui.RemoteActivity
import io.treehouses.remote.bluetoothv2.ui.home.HomeFragmentModule
import io.treehouses.remote.bluetoothv2.ui.home.HomeFragmentProvider

@Module
abstract class ActivityBuilder {
    @ContributesAndroidInjector(modules = [(HomeFragmentProvider::class)])
    abstract fun bindRemoteActivity(): RemoteActivity


}