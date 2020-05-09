package io.treehouses.remote.bluetoothv2.base.presenter

import io.treehouses.remote.bluetoothv2.base.interactor.MVPInteractor
import io.treehouses.remote.bluetoothv2.base.view.MVPView

interface MVPPresenter<V : MVPView, I : MVPInteractor> {

    fun onAttach(view: V?)

    fun onDetach()

    fun getView(): V?

}