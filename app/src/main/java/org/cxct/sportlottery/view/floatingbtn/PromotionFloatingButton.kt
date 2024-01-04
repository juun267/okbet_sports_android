package org.cxct.sportlottery.view.floatingbtn

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.motion.widget.MotionLayout
import org.cxct.sportlottery.databinding.ViewFloatingPromotionBinding
import org.cxct.sportlottery.util.SvgUtil.setAssetSvgIcon
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

    open fun startAnim(){
        binding.icon.setAssetSvgIcon("svga/ic_giftbox.svga",true)
    }
}