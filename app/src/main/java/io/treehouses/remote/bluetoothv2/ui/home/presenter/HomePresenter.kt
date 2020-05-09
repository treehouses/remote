package io.treehouses.remote.bluetoothv2.ui.home.presenter

import bleshadow.javax.inject.Inject
import io.reactivex.disposables.CompositeDisposable
import io.treehouses.remote.bluetoothv2.base.presenter.BasePresenter
import io.treehouses.remote.bluetoothv2.ui.home.interactor.HomeMVPInterator
import io.treehouses.remote.bluetoothv2.ui.home.view.HomeMVPView
import io.treehouses.remote.bluetoothv2.util.SchedulerProvider

class HomePresenter<V : HomeMVPView, I : HomeMVPInterator> @Inject internal constructor(interator: I, schedulerProvider: SchedulerProvider, compositeDisposable: CompositeDisposable) : BasePresenter<V, I>(interactor = interator, schedulerProvider = schedulerProvider, compositeDisposable = compositeDisposable), HomeMVPPresenter<V, I> {
    override fun onConnectClicked() {
    }

    override fun onDisconnectClicked() {
    }

}