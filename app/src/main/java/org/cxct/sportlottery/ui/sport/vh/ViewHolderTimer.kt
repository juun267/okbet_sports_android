package org.cxct.sportlottery.ui.sport.vh

import android.view.View
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.ui.sport.common.OddStateViewHolder
import java.util.*

abstract class ViewHolderTimer(itemView: View) : OddStateViewHolder(itemView) {
    interface TimerListener {
        fun onTimerUpdate(timeMillis: Long)
    }

    var listener: TimerListener? = null

    private var timer: Timer? = null

    fun updateTimer(
        isTimerEnable: Boolean,
        isTimerPause: Boolean,
        startTime: Int,
        isDecrease: Boolean,
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
        GlobalScope.launch(Dispatchers.Main) {
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
                    GlobalScope.launch(Dispatchers.Main) {
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
