package org.cxct.sportlottery.ui.menu

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.fragment_menu.*
import kotlinx.android.synthetic.main.fragment_menu.iv_head
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.withdraw.uwcheck.ValidateTwoFactorRequest
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.common.CustomSecurityDialog
import org.cxct.sportlottery.ui.favorite.MyFavoriteActivity
import org.cxct.sportlottery.ui.game.Page
import org.cxct.sportlottery.ui.game.publicity.GamePublicityActivity
import org.cxct.sportlottery.ui.game.language.SwitchLanguageActivity
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.ui.main.MainViewModel
import org.cxct.sportlottery.ui.money.recharge.MoneyRechargeActivity
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterActivity
import org.cxct.sportlottery.ui.profileCenter.changePassword.SettingPasswordActivity
import org.cxct.sportlottery.ui.profileCenter.otherBetRecord.OtherBetRecordActivity
import org.cxct.sportlottery.ui.profileCenter.profile.ProfileActivity
import org.cxct.sportlottery.ui.profileCenter.versionUpdate.VersionUpdateActivity
import org.cxct.sportlottery.ui.results.ResultsSettlementActivity
import org.cxct.sportlottery.ui.vip.VipActivity
import org.cxct.sportlottery.ui.withdraw.BankActivity
import org.cxct.sportlottery.ui.withdraw.WithdrawActivity
import org.cxct.sportlottery.util.*

/**
 * @app_destination 右上選單
 */
@SuppressLint("SetTextI18n")
class MenuFragment : BaseSocketFragment<MainViewModel>(MainViewModel::class) {
    private var mDownMenuListener: View.OnClickListener? = null

