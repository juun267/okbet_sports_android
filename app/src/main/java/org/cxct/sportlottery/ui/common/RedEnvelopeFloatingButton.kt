package org.cxct.sportlottery.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.motion.widget.MotionLayout
import kotlinx.android.synthetic.main.motion_view_red_envelope_floating.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.util.RedEnvelopeManager

class RedEnvelopeFloatingButton @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) :
    MotionLayout(context, attributeSet, defStyle) {


    init {
        addView(
            LayoutInflater.from(context)
                .inflate(R.layout.motion_view_red_envelope_floating, this, false)
        )
        initClickEvent()
    }

    private fun initClickEvent() {
        iv_close.setOnClickListener {
            RedEnvelopeManager.instance.clickCloseFloatBtn()
        }
    }
    fun setCountdown(countdown: Long) {
        tv_countdown.text = countdown.toString()
    }
}