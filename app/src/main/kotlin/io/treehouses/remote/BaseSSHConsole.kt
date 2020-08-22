package io.treehouses.remote

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import io.treehouses.remote.ui.services.ServicesViewModel

class BaseSSHConsole {
    lateinit var viewModel: SSHConsoleViewModel

    fun getViewModel() {
        viewModel = ViewModelProvider(this)[SSHConsoleViewModel::class.java]!!
    }
}