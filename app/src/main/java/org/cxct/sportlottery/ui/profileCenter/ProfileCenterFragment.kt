package org.cxct.sportlottery.ui.profileCenter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.gyf.immersionbar.ImmersionBar
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.listener.OnResultCallbackListener
import kotlinx.android.synthetic.main.activity_version_update.*
import kotlinx.android.synthetic.main.fragment_profile_center.*
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.db.entity.UserInfo
import org.cxct.sportlottery.event.MenuEvent
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.uploadImg.UploadImgRequest
import org.cxct.sportlottery.network.withdraw.uwcheck.ValidateTwoFactorRequest
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.aboutMe.AboutMeActivity
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.common.CustomSecurityDialog
import org.cxct.sportlottery.ui.feedback.FeedbackMainActivity
import org.cxct.sportlottery.ui.finance.FinanceActivity
import org.cxct.sportlottery.ui.game.ServiceDialog
import org.cxct.sportlottery.ui.game.language.SwitchLanguageActivity
import org.cxct.sportlottery.ui.helpCenter.HelpCenterActivity
import org.cxct.sportlottery.ui.infoCenter.InfoCenterActivity
import org.cxct.sportlottery.ui.money.recharge.MoneyRechargeActivity
import org.cxct.sportlottery.ui.profileCenter.changePassword.SettingPasswordActivity
import org.cxct.sportlottery.ui.profileCenter.changePassword.SettingPasswordActivity.Companion.PWD_PAGE
import org.cxct.sportlottery.ui.profileCenter.identity.VerifyIdentityActivity
import org.cxct.sportlottery.ui.profileCenter.identity.VerifyIdentityDialog
import org.cxct.sportlottery.ui.profileCenter.money_transfer.MoneyTransferActivity
import org.cxct.sportlottery.ui.profileCenter.otherBetRecord.OtherBetRecordActivity
import org.cxct.sportlottery.ui.profileCenter.profile.AvatarSelectorDialog
import org.cxct.sportlottery.ui.profileCenter.profile.ProfileActivity
import org.cxct.sportlottery.ui.profileCenter.timezone.TimeZoneActivity
import org.cxct.sportlottery.ui.profileCenter.versionUpdate.VersionUpdateViewModel
import org.cxct.sportlottery.ui.results.ResultsSettlementActivity
import org.cxct.sportlottery.ui.selflimit.SelfLimitActivity
import org.cxct.sportlottery.ui.vip.VipActivity
import org.cxct.sportlottery.ui.withdraw.BankActivity
import org.cxct.sportlottery.ui.withdraw.WithdrawActivity
import org.cxct.sportlottery.util.*
import org.greenrobot.eventbus.EventBus
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException

/**
 * @app_destination 个人中心
 */
