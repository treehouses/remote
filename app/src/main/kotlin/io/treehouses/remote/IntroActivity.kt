package io.treehouses.remote

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import io.treehouses.remote.callback.IntroPagerListener
import io.treehouses.remote.databinding.ActivityIntroBinding
import io.treehouses.remote.databinding.IntroScreenBluetoothBinding
import io.treehouses.remote.databinding.IntroScreenDownloadBinding
import io.treehouses.remote.databinding.IntroScreenWelcomeBinding

class IntroActivity : AppCompatActivity() {
    lateinit var binding: ActivityIntroBinding
    val callback = object : IntroPagerListener {
        override fun goToPosition(position: Int) {
            binding.introPager.setCurrentItem(position, true)
        }

        override fun goToMain() {
            startActivity(Intent(this@IntroActivity, InitialActivity::class.java))
            finish()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.introPager.adapter = IntroPageAdapter(supportFragmentManager)
        TabLayoutMediator(binding.tabDots, binding.introPager) { _, _ -> }.attach()
        binding.skipButton.setOnClickListener { callback.goToMain() }

    }
    inner class IntroPageAdapter(fragmentManager: FragmentManager) : FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount(): Int {
            return 3
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> IntroSlideWelcome(callback)
                1 -> IntroSlideDownload(callback)
                2 -> IntroSlideBluetooth(callback)
                else -> throw IllegalStateException("Nonexistent fragment")
            }
        }
    }
    class IntroSlideWelcome(val listener: IntroPagerListener) : Fragment() {
        private lateinit var welcomeBind: IntroScreenWelcomeBinding
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
            welcomeBind = IntroScreenWelcomeBinding.inflate(inflater, container, false)
            return welcomeBind.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            welcomeBind.nextBtn.setOnClickListener { listener.goToPosition(1) }
        }
    }

    class IntroSlideDownload(val listener: IntroPagerListener) : Fragment() {
        private lateinit var downloadBind: IntroScreenDownloadBinding
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
            downloadBind = IntroScreenDownloadBinding.inflate(inflater, container, false)
            return downloadBind.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            downloadBind.downloadBtn.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://treehouses.io/#!pages/download.md"))
                startActivity(intent)
            }

        }
    }

    class IntroSlideBluetooth(val listener: IntroPagerListener) : Fragment() {
        private lateinit var bluetoothBind: IntroScreenBluetoothBinding
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
            bluetoothBind = IntroScreenBluetoothBinding.inflate(inflater, container, false)
            return bluetoothBind.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            bluetoothBind.nextBtn.setOnClickListener { listener.goToMain() }
        }
    }

}