package org.cxct.sportlottery.util

import android.os.Handler
import android.os.Looper

class DelayRunable(val runnable: Runnable) {

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