class ProfileCenterFragment :
    BaseBottomNavigationFragment<ProfileCenterViewModel>(ProfileCenterViewModel::class) {

    private val mVersionUpdateViewModel: VersionUpdateViewModel by viewModel()
    //簡訊驗證彈窗
    private var customSecurityDialog: CustomSecurityDialog? = null
    private var noticeCount: Int? = null
    private var isGuest: Boolean? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile_center, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
        iv_customer_service.setOnClickListener {
            val serviceUrl = sConfigData?.customerServiceUrl
            val serviceUrl2 = sConfigData?.customerServiceUrl2
            when {
                !serviceUrl.isNullOrBlank() && !serviceUrl2.isNullOrBlank() -> {
                    ServiceDialog().show(childFragmentManager, null)
                }
                serviceUrl.isNullOrBlank() && !serviceUrl2.isNullOrBlank() -> {
                    JumpUtil.toExternalWeb(requireContext(), serviceUrl2)
                }
                !serviceUrl.isNullOrBlank() && serviceUrl2.isNullOrBlank() -> {
                    JumpUtil.toExternalWeb(requireContext(), serviceUrl)
                }
            }
        }
    }

    private fun initView() {
        tv_currency_type.text = sConfigData?.systemCurrencySign
        //信用盤打開，隱藏提款設置
        // btn_withdrawal_setting.setVisibilityByCreditSystem()
        //優惠活動
        btn_promotion.setVisibilityByCreditSystem()
        //   btn_affiliate.setVisibilityByCreditSystem()
        btn_feedback.setVisibilityByCreditSystem()
        mVersionUpdateViewModel.appVersionState.observe(viewLifecycleOwner) {
            if (it.isNewVersion) {
                //下载更新要做判断 当前有没有新版本
                update_version.setOnClickListener{
                    //外部下載
                    JumpUtil.toExternalWeb(requireActivity(), sConfigData?.mobileAppDownUrl)
                    // startActivity(Intent(requireActivity(), VersionUpdateActivity::class.java))
                }
                iv_version_new.visibility = View.VISIBLE
                return@observe
            }

            update_version.setOnClickListener {  }
            iv_version_new.visibility = View.GONE
        }

        val version = " V${BuildConfig.VERSION_NAME}"
        tv_current_version.text = version
        tv_version_code.text = getString(R.string.current_version) + version
        tv_withdraw_title.setTitleLetterSpacing2F()
        tv_deposit_title.setTitleLetterSpacing2F()
    }

    fun initToolBar() {
        ImmersionBar.with(this)
            .statusBarView(R.id.v_statusbar)
            .statusBarDarkFont(true)
            .init()
//        v_statusbar?.setPadding(0, ImmersionBar.getStatusBarHeight(this), 0, 0)
        iv_menu.setOnClickListener {
            EventBus.getDefault().post(MenuEvent(true))
        }

    }

    override fun onResume() {
        super.onResume()
        if (MultiLanguagesApplication.isNightMode) {
            tv_appearance.text =
                getString(R.string.appearance) + ": " + getString(R.string.night_mode)
        } else {
            tv_appearance.text =
                getString(R.string.appearance) + ": " + getString(R.string.day_mode)
        }
        tv_language.text = LanguageManager.getLanguageStringResource(context)
        iv_flag.setImageResource(LanguageManager.getLanguageFlag(context))
        getMoney()
    }

    private fun setupHeadButton() {
//        iv_head.setOnClickListener {
//            AvatarSelectorDialog(this, mSelectMediaListener).show(supportFragmentManager, null)
//        }

    }
    private val mSelectMediaListener = object : OnResultCallbackListener<LocalMedia> {
        override fun onResult(result: MutableList<LocalMedia>?) {
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
                if (file.exists())
                    uploadImg(file)
                else
                    throw FileNotFoundException()
            } catch (e: Exception) {
                e.printStackTrace()
                ToastUtil.showToastInCenter(
                    activity,
                    getString(R.string.error_reading_file)
                )
            }
        }

        override fun onCancel() {
            Timber.i("PictureSelector Cancel")
        }

    }
    private fun setupEditNickname() {
        rl_head.setOnClickListener{
                fragmentManager?.let { it1 ->
                    AvatarSelectorDialog(
                        activity as Activity,
                        mSelectMediaListener
                    ).show(it1, null)
                }

        }
    }

    private fun setupBalance() {
        btn_refresh_money.setOnClickListener {
            getMoney()
        }
    }

    private fun setupRechargeButton() {
        btn_recharge.setOnClickListener {
            avoidFastDoubleClick()
            viewModel.checkRechargeKYCVerify()
        }
    }

    private fun setupWithdrawButton() {
        btn_withdraw.setOnClickListener {
            avoidFastDoubleClick()
            viewModel.checkWithdrawKYCVerify()
        }
    }

    private fun getMoney() {
        btn_refresh_money.refreshMoneyLoading()
        viewModel.allTransferOut()
        viewModel.getMoney()
    }

    private fun setupLogout() {
        iv_logout.setOnClickListener {
            //退出登录并还原所有用户设置
            viewModel.doLogoutAPI()
            viewModel.doLogoutCleanUser {
                run {
//                    if (sConfigData?.thirdOpen == FLAG_OPEN)
//                        MainActivity.reStart(this)
//                    else
//                    GamePublicityActivity.reStart(requireContext())
                }
            }
        }
    }

    private fun setupMoreButtons() {
        //個人資訊
//        btn_profile.setOnClickListener {
//            startActivity(Intent(requireActivity(), ProfileActivity::class.java))
//        }
        iv_profile.setOnClickListener {
            startActivity(Intent(requireActivity(), ProfileActivity::class.java))
        }
        //額度轉換
        btn_account_transfer.setOnClickListener {
            startActivity(Intent(requireActivity(), MoneyTransferActivity::class.java))
        }

        //提款設置
        btn_withdrawal_setting.setOnClickListener {
            viewModel.settingCheckPermissions()
        }

        //其他投注記錄
        btn_other_bet_record.setOnClickListener {
            startActivity(Intent(requireActivity(), OtherBetRecordActivity::class.java))
        }

        //資金明細
        btn_fund_detail.setOnClickListener {
            startActivity(Intent(requireActivity(), FinanceActivity::class.java))
        }

        //消息中心
        btn_news_center.setOnClickListener {
            startActivity(Intent(requireActivity(), InfoCenterActivity::class.java))
        }

        //優惠活動
        btn_promotion.setOnClickListener {
            when (viewModel.userInfo.value?.testFlag) {
                TestFlag.NORMAL.index -> {
                    toProfileCenter()
                }
                TestFlag.TEST.index -> { // TODO 20220108 新增內部測試人員選項 by Hewie
                    toProfileCenter()
                }
                else -> { // TODO 20220108 沒有遊客的話，要確認一下文案是否正確 by Hewie
                    ToastUtil.showToastInCenter(
                        requireContext(),
                        getString(R.string.message_guest_no_permission)
                    )
                }
            }
        }
        //代理加盟
        btn_affiliate.setOnClickListener {
            JumpUtil.toInternalWeb(
                requireContext(),
                Constants.getAffiliateUrl(requireContext()),
                resources.getString(R.string.btm_navigation_affiliate)
            )
        }
        //会员等级
        btn_member_level.setOnClickListener {
            startActivity(Intent(requireActivity(), VipActivity::class.java))
        }
        //自我約束
        if (sConfigData?.selfRestraintVerified == "0" || sConfigData?.selfRestraintVerified == null) {
            btn_self_limit.visibility = View.GONE
        } else {
            btn_self_limit.visibility = View.VISIBLE
            btn_self_limit.setOnClickListener {
                startActivity(Intent(requireActivity(), SelfLimitActivity::class.java))
            }
        }
        //赛果结算
        btn_game_settlement.setOnClickListener {
            startActivity(Intent(requireActivity(), ResultsSettlementActivity::class.java))
        }
        //游戏规则
        btn_game_rule.setOnClickListener {
            JumpUtil.toInternalWeb(
                requireContext(),
                Constants.getGameRuleUrl(requireContext()),
                getString(R.string.game_rule)
            )
        }
        //切换语言
        btn_language.setOnClickListener {
            var intent = Intent(requireActivity(), SwitchLanguageActivity::class.java);
            intent.putExtra("type", 1)
            startActivity(intent)
        }
        //外觀
        btn_appearance.setOnClickListener {
            startActivity(Intent(requireActivity(), AppearanceActivity::class.java))
        }

        //建議反饋
        /*   tv_feedback.setOnClickListener {
               startActivity(Intent(requireActivity(), FeedbackMainActivity::class.java))
           }*/

//        btn_time_zone.visibility = View.GONE
        //时区切换
        btn_time_zone.setOnClickListener {
            startActivity(Intent(requireActivity(), TimeZoneActivity::class.java))
        }
        /**
         * 中英文，区分其他语言布局
         */
        when (LanguageManager.getSelectLanguage(requireContext())) {
            LanguageManager.Language.ZH, LanguageManager.Language.EN -> {
                lin_help_sub.children.filter { it is TextView }.forEach {
                    (it.layoutParams as LinearLayout.LayoutParams).apply {
                        this.weight = 0f
                    }
                }
            }
            else -> {
                lin_help_sub.children.filter { it is TextView }.forEach {
                    (it.layoutParams as LinearLayout.LayoutParams).apply {
                        this.weight = 0f
                    }
                }
            }
        }
        //幫助中心
        btn_help_center.setOnClickListener {
            startActivity(Intent(requireActivity(), HelpCenterActivity::class.java))
        }

        //建議反饋
        btn_feedback.setOnClickListener {
            startActivity(Intent(requireActivity(), FeedbackMainActivity::class.java))
        }
        //联系客服
        btn_custom_serivce.setOnClickListener {
            val serviceUrl = sConfigData?.customerServiceUrl
            val serviceUrl2 = sConfigData?.customerServiceUrl2
            when {
                !serviceUrl.isNullOrBlank() && !serviceUrl2.isNullOrBlank() -> {
                    ServiceDialog().show(childFragmentManager, null)
                }
                serviceUrl.isNullOrBlank() && !serviceUrl2.isNullOrBlank() -> {
                    JumpUtil.toExternalWeb(requireContext(), serviceUrl2)
                }
                !serviceUrl.isNullOrBlank() && serviceUrl2.isNullOrBlank() -> {
                    JumpUtil.toExternalWeb(requireContext(), serviceUrl)
                }
            }
        }


        //关于我们
        btn_about_us.setOnClickListener {
            startActivity(Intent(requireActivity(), AboutMeActivity::class.java))
        }

        //资产检测

    }

    // TODO 跳轉Promotion 20220108新增 by Hewie
    private fun toProfileCenter() {
        JumpUtil.toInternalWeb(
            requireContext(),
            Constants.getPromotionUrl(
                viewModel.token,
                LanguageManager.getSelectLanguage(requireContext())
            ),
            getString(R.string.promotion)
        )
    }

    /*private fun initServiceButton() {
        btn_floating_service.setView(this)
    }*/
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
                tv_account_balance.text = TextUtil.format(it)
            }
        }
          viewModel.isWithdrawShowVerifyDialog.observe(this) {
            it.getContentIfNotHandled()?.let { b ->
                if (b)
                    showKYCVerifyDialog()
                else
                    viewModel.checkWithdrawSystem()
            }
        }
        viewModel.userInfo.observe(viewLifecycleOwner) {
            updateUI(it)
        }

        viewModel.navPublicityPage.observe(viewLifecycleOwner) {
//            GamePublicityActivity.reStart(requireContext())
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
                } else {
                    viewModel.checkProfileInfoComplete()
                }
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
                    customSecurityDialog = CustomSecurityDialog(requireContext()).apply {
                        getSecurityCodeClickListener {
                            this.showSmeTimer300()
                            viewModel.sendTwoFactor()
                        }
                        positiveClickListener =
                            CustomSecurityDialog.PositiveClickListener { number ->
                                viewModel.validateTwoFactor(ValidateTwoFactorRequest(number))
                            }
                    }
                    customSecurityDialog?.show(childFragmentManager, null)
                }
            }
        }

        viewModel.errorMessageDialog.observe(viewLifecycleOwner) {
            val errorMsg = it ?: getString(R.string.unknown_error)
            CustomAlertDialog(requireContext()).apply {
                setMessage(errorMsg)
                setNegativeButtonText(null)
                setCanceledOnTouchOutside(false)
                setCancelable(false)
            }.show(childFragmentManager, null)
        }

        viewModel.twoFactorSuccess.observe(viewLifecycleOwner) {
            if (it == true)
                customSecurityDialog?.dismiss()
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
        viewModel.infoCenterRepository.unreadNoticeList.observe(viewLifecycleOwner) {
            updateNoticeCount(it.size)
        }

        viewModel.userInfo.observe(viewLifecycleOwner) {
            //是否测试用户（0-正常用户，1-游客，2-内部测试）
            updateUserIdentity(it?.testFlag)
        }


        viewModel.isWithdrawShowVerifyDialog.observe(this) {
            it.getContentIfNotHandled()?.let { b ->
                if (b)
                    showKYCVerifyDialog()
                else
                    viewModel.checkWithdrawSystem()
            }
        }

        viewModel.isRechargeShowVerifyDialog.observe(this) {
            it.getContentIfNotHandled()?.let { b ->
                if (b)
                    showKYCVerifyDialog()
                else
                    viewModel.checkRechargeSystem()
            }
        }

    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(userInfo: UserInfo?) {
        Glide.with(this)
            .load(userInfo?.iconUrl)
            .apply(RequestOptions().placeholder(R.drawable.ic_person_avatar))
            .into(iv_head1) //載入頭像

        tv_user_nickname.text = if (userInfo?.nickName.isNullOrEmpty()) {
            userInfo?.userName
        } else {
            userInfo?.nickName
        }

        btn_edit_nickname.visibility =
            if (userInfo?.setted == FLAG_NICKNAME_IS_SET) View.GONE else View.VISIBLE
        tv_user_id.text = userInfo?.userId?.toString()
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
        btn_account_transfer.visibility =
            if (thirdOpen && !thirdTransferOpen) View.VISIBLE else View.GONE

        //   btn_other_bet_record.visibility = if (!thirdOpen) View.GONE else View.VISIBLE
        btn_member_level.visibility = View.GONE //if (!thirdOpen) View.GONE else View.VISIBLE
    }

    private fun updateCreditAccountUI() {
        lin_wallet_operation.setVisibilityByCreditSystem()
    }

    //有 child activity 給定 notice button 顯示
    fun setupNoticeButton() {
        iv_user_notice.setOnClickListener {
            startActivity(
                Intent(requireActivity(), InfoCenterActivity::class.java)
                    .putExtra(InfoCenterActivity.KEY_READ_PAGE, InfoCenterActivity.YET_READ)
            )
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
        iv_circle?.visibility =
            (if (noticeCount ?: 0 > 0 && isGuest == false) View.VISIBLE else View.GONE)
    }
    //实名验证
    private fun showKYCVerifyDialog() {
        VerifyIdentityDialog().apply {
            positiveClickListener = VerifyIdentityDialog.PositiveClickListener { number ->
                startActivity(Intent(context, VerifyIdentityActivity::class.java))
            }
            serviceClickListener = VerifyIdentityDialog.PositiveClickListener { number ->
                val serviceUrl = sConfigData?.customerServiceUrl
                val serviceUrl2 = sConfigData?.customerServiceUrl2
                when {
                    !serviceUrl.isNullOrBlank() && !serviceUrl2.isNullOrBlank() -> {
                        activity?.supportFragmentManager?.let { it1 ->
                            ServiceDialog().show(
                                it1,
                                null
                            )
                        }
                    }
                    serviceUrl.isNullOrBlank() && !serviceUrl2.isNullOrBlank() -> {
                        activity?.let { it1 -> JumpUtil.toExternalWeb(it1, serviceUrl2) }
                    }
                    !serviceUrl.isNullOrBlank() && serviceUrl2.isNullOrBlank() -> {
                        activity?.let { it1 -> JumpUtil.toExternalWeb(it1, serviceUrl) }
                    }
                }
            }
        }.show(childFragmentManager, null)
    }
}