    //簡訊驗證彈窗
    private var customSecurityDialog: CustomSecurityDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCloseBtn()
        initObserve()
        initSocketObserver()
        initEvent()
        setupVersion()
    }

    private fun initSocketObserver() {
        receiver.userMoney.observe(viewLifecycleOwner) {
            viewModel.updateMoney(it)
        }
    }

    private fun setupCloseBtn() {
        btn_close.setOnClickListener {
            mDownMenuListener?.onClick(btn_close)
        }
    }

    private fun getMoney() {
        viewModel.getMoney()
    }

    private fun initObserve() {
        viewModel.navActivity.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                startActivity(Intent(context, it))
            }
        }


        viewModel.infoCenterRepository.unreadNoticeList.observe(viewLifecycleOwner) {
            menu_profile_center.updateNoticeCount(it.size)
        }

        viewModel.isLogin.observe(viewLifecycleOwner) {
            if (it)
                getMoney()
        }

        viewModel.userMoney.observe(viewLifecycleOwner) { money ->
            tv_money.text = sConfigData?.systemCurrencySign + " " + money?.let { it -> TextUtil.formatMoney(it) }
        }

        viewModel.userInfo.observe(viewLifecycleOwner) {
            updateUI(
                it?.iconUrl,
                it?.userName,
                it?.nickName,
                it?.fullName,
                StaticData.getTestFlag(it?.testFlag)
            )

            menu_profile_center.updateUserIdentity(it?.testFlag)
        }

        viewModel.rechargeSystemOperation.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { b ->
                if (b) {
                    mDownMenuListener?.onClick(tv_recharge)
                    startActivity(Intent(context, MoneyRechargeActivity::class.java))
                } else {
                    showPromptDialog(
                        getString(R.string.prompt),
                        getString(R.string.message_recharge_maintain)
                    ) {}
                }
            }
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

        viewModel.needToSendTwoFactor.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { b ->
                if (b) {
                    context?.let { it ->
                        customSecurityDialog = CustomSecurityDialog(it).apply {
                            getSecurityCodeClickListener {
                                this.showSmeTimer300()
                                viewModel.sendTwoFactor()
                            }
                            positiveClickListener = CustomSecurityDialog.PositiveClickListener { number ->
                                viewModel.validateTwoFactor(ValidateTwoFactorRequest(number))
                            }
                        }
                        customSecurityDialog?.show(parentFragmentManager, null)
                    }
                }
            }
        }

        viewModel.errorMessageDialog.observe(viewLifecycleOwner) {
            val errorMsg = it ?: getString(R.string.unknown_error)
            this.context?.let { context -> CustomAlertDialog(context) }?.apply {
                setMessage(errorMsg)
                setNegativeButtonText(null)
                setCanceledOnTouchOutside(false)
                setCancelable(false)
            }?.show(childFragmentManager, null)
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
                if (!b) {
                    context?.let { c ->
                        if (!b) phoneNumCheckDialog(c, childFragmentManager)
                    }
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
                                context,
                                SettingPasswordActivity::class.java
                            ).apply {
                                putExtra(
                                    SettingPasswordActivity.PWD_PAGE,
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
                        startActivity(Intent(context, ProfileActivity::class.java))
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
                        startActivity(Intent(context, BankActivity::class.java))
                    }
                } else {
                    startActivity(Intent(context, WithdrawActivity::class.java))
                }
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
                                context,
                                SettingPasswordActivity::class.java
                            ).apply {
                                putExtra(
                                    SettingPasswordActivity.PWD_PAGE,
                                    SettingPasswordActivity.PwdPage.BANK_PWD
                                )
                            })
                    }
                } else if (!b) {
                    startActivity(Intent(context, BankActivity::class.java))
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
                        startActivity(Intent(context, ProfileActivity::class.java))
                    }
                } else if (!b) {
                    startActivity(Intent(context, BankActivity::class.java))
                }
            }
        }

        viewModel.intoWithdraw.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                startActivity(Intent(context, WithdrawActivity::class.java))
            }
        }
    }

    private fun initEvent() {

        //個人中心
        menu_profile_center.setOnClickListener {
            viewModel.navActivity(ProfileCenterActivity::class.java)
            //遊客 TODO 20221208 拿掉遊客選項，預設以外行為需要另外定義(先預設登入) by Hewie
            mDownMenuListener?.onClick(menu_profile_center)
        }

        //充值
        tv_recharge.apply {
            setVisibilityByCreditSystem()
            setOnClickListener {
                avoidFastDoubleClick()
                viewModel.checkRechargeSystem()
            }
        }

        //提款
        tv_withdraw.apply {
            setVisibilityByCreditSystem()
            setOnClickListener {
                avoidFastDoubleClick()
                viewModel.checkWithdrawSystem()
            }
        }


        //版本更新
        menu_version_update.setOnClickListener {
            startActivity(Intent(activity, VersionUpdateActivity::class.java))
            mDownMenuListener?.onClick(menu_version_update)
        }

        //退出登入
        btn_sign_out.setOnClickListener {
            viewModel.doLogoutAPI()
            viewModel.doLogoutCleanUser {
                context?.run {
                    if (sConfigData?.thirdOpen == FLAG_OPEN)
                        MainActivity.reStart(this)
                    else {
                        GamePublicityActivity.reStart(this)
                        activity?.finish()
                    }
                }
            }
            mDownMenuListener?.onClick(btn_sign_out)
        }

    }

    private fun setupVersion() {
        tv_version.text = getString(R.string.label_version, BuildConfig.VERSION_NAME)
    }

    private fun updateUI(
        iconUrl: String?,
        userName: String?,
        nickName: String?,
        fullName: String?,
        testFlag: TestFlag?
    ) {
        Glide.with(this)
            .load(iconUrl)
            .apply(RequestOptions().placeholder(R.drawable.img_avatar_default))
            .into(iv_head) //載入頭像

        tv_name.text = when (testFlag) {
            TestFlag.GUEST -> fullName
            else -> {
                if (nickName.isNullOrEmpty()) {
                    userName
                } else {
                    nickName
                }
            }
        }

        menu_profile_center.visibility = if (testFlag == TestFlag.GUEST) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    /**
     * 選單選擇結束，需透過 listener 讓上層關閉 選單
     */
    fun setDownMenuListener(listener: View.OnClickListener?) {
        mDownMenuListener = listener
    }

}