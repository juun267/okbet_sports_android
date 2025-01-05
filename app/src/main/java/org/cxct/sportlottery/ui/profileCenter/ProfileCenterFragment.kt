package org.cxct.sportlottery.ui.profileCenter

import android.annotation.SuppressLint
import android.content.Intent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.gyf.immersionbar.ImmersionBar
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import kotlinx.android.synthetic.main.view_endcard_toolbar.*
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.VerifiedType
import org.cxct.sportlottery.common.enums.UserVipType.setLevelTagIcon
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.FragmentProfileCenterBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.uploadImg.UploadImgRequest
import org.cxct.sportlottery.network.user.UserInfo
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.finance.FinanceActivity
import org.cxct.sportlottery.ui.helpCenter.HelpCenterActivity
import org.cxct.sportlottery.ui.infoCenter.InfoCenterActivity
import org.cxct.sportlottery.ui.profileCenter.money_transfer.MoneyTransferActivity
import org.cxct.sportlottery.ui.profileCenter.otherBetRecord.OtherBetRecordActivity
import org.cxct.sportlottery.ui.profileCenter.profile.AvatarSelectorDialog
import org.cxct.sportlottery.ui.profileCenter.profile.ProfileActivity
import org.cxct.sportlottery.ui.profileCenter.invite.InviteActivity
import org.cxct.sportlottery.ui.profileCenter.taskCenter.TaskCenterActivity
import org.cxct.sportlottery.ui.profileCenter.pointshop.PointShopActivity
import org.cxct.sportlottery.ui.profileCenter.timezone.TimeZoneActivity
import org.cxct.sportlottery.ui.profileCenter.versionUpdate.VersionUpdateViewModel
import org.cxct.sportlottery.ui.profileCenter.vip.VipBenefitsActivity
import org.cxct.sportlottery.ui.profileCenter.vip.VipViewModel
import org.cxct.sportlottery.ui.promotion.PromotionListActivity
import org.cxct.sportlottery.ui.redeem.RedeemActivity
import org.cxct.sportlottery.ui.results.ResultsSettlementActivity
import org.cxct.sportlottery.ui.selflimit.SelfLimitActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.DrawableCreator
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException

/**
 * @app_destination 个人中心
 */
class ProfileCenterFragment : BaseFragment<ProfileCenterViewModel,FragmentProfileCenterBinding>() {

    private val mVersionUpdateViewModel: VersionUpdateViewModel by viewModel()
    private val vipViewModel: VipViewModel by viewModel()
    private var noticeCount: Int = 0
    private var isGuest: Boolean? = null

    override fun onInitView(view: View) {
        initToolBar()
        setupNoticeButton()
        initView()
        setupHeadButton()
        setupEditNickname()
        setupBalance()
        setupRechargeButton()
        setupWithdrawButton()
        setupLogout()
        setupMoreButtons()
        initObserve()
        updateThirdOpenUI()
        updateCreditAccountUI()
        setupServiceButton()
    }

    private fun setupServiceButton() {
        binding.ivCustomerService.setServiceClick(childFragmentManager)
    }

    private fun initView() {
        binding.tvCurrencyType.text = sConfigData?.systemCurrencySign
        //信用盤打開，隱藏提款設置
        // btn_withdrawal_setting.setVisibilityByCreditSystem()
        //默认显示代理入口
        binding.btnAffiliate.isVisible = (sConfigData?.frontEntranceStatus != "0")
        //   btn_affiliate.setVisibilityByCreditSystem()
        mVersionUpdateViewModel.appMinVersionState.observe(viewLifecycleOwner) {
            binding.ivVersionNew.isVisible = it.isShowUpdateDialog
            binding.btnVersionNow.isEnabled = it.isShowUpdateDialog
            binding.btnVersionNow.setOnClickListener {
                //外部下載
                JumpUtil.toExternalWeb(requireActivity(), sConfigData?.mobileAppDownUrl)
            }
        }
        mVersionUpdateViewModel.checkAppMinVersion()
        val version = " V${BuildConfig.VERSION_NAME}"
        binding.tvCurrentVersion.text = version
        binding.tvVersionCode.text = getString(R.string.current_version) + version
        binding.tvWithdrawTitle.setTitleLetterSpacing2F()
        binding.tvDepositTitle.setTitleLetterSpacing2F()
        if (StaticData.vipOpened()){
            binding.userVipView.visible()
            binding.userVipView.setup(this,vipViewModel)
            binding.userVipView.setOnClickListener {
                runAfterLogined{
                    startActivity(VipBenefitsActivity::class.java)
                }
            }
        }else{
            binding.userVipView.gone()
        }
    }

