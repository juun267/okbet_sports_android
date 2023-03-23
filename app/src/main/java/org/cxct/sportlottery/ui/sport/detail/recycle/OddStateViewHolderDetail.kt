package org.cxct.sportlottery.ui.sport.detail.recycle

import android.os.Handler
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.common.OddState
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.ui.game.widget.OddsButtonDetail

abstract class OddStateViewHolderDetail(itemView: View) : RecyclerView.ViewHolder(itemView) {
    interface OddStateChangeListener {
        fun refreshOddButton(odd: Odd)
    }

    companion object {
        private const val HIGH_LIGHT_TIME: Long = 3000
    }

    abstract val oddStateChangeListener: OddStateChangeListener
    private val mHandler: Handler by lazy { Handler() }

    protected fun setupOddState(oddsButton: OddsButtonDetail, itemOdd: Odd?) {
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

    private fun highLightRunnable(oddsButton: OddsButtonDetail, itemOdd: Odd): Runnable {
        return Runnable {
            itemOdd.oddState = OddState.SAME.state
            setupOddState(oddsButton, itemOdd)
//            oddStateChangeListener.refreshOddButton(itemOdd)
            itemOdd.runnable = null
            itemOdd.runnable?.let { mHandler.removeCallbacks(it) }
        }
    }

    private fun resetRunnable(oddsButton: OddsButtonDetail, itemOdd: Odd) {
        itemOdd.runnable?.let {
            mHandler.removeCallbacks(it)
        }
        if (itemOdd.oddState == OddState.SAME.state) return
        val runnable = highLightRunnable(oddsButton, itemOdd)
        itemOdd.runnable = runnable
        mHandler.postDelayed(runnable, HIGH_LIGHT_TIME)
    }
}