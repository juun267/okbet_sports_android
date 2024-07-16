package org.cxct.sportlottery.ui.maintab.home.ambassador

import android.os.Bundle
import android.widget.ImageView
import androidx.core.view.isInvisible
import androidx.viewpager.widget.ViewPager
import com.stx.xhb.androidx.entity.BaseBannerInfo
import com.stx.xhb.androidx.transformers.Transformer
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.databinding.DialogAmbassadorImageBinding
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel


class AmbassadorImageDialog : BaseDialog<BaseViewModel,DialogAmbassadorImageBinding>() {

    init {
        setStyle(R.style.FullScreen)
    }

    companion object {
        const val IMAGE_IDS = "image_ids"
        const val POSITION = "position"

        fun newInstance(imageIds: ArrayList<Int>, position: Int)= AmbassadorImageDialog().apply{
            arguments = Bundle().apply {
                putIntegerArrayList(IMAGE_IDS,imageIds)
                putInt(POSITION,position)
            }
        }
    }

    private val imageIds by lazy { arguments?.getIntegerArrayList(IMAGE_IDS)?: arrayListOf() }
    private val initPosition by lazy { arguments?.getInt(POSITION,0)?:0}

    override fun onInitView() {
        initClick()
        setUpBanner()
    }

    private fun initClick() = binding.run {
        ivArrowLeft.setOnClickListener {
            if (xbanner.realCount == 1) {
                return@setOnClickListener
            }
            xbanner.setBannerCurrentItem(if (xbanner.bannerCurrentItem == 0) xbanner.realCount - 1 else xbanner.bannerCurrentItem - 1,
                true)
        }
        ivArrowRight.setOnClickListener {
            if (xbanner.realCount == 1) {
                return@setOnClickListener
            }
            xbanner.setBannerCurrentItem(if (xbanner.bannerCurrentItem == xbanner.realCount - 1) 0 else xbanner.bannerCurrentItem + 1,
                true)
        }
        btnClose.setOnClickListener {
            dismiss()
        }
    }

    private fun setUpBanner() = binding.run {
        //sid 要求取消自动循环
        xbanner.setHandLoop(false)
        xbanner.setAutoPlayAble(false)
        //使用xbanner的Depth 动画时，点击第一个banner，会触发第二个banner的点击，故此使用自定义的
        xbanner.setPageTransformer(Transformer.Depth)
        //因为onItemClick 返回的位置不对
//        xbanner.setOnItemClickListener(this@PopImageDialog)
        xbanner.loadImage { _, model, view, position ->
            (view as ImageView).load((model as ImageItem).resId)
        }
        xbanner.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int,
            ) {
            }

            override fun onPageSelected(position: Int) {
                updateIndicate()
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })
        val imageItems = imageIds.map { ImageItem(it) }
        xbanner.setBannerData(imageItems)
        updateIndicate()
        xbanner.bannerCurrentItem = initPosition
    }

    private fun updateIndicate() {
        val currentPosition = binding.xbanner.bannerCurrentItem
        binding.tvIndicator.text = "${currentPosition + 1}/${imageIds.size }"
        binding.ivArrowLeft.isInvisible = currentPosition == 0
        binding.ivArrowRight.isInvisible = currentPosition == (imageIds.size-1)
    }
    data class ImageItem(val resId: Int): BaseBannerInfo{
        override fun getXBannerUrl() = null
        override fun getXBannerTitle()= null
    }
}