package io.treehouses.remote.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import io.github.kbiakov.codeview.CodeView
import io.github.kbiakov.codeview.adapters.Options
import io.github.kbiakov.codeview.highlight.ColorTheme
import io.treehouses.remote.databinding.CodeViewBinding
import io.treehouses.remote.databinding.FragmentShowBluetoothFileBinding
import io.treehouses.remote.utils.LogUtils
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
        bluetoothBind.pbar.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            val code = context?.assets?.open("bluetooth-server.txt")?.bufferedReader().use { it?.readText() }
            LogUtils.log("GOT CODE " + code)
            withContext(Dispatchers.Main) {
                if (code == null) {
                    bluetoothBind.fileNotFound.visibility = View.VISIBLE
                } else {
                    withContext(Dispatchers.Default) {
                        val codeView = createCodeView(code)
                        withContext(Dispatchers.Main) {
                            bluetoothBind.scriptContainer.addView(codeView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
                            bluetoothBind.pbar.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    fun createCodeView(code : String) : CodeView {
        val codeView = CodeViewBinding.inflate(layoutInflater).root
        codeView.setOptions(Options.Default.get(requireContext())
            .withLanguage("python")
            .withCode(code)
            .withTheme(ColorTheme.MONOKAI))
        return codeView
    }
}