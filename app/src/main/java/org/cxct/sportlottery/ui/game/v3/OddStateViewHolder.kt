package org.cxct.sportlottery.ui.game.v3

import android.os.Handler
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.network.bet.info.MatchOdd
import org.cxct.sportlottery.network.odds.list.Odd
import org.cxct.sportlottery.ui.game.widget.OddButton

abstract class OddStateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    interface OddStateChangeListener {
        fun refreshOddButton(odd: Odd)
    }

    companion object {
        private const val HIGH_LIGHT_TIME: Long = 3000
    }

    abstract val oddStateChangeListener: OddStateChangeListener
    private val mHandler: Handler by lazy { Handler() }

    protected fun setupOddState(oddButton: OddButton, itemOdd: Odd?) {

        when (itemOdd?.oddState) {
            MatchOdd.OddState.SAME.state -> {
                itemOdd.runnable?.let {
                    return
                } ?: run {
                    oddButton.oddStatus = MatchOdd.OddState.SAME.state
                }
            }
            MatchOdd.OddState.LARGER.state -> {
                oddButton.oddStatus = MatchOdd.OddState.LARGER.state
                resetRunnable(itemOdd)
            }
            MatchOdd.OddState.SMALLER.state -> {
                oddButton.oddStatus = MatchOdd.OddState.SMALLER.state
                resetRunnable(itemOdd)
            }
        }
    }

    private fun highLightRunnable(itemOdd: Odd): Runnable {
        return Runnable {
            oddStateChangeListener.refreshOddButton(itemOdd)
            itemOdd.oddState = MatchOdd.OddState.SAME.state
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