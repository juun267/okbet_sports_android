package org.cxct.sportlottery.ui.profileCenter

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.listener.OnResultCallbackListener
import kotlinx.android.synthetic.main.activity_profile_center.*
import kotlinx.android.synthetic.main.content_bet_info_item_v3.view.*
import kotlinx.android.synthetic.main.view_bottom_navigation_sport.*
import kotlinx.android.synthetic.main.view_nav_right.*
import kotlinx.android.synthetic.main.view_toolbar_main.*
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.db.entity.UserInfo
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.bet.add.betReceipt.Receipt
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.uploadImg.UploadImgRequest
import org.cxct.sportlottery.network.withdraw.uwcheck.ValidateTwoFactorRequest
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseBottomNavActivity
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.common.CustomSecurityDialog
import org.cxct.sportlottery.ui.finance.FinanceActivity
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.game.ServiceDialog
import org.cxct.sportlottery.ui.game.betList.BetListFragment
import org.cxct.sportlottery.ui.game.language.SwitchLanguageActivity
import org.cxct.sportlottery.ui.game.publicity.GamePublicityActivity
import org.cxct.sportlottery.ui.helpCenter.HelpCenterActivity
import org.cxct.sportlottery.ui.infoCenter.InfoCenterActivity
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterOkActivity
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.menu.ChangeLanguageDialog
import org.cxct.sportlottery.ui.menu.ChangeOddsTypeDialog
import org.cxct.sportlottery.ui.menu.MenuFragment
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.ui.money.recharge.MoneyRechargeActivity
import org.cxct.sportlottery.ui.profileCenter.changePassword.SettingPasswordActivity
import org.cxct.sportlottery.ui.profileCenter.changePassword.SettingPasswordActivity.Companion.PWD_PAGE
import org.cxct.sportlottery.ui.profileCenter.identity.VerifyIdentityActivity
import org.cxct.sportlottery.ui.profileCenter.identity.VerifyIdentityDialog
import org.cxct.sportlottery.ui.profileCenter.money_transfer.MoneyTransferActivity
import org.cxct.sportlottery.ui.profileCenter.nickname.ModifyProfileInfoActivity
import org.cxct.sportlottery.ui.profileCenter.nickname.ModifyType
import org.cxct.sportlottery.ui.profileCenter.otherBetRecord.OtherBetRecordActivity
import org.cxct.sportlottery.ui.profileCenter.profile.AvatarSelectorDialog
import org.cxct.sportlottery.ui.profileCenter.profile.ProfileActivity
import org.cxct.sportlottery.ui.profileCenter.timezone.TimeZoneActivity
import org.cxct.sportlottery.ui.results.ResultsSettlementActivity
import org.cxct.sportlottery.ui.selflimit.SelfLimitActivity
import org.cxct.sportlottery.ui.vip.VipActivity
import org.cxct.sportlottery.ui.withdraw.BankActivity
import org.cxct.sportlottery.ui.withdraw.WithdrawActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.TextUtil.formatMoneyNoDecimal
import org.cxct.sportlottery.util.TimeUtil.getRemainDay
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException

/**
 * @app_destination 個人中心 旧版
 */
