package io.treehouses.remote.utils

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

class AppLifecycleObserver : DefaultLifecycleObserver {
    private var activityReferences = 0
    private var isActivityChangingConfigurations = false

    override fun onStart(owner: LifecycleOwner) {
        if (++activityReferences == 1 && !isActivityChangingConfigurations) {
            // App enters foreground
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        isActivityChangingConfigurations = owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        if (--activityReferences == 0 && !isActivityChangingConfigurations) {
            // App enters background
        }
    }
}