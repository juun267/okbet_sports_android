package org.cxct.sportlottery.view.dialog.promotion

import androidx.constraintlayout.widget.ConstraintLayout
import com.youth.banner.indicator.CircleIndicator
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogPromotionPopupBinding
import org.cxct.sportlottery.network.index.config.ImageData
import org.cxct.sportlottery.repository.ImageType
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.getMarketSwitch

class PromotionPopupDialog(private val promotionPopupListener: () -> Unit)
    : BaseDialog<BaseViewModel,DialogPromotionPopupBinding>() {

    init {
        setStyle(R.style.FullScreen)
    }
    companion object{
        fun needShow():Boolean{
            if (sConfigData?.imageList?.any { it.imageType == ImageType.DIALOG_PROMOTION.code && !it.imageName3.isNullOrEmpty() && (!getMarketSwitch() && !it.isHidden) } != true) {
                return false
            }
            return true
        }
    }

    override fun onInitView() {
        initView()
    }

    private fun initView() {
        binding.ivClose.setOnClickListener { dismiss() }
        val promotionList = mutableListOf<PromotionData>()
        sConfigData?.imageList?.sortedWith(compareByDescending<ImageData> { it.imageSort }.thenByDescending { it.createdAt })?.map { imageData ->
            //最多顯示9筆
            if (promotionList.size < 9 && imageData.imageType == ImageType.DIALOG_PROMOTION.code && !imageData.imageName3.isNullOrEmpty() && (!getMarketSwitch() && !imageData.isHidden)) {
                promotionList.add(
                    PromotionData(
                        imgUrl = "${sConfigData?.resServerHost}${imageData.imageName3}",
                        title = imageData.imageText1
                    )
                )
            }
        }

        with(binding.banner) {
            addBannerLifecycleObserver(activity)
                .setAdapter(PromotionAdapter(promotionList))

            val bannerLayoutParams = binding.banner.layoutParams as ConstraintLayout.LayoutParams

            //若只有一項仍然配置indicator會造成banner拉伸
            if (promotionList.size > 1) {
                indicator = CircleIndicator(context)

                //指示器與底部的距離
                bannerLayoutParams.setMargins(0, 0, 0, 12.dp)
            } else {
                //指示器不顯示 底部距離移除
                bannerLayoutParams.setMargins(0, 0, 0, 0)
            }

            setOnBannerListener { _, _ -> //data, position
                promotionPopupListener.invoke()
                dismiss()
            }
        }
    }

}