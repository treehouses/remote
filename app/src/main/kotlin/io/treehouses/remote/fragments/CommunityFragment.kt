package io.treehouses.remote.fragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import io.treehouses.remote.bases.BaseFragment
import io.treehouses.remote.callback.BackPressReceiver
import io.treehouses.remote.databinding.FragmentCommunityBinding
import io.treehouses.remote.utils.GPSService

class CommunityFragment : BaseFragment(), BackPressReceiver {
    private lateinit var bind: FragmentCommunityBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        bind = FragmentCommunityBinding.inflate(inflater, container, false)
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind.pbar.visibility = View.VISIBLE
        bind.map.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                bind.pbar.visibility = View.GONE
            }
        }
        bind.map.settings.javaScriptEnabled = true
        #bind.map.loadUrl("https://www.google.com/maps/d/u/0/viewer?ll=11.88717970130264%2C10.93123241891862&z=4&mid=1rO3RmHQnrSNsBwB9skqHos970zI-ZVAA")
        bind.map.loadUrl("http://maps.media.mit.edu/remote.html")

        startGPSService()
    }

    private fun startGPSService() {
        val intent = Intent(requireContext(), GPSService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().startForegroundService(intent)
        } else {
            requireContext().startService(intent)
        }
    }


    override fun onBackPressed() {
        if (bind.map.canGoBack()) {
            bind.map.goBack()
        }
    }

}
