package io.treehouses.remote

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.tabs.TabLayoutMediator
import io.treehouses.remote.databinding.ActivityIntroBinding

class IntroActivity : AppCompatActivity() {
    lateinit var bind: ActivityIntroBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityIntroBinding.inflate(layoutInflater)
        TabLayoutMediator(bind.tabDots, bind.introPager) { tab, position ->
            tab.text = "OBJECT ${(position + 1)}"
        }.attach()
    }
}