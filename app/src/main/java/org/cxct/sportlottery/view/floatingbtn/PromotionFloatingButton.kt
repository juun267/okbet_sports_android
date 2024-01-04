package org.cxct.sportlottery.view.floatingbtn

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ViewFloatingPromotionBinding
import org.cxct.sportlottery.util.SvgUtil.setAssetSvgIcon
import splitties.systemservices.layoutInflater

class PromotionFloatingButton @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attributeSet, defStyle) {

    private val binding = ViewFloatingPromotionBinding.inflate(layoutInflater,this,true)

    init {
        binding.root.setOnClickListener { PromotionManager.instance.clickContent() }
        binding.ivClose.setOnClickListener { PromotionManager.instance.clickCloseFloatBtn() }
        binding.root.setOnTouchListener(SuckEdgeTouch())
    }

    open fun startAnim(){
        binding.imageView.setAssetSvgIcon("svga/ic_giftbox.svga",true)
    }
}