class ProfileCenterActivity :
    BaseBottomNavActivity<ProfileCenterViewModel>(ProfileCenterViewModel::class) {
    //簡訊驗證彈窗
    private var customSecurityDialog: CustomSecurityDialog? = null

    //KYC驗證彈窗
    private var kYCVerifyDialog: CustomSecurityDialog? = null
    private var betListFragment = BetListFragment()

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
                    this@ProfileCenterActivity,
                    getString(R.string.error_reading_file)
                )
            }
        }

        override fun onCancel() {
            Timber.i("PictureSelector Cancel")
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusbar(R.color.color_232C4F_FFFFFF,true)
        setContentView(R.layout.activity_profile_center)
        initToolBar()
        initBottomNavigation()
        initMenu()
        setupNoticeButton(iv_notice)
        initView()
        setupHeadButton()
        setupEditNickname()
        setupBalance()
        setupRechargeButton()
        setupWithdrawButton()
        setupLogout()
        setupMoreButtons()
        initBottomNav()
        getUserInfo()
        initObserve()
        updateThirdOpenUI()
        updateCreditAccountUI()
    }

    override fun initToolBar() {
        iv_logo.setImageResource(R.drawable.ic_logo)
        iv_logo.setOnClickListener {
            viewModel.navMainPage(ThirdGameCategory.MAIN)
        }

        iv_language.setImageResource(LanguageManager.getLanguageFlag(this))

        //頭像 當 側邊欄 開/關
        iv_menu.setOnClickListener {
            if (drawer_layout.isDrawerOpen(nav_right)) drawer_layout.closeDrawers()
            else {
                drawer_layout.openDrawer(nav_right)
                viewModel.getMoney()
            }
        }

        btn_login.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        btn_register.setOnClickListener {
            startActivity(Intent(this, RegisterOkActivity::class.java))
        }

        tv_odds_type.setOnClickListener {
            ChangeOddsTypeDialog().show(supportFragmentManager, null)
        }

        iv_language.setOnClickListener {
            ChangeLanguageDialog(ChangeLanguageDialog.ClearBetListListener {
                viewModel.betInfoRepository.clear()
            }).show(supportFragmentManager, null)
        }
    }

    private fun initView() {
        tv_currency_type.text = sConfigData?.systemCurrencySign
        //信用盤打開，隱藏提款設置
        btn_withdrawal_setting.setVisibilityByCreditSystem()
        //優惠活動
        btn_promotion.setVisibilityByCreditSystem()
        btn_affiliate.setVisibilityByCreditSystem()
        btn_feedback.setVisibilityByCreditSystem()
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
        tv_language.text = LanguageManager.getLanguageStringResource(this)
        iv_flag.setImageResource(LanguageManager.getLanguageFlag(this))
        getMoney()
    }

    private fun setupHeadButton() {
        iv_head.setOnClickListener {
            AvatarSelectorDialog(this, mSelectMediaListener).show(supportFragmentManager, null)
        }

    }

    private fun setupEditNickname() {
        btn_edit_nickname.setOnClickListener {
            startActivity(
                Intent(
                    this@ProfileCenterActivity,
                    ModifyProfileInfoActivity::class.java
                ).apply {
                    putExtra(ModifyProfileInfoActivity.MODIFY_INFO, ModifyType.NickName)
                })
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
        refreshMoneyLoading()
        viewModel.getMoney()
    }

    private fun refreshMoneyLoading() {
        btn_refresh_money.visibility = View.GONE
    }

    private fun refreshMoneyHideLoading() {
        btn_refresh_money.visibility = View.VISIBLE
    }

    private fun setupLogout() {
        iv_logout.setOnClickListener {
            viewModel.doLogoutAPI()
            viewModel.doLogoutCleanUser {
                run {
//                    if (sConfigData?.thirdOpen == FLAG_OPEN)
//                        MainActivity.reStart(this)
//                    else
                    GamePublicityActivity.reStart(this)
                }
            }
        }
    }

    private fun setupMoreButtons() {
        //個人資訊
        btn_profile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        //額度轉換
        btn_account_transfer.setOnClickListener {
            startActivity(Intent(this, MoneyTransferActivity::class.java))
        }

        //提款設置
        btn_withdrawal_setting.setOnClickListener {
            viewModel.settingCheckPermissions()
        }

        //其他投注記錄
        btn_other_bet_record.setOnClickListener {
            startActivity(Intent(this, OtherBetRecordActivity::class.java))
        }

        //資金明細
        btn_fund_detail.setOnClickListener {
            startActivity(Intent(this, FinanceActivity::class.java))
        }

        //消息中心
        btn_news_center.setOnClickListener {
            startActivity(Intent(this, InfoCenterActivity::class.java))
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
                        this,
                        getString(R.string.message_guest_no_permission)
                    )
                }
            }
        }
        //代理加盟
        btn_affiliate.setOnClickListener {
            JumpUtil.toInternalWeb(
                this,
                Constants.getAffiliateUrl(this),
                resources.getString(R.string.btm_navigation_affiliate)
            )
        }
        //会员等级
        btn_member_level.setOnClickListener {
            startActivity(Intent(this, VipActivity::class.java))
        }
        //自我約束
        if (sConfigData?.selfRestraintVerified == "0" || sConfigData?.selfRestraintVerified == null) {
            btn_self_limit.visibility = View.GONE
        } else {
            btn_self_limit.visibility = View.VISIBLE
            btn_self_limit.setOnClickListener {
                startActivity(Intent(this, SelfLimitActivity::class.java))
            }
        }
        //赛果结算
        btn_game_settlement.setOnClickListener {
            startActivity(Intent(this, ResultsSettlementActivity::class.java))
        }
        //游戏规则
        btn_game_rule.setOnClickListener {
            JumpUtil.toInternalWeb(
                this,
                Constants.getGameRuleUrl(this),
                getString(R.string.game_rule)
            )
        }
        //切换语言
        btn_language.setOnClickListener {
            var intent = Intent(this, SwitchLanguageActivity::class.java);
            intent.putExtra("type", 1)
            startActivity(intent)
        }
        //外觀
        btn_appearance.setOnClickListener {
            startActivity(Intent(this, AppearanceActivity::class.java))
        }