    fun initToolBar() {
        ImmersionBar.with(this)
            .statusBarView(R.id.v_statusbar)
            .statusBarDarkFont(true)
            .init()
//        v_statusbar?.setPadding(0, ImmersionBar.getStatusBarHeight(this), 0, 0)

    }

    override fun onResume() {
        super.onResume()
        getUserInfo()
        getMoney()
    }

    private fun setupHeadButton() {
        binding.tvUserNickname.setOnClickListener {
            runAfterLogined{}
        }

    }

    private val mSelectMediaListener = object : OnResultCallbackListener<LocalMedia> {
        override fun onResult(result: ArrayList<LocalMedia>?) {
            try {
                // 图片选择结果回调
                // LocalMedia 里面返回三种path
                // 1.media.getPath(); 为原图path
                // 2.media.getCutPath();为裁剪后path，需判断media.isCut();是否为true
                // 3.media.getCompressPath();为压缩后path，需判断media.isCompressed();是否为true
                // 如果裁剪并压缩了，已取压缩路径为准，因为是先裁剪后压缩的

                val media = result?.firstOrNull() //這裡應當只會有一張圖片
                val path = when {
                    media?.isCompressed == true -> media.compressPath
                    media?.isCut == true -> media.cutPath
                    else -> media?.path
                }

                val file = File(path!!)
                if (file.exists()) uploadImg(file)
                else throw FileNotFoundException()
            } catch (e: Exception) {
                e.printStackTrace()
                ToastUtil.showToastInCenter(
                    activity, getString(R.string.error_reading_file)
                )
            }
        }

        override fun onCancel() {
            Timber.i("PictureSelector Cancel")
        }

    }

    private fun setupEditNickname() {
        binding.rlHead.setOnClickListener {
            runAfterLogined {
                val dialog = AvatarSelectorDialog()
                dialog.mSelectListener = mSelectMediaListener
                dialog.show(childFragmentManager, null)
            }
        }
    }

    private fun setupBalance() {
        binding.btnRefreshMoney.setOnClickListener {
            getMoney()
        }
    }

    private fun setupRechargeButton() {
        binding.btnRecharge.clickDelay{
            runAfterLogined {
                (requireActivity() as BaseActivity<*,*>).jumpToDeposit("存款按钮(我的)")
            }
        }
    }

    private fun setupWithdrawButton() {
        binding.btnWithdraw.isVisible = !Constants.channelSwitch
        binding.btnWithdraw.clickDelay {
            runAfterLogined{
                requireActivity().jumpToWithdraw()
            }
        }
    }

    private fun getMoney() {
        binding.btnRefreshMoney.refreshMoneyLoading()
        viewModel.getMoneyAndTransferOut()
    }

    private fun setupLogout() {
        binding.ivLogout.setOnClickListener {
            //退出登录并还原所有用户设置
            viewModel.doLogoutAPI()
        }
    }

