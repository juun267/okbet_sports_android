package org.cxct.sportlottery.ui.game.publicity

import android.os.Handler
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.common.enums.OddState
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.ui.game.widget.OddsButtonHome

abstract class OddStateHomeViewHolder(val lifecycleOwner: LifecycleOwner, itemView: View) : RecyclerView.ViewHolder(itemView) {

    init {
        lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    onLifeDestroy()
                }
            }
        })
    }

    interface OddStateChangeListener {
        fun refreshOddButton(odd: Odd)
    }

    companion object {
        private const val HIGH_LIGHT_TIME: Long = 3500
    }

    abstract val oddStateChangeListener: OddStateChangeListener
    var hasHandler = false
    private val mHandler: Handler by lazy {
        hasHandler = true
        Handler()
    }

    protected fun setupOddState(oddsButton: OddsButtonHome, itemOdd: Odd?) {
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

    private fun highLightRunnable(oddsButton: OddsButtonHome, itemOdd: Odd): Runnable {
        return Runnable {
            itemOdd.oddState = OddState.SAME.state
            setupOddState(oddsButton, itemOdd)
//            oddStateChangeListener.refreshOddButton(itemOdd)
            itemOdd.runnable?.let { mHandler.removeCallbacks(it) }
            itemOdd.runnable = null
        }
    }

    private fun resetRunnable(oddsButton: OddsButtonHome, itemOdd: Odd) {
        itemOdd.runnable?.let {
            mHandler.removeCallbacks(it)
        }
        if (itemOdd.oddState == OddState.SAME.state) return
        val runnable = highLightRunnable(oddsButton, itemOdd)
        itemOdd.runnable = runnable
        mHandler.postDelayed(runnable, HIGH_LIGHT_TIME)
    }

    open fun onLifeDestroy() {
        if (hasHandler) {
            mHandler.removeCallbacksAndMessages(null)
        }
    }
}