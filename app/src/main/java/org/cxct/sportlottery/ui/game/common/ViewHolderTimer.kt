package org.cxct.sportlottery.ui.game.common

import android.os.Handler
import android.os.Looper
import android.view.View
import java.util.*

/**
 *  Created by jackson on 2022/5/10
 */
abstract class ViewHolderTimer(itemView: View) : OddStateViewHolder(itemView) {
    interface TimerListener {
        fun onTimerUpdate(timeMillis: Long)
    }

    protected var listener: TimerListener? = null

    private var timer: Timer? = null

    fun updateTimer(
        isTimerEnable: Boolean,
        isTimerPause: Boolean,
        startTime: Int,
        isDecrease: Boolean
    ) {
        when (isTimerEnable) {
            false -> {
                stopTimer()
            }

            true -> {
                startTimer(isTimerPause, startTime, isDecrease)
            }

        }
    }

    private fun startTimer(isTimerPause: Boolean, startTime: Int, isDecrease: Boolean) {
        var timeMillis = startTime * 1000L
        stopTimer()
        Handler(Looper.getMainLooper()).post {
            listener?.onTimerUpdate(timeMillis)
        }

        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                when (isDecrease) {
                    true -> {
                        if (!isTimerPause) timeMillis -= 1000
                    }
                    false -> {
                        if (!isTimerPause) timeMillis += 1000
                    }
                }

                if (timeMillis > 0) {
                    Handler(Looper.getMainLooper()).post {
                        listener?.onTimerUpdate(timeMillis)
                    }
                }
            }
        }, 1000L, 1000L)
    }

    fun stopTimer() {
        timer?.cancel()
        timer = null
    }
}