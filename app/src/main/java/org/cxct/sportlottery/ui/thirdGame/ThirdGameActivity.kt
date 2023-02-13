package org.cxct.sportlottery.ui.thirdGame

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Handler
import androidx.lifecycle.viewModelScope
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ImmersionBar
import kotlinx.android.synthetic.main.activity_third_game.*
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.db.entity.UserInfo
import org.cxct.sportlottery.event.MoneyEvent
import org.cxct.sportlottery.network.withdraw.uwcheck.ValidateTwoFactorRequest
import org.cxct.sportlottery.repository.TestFlag
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.common.CustomSecurityDialog
import org.cxct.sportlottery.ui.common.WebActivity
import org.cxct.sportlottery.ui.game.ServiceDialog
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.money.recharge.MoneyRechargeActivity
import org.cxct.sportlottery.ui.profileCenter.changePassword.SettingPasswordActivity
import org.cxct.sportlottery.ui.profileCenter.identity.VerifyIdentityActivity
import org.cxct.sportlottery.ui.profileCenter.identity.VerifyIdentityDialog
import org.cxct.sportlottery.ui.profileCenter.profile.ProfileActivity
import org.cxct.sportlottery.ui.withdraw.BankActivity
import org.cxct.sportlottery.ui.withdraw.WithdrawActivity
import org.cxct.sportlottery.util.EventBusUtil
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.ToastUtil

open class ThirdGameActivity : WebActivity() {

    private var mUserInfo: UserInfo? = null

    //簡訊驗證彈窗
    private var customSecurityDialog: CustomSecurityDialog? = null
    //KYC驗證彈窗
    private var kYCVerifyDialog: CustomSecurityDialog? = null

    private val mGameCategoryCode: String? by lazy { intent?.getStringExtra(GAME_CATEGORY_CODE) }

    private var isCashSave = false

    override fun init() {
        setupActivityOrientation()
        disableSystemUI()
        setContentView(R.layout.activity_third_game)
        setCookie()
        setupWebView(web_view)
        loadUrl(web_view)
        setupMenu()
        initObserve()
    }

    private fun disableSystemUI() {
        ImmersionBar.with(this).hideBar(BarHide.FLAG_HIDE_BAR).init()
    }

    override fun onBackPressed() {
        if (web_view.canGoBack()) {
            super.onBackPressed()
        } else {
            viewModel.viewModelScope.launch {
                viewModel.allTransferOut()
                super.onBackPressed()
                Handler().postDelayed({
                    EventBusUtil.post(MoneyEvent(true))
                }, 2000)
            }
        }
    }

