package org.cxct.sportlottery.ui.thirdGame

import android.content.Intent
import kotlinx.android.synthetic.main.activity_third_game.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.db.entity.UserInfo
import org.cxct.sportlottery.repository.TestFlag
import org.cxct.sportlottery.ui.common.WebActivity
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.money.recharge.MoneyRechargeActivity
import org.cxct.sportlottery.ui.profileCenter.changePassword.SettingPasswordActivity
import org.cxct.sportlottery.ui.profileCenter.profile.ProfileActivity
import org.cxct.sportlottery.ui.withdraw.BankActivity
import org.cxct.sportlottery.ui.withdraw.WithdrawActivity
import org.cxct.sportlottery.util.ToastUtil

open class ThirdGameActivity : WebActivity() {

    private var mUserInfo: UserInfo? = null

    override fun init() {
        setContentView(R.layout.activity_third_game)
        setCookie()
        setupWebView(web_view)
        loadUrl(web_view)
        setupMenu()
        initObserve()
    }

    private fun setupMenu() {
        motion_menu.setOnMenuListener(object : MotionFloatingMenu.OnMenuListener {
            override fun onHome() {
                finish()
            }

            override fun onCashSave() {
                if (checkLogin()) {
                    viewModel.checkRechargeSystem()
                }
            }

            override fun onCashGet() {
                if (checkLogin()) {
                    viewModel.checkWithdrawSystem()
                }
            }
        })
    }


    private fun checkLogin(): Boolean {
        return when (mUserInfo?.testFlag) {
            TestFlag.NORMAL.index -> true
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
    }
}
