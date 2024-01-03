package org.cxct.sportlottery.view.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.stx.xhb.androidx.XBanner
import kotlinx.android.synthetic.main.dialog_pop_image.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.index.config.ImageData
import org.cxct.sportlottery.repository.ImageType
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.ui.common.bean.XBannerImage
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.LanguageManager

/**
 * 顯示棋牌彈窗
 */
class PopImageDialog :
    BaseDialog<BaseViewModel>(BaseViewModel::class), XBanner.OnItemClickListener {

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

    var onDismiss: (() -> Unit)? = null
    val imageType by lazy { arguments?.getInt(IMAGE_TYPE) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.dialog_pop_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClose()
        setUpBanner()
        setLayoutParams()
    }

    private fun setLayoutParams() {
        if (imageType == ImageType.DIALOG_HOME.code) {
            if (xbanner.layoutParams is ConstraintLayout.LayoutParams) {
                val lp = (xbanner.layoutParams as ConstraintLayout.LayoutParams)
                lp.dimensionRatio = "1:1"
                xbanner.layoutParams = lp
            }
        }
    }

    private fun setupClose() {
        btn_close.setOnClickListener {
            dismiss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismiss?.invoke()
    }

    private fun setUpBanner() {
        val lang = LanguageManager.getSelectLanguage(context).key
        val imageList = sConfigData?.imageList?.filter {
            it.imageType == imageType && it.lang == lang && !it.imageName1.isNullOrEmpty()
        }
            ?.sortedWith(compareByDescending<ImageData> { it.imageSort }.thenByDescending { it.createdAt })

        val loopEnable = (imageList?.size ?: 0) > 1
        if (imageList.isNullOrEmpty()) {
            return
        }

        xbanner.setHandLoop(loopEnable)
        xbanner.setAutoPlayAble(loopEnable)
        xbanner.setOnItemClickListener(this@PopImageDialog)
        xbanner.loadImage { _, model, view, _ ->
            (view as ImageView).load((model as XBannerImage).imgUrl, R.drawable.img_banner01)
        }

        val host = sConfigData?.resServerHost
        val images = imageList.map {
            XBannerImage(it.imageText1 + "", host + it.imageName1, it.appUrl)
        }

        if (imageType == 7 && images.isNotEmpty()) {
            xbanner.visible()
        }
        xbanner.setBannerData(images.toMutableList())
    }

    override fun onItemClick(banner: XBanner?, model: Any?, view: View?, position: Int) {
        val jumpUrl = (model as XBannerImage).jumpUrl
        if (jumpUrl.isEmptyStr()) {
            return
        }

        if (jumpUrl!!.contains("sweepstakes")) {
            JumpUtil.toLottery(requireActivity(),
                Constants.getLotteryH5Url(requireContext(), LoginRepository.token))
        } else {
            JumpUtil.toInternalWeb(requireActivity(), jumpUrl, "")
        }
          dismissAllowingStateLoss()
    }


}