    private fun setupActivityOrientation() {
        when (mGameCategoryCode) {
            ThirdGameCategory.QP.name -> {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
        }
    }

    private fun setupMenu() {
        motion_menu.setOnMenuListener(object : MotionFloatingMenu.OnMenuListener {
            override fun onHome() {
                onBackPressed()
            }

            override fun onCashSave() {
                if (checkLogin()) {
                    viewModel.checkRechargeKYCVerify()
                }
            }

            override fun onCashGet() {
                if (checkLogin()) {
                    avoidFastDoubleClick()
                    viewModel.checkWithdrawKYCVerify()
                }
            }
        })
    }


    private fun checkLogin(): Boolean {
        return when (mUserInfo?.testFlag) {
            TestFlag.NORMAL.index -> true
            TestFlag.TEST.index -> true // TODO 20221208 增加了內部測試選項
            TestFlag.GUEST.index -> {
                ToastUtil.showToastInCenter(this, resources.getString(R.string.message_guest_no_permission))
                false
            }
            else -> {
                startActivity(Intent(this, LoginActivity::class.java))
                false
            }
        }
    }

    private fun initObserve() {
        viewModel.userInfo.observe(this, {
            mUserInfo = it
        })

        viewModel.withdrawSystemOperation.observe(this, {
            val operation = it.getContentIfNotHandled()
            if (operation == false) {
                showPromptDialog(getString(R.string.prompt), getString(R.string.message_withdraw_maintain)) {}
            }
        })

        viewModel.rechargeSystemOperation.observe(this, {
            it.getContentIfNotHandled()?.let { b ->
                if (b) {
                    startActivity(Intent(this, MoneyRechargeActivity::class.java))
                } else {
                    showPromptDialog(getString(R.string.prompt), getString(R.string.message_recharge_maintain)) {}
                }
            }
        })

        viewModel.needToUpdateWithdrawPassword.observe(this, {
            it.getContentIfNotHandled()?.let { b ->
                if (b) {
                    showPromptDialog(getString(R.string.withdraw_setting), getString(R.string.please_setting_withdraw_password), getString(R.string.go_to_setting),true) {
                        startActivity(Intent(this, SettingPasswordActivity::class.java).apply { putExtra(
                            SettingPasswordActivity.PWD_PAGE, SettingPasswordActivity.PwdPage.BANK_PWD) })
                    }
                } else {
                    viewModel.checkProfileInfoComplete()
                }
            }
        })

        viewModel.needToCompleteProfileInfo.observe(this, {
            it.getContentIfNotHandled()?.let { b ->
                if (b) {
                    showPromptDialog(getString(R.string.withdraw_setting), getString(R.string.please_complete_profile_info), getString(R.string.go_to_setting),true) {
                        startActivity(Intent(this, ProfileActivity::class.java))
                    }
                } else {
                    viewModel.checkBankCardPermissions()
                }
            }
        })

        viewModel.needToBindBankCard.observe(this, {
            it.getContentIfNotHandled()?.let { messageId ->
                if (messageId != -1) {
                    showPromptDialog(getString(R.string.withdraw_setting), getString(messageId), getString(R.string.go_to_setting),  true) {
                        startActivity(Intent(this, BankActivity::class.java))
                    }
                } else {
                    startActivity(Intent(this, WithdrawActivity::class.java))
                }
            }
        })

        viewModel.settingNeedToUpdateWithdrawPassword.observe(this, {
            it.getContentIfNotHandled()?.let { b ->
                if (b) {
                    showPromptDialog(getString(R.string.withdraw_setting), getString(R.string.please_setting_withdraw_password), getString(R.string.go_to_setting),true) {
                        startActivity(Intent(this, SettingPasswordActivity::class.java).apply { putExtra(
                            SettingPasswordActivity.PWD_PAGE, SettingPasswordActivity.PwdPage.BANK_PWD) })
                    }
                } else if (!b) {
                    startActivity(Intent(this, BankActivity::class.java))
                }
            }
        })

        viewModel.settingNeedToCompleteProfileInfo.observe(this, {
            it.getContentIfNotHandled()?.let { b ->
                if (b) {
                    showPromptDialog(getString(R.string.withdraw_setting), getString(R.string.please_complete_profile_info), getString(R.string.go_to_setting),true) {
                        startActivity(Intent(this, ProfileActivity::class.java))
                    }
                } else if (!b) {
                    startActivity(Intent(this, BankActivity::class.java))
                }
            }
        })

        viewModel.needToSendTwoFactor.observe(this) {
            it.getContentIfNotHandled()?.let { b ->
                if (b) {
                        customSecurityDialog = CustomSecurityDialog(this).apply {
                            getSecurityCodeClickListener {
                                this.showSmeTimer300()
                                viewModel.sendTwoFactor()
                            }
                            positiveClickListener = CustomSecurityDialog.PositiveClickListener { number ->
                                viewModel.validateTwoFactor(ValidateTwoFactorRequest(number))
                            }
                        }
                        customSecurityDialog?.show(supportFragmentManager, null)

                }
            }
        }

        //確認收到簡訊驗證碼
        viewModel.twoFactorResult.observe(this) {
            //傳送驗證碼成功後才能解鎖提交按鈕
            customSecurityDialog?.setPositiveBtnClickable(it?.success ?: false)
            sConfigData?.hasGetTwoFactorResult = true
        }

        //簡訊驗證成功
        viewModel.twoFactorSuccess.observe(this) {
            if (it == true)
                customSecurityDialog?.dismiss()
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
}
