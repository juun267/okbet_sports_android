package org.cxct.sportlottery.ui.game.widget


import android.content.Context
import android.graphics.Paint
import android.graphics.drawable.Drawable
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
 * @description 賠率按鈕(預設圓角)
 * @edit:
 * 2021/07/05 擴展配適直角
 */
class OddsButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {


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


    var mFillet = true


    var mBackground: Drawable? = null


    init {
        init(attrs)
    }


    private fun init(attrs: AttributeSet?) {

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.OddsButton)
        mFillet = typedArray.getBoolean(R.styleable.OddsButton_ob_fillet, true)
        mBackground =
            typedArray.getDrawable(R.styleable.OddsButton_ob_background)
                ?: context.theme.getDrawable(R.drawable.selector_button_radius_4_odds)
        try {
            inflate(context, R.layout.button_odd_detail, this).apply {
                button_odd_detail.background = mBackground
            }
        } catch (e: Exception) {
            typedArray.recycle()
        }

    }


    fun setupOdd(odd: Odd?, oddsType: OddsType) {
        tv_name.text = odd?.name
        tv_spread.text = odd?.spread
        tv_odds?.text = TextUtil.formatForOdd(getOdds(odd, oddsType))
        betStatus = odd?.status ?: BetStatus.LOCKED.code
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
                    ContextCompat.getDrawable(
                        context,
                        if (mFillet) R.drawable.bg_radius_4_button_unselected_green
                        else R.drawable.bg_radius_0_button_green
                    )

                isActivated = true
            }
            OddState.SMALLER.state -> {
                button_odd_detail.background =
                    ContextCompat.getDrawable(
                        context,
                        if (mFillet) R.drawable.bg_radius_4_button_unselected_red
                        else R.drawable.bg_radius_0_button_red
                    )

                isActivated = true
            }
            else -> {
                button_odd_detail.background =
                    ContextCompat.getDrawable(
                        context,
                        if (mFillet) R.drawable.selector_button_radius_4_odds
                        else R.drawable.selector_button_radius_0_odds
                    )

                isActivated = false
            }
        }
    }


    /*設置中間線*/
    fun setupSpreadCenterLine() {
        tv_spread?.paint?.flags = Paint.STRIKE_THRU_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG
    }


    /*設置賠率顏色*/
    fun setupOddsTextColor(color: Int) {
        tv_odds?.setTextColor(ContextCompat.getColor(context, color))
    }

}