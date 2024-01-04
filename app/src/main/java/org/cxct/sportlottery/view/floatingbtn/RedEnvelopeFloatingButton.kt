package org.cxct.sportlottery.view.floatingbtn

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.motion.widget.MotionLayout
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ViewFloatingRedenvelopeBinding
import org.cxct.sportlottery.util.TimeUtil
import splitties.systemservices.layoutInflater

class RedEnvelopeFloatingButton @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) :
    MotionLayout(context, attributeSet, defStyle) {

    private val binding = ViewFloatingRedenvelopeBinding.inflate(layoutInflater,this,true)

    init {
        initClickEvent()
    }

    private fun initClickEvent() {
        binding.ivClose.setOnClickListener {
            RedEnvelopeManager.instance.clickCloseFloatBtn()
        }
    }
    fun setCountdown(countdown: Long) {
        binding.tvCountdown.text = TimeUtil.timeFormat(countdown * 1000, TimeUtil.HM_FORMAT_MS)
    }
}