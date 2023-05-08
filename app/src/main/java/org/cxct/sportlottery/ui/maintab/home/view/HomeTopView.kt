package org.cxct.sportlottery.ui.maintab.home.view

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.stx.xhb.androidx.XBanner
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.index.config.ImageData
import org.cxct.sportlottery.repository.ConfigRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.common.bean.XBannerImage
import org.cxct.sportlottery.ui.login.signIn.LoginOKActivity
import org.cxct.sportlottery.ui.maintab.home.MainHomeFragment2
import org.cxct.sportlottery.ui.money.recharge.MoneyRechargeActivity
import org.cxct.sportlottery.ui.profileCenter.identity.VerifyIdentityDialog
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.LanguageManager

class HomeTopView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0)
    : LinearLayout(context, attrs, defStyle), XBanner.OnItemClickListener {

    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.layout_home_top, this, true)
        initLogin()
    }


    private fun initBanner() {
        val lang = LanguageManager.getSelectLanguage(context).key
        setUpBanner(lang,2, R.id.topBanner)
        setUpBanner(lang, 5, R.id.promotionsBanner)
    }

    private fun setUpBanner(lang: String,
                            imageType: Int,
                            bannerId: Int) {


        var imageList = sConfigData?.imageList?.filter {
            it.imageType == imageType && it.lang == lang && !it.imageName1.isNullOrEmpty()
        }?.sortedWith(compareByDescending<ImageData> { it.imageSort }.thenByDescending { it.createdAt })

        val loopEnable = imageList?.size ?: 0 > 1
        if (imageList.isNullOrEmpty()) {
            return
        }

        val xbanner = findViewById<XBanner>(bannerId)

        xbanner.setHandLoop(loopEnable)
        xbanner.setAutoPlayAble(loopEnable)
        xbanner.setOnItemClickListener(this@HomeTopView)
        xbanner.loadImage { _, model, view, _ ->
            (view as ImageView).load((model as XBannerImage).imgUrl, R.drawable.img_banner01)
        }

        val host = sConfigData?.resServerHost
        val images = imageList.map {
            XBannerImage(it.imageText1 + "", host + it.imageName1, it.appUrl)
        }

        xbanner.setBannerData(images.toMutableList())
    }

    override fun onItemClick(banner: XBanner, model: Any, view: View, position: Int) {
        val jumpUrl = (model as XBannerImage).jumpUrl
        if (jumpUrl.isEmptyStr()) {
            return
        }

        if (jumpUrl!!.contains("sweepstakes")) {
            JumpUtil.toLottery(context, Constants.getLotteryH5Url(context, LoginRepository.token))
        } else {
            JumpUtil.toInternalWeb(context, jumpUrl, "")
        }

    }

    private fun initLogin() {
        if (LoginRepository.isLogined()) {
            findViewById<View>(R.id.depositLayout).visible()
            return
        }

        findViewById<View>(R.id.loginLayout).visible()
        findViewById<View>(R.id.tvLogin).setOnClickListener { context.startActivity(Intent(context, LoginOKActivity::class.java)) }
        findViewById<View>(R.id.tvRegist).setOnClickListener { LoginOKActivity.startRegist(context) }

    }

    fun setup(fragment: MainHomeFragment2) {

        ConfigRepository.onNewConfig(fragment) { initBanner() }
        findViewById<View>(R.id.vSports).setOnClickListener { fragment.jumpToInplaySport() }
        findViewById<View>(R.id.vOkgames).setOnClickListener { fragment.jumpToOKGames() }

        if (!LoginRepository.isLogined()) {
            findViewById<View>(R.id.ivGoogle).setOnClickListener { LoginOKActivity.googleLoging(context) }
            return
        }

        initRechargeClick(fragment)
    }

    private fun initRechargeClick(fragment: MainHomeFragment2) {

        val depositClick = OnClickListener {
            if (UserInfoRepository.userInfo.value?.vipType != 1) {
                fragment.viewModel.checkRechargeKYCVerify()
                return@OnClickListener
            }

            fragment.showPromptDialog(context.getString(R.string.prompt), context.getString(R.string.N643)) {

            }
        }

        findViewById<View>(R.id.tvDeposit).setOnClickListener(depositClick)
        findViewById<View>(R.id.ivGLive).setOnClickListener(depositClick)
        findViewById<View>(R.id.ivPaymaya).setOnClickListener(depositClick)
        findViewById<View>(R.id.ivPayX).setOnClickListener(depositClick)
        findViewById<View>(R.id.ivFortunepay).setOnClickListener(depositClick)

        fragment.viewModel.isRechargeShowVerifyDialog.observe(fragment.viewLifecycleOwner) {
            val b = it.getContentIfNotHandled() ?: return@observe
            if (b) {
                VerifyIdentityDialog().show(fragment.childFragmentManager, null)
            } else {
                fragment.loading()
                fragment.viewModel.checkRechargeSystem()
            }
        }

        fragment.viewModel.rechargeSystemOperation.observe(fragment.viewLifecycleOwner) {
            fragment.hideLoading()
            val b = it.getContentIfNotHandled() ?: return@observe
            if (b) {
                context.startActivity(Intent(context, MoneyRechargeActivity::class.java))
                return@observe
            }

            fragment.showPromptDialog(context.getString(R.string.prompt),
                context.getString(R.string.message_recharge_maintain)) {
            }

        }

    }


}