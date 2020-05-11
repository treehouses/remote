package io.treehouses.remote.bluetoothv2.ui.home

import dagger.Module
import dagger.Provides
import io.treehouses.remote.bluetoothv2.ui.home.interactor.HomeInteractor
import io.treehouses.remote.bluetoothv2.ui.home.interactor.HomeMVPInterator
import io.treehouses.remote.bluetoothv2.ui.home.presenter.HomeMVPPresenter
import io.treehouses.remote.bluetoothv2.ui.home.presenter.HomePresenter
import io.treehouses.remote.bluetoothv2.ui.home.view.HomeMVPView

@Module
class HomeFragmentModule {

    @Provides
    internal fun provideHomeInteractor(interactor: HomeInteractor): HomeMVPInterator = interactor

    @Provides
    internal fun provideHomePresenter(presenter: HomePresenter<HomeMVPView, HomeMVPInterator>)
            : HomeMVPPresenter<HomeMVPView, HomeMVPInterator> = presenter
}