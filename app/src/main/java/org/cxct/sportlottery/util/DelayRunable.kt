package org.cxct.sportlottery.util

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LifecycleOwner
import org.cxct.sportlottery.common.extentions.doOnDestory

class DelayRunable(lifecycleOwner: LifecycleOwner? = null, val runnable: Runnable) {

    init {
        lifecycleOwner?.doOnDestory { clear() }
    }

    private val handler = Handler(Looper.getMainLooper())

    fun doOnDelay(delay: Long) {
        handler.removeCallbacks(runnable)
        if (delay == 0L) {
            runnable.run()
        } else {
            handler.postDelayed(runnable, delay)
        }
    }

    fun clear() {
        handler.removeCallbacks(runnable)
    }
}