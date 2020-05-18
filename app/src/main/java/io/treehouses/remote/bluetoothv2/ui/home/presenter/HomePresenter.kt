package io.treehouses.remote.bluetoothv2.ui.home.presenter

import javax.inject.Inject
import io.reactivex.disposables.CompositeDisposable
import io.treehouses.remote.bluetoothv2.base.presenter.BasePresenter
import io.treehouses.remote.bluetoothv2.ui.home.interactor.HomeMVPInterator
import io.treehouses.remote.bluetoothv2.ui.home.view.HomeMVPView
import io.treehouses.remote.bluetoothv2.util.SchedulerProvider

class HomePresenter<V : HomeMVPView, I : HomeMVPInterator> @Inject internal constructor(interator: I, schedulerProvider: SchedulerProvider, compositeDisposable: CompositeDisposable) : BasePresenter<V, I>(interactor = interator, schedulerProvider = schedulerProvider, compositeDisposable = compositeDisposable), HomeMVPPresenter<V, I> {
    override fun onConnectClicked() {
        interactor?.connectToDevice("")
    }

    override fun onDisconnectClicked() {
        interactor?.disconnectDevice()
    }

    override fun onScanClicked() {
        getView()?.showProgress()
        interactor?.let {
            compositeDisposable.add(it.scanDevices()
                    .compose(schedulerProvider.ioToMainObservableScheduler())
                    .subscribe({ result ->
                        getView()?.showDevice(result)
                        getView()?.hideProgress()
                    }, { err -> getView()?.hideProgress() }))
        }

    }

}