package org.cxct.sportlottery.view.dialog.promotion

import android.app.AlertDialog
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import com.youth.banner.indicator.CircleIndicator
import org.cxct.sportlottery.databinding.DialogPromotionPopupBinding
import org.cxct.sportlottery.repository.ImageType
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.ScreenUtil
import org.cxct.sportlottery.util.isGooglePlayVersion

class PromotionPopupDialog(val activity: FragmentActivity, private val promotionPopupListener: PromotionPopupListener) :
    AlertDialog(activity) {
    private var _binding: DialogPromotionPopupBinding? = null
    private val binding get() = _binding!!

    open class PromotionPopupListener(private val onClickImageListener: () -> Unit) {
        fun onClickImageListener() = onClickImageListener.invoke()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        _binding = DialogPromotionPopupBinding.inflate(activity.layoutInflater)
        setContentView(binding.root)
        window?.setLayout(
            ScreenUtil.getScreenWidth(context) - 40.dp,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window?.setGravity(Gravity.CENTER)
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        initView()
    }

    private fun initView() {
        binding.ivClose.setOnClickListener { dismiss() }
        val promotionList = mutableListOf<PromotionData>()
        sConfigData?.imageList?.map { imageData ->
            //最多顯示9筆
            if (promotionList.size < 9 && imageData.imageType == ImageType.PROMOTION.code && !imageData.imageName3.isNullOrEmpty() && !(isGooglePlayVersion() && imageData.isHidden)) {
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
                promotionPopupListener.onClickImageListener()
                dismiss()
            }
        }
    }

}