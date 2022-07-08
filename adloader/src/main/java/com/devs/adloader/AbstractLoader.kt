package com.devs.adloader

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

abstract class AbstractLoader(
    protected val lifecycle: Lifecycle,
) : DefaultLifecycleObserver {
    companion object{
         const val TAG = "AD_LOADER_DEBUG"
    }
    init {
        lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {

    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
    }

    override fun onStop(owner: LifecycleOwner) {

    }

    abstract fun destroy()
    override fun onDestroy(owner: LifecycleOwner) {
        destroy()
        super.onDestroy(owner)
        lifecycle.removeObserver(this)

    }
}