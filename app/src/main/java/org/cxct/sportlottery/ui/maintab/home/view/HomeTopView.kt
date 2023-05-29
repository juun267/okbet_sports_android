package org.cxct.sportlottery.ui.maintab.home.view

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.PagerSnapHelper
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.stx.xhb.androidx.XBanner
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.common.extentions.setOnClickListeners
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.LayoutHomeTopBinding
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
import org.cxct.sportlottery.util.getSportEnterIsClose
import timber.log.Timber

class HomeTopView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle), XBanner.OnItemClickListener {

    val binding: LayoutHomeTopBinding

    init {
        orientation = VERTICAL
        binding = LayoutHomeTopBinding.inflate(LayoutInflater.from(context), this)
        initLogin()
        initSportEnterStatus()
    }

    /**
     * 检测体育服务是否关闭
     */
    fun initSportEnterStatus() {
        if (getSportEnterIsClose()) {
            binding.tvSportClose.visible()
        } else {
            binding.tvSportClose.gone()
        }
    }

    private fun initBanner() {
        val lang = LanguageManager.getSelectLanguage(context).key
        setUpBanner(lang, 2)
        setUpBanner(lang, 5)
    }

    private fun setUpBanner(lang: String, imageType: Int) {
        val imageList = sConfigData?.imageList?.filter {
            it.imageType == imageType && it.lang == lang && !it.imageName1.isNullOrEmpty()
        }?.sortedWith(compareByDescending<ImageData> { it.imageSort }.thenByDescending { it.createdAt })
        val loopEnable = (imageList?.size ?: 0) > 1
        if (imageList.isNullOrEmpty()) {
            return
        }
        if (imageType == 2) {
            val xbanner = findViewById<XBanner>(R.id.topBanner)
            xbanner.setHandLoop(loopEnable)
            xbanner.setOnItemClickListener(this@HomeTopView)
            xbanner.loadImage { _, model, view, _ ->
                (view as ImageView).load((model as XBannerImage).imgUrl, R.drawable.img_banner01)
            }
            val host = sConfigData?.resServerHost
            val images = imageList.map {
                Timber.d("host:$host url1:${host + it.imageName1}")
                XBannerImage(it.imageText1 + "", host + it.imageName1, it.appUrl)
            }
            //opt1 ->ImageType = 5,为活动轮播图
            //opt2 ->后台有配置
            //满足以上两点 -> 显示活动轮播图r
            if (images.isNotEmpty()) {
                xbanner.visible()
            }
            xbanner.setBannerData(images.toMutableList())
        } else {
            val host = sConfigData?.resServerHost
            val promoteImages = imageList.map {
                Timber.d("host:$host url4:${host + it.imageName4}")
                XBannerImage(it.imageText1 + "", host + it.imageName4, it.appUrl)
            }
            setUpPromoteView(promoteImages)
        }
    }

    private fun setUpPromoteView(imageList: List<XBannerImage>) {
        val promoteAdapter =
            object : BaseQuickAdapter<XBannerImage, BaseViewHolder>(R.layout.item_promote_view) {
                override fun convert(holder: BaseViewHolder, item: XBannerImage) {
                    val view = holder.getView<ImageView>(R.id.ivItemPromote)
                    view.load(item.imgUrl, R.drawable.img_banner01)
                }

            }
        promoteAdapter.setNewInstance(imageList.toMutableList())
        binding.rcvPromote.apply {
            adapter = promoteAdapter
            if (onFlingListener == null) {
                PagerSnapHelper().attachToRecyclerView(binding.rcvPromote)
            }
        }

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
            binding.depositLayout.visible()
            return
        }

        binding.loginLayout.visible()
        binding.tvLogin.setOnClickListener {
            context.startActivity(
                Intent(
                    context, LoginOKActivity::class.java
                )
            )
        }
        binding.tvRegist.setOnClickListener { LoginOKActivity.startRegist(context) }

    }

    fun setup(fragment: MainHomeFragment2) {

        ConfigRepository.onNewConfig(fragment) { initBanner() }
        binding.vSports.setOnClickListener { fragment.jumpToInplaySport() }
        binding.vOkgames.setOnClickListener { fragment.jumpToOKGames() }

        if (!LoginRepository.isLogined()) {
            binding.ivGoogle.setOnClickListener {
                LoginOKActivity.googleLoging(
                    context
                )
            }
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

            fragment.showPromptDialog(
                context.getString(R.string.prompt), context.getString(R.string.N643)
            ) {

            }
        }

        setOnClickListeners(
            binding.tvDeposit,
            binding.ivGLive,
            binding.ivPaymaya,
            binding.ivPayX,
            binding.ivFortunepay,
        ) {
            depositClick.onClick(it)
        }


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

            fragment.showPromptDialog(
                context.getString(R.string.prompt),
                context.getString(R.string.message_recharge_maintain)
            ) {}

        }

    }


}