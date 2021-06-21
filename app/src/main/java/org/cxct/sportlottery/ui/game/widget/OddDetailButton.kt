package org.cxct.sportlottery.ui.game.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.button_odd_detail.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.detail.Odd
import org.cxct.sportlottery.network.odds.list.BetStatus
import org.cxct.sportlottery.network.odds.list.OddState
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.getOdds

/**
 * @author Kevin
 * @create 2021/06/21
 */
class OddDetailButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {


    interface OnOddStatusChangedListener {
        fun onOddStateChangedFinish()
    }


    var betStatus: Int? = null
        set(value) {
            field = value
            field?.let {
                setupBetStatus(it)
            }
        }


    var oddStatus: Int? = null
        set(value) {
            field = value
            field?.let {
                setupOddState(it)
            }
        }


    var onOddStatusChangedListener: OnOddStatusChangedListener? = null


    init {
        init()
    }


    private fun init() {
        inflate(context, R.layout.button_odd_detail, this)
    }


    fun setupOdd(odd: Odd, oddsType: OddsType) {
        tv_name.text = odd.name
        tv_spread.text = odd.spread
        tv_odds?.text = TextUtil.formatForOdd(getOdds(odd, oddsType))
    }


    private fun setupBetStatus(betStatus: Int) {
        img_odd_lock.visibility =
            if (betStatus == BetStatus.LOCKED.code || betStatus == BetStatus.DEACTIVATED.code) {
                View.VISIBLE
            } else {
                View.GONE
            }

        isEnabled = (betStatus == BetStatus.ACTIVATED.code)
    }


    private fun setupOddState(oddState: Int) {
        if (!isEnabled) return

        when (oddState) {
            OddState.LARGER.state -> {
                button_odd_detail.background =
                    ContextCompat.getDrawable(context, R.drawable.bg_radius_4_button_unselected_green)

                isActivated = true
            }
            OddState.SMALLER.state -> {
                button_odd_detail.background =
                    ContextCompat.getDrawable(context, R.drawable.bg_radius_4_button_unselected_red)

                isActivated = true
            }
            else -> {
                button_odd_detail.background =
                    ContextCompat.getDrawable(context, R.drawable.selector_button_radius_4_odds)

                isActivated = false
            }
        }
    }


}