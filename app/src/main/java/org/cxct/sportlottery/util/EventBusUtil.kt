package org.cxct.sportlottery.util

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import org.greenrobot.eventbus.EventBus

object EventBusUtil {

    fun targetLifecycle(lifecycleOwner: LifecycleOwner) {
        if (lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
            return
        }
        register(lifecycleOwner)
        lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (Lifecycle.Event.ON_DESTROY == event) {
                    unregister(lifecycleOwner)
                }
            }
        })
    }

    fun register(target: Any) {
        if (!EventBus.getDefault().isRegistered(target)) {
            EventBus.getDefault().register(target)
        }
    }

    fun unregister(target: Any) {
        if (EventBus.getDefault().isRegistered(target)) {
            EventBus.getDefault().unregister(target)
        }
    }

    fun post(event: Any) {
        EventBus.getDefault().post(event)
    }

    fun postSticky(event: Any) {
        EventBus.getDefault().postSticky(event)
    }

    fun removeStickyEvent(eventClass: Class<*>) {
        EventBus.getDefault().removeStickyEvent(eventClass)
    }
}