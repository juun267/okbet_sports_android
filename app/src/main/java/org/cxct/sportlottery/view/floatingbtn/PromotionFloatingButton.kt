package org.cxct.sportlottery.view.floatingbtn

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.motion.widget.MotionLayout
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ViewFloatingPromotionBinding
import org.cxct.sportlottery.util.DisplayUtil.dp
import splitties.systemservices.layoutInflater

class PromotionFloatingButton @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : MotionLayout(context, attributeSet, defStyle) {

    private val binding = ViewFloatingPromotionBinding.inflate(layoutInflater,this,true)

    init {
        binding.movableLayout.setOnClickListener { PromotionManager.instance.clickContent() }
        binding.ivClose.setOnClickListener { PromotionManager.instance.clickCloseFloatBtn() }
    }

}