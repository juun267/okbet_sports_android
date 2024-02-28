package org.cxct.sportlottery.ui.thirdGame

import android.content.Intent
import android.webkit.WebView
import androidx.lifecycle.lifecycleScope
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ImmersionBar
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.ActivityThirdGameBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.user.UserInfo
import org.cxct.sportlottery.network.withdraw.uwcheck.ValidateTwoFactorRequest
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.TestFlag
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.common.WebActivity
import org.cxct.sportlottery.ui.common.WebActivity.Companion.FIRM_CODE
import org.cxct.sportlottery.ui.common.WebActivity.Companion.GAME_CATEGORY_CODE
import org.cxct.sportlottery.ui.common.WebActivityImp
import org.cxct.sportlottery.ui.common.dialog.CustomSecurityDialog
import org.cxct.sportlottery.ui.maintab.MainViewModel
import org.cxct.sportlottery.ui.money.recharge.MoneyRechargeActivity
import org.cxct.sportlottery.ui.money.withdraw.BankActivity
import org.cxct.sportlottery.ui.money.withdraw.WithdrawActivity
import org.cxct.sportlottery.ui.profileCenter.changePassword.SettingPasswordActivity
import org.cxct.sportlottery.ui.profileCenter.identity.VerifyIdentityDialog
import org.cxct.sportlottery.ui.profileCenter.profile.ProfileActivity
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.util.isThirdTransferOpen
import org.cxct.sportlottery.util.startLogin
import org.cxct.sportlottery.view.dialog.ToGcashDialog

open class ThirdGameActivity : BaseActivity<MainViewModel, ActivityThirdGameBinding>() {

    private var mUserInfo: UserInfo? = null

    //簡訊驗證彈窗
    private var customSecurityDialog: CustomSecurityDialog? = null

    private val firmCode by lazy { intent.getStringExtra(FIRM_CODE) }
    private val gameType by lazy { intent.getStringExtra(GAME_CATEGORY_CODE) }
    private val mUrl: String by lazy { intent?.getStringExtra(WebActivity.KEY_URL) ?: "about:blank" }

    private val webActivityImp by lazy { WebActivityImp(this,this::overrideUrlLoading) }

    override fun onInitView()=binding.run {
        disableSystemUI()
        webActivityImp.setCookie(mUrl)
        webActivityImp.setupWebView(webView)
        webView.loadUrl(mUrl)
        setupMenu()
        initObserve()
        postRefreshToken() // 避免用户长期在三方游戏中导致token过期
        ServiceBroadcastReceiver.thirdGamesMaintain.collectWith(lifecycleScope) {
            if (it.maintain == 1 && firmCode == it.firmType /*&& gameType == it.gameType*/) {
                motionMenu.gone()
                showErrorPromptDialog(getString(R.string.error), getString(R.string.hint_game_maintenance)) {
                    finish()
                }
            }
        }
    }
     fun overrideUrlLoading(view: WebView, url: String): Boolean {
        if (url.isEmptyStr()) {
            view.loadUrl(url)
            return false
        }

        val requestUrl = url.replace("https", "http", true)
        val host = Constants.getBaseUrl().replace("https", "http", true)
        if (requestUrl.startsWith(host, true)) {
            finish()
            return true
        }
         view.loadUrl(url)
        return false
    }

    private fun disableSystemUI() {
        ImmersionBar.with(this).hideBar(BarHide.FLAG_HIDE_BAR).init()
    }

    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            super.onBackPressed()
            return
        }

        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseRefreshToken()
        binding.webView.destroy()
        if (isThirdTransferOpen()) {
            LoginRepository.allTransferOut()
        }
    }

    private fun setupMenu() {
        binding.motionMenu.setOnMenuListener(object : MotionFloatingMenu.OnMenuListener {
            override fun onHome() {
                finish()
            }

            override fun onCashSave() {
                if (checkLogin()) {
                    ToGcashDialog.showByClick {
                        viewModel.checkRechargeKYCVerify()
                    }
                }
            }

            override fun onCashGet() {
                if (checkLogin()) {
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
                startLogin()
                false
            }
        }
    }

    private fun initObserve() {
        viewModel.userInfo.observe(this) {
            mUserInfo = it
        }

        viewModel.withdrawSystemOperation.observe(this) {
            val operation = it.getContentIfNotHandled()
            if (operation == false) {
                showPromptDialog(getString(R.string.prompt),
                    getString(R.string.message_withdraw_maintain)) {}
            }
        }

        viewModel.rechargeSystemOperation.observe(this) {
            it.getContentIfNotHandled()?.let { b ->
                if (b) {
                    startActivity(Intent(this, MoneyRechargeActivity::class.java))
                } else {
                    showPromptDialog(getString(R.string.prompt),
                        getString(R.string.message_recharge_maintain)) {}
                }
            }
        }

        viewModel.needToUpdateWithdrawPassword.observe(this) {
            it.getContentIfNotHandled()?.let { b ->
                if (b) {
                    showPromptDialog(getString(R.string.withdraw_setting),
                        getString(R.string.please_setting_withdraw_password),
                        getString(R.string.go_to_setting),
                        true) {
                        startActivity(Intent(this, SettingPasswordActivity::class.java).apply {
                            putExtra(
                                SettingPasswordActivity.PWD_PAGE,
                                SettingPasswordActivity.PwdPage.BANK_PWD)
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
                    showPromptDialog(getString(R.string.withdraw_setting),
                        getString(R.string.please_complete_profile_info),
                        getString(R.string.go_to_setting),
                        true) {
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
                    showPromptDialog(getString(R.string.withdraw_setting),
                        getString(messageId),
                        getString(R.string.go_to_setting),
                        true) {
                        startActivity(Intent(this, BankActivity::class.java))
                    }
                } else {
                    startActivity(Intent(this, WithdrawActivity::class.java))
                }
            }
        }

        viewModel.settingNeedToUpdateWithdrawPassword.observe(this) {
            it.getContentIfNotHandled()?.let { b ->
                if (b) {
                    showPromptDialog(getString(R.string.withdraw_setting),
                        getString(R.string.please_setting_withdraw_password),
                        getString(R.string.go_to_setting),
                        true) {
                        startActivity(Intent(this, SettingPasswordActivity::class.java).apply {
                            putExtra(
                                SettingPasswordActivity.PWD_PAGE,
                                SettingPasswordActivity.PwdPage.BANK_PWD)
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
                    showPromptDialog(getString(R.string.withdraw_setting),
                        getString(R.string.please_complete_profile_info),
                        getString(R.string.go_to_setting),
                        true) {
                        startActivity(Intent(this, ProfileActivity::class.java))
                    }
                } else if (!b) {
                    startActivity(Intent(this, BankActivity::class.java))
                }
            }
        }

        viewModel.needToSendTwoFactor.observe(this) {
            it.getContentIfNotHandled()?.let { b ->
                if (b) {
                        customSecurityDialog = CustomSecurityDialog().apply {
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
        VerifyIdentityDialog().show(supportFragmentManager, null)
    }

    private val refreshTokenDuring = 5 * 60_000L
    private val refreshTokenRunnable = Runnable {
        lifecycleScope.launch { runWithCatch { UserInfoRepository.getUserInfo() } }
        postRefreshToken()
    }

    private fun postRefreshToken() {
        if (LoginRepository.isLogined() && !isDestroyed) {
            binding.root.postDelayed(refreshTokenRunnable, refreshTokenDuring)
        }
    }

    private fun releaseRefreshToken() {
        binding.root.removeCallbacks(refreshTokenRunnable)
    }

}
