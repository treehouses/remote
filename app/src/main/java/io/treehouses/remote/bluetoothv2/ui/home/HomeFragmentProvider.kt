package io.treehouses.remote.bluetoothv2.ui.home

import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.treehouses.remote.bluetoothv2.ui.home.view.HomeFragment

@Module
abstract class HomeFragmentProvider {

    @ContributesAndroidInjector(modules = [HomeFragmentModule::class])
    internal abstract fun provideHomeFragmentFactory(): HomeFragment
}