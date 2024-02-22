package org.cxct.sportlottery.ui.sport.oddsbtn

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.enums.BetStatus
import org.cxct.sportlottery.common.enums.OddState
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.databinding.ButtonOddEndingBinding
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.util.*
import splitties.systemservices.layoutInflater


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

    private val binding by lazy { ButtonOddEndingBinding.inflate(layoutInflater,this) }

    init {
        foreground = ContextCompat.getDrawable(context, R.drawable.fg_ripple)
        binding.root.setBackgroundResource(R.drawable.selector_button_radius_6_odds_trans)
    }

    fun setupOdd(odd: Odd?, oddsType: OddsType) {
        mOdd = odd
        mOddsType = oddsType
        binding.tvOdds?.text = odd?.name

        val select = odd?.id?.let { QuickListManager.containOdd(it) } ?: false
        isSelected = select
        odd?.isSelected = select
        //[Martin]馬來盤＆印尼盤會有負數的賠率
        //betStatus = if (getOdds(odd, oddsType) <= 0.0 || odd == null) BetStatus.LOCKED.code else odd.status
        betStatus = if (odd == null) BetStatus.LOCKED.code else odd.status
        binding.tvSpread.text = TextUtil.formatForOdd(odd?.odds?:0)
    }

    //常駐顯示按鈕 依狀態隱藏鎖頭
    private fun setupBetStatus(betStatus: Int) {
        
        when(betStatus) {
            BetStatus.ACTIVATED.code -> {
                bindStatu(true, true, false, false)
            }

            BetStatus.LOCKED.code -> {
                bindStatu(false, false, true, false)
            }

            else -> {
                bindStatu(false, false, false, true)
            }
        }

    }

    private fun bindStatu(enable: Boolean, isActivated: Boolean, isLocked: Boolean, isDeactivated: Boolean) {
        isEnabled = enable
        binding.tvOdds.isVisible = isActivated
        binding.tvSpread.isVisible = isActivated
        binding.imgOddLock.isVisible = isLocked
        binding.imgOddUnknown.isVisible = isDeactivated
    }

    private fun setupOddState(oddState: Int) {
        if (!isEnabled) return
        when (oddState) {
            OddState.LARGER.state -> {
                binding.tvOdds.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.color_1EB65B
                    )
                )
                isActivated = false
            }
            OddState.SMALLER.state -> {
                binding.tvOdds.setTextColor(
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
        binding.tvOdds.setTextColor(
            ContextCompat.getColorStateList(
                context,
                if (MultiLanguagesApplication.isNightMode) R.color.selector_button_odd_bottom_text_dark
                else R.color.selector_button_odd_bottom_text
            )
        )
    }

}