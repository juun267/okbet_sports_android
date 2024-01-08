package org.cxct.sportlottery.view.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.viewpager.widget.ViewPager
import com.stx.xhb.androidx.XBanner
import com.stx.xhb.androidx.transformers.Transformer
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.DialogPopImageBinding
import org.cxct.sportlottery.network.index.config.ImageData
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.ImageType
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.ui.common.bean.XBannerImage
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.LanguageManager

/**
 * 顯示棋牌彈窗
 */
class PopImageDialog : BaseDialog<BaseViewModel>(BaseViewModel::class), XBanner.OnItemClickListener {

    init {
        setStyle(R.style.FullScreen)
    }

    companion object {
        const val IMAGE_TYPE = "imageType"
        var showHomeDialog = true
        var showOKGameDialog = true
        var showOKLiveDialog = true
        var showSportDialog = true
        fun checkImageTypeAvailable(imageType: Int) = sConfigData?.imageList?.filter {
            it.imageType == imageType && it.lang == LanguageManager.getSelectLanguage(
                MultiLanguagesApplication.appContext).key && !it.imageName1.isNullOrEmpty()
        }?.isNotEmpty() == true
    }
    lateinit var binding : DialogPopImageBinding
    var onDismiss: ((clickDismiss: Boolean) -> Unit)? = null
    val imageType by lazy { arguments?.getInt(IMAGE_TYPE) }
    lateinit var imageList : List<ImageData>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding=DialogPopImageBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClick()
        setUpBanner()
    }


    private fun initClick()=binding.run {
        tvArrowLeft.text = "<"
        tvArrowLeft.setOnClickListener {
            if (xbanner.realCount==1){
                return@setOnClickListener
            }
            xbanner.setBannerCurrentItem(if(xbanner.bannerCurrentItem==0) xbanner.realCount-1 else xbanner.bannerCurrentItem-1,true)
        }
        tvArrowRight.text = ">"
        tvArrowRight.setOnClickListener {
            if (xbanner.realCount==1){
                return@setOnClickListener
            }
            xbanner.setBannerCurrentItem(if(xbanner.bannerCurrentItem==xbanner.realCount-1) 0 else xbanner.bannerCurrentItem+1,true)
        }
        btnClose.setOnClickListener {
            dismiss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismiss?.invoke(false)
    }

    private fun setUpBanner() = binding.run {
        val lang = LanguageManager.getSelectLanguage(context).key
         imageList = sConfigData?.imageList?.filter {
            it.imageType == imageType && it.lang == lang && !it.imageName1.isNullOrEmpty()
        }
            ?.sortedWith(compareByDescending<ImageData> { it.imageSort }.thenByDescending { it.createdAt })?: listOf()
        linIndicator.isVisible = imageList.size>1
        if (imageList.isNullOrEmpty()) {
            return
        }
        val loopEnable = (imageList?.size ?: 0) > 1
         //sid 要求取消自动循环
        xbanner.setHandLoop(loopEnable)
        xbanner.setAutoPlayAble(false)
        xbanner.setPageTransformer(Transformer.Depth)
        //使用xbanner的Depth 动画时，点击第一个banner，会触发第二个banner的点击，故此使用自定义的
        xbanner.setCustomPageTransformer(org.cxct.sportlottery.util.DepthPageTransformer())
        xbanner.setOnItemClickListener(this@PopImageDialog)
        xbanner.loadImage { _, model, view, _ ->
            (view as ImageView).load((model as XBannerImage).imgUrl, R.drawable.img_banner01)
        }
        xbanner.setOnPageChangeListener(object :ViewPager.OnPageChangeListener{
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
        if (imageType == ImageType.DIALOG_HOME.code && images.isNotEmpty()) {
            xbanner.visible()
        }
        xbanner.setBannerData(images)
        updateIndicate()
    }

    override fun onItemClick(banner: XBanner?, model: Any?, view: View?, position: Int) {
        val jumpUrl = (model as XBannerImage).jumpUrl
        if (!jumpUrl.isNullOrEmpty()) {
            JumpUtil.toInternalWeb(requireActivity(), jumpUrl, model.title)
            dismissAllowingStateLoss()
        }
        onDismiss?.invoke(true)
    }
    private fun updateIndicate(){
        binding.tvIndicator.text = "${binding.xbanner.bannerCurrentItem+1}/${imageList?.size?:0}"
    }
}