    private fun setupMoreButtons()=binding.run {

        blockAmount.setVisibilityByMarketSwitch()
        tvTermsCondition.setVisibilityByMarketSwitch()
        btnFundDetail.setVisibilityByMarketSwitch()
        btnHelpCenter.setVisibilityByMarketSwitch()
        btnPromotion.setVisibilityByMarketSwitch()
        btnInviteFriend.isVisible = StaticData.inviteUserOpened()
        btnOtherBetRecord.setVisibilityByMarketSwitch()
        btnAffiliate.setVisibilityByMarketSwitch()
        btnAboutUs.setVisibilityByMarketSwitch()
        btnTaskCenter.isVisible = StaticData.taskCenterOpened()
        btnPointShop.isVisible = StaticData.pointShopOpened()
        ivProfile.setOnClickListener {
            runAfterLogined{startActivity(Intent(requireActivity(), ProfileActivity::class.java)) }
        }
        btnRedeem.setOnClickListener {
            runAfterLogined{ startActivity(Intent(requireActivity(),RedeemActivity::class.java))}
        }
        //額度轉換
        btnAccountTransfer.setOnClickListener {
            runAfterLogined{ startActivity(Intent(requireActivity(), MoneyTransferActivity::class.java)) }
        }
        //其他投注記錄
        btnOtherBetRecord.setOnClickListener {
            runAfterLogined{ startActivity(Intent(requireActivity(), OtherBetRecordActivity::class.java))}
        }

        //資金明細
        btnFundDetail.setOnClickListener {
            runAfterLogined{ startActivity(Intent(requireActivity(), FinanceActivity::class.java)) }
        }
        //優惠活動
        btnPromotion.setOnClickListener {
            PromotionListActivity.startFrom(context(), "我的页面")
        }
        btnInviteFriend.setOnClickListener {
            runAfterLogined { startActivity(InviteActivity::class.java) }
        }
        //代理加盟
        btnAffiliate.setOnClickListener {
            JumpUtil.toInternalWeb(
                requireContext(),
                Constants.getAffiliateUrl(requireContext()),
                resources.getString(R.string.btm_navigation_affiliate)
            )
        }
        btnPointShop.setOnClickListener {
            startActivity(PointShopActivity::class.java)
        }
        //自我約束
        if (sConfigData?.selfRestraintVerified == "0" || sConfigData?.selfRestraintVerified == null) {
            btnSelfLimit.visibility = View.GONE
        } else {
            btnSelfLimit.visibility = View.VISIBLE
            btnSelfLimit.setOnClickListener {
                runAfterLogined{ startActivity(Intent(requireActivity(), SelfLimitActivity::class.java)) }
            }
        }
        //赛果结算
        btnGameSettlement.setOnClickListener {
            //检查是否关闭入口
            checkSportStatus(requireActivity() as BaseActivity<*,*>) {
                startActivity(Intent(requireActivity(), ResultsSettlementActivity::class.java))
            }
        }
        //时区切换
        btnTimeZone.setOnClickListener {
            startActivity(Intent(requireActivity(), TimeZoneActivity::class.java))
        }
        /**
         * 中英文，区分其他语言布局
         */
        when (LanguageManager.getSelectLanguage(requireContext())) {
            LanguageManager.Language.ZH, LanguageManager.Language.EN -> {
                linHelpSub.children.filter { it is TextView }.forEach {
                    (it.layoutParams as LinearLayout.LayoutParams).apply {
                        this.weight = 0f
                    }
                }
            }

            else -> {
                linHelpSub.children.filter { it is TextView }.forEach {
                    (it.layoutParams as LinearLayout.LayoutParams).apply {
                        this.weight = 0f
                    }
                }
            }
        }
        //幫助中心
        btnHelpCenter.setOnClickListener {
            startActivity(Intent(requireActivity(), HelpCenterActivity::class.java))
        }
        //关于我们
        btnAboutUs.setOnClickListener {
            startActivity(
                Intent(
                    requireActivity(),
                    org.cxct.sportlottery.ui.aboutMe.AboutMeActivity::class.java
                )
            )
        }

        //任务中心
        btnTaskCenter.setOnClickListener {
            context?.let {
                startActivity(Intent(it, TaskCenterActivity::class.java))
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        getMoney()
    }

    private fun getUserInfo() {
        viewModel.getUserInfo()
        vipViewModel.getUserVip()
    }

    private fun initObserve() {
        viewModel.userMoney.observe(viewLifecycleOwner) {
            if(LoginRepository.isLogined()){
                binding.tvAccountBalance.text = TextUtil.format(it?:0)
                binding.btnRefreshMoney.isVisible = true
            }else{
                binding.tvAccountBalance.text = "--"
                binding.btnRefreshMoney.isVisible = false
            }
        }

        viewModel.userInfo.observe(viewLifecycleOwner) {
            updateUI(it)
            //是否测试用户（0-正常用户，1-游客，2-内部测试）
            updateUserIdentity(it?.testFlag)
        }

        viewModel.editIconUrlResult.observe(viewLifecycleOwner) {
            val iconUrlResult = it?.getContentIfNotHandled()
            if (iconUrlResult?.success == true)
                showPromptDialog(
                    getString(R.string.prompt),
                    getString(R.string.save_avatar_success)
                ) {}
            else
                iconUrlResult?.msg?.let { msg -> showErrorPromptDialog(title = "", msg) {} }
        }
        InfoCenterRepository.totalUnreadMsgCount.observe(viewLifecycleOwner) {
            updateNoticeCount(it)
        }
        ConfigRepository.config.observe(this){
            binding.btnInviteFriend.isVisible = StaticData.inviteUserOpened()
        }
        viewModel.taskRedDotEvent.collectWith(lifecycleScope){
            binding.ivTaskDot.isVisible = it
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(userInfo: UserInfo?)=binding.run {
        Glide.with(requireContext())
            .load(userInfo?.iconUrl)
            .apply(RequestOptions().placeholder(R.drawable.ic_person_avatar))
            .into(ivHead1) //載入頭像
        ivVipLevel.setLevelTagIcon(userInfo?.levelCode)
        tvUserNickname.text = if(userInfo==null){
            getString(R.string.C047)
        }else if (userInfo?.nickName.isNullOrEmpty()) {
            userInfo?.userName
        } else {
            userInfo?.nickName
        }
        bindVerifyStatus(userInfo)
        btnEditNickname.visibility = if (userInfo==null||userInfo?.setted == FLAG_NICKNAME_IS_SET) View.GONE else View.VISIBLE
        labelUserName.text = if(userInfo==null) getString(R.string.C049) else "${getString(R.string.username)}："
        tvUserUsername.text = userInfo?.userName
        ivProfile.isVisible = userInfo!=null
//        if (getRemainDay(userInfo?.uwEnableTime) > 0) {
//            ivNotice.visibility = View.VISIBLE
//            ivNotice.setOnClickListener {
//                viewModel.getLockMoney()
//            }
//        } else {
//            ivNotice.visibility = View.GONE
//        }
    }

    private fun uploadImg(file: File) {
        val userId = viewModel.userInfo.value?.userId.toString()
        val uploadImgRequest =
            UploadImgRequest(userId, file, UploadImgRequest.PlatformCodeType.AVATAR)
        viewModel.uploadImage(uploadImgRequest)
    }

    private fun updateThirdOpenUI() {
        // 三方游戏开启并且自动转换关闭的情况下才先额度转换的开关
        val thirdOpen = sConfigData?.thirdOpen == FLAG_OPEN
        val thirdTransferOpen = sConfigData?.thirdTransferOpen == FLAG_OPEN
        binding.btnAccountTransfer.visibility =
            if (thirdOpen && !thirdTransferOpen) View.VISIBLE else View.GONE
    }

    private fun updateCreditAccountUI() {
        binding.linWalletOperation.setVisibilityByMarketSwitch()
    }

    //有 child activity 給定 notice button 顯示
    private fun setupNoticeButton() {
        binding.ivUserNotice.setOnClickListener {
            runAfterLogined{
                InfoCenterActivity.startWith(it.context, noticeCount > 0)
            }
        }
    }

    private fun updateNoticeCount(noticeCount: Int) {
        this.noticeCount = noticeCount
        updateNoticeButton()
    }

    private fun updateUserIdentity(isGuest: Long?) {
        this.isGuest = when (isGuest) {
            0.toLong() -> false
            1.toLong() -> true
            else -> null
        }
        updateNoticeButton()
    }

    private fun updateNoticeButton() {
        binding.ivCircle.visibility =
            (if (noticeCount > 0 && isGuest == false) View.VISIBLE else View.GONE)
    }

    private fun bindVerifyStatus(userInfo: UserInfo?) {
        binding.tvKycStatus.isVisible = if(userInfo==null) false else sConfigData?.realNameWithdrawVerified.isStatusOpen()
                || sConfigData?.realNameRechargeVerified.isStatusOpen() || !getMarketSwitch()
        VerifiedType.getVerifiedType(userInfo).let{
            setVerify(text = it.nameResId, color = ContextCompat.getColor(requireContext(),it.colorResId))
        }
    }

    private fun setVerify(text: Int, color: Int) {
        binding.tvKycStatus.setText(text)
        val bgDrawable = DrawableCreator.Builder()
            .setSolidColor(color)
            .setSizeHeight(18.dp.toFloat())
            .setCornersRadius(9.dp.toFloat())
            .build()
        binding.tvKycStatus.setBackgroundDrawable(bgDrawable)
    }
    private fun runAfterLogined(block: () -> Unit){
        loginedRun(requireContext(),true){
            block.invoke()
        }
    }
}