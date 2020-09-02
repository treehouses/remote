package io.treehouses.remote.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import io.treehouses.remote.databinding.FragmentShowBluetoothFileBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ShowBluetoothFile : Fragment() {
    private lateinit var bluetoothBind : FragmentShowBluetoothFileBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bluetoothBind = FragmentShowBluetoothFileBinding.inflate(inflater, container, false)
        return bluetoothBind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch(Dispatchers.IO) {
            val code = context?.assets?.open("bluetooth-server.txt")?.bufferedReader().use { it?.readText() }
            withContext(Dispatchers.Main) {
                if (code == null) {
                    bluetoothBind.fileNotFound.visibility = View.VISIBLE
                } else {
                    bluetoothBind.codeView.text = code
                }
            }
        }
    }

}