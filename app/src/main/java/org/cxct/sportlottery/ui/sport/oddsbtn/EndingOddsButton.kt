package org.cxct.sportlottery.ui.sport.oddsbtn

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.button_odd_ending.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.enums.BetStatus
import org.cxct.sportlottery.common.enums.OddState
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.util.*


class EndingOddsButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

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

    private var mOdd: Odd? = null

    private var mOddsType: OddsType = OddsType.EU

    init {
        setBackgroundResource(R.drawable.selector_button_radius_4_odds)
        inflate(context, R.layout.button_odd_ending, this)
    }

    fun setupOdd(odd: Odd?, oddsType: OddsType) {
        mOdd = odd
        mOddsType = oddsType
        tv_odds?.text = odd?.name

        val select = odd?.id?.let { QuickListManager.containOdd(it) } ?: false
        isSelected = select
        odd?.isSelected = select
        //[Martin]馬來盤＆印尼盤會有負數的賠率
        //betStatus = if (getOdds(odd, oddsType) <= 0.0 || odd == null) BetStatus.LOCKED.code else odd.status
        betStatus = if (odd == null) BetStatus.LOCKED.code else odd.status

    }

    //常駐顯示按鈕 依狀態隱藏鎖頭
    private fun setupBetStatus(betStatus: Int) {
        img_odd_lock.apply {
            visibility =
                if (betStatus == BetStatus.LOCKED.code) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }

        img_odd_unknown.apply {
            visibility =
                if (betStatus == BetStatus.DEACTIVATED.code) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }

        isEnabled = (betStatus == BetStatus.ACTIVATED.code)
    }

    private fun setupOddState(oddState: Int) {
        if (!isEnabled) return
        when (oddState) {
            OddState.LARGER.state -> {
                tv_odds.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.color_1EB65B
                    )
                )
                isActivated = false
            }
            OddState.SMALLER.state -> {
                tv_odds.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.color_E23434
                    )
                )
                isActivated = false
            }
            OddState.SAME.state -> {
                resetOddsValueState()
                isActivated = false
            }
        }

    }

    private fun resetOddsValueState() {
        tv_odds.setTextColor(
            ContextCompat.getColorStateList(
                context,
                if (MultiLanguagesApplication.isNightMode) R.color.selector_button_odd_bottom_text_dark
                else R.color.selector_button_odd_bottom_text
            )
        )
    }

}