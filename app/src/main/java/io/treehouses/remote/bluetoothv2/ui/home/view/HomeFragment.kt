package io.treehouses.remote.bluetoothv2.ui.home.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.polidea.rxandroidble2.scan.ScanResult
import io.reactivex.Flowable.interval
import io.reactivex.Observable
import io.reactivex.Observable.interval
import io.treehouses.remote.R
import io.treehouses.remote.bluetoothv2.base.view.BaseFragment
import io.treehouses.remote.bluetoothv2.ui.ReadWriteActivity
import io.treehouses.remote.bluetoothv2.ui.home.interactor.HomeMVPInterator
import io.treehouses.remote.bluetoothv2.ui.home.presenter.HomeMVPPresenter
import kotlinx.android.synthetic.main.fragment_home.*
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class HomeFragment : BaseFragment(), HomeMVPView, (ScanResult) -> Unit {
    private var resultsAdapter =
            ScanResultsAdapter(this)

    @Inject
    internal lateinit var presenter: HomeMVPPresenter<HomeMVPView, HomeMVPInterator>

    override fun setUp() {
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        presenter.onAttach(this)
        btn_connect.setOnClickListener {
            presenter.onScanClicked()
        }
        rv_devices.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = resultsAdapter
        }
    }

    override fun showDevice(scanResult: ScanResult) {
        resultsAdapter.addScanResult(scanResult)
    }

    override fun showError(message: String) {
        Snackbar.make(btn_connect, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun invoke(res: ScanResult) {
        var a = res.bleDevice.establishConnection(false).flatMapSingle { it.discoverServices() }
                .take(1)
                .subscribe(
                        { t ->
                            startActivity(ReadWriteActivity.newInstance(requireContext(), res.bleDevice.macAddress, t.bluetoothGattServices[0].uuid))
                        }, // Print services
                        { Log.e("ERROR", "WHOOPS!", it) }
                )


    }


}
