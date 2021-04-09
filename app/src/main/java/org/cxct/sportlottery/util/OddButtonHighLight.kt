package org.cxct.sportlottery.util

import android.os.Handler
import android.widget.TextView
import androidx.core.content.ContextCompat
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.detail.Odd
import org.cxct.sportlottery.network.odds.list.OddState

const val CHANGING_ITEM_BG_COLOR_DURATION: Long = 3000

object OddButtonHighLight {

    fun set(tvName: TextView?, tvOdds: TextView, tvSpread: TextView?, odd: Odd) {

        when (odd.oddState) {
            OddState.LARGER.state -> {
                tvOdds.background = ContextCompat.getDrawable(tvOdds.context, R.drawable.bg_radius_4_button_unselected_green)
                tvOdds.setTextColor(ContextCompat.getColor(tvOdds.context, R.color.colorWhite))
                tvSpread?.setTextColor(ContextCompat.getColor(tvSpread.context, R.color.colorWhite))
                tvName?.setTextColor(ContextCompat.getColor(tvName.context, R.color.colorWhite))
            }
            OddState.SMALLER.state -> {
                tvOdds.background = ContextCompat.getDrawable(tvOdds.context, R.drawable.bg_radius_4_button_unselected_red)
                tvOdds.setTextColor(ContextCompat.getColor(tvOdds.context, R.color.colorWhite))
                tvSpread?.setTextColor(ContextCompat.getColor(tvSpread.context, R.color.colorWhite))
                tvName?.setTextColor(ContextCompat.getColor(tvName.context, R.color.colorWhite))
            }

        }
        Handler().postDelayed(
            {
                when (odd.isSelect) {
                    true -> {
                        tvOdds.background = ContextCompat.getDrawable(tvOdds.context, R.drawable.bg_radius_4_button_orangelight)
                        tvOdds.setTextColor(ContextCompat.getColor(tvOdds.context, R.color.colorWhite))
                        tvSpread?.setTextColor(ContextCompat.getColor(tvSpread.context, R.color.colorWhite))
                        tvName?.setTextColor(ContextCompat.getColor(tvName.context, R.color.colorWhite))
                    }
                    false -> {
                        tvOdds.background = ContextCompat.getDrawable(tvOdds.context, R.drawable.bg_radius_4_button_white_white6)
                        tvOdds.setTextColor(ContextCompat.getColor(tvOdds.context, R.color.colorBlackLight))
                        tvSpread?.setTextColor(ContextCompat.getColor(tvSpread.context, R.color.colorRedDark))
                        tvName?.setTextColor(ContextCompat.getColor(tvName.context, R.color.colorBlackLight))
                    }
                }
            }, CHANGING_ITEM_BG_COLOR_DURATION
        )
    }
}