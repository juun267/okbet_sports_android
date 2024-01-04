package org.cxct.sportlottery.view.floatingbtn

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.motion.widget.MotionLayout
import org.cxct.sportlottery.databinding.MotionLotteryFloatingBinding
import splitties.systemservices.layoutInflater

class LotteryFloatingButton @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0,
) :
    MotionLayout(context, attributeSet, defStyle) {

    private val binding = MotionLotteryFloatingBinding.inflate(layoutInflater,this,true)
    init {
        initClickEvent()
    }

    private fun initClickEvent() {
        binding.movableLayout.setOnClickListener {
            LotteryManager.instance.clickOpenFloatBtn()
        }
        binding.ivClose.setOnClickListener {
            LotteryManager.instance.clickCloseFloatBtn()
        }
    }

    fun setTime(name: String, startdate: String) {
        binding.tvName.text = name
        binding.tvTime.text = startdate
    }
}