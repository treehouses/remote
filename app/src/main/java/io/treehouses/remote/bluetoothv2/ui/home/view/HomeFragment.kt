package io.treehouses.remote.bluetoothv2.ui.home.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.polidea.rxandroidble2.scan.ScanResult

import io.treehouses.remote.R
import io.treehouses.remote.bluetoothv2.ScanResultsAdapter
import io.treehouses.remote.bluetoothv2.base.view.BaseFragment

class HomeFragment : BaseFragment(), HomeMVPView, (ScanResult) -> Unit {
    private var resultsAdapter =
            ScanResultsAdapter(this)
    override fun setUp() {
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun showDevice(scanResult: ScanResult) {
        resultsAdapter.addScanResult(scanResult)
    }

    override fun invoke(p1: ScanResult) {
    }


}
