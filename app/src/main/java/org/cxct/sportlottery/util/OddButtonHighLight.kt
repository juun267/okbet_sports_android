package org.cxct.sportlottery.util

import android.os.Handler
import android.widget.TextView
import androidx.core.content.ContextCompat
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.detail.Odd
import org.cxct.sportlottery.network.odds.list.OddState
import org.cxct.sportlottery.ui.game.outright.CHANGING_ITEM_BG_COLOR_DURATION

object OddButtonHighLight {

    fun set(textView: TextView, odd: Odd) {

        when (odd.oddState) {
            OddState.LARGER.state -> {
                textView.background = ContextCompat.getDrawable(textView.context, R.drawable.bg_radius_4_button_unselected_green)
                textView.setTextColor(ContextCompat.getColor(textView.context, R.color.colorWhite))
            }
            OddState.SMALLER.state -> {
                textView.background = ContextCompat.getDrawable(textView.context, R.drawable.bg_radius_4_button_unselected_red)
                textView.setTextColor(ContextCompat.getColor(textView.context, R.color.colorWhite))
            }
        }
        Handler().postDelayed(
            {
                when (odd.isSelect) {
                    true -> {
                        textView.background = ContextCompat.getDrawable(textView.context, R.drawable.bg_radius_4_button_orangelight)
                        textView.setTextColor(ContextCompat.getColor(textView.context, R.color.colorWhite))
                    }
                    false -> {
                        textView.background = ContextCompat.getDrawable(textView.context, R.drawable.bg_radius_4_button_white_white6)
                        textView.setTextColor(ContextCompat.getColor(textView.context, R.color.colorBlack))
                    }
                }
            }, CHANGING_ITEM_BG_COLOR_DURATION
        )
    }
}