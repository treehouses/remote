package io.treehouses.remote.ui.discover

import androidx.fragment.app.viewModels
import io.treehouses.remote.bases.BaseFragment
import io.treehouses.remote.interfaces.FragmentDialogInterface

class DiscoverFragment : BaseFragment(), FragmentDialogInterface {

    protected val viewModel: DiscoverViewModel by viewModels(ownerProducer = {this})

}