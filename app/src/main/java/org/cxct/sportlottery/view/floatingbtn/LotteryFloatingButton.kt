package org.cxct.sportlottery.view.floatingbtn

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.motion.widget.MotionLayout
import kotlinx.android.synthetic.main.motion_lottery_floating.view.*
import kotlinx.android.synthetic.main.motion_view_red_envelope_floating.view.iv_close
import org.cxct.sportlottery.R
import org.cxct.sportlottery.util.LotteryManager

class LotteryFloatingButton @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0,
) :
    MotionLayout(context, attributeSet, defStyle) {


    init {
        addView(
            LayoutInflater.from(context)
                .inflate(R.layout.motion_lottery_floating, this, false)
        )
        initClickEvent()
    }

    private fun initClickEvent() {
        movable_layout.setOnClickListener {
            LotteryManager.instance.clickOpenFloatBtn()
        }
        iv_close.setOnClickListener {
            LotteryManager.instance.clickCloseFloatBtn()
        }
    }

    fun setTime(name: String, startdate: String) {
        tv_name.text = name
        tv_time.text = startdate
    }
}