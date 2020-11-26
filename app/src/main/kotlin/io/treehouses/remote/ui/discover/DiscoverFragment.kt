package io.treehouses.remote.ui.discover

import android.os.Bundle
import android.text.AutoText.getSize
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import io.treehouses.remote.bases.BaseFragment
import io.treehouses.remote.databinding.ActivityDiscoverFragmentBinding
import io.treehouses.remote.fragments.DiscoverFragment
import io.treehouses.remote.interfaces.FragmentDialogInterface
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class DiscoverFragment : BaseFragment(), FragmentDialogInterface {

    protected val viewModel: DiscoverViewModel by viewModels(ownerProducer = {this})
    private lateinit var bind: ActivityDiscoverFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = ActivityDiscoverFragmentBinding.inflate(inflater, container, false)
        viewModel.onLoad()
        mChatService = listener.getChatService()
        mChatService.updateHandler(mHandler)

        return bind.root
    }



}