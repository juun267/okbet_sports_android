package org.cxct.sportlottery.ui.game.widget

import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.content_eps_odd_button.view.*
import org.cxct.sportlottery.R


class EpsOddButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    init {
        initView()
    }

    private fun initView() {
        try {
            inflate(context, R.layout.content_eps_odd_button, this).apply {
                txv_extInfo.text = ""
                txv_odds.text = ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setOddsValue(extInfo: String, odds: String) {
        txv_extInfo.text = extInfo
        txv_odds.text = odds
    }

    fun setFlag(){
        txv_extInfo.paint.flags = Paint. STRIKE_THRU_TEXT_FLAG
    }

}