//        btn_time_zone.visibility = View.GONE
        //时区切换
        btn_time_zone.setOnClickListener {
            startActivity(Intent(this, TimeZoneActivity::class.java))
        }

        //幫助中心
        btn_help_center.setOnClickListener {
            startActivity(Intent(this, HelpCenterActivity::class.java))
        }


        //联系客服
        btn_custom_serivce.setOnClickListener {
            val serviceUrl = sConfigData?.customerServiceUrl
            val serviceUrl2 = sConfigData?.customerServiceUrl2
            when {
                !serviceUrl.isNullOrBlank() && !serviceUrl2.isNullOrBlank() -> {
                    ServiceDialog().show(supportFragmentManager, null)
                }
                serviceUrl.isNullOrBlank() && !serviceUrl2.isNullOrBlank() -> {
                    JumpUtil.toExternalWeb(this, serviceUrl2)
                }
                !serviceUrl.isNullOrBlank() && serviceUrl2.isNullOrBlank() -> {
                    JumpUtil.toExternalWeb(this, serviceUrl)
                }
            }
        }
        //关于我们
        btn_about_us.setOnClickListener {
            JumpUtil.toInternalWeb(
                this,
                Constants.getAboutUsUrl(this),
                getString(R.string.about_us)
            )
        }
    }

    // TODO 跳轉Promotion 20220108新增 by Hewie
    private fun toProfileCenter() {
        JumpUtil.toInternalWeb(
            this,
            Constants.getPromotionUrl(
                viewModel.token,
                LanguageManager.getSelectLanguage(this@ProfileCenterActivity)
            ),
            getString(R.string.promotion)
        )
    }

    private fun initBottomNav() {
        bottom_nav_view.selectedItemId = R.id.my_account_page
        bottom_nav_view.setOnNavigationItemSelectedListener {
            //20200303 紀錄：跳轉其他 Activity 頁面，不需要切換 BottomNav 選取狀態
            when (it.itemId) {
                R.id.home_page -> {
//                    startActivity(Intent(this, MainActivity::class.java))
                    GamePublicityActivity.reStart(this)
                    false
                }
                R.id.game_page -> {
                    startActivity(Intent(this, GameActivity::class.java))
                    false
                }
                R.id.promotion_page -> {
                    JumpUtil.toInternalWeb(
                        this,
                        Constants.getPromotionUrl(
                            viewModel.token,
                            LanguageManager.getSelectLanguage(this@ProfileCenterActivity)
                        ),
                        getString(R.string.promotion)
                    )
                    false
                }
                R.id.chat_page -> {
                    false
                }
                R.id.my_account_page -> {
                    when (viewModel.isLogin.value) {
                        true -> { //登入
                            startActivity(Intent(this, ProfileCenterActivity::class.java))
                        }
                        else -> { //尚未登入
                            startActivity(Intent(this, RegisterOkActivity::class.java))
                        }
                    }
                    true
                }
                else -> false
            }
        }

        //聊天室按鈕 啟用判斷
        bottom_nav_view.menu.findItem(R.id.chat_page).isVisible = sConfigData?.chatOpen == FLAG_OPEN
    }

    /*private fun initServiceButton() {
        btn_floating_service.setView(this)
    }*/

    private fun getUserInfo() {
        viewModel.getUserInfo()
    }

    private fun initObserve() {
        viewModel.userMoney.observe(this) {
            it?.let {
                refreshMoneyHideLoading()
                tv_account_balance.text = TextUtil.format(it)
            }
        }

        viewModel.userInfo.observe(this) {
            updateUI(it)
        }

        viewModel.navPublicityPage.observe(this) {
            GamePublicityActivity.reStart(this)
        }

        viewModel.lockMoney.observe(this) {
            if (it?.toInt()!! > 0) {
                ivNotice.visibility = View.VISIBLE
                ivNotice.setOnClickListener { view ->
                    val depositSpannable =
                        SpannableString(
                            getString(
                                R.string.text_security_money,
                                formatMoneyNoDecimal(it)
                            )
                        )
                    val daysLeftText = getString(
                        R.string.text_security_money2,
                        getRemainDay(viewModel.userInfo.value?.uwEnableTime).toString()
                    )
                    val remainDaySpannable = SpannableString(daysLeftText)
                    val remainDay = getRemainDay(viewModel.userInfo.value?.uwEnableTime).toString()
                    val remainDayStartIndex = daysLeftText.indexOf(remainDay)
                    remainDaySpannable.setSpan(
                        ForegroundColorSpan(
                            ContextCompat.getColor(this, R.color.color_317FFF_1053af)
                        ),
                        remainDayStartIndex,
                        remainDayStartIndex + remainDay.length, 0
                    )

                    SecurityDepositDialog().apply {
                        this.depositText = depositSpannable
                        this.daysLeftText = remainDaySpannable
                    }.show(supportFragmentManager, this::class.java.simpleName)
                }
            } else {
                ivNotice.visibility = View.GONE
            }
        }

        viewModel.withdrawSystemOperation.observe(this) {
            val operation = it.getContentIfNotHandled()
            if (operation == false) {
                showPromptDialog(
                    getString(R.string.prompt),
                    getString(R.string.message_withdraw_maintain)
                ) {}
            }
        }

        viewModel.rechargeSystemOperation.observe(this) {
            it.getContentIfNotHandled()?.let { b ->
                if (b) {
                    startActivity(Intent(this, MoneyRechargeActivity::class.java))
                } else {
                    showPromptDialog(
                        getString(R.string.prompt),
                        getString(R.string.message_recharge_maintain)
                    ) {}
                }
            }
        }

        viewModel.needToUpdateWithdrawPassword.observe(this) {
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
                                this,
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

        viewModel.needToCompleteProfileInfo.observe(this) {
            it.getContentIfNotHandled()?.let { b ->
                if (b) {
                    showPromptDialog(
                        getString(R.string.withdraw_setting),
                        getString(R.string.please_complete_profile_info),
                        getString(R.string.go_to_setting),
                        true
                    ) {
                        startActivity(Intent(this, ProfileActivity::class.java))
                    }
                } else {
                    viewModel.checkBankCardPermissions()
                }
            }
        }

        viewModel.needToBindBankCard.observe(this) {
            it.getContentIfNotHandled()?.let { messageId ->
                if (messageId != -1) {
                    showPromptDialog(
                        getString(R.string.withdraw_setting),
                        getString(messageId),
                        getString(R.string.go_to_setting),
                        true
                    ) {
                        startActivity(Intent(this, BankActivity::class.java))
                    }
                } else {
                    startActivity(Intent(this, WithdrawActivity::class.java))
                }
            }
        }

        viewModel.needToSendTwoFactor.observe(this) {
            it.getContentIfNotHandled()?.let { b ->
                if (b) {
                    customSecurityDialog = CustomSecurityDialog(this).apply {
                        getSecurityCodeClickListener {
                            this.showSmeTimer300()
                            viewModel.sendTwoFactor()
                        }
                        positiveClickListener =
                            CustomSecurityDialog.PositiveClickListener { number ->
                                viewModel.validateTwoFactor(ValidateTwoFactorRequest(number))
                            }
                    }
                    customSecurityDialog?.show(supportFragmentManager, null)
                }
            }
        }

        viewModel.errorMessageDialog.observe(this) {
            val errorMsg = it ?: getString(R.string.unknown_error)
            CustomAlertDialog(this).apply {
                setMessage(errorMsg)
                setNegativeButtonText(null)
                setCanceledOnTouchOutside(false)
                setCancelable(false)
            }.show(supportFragmentManager, null)
        }

        viewModel.twoFactorSuccess.observe(this) {
            if (it == true)
                customSecurityDialog?.dismiss()
        }

        viewModel.twoFactorResult.observe(this) {
            //傳送驗證碼成功後才能解鎖提交按鈕
            customSecurityDialog?.setPositiveBtnClickable(it?.success ?: false)
            sConfigData?.hasGetTwoFactorResult = true
        }

        //使用者沒有電話號碼
        viewModel.showPhoneNumberMessageDialog.observe(this) {
            it.getContentIfNotHandled()?.let { b ->
                if (!b) phoneNumCheckDialog(this, supportFragmentManager)
            }
        }

        viewModel.settingNeedToUpdateWithdrawPassword.observe(this) {
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
                                this,
                                SettingPasswordActivity::class.java
                            ).apply {
                                putExtra(
                                    PWD_PAGE,
                                    SettingPasswordActivity.PwdPage.BANK_PWD
                                )
                            })
                    }
                } else if (!b) {
                    startActivity(Intent(this, BankActivity::class.java))
                }
            }
        }

        viewModel.settingNeedToCompleteProfileInfo.observe(this) {
            it.getContentIfNotHandled()?.let { b ->
                if (b) {
                    showPromptDialog(
                        getString(R.string.withdraw_setting),
                        getString(R.string.please_complete_profile_info),
                        getString(R.string.go_to_setting),
                        true
                    ) {
                        startActivity(Intent(this, ProfileActivity::class.java))
                    }
                } else if (!b) {
                    startActivity(Intent(this, BankActivity::class.java))
                }
            }
        }

        viewModel.editIconUrlResult.observe(this) {
            val iconUrlResult = it?.getContentIfNotHandled()
            if (iconUrlResult?.success == true)
                showPromptDialog(
                    getString(R.string.prompt),
                    getString(R.string.save_avatar_success)
                ) {}
            else
                iconUrlResult?.msg?.let { msg -> showErrorPromptDialog(msg) {} }
        }

        viewModel.intoWithdraw.observe(this) {
            it.getContentIfNotHandled()?.let {
                startActivity(Intent(this, WithdrawActivity::class.java))
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
            .apply(RequestOptions().placeholder(R.drawable.img_avatar_default))
            .into(iv_head1) //載入頭像

        tv_user_nickname.text = if (userInfo?.nickName.isNullOrEmpty()) {
            userInfo?.userName
        } else {
            userInfo?.nickName
        }

        btn_edit_nickname.visibility =
            if (userInfo?.setted == FLAG_NICKNAME_IS_SET) View.GONE else View.VISIBLE
        tv_user_id.text = userInfo?.userId?.toString()
        if (getRemainDay(userInfo?.uwEnableTime) > 0) {
            ivNotice.visibility = View.VISIBLE
            ivNotice.setOnClickListener {
                viewModel.getLockMoney()
            }
        } else {
            ivNotice.visibility = View.GONE
        }
    }

    private fun uploadImg(file: File) {
        val userId = viewModel.userInfo.value?.userId.toString()
        val uploadImgRequest =
            UploadImgRequest(userId, file, UploadImgRequest.PlatformCodeType.AVATAR)
        viewModel.uploadImage(uploadImgRequest)
    }

    private fun updateThirdOpenUI() {
        val thirdOpen = sConfigData?.thirdOpen == FLAG_OPEN
        // 暫時隱藏入口 by Bee
        if (sConfigData?.creditSystem == FLAG_CREDIT_OPEN || baseContext.getString(R.string.app_name) != "OKBET") {
            btn_account_transfer.visibility = if (!thirdOpen) View.GONE else View.VISIBLE
        } else {
            btn_account_transfer.visibility = /*if (!thirdOpen)*/ View.GONE /*else View.VISIBLE*/
        }
        btn_other_bet_record.visibility = if (!thirdOpen) View.GONE else View.VISIBLE
        btn_member_level.visibility = View.GONE //if (!thirdOpen) View.GONE else View.VISIBLE
        bottom_nav_view.visibility = if (!thirdOpen) View.GONE else View.VISIBLE
    }

    private fun updateCreditAccountUI() {
        lin_wallet_operation.setVisibilityByCreditSystem()
    }

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
        }.show(supportFragmentManager, null)
    }

    override fun clickMenuEvent() {
        if (drawer_layout.isDrawerOpen(nav_right)) drawer_layout.closeDrawers()
        else {
            drawer_layout.openDrawer(nav_right)
            viewModel.getMoney()
        }
    }

    override fun initMenu() {
        try {
            //關閉側邊欄滑動行為
            drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

            //選單選擇結束要收起選單
            val menuFrag =
                supportFragmentManager.findFragmentById(R.id.fragment_menu) as MenuFragment
            menuFrag.setDownMenuListener { drawer_layout.closeDrawers() }
            nav_right.layoutParams.width = MetricsUtil.getMenuWidth() //動態調整側邊欄寬

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun initBottomNavigation() {
        sport_bottom_navigation.apply {
            setNavigationItemClickListener {
                when (it) {
                    R.id.navigation_home -> {
                        viewModel.navHome()
                        finish()
                        false
                    }
                    R.id.navigation_sport -> {
                        viewModel.navGame()
                        finish()
                        false
                    }
                    R.id.navigation_account_history -> {
                        viewModel.navAccountHistory()
                        finish()
                        false
                    }
                    R.id.navigation_transaction_status -> {
                        viewModel.navTranStatus()
                        finish()
                        false
                    }
                    R.id.navigation_my -> {
                        true
                    }
                    else -> false
                }
            }

            setSelected(R.id.navigation_my)
        }
    }

    override fun onBackPressed() {
        //返回鍵優先關閉投注單fragment
        if (supportFragmentManager.backStackEntryCount != 0) {
            for (i in 0 until supportFragmentManager.backStackEntryCount) {
                supportFragmentManager.popBackStack()
            }
            return
        }
        super.onBackPressed()
    }

    override fun showBetListPage() {
        betListFragment =
            BetListFragment.newInstance(object : BetListFragment.BetResultListener {
                override fun onBetResult(
                    betResultData: Receipt?,
                    betParlayList: List<ParlayOdd>,
                    isMultiBet: Boolean
                ) {
                    showBetReceiptDialog(betResultData, betParlayList, isMultiBet, R.id.fl_bet_list)
                }

            })

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.push_bottom_to_top_enter,
                R.anim.pop_bottom_to_top_exit,
                R.anim.push_bottom_to_top_enter,
                R.anim.pop_bottom_to_top_exit
            )
            .add(R.id.fl_bet_list, betListFragment)
            .addToBackStack(BetListFragment::class.java.simpleName)
            .commit()
    }

    override fun getBetListPageVisible(): Boolean {
        return betListFragment.isVisible
    }

    override fun updateBetListCount(num: Int) {
        sport_bottom_navigation.setBetCount(num)
    }
    override fun updateBetListOdds(list: MutableList<BetInfoListData>) {
        val multipleOdds = getMultipleOdds(list)
        cl_bet_list_bar.tvOdds.text = multipleOdds
    }

    override fun showLoginNotify() {
        snackBarLoginNotify.apply {
            setAnchorView(R.id.my_profile_bottom_navigation)
            show()
        }
    }

    override fun showMyFavoriteNotify(myFavoriteNotifyType: Int) {
        setSnackBarMyFavoriteNotify(myFavoriteNotifyType)
        snackBarMyFavoriteNotify?.apply {
            setAnchorView(R.id.my_profile_bottom_navigation)
            show()
        }
    }

    override fun updateUiWithLogin(isLogin: Boolean) {
        if (isLogin) {
            btn_login.visibility = View.GONE
            iv_menu.visibility = View.VISIBLE
            iv_notice.visibility = View.VISIBLE
            btn_register.visibility = View.GONE
            toolbar_divider.visibility = View.GONE
            iv_head.visibility = View.GONE
            tv_odds_type.visibility = View.GONE
        } else {
            btn_login.visibility = View.VISIBLE
            btn_register.visibility = View.VISIBLE
            toolbar_divider.visibility = View.VISIBLE
            iv_head.visibility = View.GONE
            tv_odds_type.visibility = View.GONE
            iv_menu.visibility = View.GONE
            iv_notice.visibility = View.GONE
        }
    }

    override fun updateOddsType(oddsType: OddsType) {
        tv_odds_type.text = getString(oddsType.res)
    }

    override fun navOneSportPage(thirdGameCategory: ThirdGameCategory?) {
        if (thirdGameCategory != null) {
            val intent = Intent(this, MainActivity::class.java)
                .putExtra(MainActivity.ARGS_THIRD_GAME_CATE, thirdGameCategory)
            startActivity(intent)

            return
        }

        startActivity(Intent(this, GamePublicityActivity::class.java))
    }

}