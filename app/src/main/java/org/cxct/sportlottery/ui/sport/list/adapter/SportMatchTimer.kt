package org.cxct.sportlottery.ui.sport.list.adapter

import java.util.Timer
import java.util.TimerTask

class SportMatchTimer {

    private var timer: Timer? = null

    fun isRuning() = timer != null

    fun stop() {
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
    }

    fun start(delay: Long, period: Long, task: TimerTask) {
        stop()
        timer = Timer().apply { schedule(task, delay, period) }
    }

}