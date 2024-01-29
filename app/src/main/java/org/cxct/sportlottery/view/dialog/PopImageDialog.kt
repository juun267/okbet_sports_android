package org.cxct.sportlottery.view.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.ViewPager
import com.stx.xhb.androidx.transformers.Transformer
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.DialogPopImageBinding
import org.cxct.sportlottery.network.index.config.ImageData
import org.cxct.sportlottery.repository.ImageType
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.ui.common.bean.XBannerImage
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.LogUtil

/**
 * 顯示棋牌彈窗
 */
class PopImageDialog() : BaseDialog<BaseViewModel>(BaseViewModel::class) {

    init {
        setStyle(R.style.FullScreen)
    }

    companion object {
        private const val IMAGE_TYPE = "imageType"
        // DIALOG_HOME(7),
//        DIALOG_SPORT(14),
//        DIALOG_OKGAME(16),
//        DIALOG_OKLIVE(25),
//        DIALOG_OKGAMES_HOME(23),//OKGames包，默认进入棋牌页时候的活动弹窗
        private var imageTypeEnableMap = mutableMapOf<Int, Boolean>(
            ImageType.DIALOG_HOME.code to true,
            ImageType.DIALOG_SPORT.code to true,
            ImageType.DIALOG_OKGAME.code to true,
            ImageType.DIALOG_OKLIVE.code to true,
            ImageType.DIALOG_OKGAMES_HOME.code to true
        )
        fun resetImageType(){
            imageTypeEnableMap.keys.forEach {
                imageTypeEnableMap[it] = true
            }
        }
        fun checkImageTypeEnable(imageType: Int): Boolean{
            return imageTypeEnableMap[imageType]?:false
        }

        private fun checkImageTypeAvailable(imageType: Int) = sConfigData?.imageList?.filter {
            it.imageType == imageType && it.lang == LanguageManager.getSelectLanguage(
                MultiLanguagesApplication.appContext).key && !it.imageName1.isNullOrEmpty()
        }?.isNotEmpty() == true

        fun showDialog(manager: FragmentManager, imageType: Int) {
            if (checkImageTypeAvailable(imageType) && imageTypeEnableMap[imageType] == true) {
                val dialog = PopImageDialog()
                val bundle = Bundle()
                bundle.putInt(IMAGE_TYPE, imageType)
                dialog.arguments = bundle
                dialog.show(manager)
                imageTypeEnableMap[imageType] = false
            }
        }
    }

    private lateinit var binding: DialogPopImageBinding
    private val imageType by lazy { arguments?.getInt(IMAGE_TYPE) }
    private lateinit var imageList: List<ImageData>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = DialogPopImageBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClick()
        setUpBanner()
    }


    private fun initClick() = binding.run {
        tvArrowLeft.text = "<"
        tvArrowLeft.setOnClickListener {
            if (xbanner.realCount == 1) {
                return@setOnClickListener
            }
            xbanner.setBannerCurrentItem(if (xbanner.bannerCurrentItem == 0) xbanner.realCount - 1 else xbanner.bannerCurrentItem - 1,
                true)
        }
        tvArrowRight.text = ">"
        tvArrowRight.setOnClickListener {
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
        val lang = LanguageManager.getSelectLanguage(context).key
        imageList = sConfigData?.imageList?.filter {
            it.imageType == imageType && it.lang == lang && !it.imageName1.isNullOrEmpty()
        }
            ?.sortedWith(compareByDescending<ImageData> { it.imageSort }.thenByDescending { it.createdAt })
            ?: listOf()
        linIndicator.isVisible = imageList.size > 1
        if (imageList.isNullOrEmpty()) {
            return
        }
        val loopEnable = imageList.size > 1
        //sid 要求取消自动循环
        xbanner.setHandLoop(loopEnable)
        xbanner.setAutoPlayAble(false)
        //使用xbanner的Depth 动画时，点击第一个banner，会触发第二个banner的点击，故此使用自定义的
        xbanner.setPageTransformer(Transformer.Depth)
        //因为onItemClick 返回的位置不对
//        xbanner.setOnItemClickListener(this@PopImageDialog)
        xbanner.loadImage { _, model, view, position ->
            (view as ImageView).load((model as XBannerImage).imgUrl, R.drawable.img_banner01)
            view.setOnClickListener {
                val realImageData = imageList.getOrNull(xbanner.bannerCurrentItem)
                LogUtil.d("bannerCurrentItem=" + xbanner.bannerCurrentItem + ",jumpUrl=" + realImageData?.appUrl)
                realImageData?.let {
                    if (!it.appUrl.isNullOrEmpty()) {
                        JumpUtil.toInternalWeb(requireActivity(), it.appUrl, it.imageText1)
                        dismissAllowingStateLoss()
                    }
                }
            }
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

        val host = sConfigData?.resServerHost
        val images = imageList.map {
            XBannerImage(it.imageText1 + "", host + it.imageName1, it.appUrl)
        }.toMutableList()
        LogUtil.toJson(images)
        if (imageType == ImageType.DIALOG_HOME.code && images.isNotEmpty()) {
            xbanner.visible()
        }
        xbanner.setBannerData(images)
        updateIndicate()
    }

    private fun updateIndicate() {
        binding.tvIndicator.text =
            "${binding.xbanner.bannerCurrentItem + 1}/${imageList?.size ?: 0}"
    }
}