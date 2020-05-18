package io.treehouses.remote.bluetoothv2.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import io.treehouses.remote.R
import io.treehouses.remote.bluetoothv2.base.view.BaseActivity
import io.treehouses.remote.bluetoothv2.ui.home.view.HomeFragment
import javax.inject.Inject


class RemoteActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remote)
        supportFragmentManager.beginTransaction().replace(R.id.content, HomeFragment()).commit()
    }

    override fun onFragmentAttached() {
    }

    override fun onFragmentDetached(tag: String) {
    }

}