package org.cxct.sportlottery.ui.game.common

import android.os.Handler
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.network.odds.detail.Odd
import org.cxct.sportlottery.network.odds.list.OddState
import org.cxct.sportlottery.ui.game.widget.OddsButton

/**
 * @author Kevin
 * @create 2021/06/21
 */
open class OddDetailStateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    interface OddStateChangeListener {
        fun refreshOddButton(odd: Odd)
    }

    companion object {
        private const val HIGH_LIGHT_TIME: Long = 3000
    }

    var oddStateChangeListener: OddStateChangeListener? = null
    private val mHandler: Handler by lazy { Handler() }

    protected fun setupOddState(oddsButton: OddsButton, itemOdd: Odd?) {
        when (itemOdd?.oddState) {
            OddState.SAME.state -> {
                itemOdd.runnable?.let {
                    return
                } ?: run {
                    oddsButton.oddStatus = OddState.SAME.state
                }
            }
            OddState.LARGER.state -> {
                oddsButton.oddStatus = OddState.LARGER.state
                resetRunnable(itemOdd)
            }
            OddState.SMALLER.state -> {
                oddsButton.oddStatus = OddState.SMALLER.state
                resetRunnable(itemOdd)
            }
        }
    }

    private fun highLightRunnable(itemOdd: Odd): Runnable {
        return Runnable {
            oddStateChangeListener?.refreshOddButton(itemOdd)
            itemOdd.oddState = OddState.SAME.state
            itemOdd.runnable = null
        }
    }

    private fun resetRunnable(itemOdd: Odd) {
        itemOdd.runnable?.let {
            mHandler.removeCallbacks(it)
        }
        val runnable = highLightRunnable(itemOdd)
        itemOdd.runnable = runnable
        mHandler.postDelayed(runnable, HIGH_LIGHT_TIME)
    }
}