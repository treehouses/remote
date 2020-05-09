package io.treehouses.remote.bluetoothv2.base.presenter

import io.reactivex.disposables.CompositeDisposable
import io.treehouses.remote.bluetoothv2.base.interactor.MVPInteractor
import io.treehouses.remote.bluetoothv2.base.view.MVPView
import io.treehouses.remote.bluetoothv2.util.SchedulerProvider

abstract class BasePresenter<V : MVPView, I : MVPInteractor> internal constructor(protected var interactor: I?, protected val schedulerProvider: SchedulerProvider, protected val compositeDisposable: CompositeDisposable) : MVPPresenter<V, I> {

    private var view: V? = null
    private val isViewAttached: Boolean get() = view != null

    override fun onAttach(view: V?) {
        this.view = view
    }

    override fun getView(): V? = view

    override fun onDetach() {
        compositeDisposable.dispose()
        view = null
        interactor = null
    }

}