package org.cxct.sportlottery.view.floatingbtn

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.motion.widget.MotionLayout
import kotlinx.android.synthetic.main.motion_lottery_floating.view.*
import org.cxct.sportlottery.databinding.MotionViewLuckyWheelFloatingBinding
import org.cxct.sportlottery.util.LotteryManager
import org.cxct.sportlottery.util.LuckyWheelManager

class LuckyWheelFloatingButton @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) :
    MotionLayout(context, attributeSet, defStyle) {
    private val binding: MotionViewLuckyWheelFloatingBinding

    init {
        binding = MotionViewLuckyWheelFloatingBinding.inflate(LayoutInflater.from(context),this,false)
        addView(binding.root)
        initClickEvent()
    }

    private fun initClickEvent() {
        binding.movableLayout.setOnClickListener {
            LuckyWheelManager.instance.clickContent()
        }
        binding.ivClose.setOnClickListener {
            LuckyWheelManager.instance.clickCloseFloatBtn()
        }
    }
}