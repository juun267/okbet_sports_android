package org.cxct.sportlottery.ui.profileCenter

import android.annotation.SuppressLint
import android.content.Intent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.gyf.immersionbar.ImmersionBar
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.UserVipType.setLevelTagIcon
import org.cxct.sportlottery.common.extentions.clickDelay
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.startActivity
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.FragmentProfileCenterBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.uploadImg.UploadImgRequest
import org.cxct.sportlottery.network.user.UserInfo
import org.cxct.sportlottery.network.withdraw.uwcheck.ValidateTwoFactorRequest
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.common.dialog.CustomAlertDialog
import org.cxct.sportlottery.ui.common.dialog.CustomSecurityDialog
import org.cxct.sportlottery.ui.finance.FinanceActivity
import org.cxct.sportlottery.ui.helpCenter.HelpCenterActivity
import org.cxct.sportlottery.ui.infoCenter.InfoCenterActivity
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.money.recharge.MoneyRechargeActivity
import org.cxct.sportlottery.ui.money.withdraw.BankActivity
import org.cxct.sportlottery.ui.money.withdraw.WithdrawActivity
import org.cxct.sportlottery.ui.profileCenter.changePassword.SettingPasswordActivity
import org.cxct.sportlottery.ui.profileCenter.changePassword.SettingPasswordActivity.Companion.PWD_PAGE
import org.cxct.sportlottery.ui.profileCenter.identity.VerifyIdentityDialog
import org.cxct.sportlottery.ui.profileCenter.money_transfer.MoneyTransferActivity
import org.cxct.sportlottery.ui.profileCenter.otherBetRecord.OtherBetRecordActivity
import org.cxct.sportlottery.ui.profileCenter.profile.AvatarSelectorDialog
import org.cxct.sportlottery.ui.profileCenter.profile.ProfileActivity
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
import org.cxct.sportlottery.view.dialog.ToGcashDialog
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

    //簡訊驗證彈窗
    private var customSecurityDialog: CustomSecurityDialog? = null
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
        getUserInfo()
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
                startActivity(VipBenefitsActivity::class.java)
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
        getMoney()
    }

    private fun setupHeadButton() {
        binding.ivHead1.setOnClickListener { it.context.startActivity<VipBenefitsActivity>() }
//        iv_head.setOnClickListener {
//            AvatarSelectorDialog(this, mSelectMediaListener).show(supportFragmentManager, null)
//        }

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
            fragmentManager?.let { it1 ->
                val dialog = AvatarSelectorDialog()
                dialog.mSelectListener = mSelectMediaListener
                dialog.show(it1, null)
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
            //Glife用户
            (activity as MainTabActivity).checkRechargeKYCVerify()
        }
    }

    private fun setupWithdrawButton() {
        binding.btnWithdraw.clickDelay {
            //Glife用户
            ToGcashDialog.showByClick{
                viewModel.checkWithdrawKYCVerify()
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
        btnOtherBetRecord.setVisibilityByMarketSwitch()
        btnAffiliate.setVisibilityByMarketSwitch()
        btnAboutUs.setVisibilityByMarketSwitch()
        ivProfile.setOnClickListener {
            startActivity(Intent(requireActivity(), ProfileActivity::class.java))
        }
        btnRedeem.setOnClickListener {
            startActivity(Intent(requireActivity(),RedeemActivity::class.java))
        }
        //額度轉換
        btnAccountTransfer.setOnClickListener {
            startActivity(Intent(requireActivity(), MoneyTransferActivity::class.java))
        }
        //其他投注記錄
        btnOtherBetRecord.setOnClickListener {
            startActivity(Intent(requireActivity(), OtherBetRecordActivity::class.java))
        }

        //資金明細
        btnFundDetail.setOnClickListener {
            startActivity(Intent(requireActivity(), FinanceActivity::class.java))
        }
        //優惠活動
        btnPromotion.setOnClickListener {
            startActivity(PromotionListActivity::class.java)
        }
        //代理加盟
        btnAffiliate.setOnClickListener {
            JumpUtil.toInternalWeb(
                requireContext(),
                Constants.getAffiliateUrl(requireContext()),
                resources.getString(R.string.btm_navigation_affiliate)
            )
        }
        //自我約束
        if (sConfigData?.selfRestraintVerified == "0" || sConfigData?.selfRestraintVerified == null) {
            btnSelfLimit.visibility = View.GONE
        } else {
            btnSelfLimit.visibility = View.VISIBLE
            btnSelfLimit.setOnClickListener {
                startActivity(Intent(requireActivity(), SelfLimitActivity::class.java))
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

        //资产检测

    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        getMoney()
    }

    private fun getUserInfo() {
        viewModel.getUserInfo()
    }

    private fun initObserve() {
        viewModel.userMoney.observe(viewLifecycleOwner) {
            it?.let {
                binding.tvAccountBalance.text = TextUtil.format(it)
            }
        }

        viewModel.userInfo.observe(viewLifecycleOwner) {
            updateUI(it)
        }


        viewModel.withdrawSystemOperation.observe(viewLifecycleOwner) {
            val operation = it.getContentIfNotHandled()
            if (operation == false) {
                showPromptDialog(
                    getString(R.string.prompt),
                    getString(R.string.message_withdraw_maintain)
                ) {}
            }
        }

        viewModel.rechargeSystemOperation.observe(viewLifecycleOwner) {
            hideLoading()
            it.getContentIfNotHandled()?.let { b ->
                if (b) {
                    startActivity(Intent(requireActivity(), MoneyRechargeActivity::class.java))
                } else {
                    showPromptDialog(
                        getString(R.string.prompt),
                        getString(R.string.message_recharge_maintain)
                    ) {}
                }
            }
        }

        viewModel.needToUpdateWithdrawPassword.observe(viewLifecycleOwner) {

            it.getContentIfNotHandled()?.let { b ->
                if (b) {
                    showPromptDialog(
                        getString(R.string.withdraw_setting),
                        getString(R.string.please_setting_withdraw_password),
                        getString(R.string.go_to_setting),
                        true
                    ) {
                        startActivity(Intent(
                            requireActivity(), SettingPasswordActivity::class.java
                        ).apply {
                            putExtra(
                                PWD_PAGE, SettingPasswordActivity.PwdPage.BANK_PWD
                            )
                        })
                    }
                } else {
                    viewModel.checkProfileInfoComplete()
                }
            }
        }
        viewModel.isWithdrawShowVerifyDialog.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { b ->
                if (b)
                    showKYCVerifyDialog()
                else
                    viewModel.checkWithdrawSystem()
            }
        }
        viewModel.needToCompleteProfileInfo.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { b ->
                if (b) {
                    showPromptDialog(
                        getString(R.string.withdraw_setting),
                        getString(R.string.please_complete_profile_info),
                        getString(R.string.go_to_setting),
                        true
                    ) {
                        startActivity(Intent(requireActivity(), ProfileActivity::class.java))
                    }
                } else {
                    viewModel.checkBankCardPermissions()
                }
            }
        }

        viewModel.needToBindBankCard.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { messageId ->
                if (messageId != -1) {
                    showPromptDialog(
                        getString(R.string.withdraw_setting),
                        getString(messageId),
                        getString(R.string.go_to_setting),
                        true
                    ) {
                        startActivity(Intent(requireActivity(), BankActivity::class.java))
                    }
                } else {
                    startActivity(Intent(requireActivity(), WithdrawActivity::class.java))
                }
            }
        }


        viewModel.needToSendTwoFactor.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { b ->
                if (b) {
                    customSecurityDialog = CustomSecurityDialog().apply {
                        getSecurityCodeClickListener {
                            this.showSmeTimer300()
                            this@ProfileCenterFragment.viewModel.sendTwoFactor()
                        }
                        positiveClickListener =
                            CustomSecurityDialog.PositiveClickListener { number ->
                                this@ProfileCenterFragment.viewModel.validateTwoFactor(ValidateTwoFactorRequest(number))
                            }
                    }
                    customSecurityDialog?.show(childFragmentManager, null)
                }
            }
        }

        viewModel.errorMessageDialog.observe(viewLifecycleOwner) {
            val errorMsg = it ?: getString(R.string.unknown_error)
            CustomAlertDialog().apply {
                setMessage(errorMsg)
                setNegativeButtonText(null)
                setCanceledOnTouchOutside(false)
                setCancelable(false)
            }.show(childFragmentManager, null)
        }

        viewModel.twoFactorSuccess.observe(viewLifecycleOwner) {
            if (it == true) customSecurityDialog?.dismiss()
        }

        viewModel.twoFactorResult.observe(viewLifecycleOwner) {
            //傳送驗證碼成功後才能解鎖提交按鈕
            customSecurityDialog?.setPositiveBtnClickable(it?.success ?: false)
            sConfigData?.hasGetTwoFactorResult = true
        }

        //使用者沒有電話號碼
        viewModel.showPhoneNumberMessageDialog.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { b ->
                if (!b) phoneNumCheckDialog(requireContext(), childFragmentManager)
            }
        }

        viewModel.settingNeedToUpdateWithdrawPassword.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { b ->
                if (b) {
                    showPromptDialog(
                        getString(R.string.withdraw_setting),
                        getString(R.string.please_setting_withdraw_password),
                        getString(R.string.go_to_setting),
                        true
                    ) {
                        startActivity(
                            Intent(
                                requireActivity(),
                                SettingPasswordActivity::class.java
                            ).apply {
                                putExtra(
                                    PWD_PAGE,
                                    SettingPasswordActivity.PwdPage.BANK_PWD
                                )
                            })
                    }
                } else if (!b) {
                    startActivity(Intent(requireActivity(), BankActivity::class.java))
                }
            }
        }

        viewModel.settingNeedToCompleteProfileInfo.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { b ->
                if (b) {
                    showPromptDialog(
                        getString(R.string.withdraw_setting),
                        getString(R.string.please_complete_profile_info),
                        getString(R.string.go_to_setting),
                        true
                    ) {
                        startActivity(Intent(requireActivity(), ProfileActivity::class.java))
                    }
                } else if (!b) {
                    startActivity(Intent(requireActivity(), BankActivity::class.java))
                }
            }
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

        viewModel.intoWithdraw.observe(viewLifecycleOwner) { it ->
            it.getContentIfNotHandled()?.let {
                startActivity(Intent(requireActivity(), WithdrawActivity::class.java))
            }
        }
        InfoCenterRepository.totalUnreadMsgCount.observe(viewLifecycleOwner) {
            updateNoticeCount(it)
        }

        viewModel.userInfo.observe(viewLifecycleOwner) {
            //是否测试用户（0-正常用户，1-游客，2-内部测试）
            updateUserIdentity(it?.testFlag)
        }


        viewModel.isWithdrawShowVerifyDialog.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { b ->
                if (b)
                    showKYCVerifyDialog()
                else
                    viewModel.checkWithdrawSystem()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(userInfo: UserInfo?)=binding.run {
        Glide.with(requireContext())
            .load(userInfo?.iconUrl)
            .apply(RequestOptions().placeholder(R.drawable.ic_person_avatar))
            .into(ivHead1) //載入頭像
        ivVipLevel.setLevelTagIcon(userInfo?.levelCode)
        tvUserNickname.text = if (userInfo?.nickName.isNullOrEmpty()) {
            userInfo?.userName
        } else {
            userInfo?.nickName
        }
        bindVerifyStatus(userInfo)
        btnEditNickname.visibility =
            if (userInfo?.setted == FLAG_NICKNAME_IS_SET) View.GONE else View.VISIBLE
        labelUserName.text = "${getString(R.string.username)}："
        tvUserUsername.text = userInfo?.userName
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
        binding.ivUserNotice.setOnClickListener {InfoCenterActivity.startWith(it.context, noticeCount > 0) }
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

    //实名验证
    private fun showKYCVerifyDialog() {
        VerifyIdentityDialog().show(childFragmentManager, null)
    }
    private fun bindVerifyStatus(userInfo: UserInfo?) {
        binding.tvKycStatus.isVisible = sConfigData?.realNameWithdrawVerified.isStatusOpen()
                || sConfigData?.realNameRechargeVerified.isStatusOpen() || !getMarketSwitch()

        when (userInfo?.verified) {
            ProfileActivity.VerifiedType.PASSED.value -> {
                setVerify(R.string.kyc_passed, R.color.color_1EB65B)
            }
            ProfileActivity.VerifiedType.NOT_YET.value,ProfileActivity.VerifiedType.VERIFIED_FAILED.value -> {
                setVerify(R.string.kyc_unverified, R.color.color_C4CDE3)
            }
            ProfileActivity.VerifiedType.VERIFYING.value,ProfileActivity.VerifiedType.VERIFIED_WAIT.value -> {
                setVerify(R.string.kyc_unverifing, R.color.color_FF8A00)

            }
            ProfileActivity.VerifiedType.REVERIFIED_NEED.value -> {
                setVerify(R.string.P211, R.color.color_FF8A00)

            }
            ProfileActivity.VerifiedType.REVERIFYING.value -> {
                setVerify(R.string.P196, R.color.color_FF8A00)
            }
            else -> {
                setVerify(R.string.kyc_unverified, R.color.color_C4CDE3)
            }
        }
    }

    private fun setVerify(text: Int, colorResId: Int) {
        binding.tvKycStatus.setText(text)
        val bgDrawable = DrawableCreator.Builder()
            .setSolidColor(ContextCompat.getColor(requireContext(), colorResId))
            .setSizeHeight(18.dp.toFloat())
            .setCornersRadius(9.dp.toFloat())
            .build()
        binding.tvKycStatus.setBackgroundDrawable(bgDrawable)
    }
}