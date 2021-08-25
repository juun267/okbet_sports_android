package org.cxct.sportlottery.ui.common

import android.os.Handler
import java.util.*

interface TimerManager {
    var startTime: Long
    var timer: Timer
    var timerHandler: Handler

    fun startTimer() {
        timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                timerHandler.sendEmptyMessage(0)
            }
        }, 0, 1000)
    }

    fun cancelTimer() {
        timer.apply {
            cancel()
            purge()
        }
    }
}