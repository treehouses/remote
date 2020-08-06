package io.treehouses.remote.Fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import io.treehouses.remote.bases.BaseFragment
import io.treehouses.remote.callback.BackPressReceiver
import io.treehouses.remote.databinding.FragmentCommunityBinding

class CommunityFragment : BaseFragment(), BackPressReceiver {
    private lateinit var bind: FragmentCommunityBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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
        bind.map.loadUrl("https://www.google.com/maps/d/u/0/viewer?ll=11.88717970130264%2C10.93123241891862&z=4&mid=1rO3RmHQnrSNsBwB9skqHos970zI-ZVAA")

    }

    override fun onBackPressed() {
        if (bind.map.canGoBack()) {
            bind.map.goBack()
        }
    }

}