package org.cxct.sportlottery.ui.game.common

import android.os.Handler
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.common.OddState
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.ui.game.widget.OddsButton
import org.cxct.sportlottery.ui.game.widget.OddsOutrightButton

abstract class OddStateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    interface OddStateChangeListener {
        fun refreshOddButton(odd: Odd)
    }

    companion object {
        private const val HIGH_LIGHT_TIME: Long = 3000
    }

    abstract val oddStateChangeListener: OddStateChangeListener
    private val mHandler: Handler by lazy { Handler() }

    fun setupOddState(oddsButton: OddsButton, itemOdd: Odd?) {
        itemOdd?.let { odd ->
            if (oddsButton.oddStatus == odd.oddState) return
            when (odd.oddState) {
                OddState.SAME.state -> {
                    oddsButton.oddStatus = OddState.SAME.state
                }
                OddState.LARGER.state -> {
                    oddsButton.oddStatus = OddState.LARGER.state
                    resetRunnable(oddsButton, odd)
                }
                OddState.SMALLER.state -> {
                    oddsButton.oddStatus = OddState.SMALLER.state
                    resetRunnable(oddsButton, odd)
                }
            }
        }
    }

    fun setupOddState(oddsButton: OddsOutrightButton, itemOdd: Odd?) {
        itemOdd?.let { odd ->
            if (oddsButton.oddStatus == odd.oddState) return
            when (odd.oddState) {
                OddState.SAME.state -> {
                    oddsButton.oddStatus = OddState.SAME.state
                }
                OddState.LARGER.state -> {
                    oddsButton.oddStatus = OddState.LARGER.state
                    resetRunnable(oddsButton, odd)
                }
                OddState.SMALLER.state -> {
                    oddsButton.oddStatus = OddState.SMALLER.state
                    resetRunnable(oddsButton, odd)
                }
            }
        }
    }

    private fun highLightRunnable(oddsButton: OddsButton, itemOdd: Odd): Runnable {
        return Runnable {
            itemOdd.oddState = OddState.SAME.state
            oddsButton.oddStatus = OddState.SAME.state
            itemOdd.runnable?.let { mHandler.removeCallbacks(it) }
            itemOdd.runnable = null
        }
    }

    private fun highLightRunnable(oddsButton: OddsOutrightButton, itemOdd: Odd): Runnable {
        return Runnable {
            itemOdd.oddState = OddState.SAME.state
            oddsButton.oddStatus = OddState.SAME.state
            itemOdd.runnable?.let { mHandler.removeCallbacks(it) }
            itemOdd.runnable = null
        }
    }

    private fun resetRunnable(oddsButton: OddsButton, itemOdd: Odd) {
        itemOdd.runnable?.let {
            mHandler.removeCallbacks(it)
        }
        if (itemOdd.oddState == OddState.SAME.state) return
        val runnable = highLightRunnable(oddsButton, itemOdd)
        itemOdd.runnable = runnable
        mHandler.postDelayed(runnable, HIGH_LIGHT_TIME)
    }

    private fun resetRunnable(oddsButton: OddsOutrightButton, itemOdd: Odd) {
        itemOdd.runnable?.let {
            mHandler.removeCallbacks(it)
        }
        if (itemOdd.oddState == OddState.SAME.state) return
        val runnable = highLightRunnable(oddsButton, itemOdd)
        itemOdd.runnable = runnable
        mHandler.postDelayed(runnable, HIGH_LIGHT_TIME)
    }
}