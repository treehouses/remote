package io.treehouses.remote.bluetoothv2.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.polidea.rxandroidble2.scan.ScanResult
import dagger.android.DispatchingAndroidInjector
import io.treehouses.remote.R
import io.treehouses.remote.bluetoothv2.base.view.BaseActivity
import io.treehouses.remote.bluetoothv2.ui.home.view.ScanResultsAdapter
import io.treehouses.remote.bluetoothv2.services.BluetoothConnectionService
import io.treehouses.remote.bluetoothv2.ui.home.view.HomeFragment
import javax.inject.Inject


class RemoteActivity : BaseActivity(), (ScanResult) -> Unit {
    @Inject
    internal lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remote)
        supportFragmentManager.beginTransaction().replace(R.id.content, HomeFragment()).commit()
    }

    override fun onFragmentAttached() {
    }

    override fun onFragmentDetached(tag: String) {
    }

    override fun invoke(p1: ScanResult) {
    }

    override fun supportFragmentInjector() = fragmentDispatchingAndroidInjector

}