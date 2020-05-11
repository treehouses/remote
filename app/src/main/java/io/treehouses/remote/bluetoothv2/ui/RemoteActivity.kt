package io.treehouses.remote.bluetoothv2.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.polidea.rxandroidble2.scan.ScanResult
import io.treehouses.remote.R
import io.treehouses.remote.bluetoothv2.ui.home.view.ScanResultsAdapter
import io.treehouses.remote.bluetoothv2.services.BluetoothConnectionService
import io.treehouses.remote.bluetoothv2.ui.home.view.HomeFragment


class RemoteActivity : AppCompatActivity(), (ScanResult) -> Unit {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remote)
        supportFragmentManager.beginTransaction().replace(R.id.content, HomeFragment())

    }

    override fun invoke(p1: ScanResult) {
    }

}