package org.cxct.sportlottery.ui.menu

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.fragment_menu.*
import kotlinx.android.synthetic.main.fragment_menu.iv_head
import kotlinx.android.synthetic.main.view_toolbar_main.*
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.common.StatusSheetAdapter
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.ui.favorite.MyFavoriteActivity
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.ui.main.MainViewModel
import org.cxct.sportlottery.ui.money.recharge.MoneyRechargeActivity
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterActivity
import org.cxct.sportlottery.ui.profileCenter.changePassword.SettingPasswordActivity
import org.cxct.sportlottery.ui.profileCenter.otherBetRecord.OtherBetRecordActivity
import org.cxct.sportlottery.ui.profileCenter.profile.ProfileActivity
import org.cxct.sportlottery.ui.profileCenter.sportRecord.BetRecordActivity
import org.cxct.sportlottery.ui.profileCenter.versionUpdate.VersionUpdateActivity
import org.cxct.sportlottery.ui.results.ResultsSettlementActivity
import org.cxct.sportlottery.ui.vip.VipActivity
import org.cxct.sportlottery.ui.withdraw.BankActivity
import org.cxct.sportlottery.ui.withdraw.WithdrawActivity
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.ToastUtil

/**
 * 遊戲右側功能選單
 */
@SuppressLint("SetTextI18n")
class MenuFragment : BaseSocketFragment<MainViewModel>(MainViewModel::class) {
    private var mDownMenuListener: View.OnClickListener? = null

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
        getOddsType()
        updateLanguageItem()
    }

    private fun updateLanguageItem() {
        menu_language.text = LanguageManager.getLanguageStringResource(context)
        menu_language.updateLanguageImage()
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
        viewModel.oddsType.observe(viewLifecycleOwner) {
            updateOddsType(it)
        }

        viewModel.infoCenterRepository.unreadNoticeList.observe(viewLifecycleOwner) {
            menu_profile_center.updateNoticeCount(it.size)
        }

        viewModel.isLogin.observe(viewLifecycleOwner) {
            if (it)
                getMoney()
        }

        viewModel.isCreditAccount.observe(viewLifecycleOwner) {
            updateUIVisibility(it)
        }

        viewModel.userMoney.observe(viewLifecycleOwner) { money ->
            tv_money.text =
                sConfigData?.systemCurrencySign + money?.let { it -> TextUtil.formatMoney(it) }
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

    }

    private fun initEvent() {

        //個人中心
        menu_profile_center.setOnClickListener {
            when (viewModel.userInfo.value?.testFlag) {
                TestFlag.NORMAL.index -> {
                    startActivity(Intent(context, ProfileCenterActivity::class.java))
                }
                TestFlag.TEST.index -> {
                    startActivity(Intent(context, ProfileCenterActivity::class.java))
                }
                null -> { //尚未登入
                    startActivity(Intent(context, LoginActivity::class.java))
                }
                else -> { //遊客 TODO 20221208 拿掉遊客選項，預設以外行為需要另外定義(先預設登入) by Hewie
                    startActivity(Intent(context, LoginActivity::class.java))
                    //ToastUtil.showToastInCenter(context, getString(R.string.message_guest_no_permission))
                }
            }
            mDownMenuListener?.onClick(menu_profile_center)
        }

        //充值
        tv_recharge.setOnClickListener {
            viewModel.checkRechargeSystem()
        }

        //提款
        tv_withdraw.setOnClickListener {
            viewModel.checkWithdrawSystem()
        }

        //我的賽事
        menu_my_favorite.setOnClickListener {
            startActivity(Intent(context, MyFavoriteActivity::class.java))
            mDownMenuListener?.onClick(menu_my_favorite)
        }

        //其他投注記錄
        menu_other_bet_record.setOnClickListener {
            startActivity(Intent(context, OtherBetRecordActivity::class.java))
            mDownMenuListener?.onClick(menu_other_bet_record)
        }

        //會員層級
        menu_member_level.setOnClickListener {
            startActivity(Intent(context, VipActivity::class.java))
            mDownMenuListener?.onClick(menu_member_level)
        }

        //賽果結算
        menu_game_result.setOnClickListener {
            startActivity(Intent(activity, ResultsSettlementActivity::class.java))
            mDownMenuListener?.onClick(menu_game_result)
        }

        //遊戲規則
        menu_game_rule.setOnClickListener {
            JumpUtil.toInternalWeb(
                requireContext(),
                Constants.getGameRuleUrl(requireContext()),
                getString(R.string.game_rule)
            )
            mDownMenuListener?.onClick(menu_game_rule)
        }

        //版本更新
        menu_version_update.setOnClickListener {
            startActivity(Intent(activity, VersionUpdateActivity::class.java))
            mDownMenuListener?.onClick(menu_version_update)
        }

        //退出登入
        btn_sign_out.setOnClickListener {
            viewModel.doLogoutCleanUser {
                context?.run {
                    if (sConfigData?.thirdOpen == FLAG_OPEN)
                        MainActivity.reStart(this)
                    else
                        GameActivity.reStart(this)
                }
            }
            mDownMenuListener?.onClick(btn_sign_out)
        }

        //語系選擇
        menu_language.setOnClickListener {
            GameActivity.reStartWithSwitchLanguage(requireContext())
//            context?.let {
//                showBottomSheetDialog("",
//                    viewModel.getLanguageStatusSheetList(it),
//                    viewModel.getLanguageStatusSheetList(it)
//                        .find { it.showName == LanguageManager.getLanguageStringResource(context) }
//                        ?: StatusSheetData(
//                            "0",
//                            context?.resources?.getString(R.string.language_cn)
//                        ),
//                    StatusSheetAdapter.ItemCheckedListener { _, data ->
//                        activity?.run {
//                            val select = when (data.showName) {
//                                context?.resources?.getString(R.string.language_cn) -> LanguageManager.Language.ZH
//                                context?.resources?.getString(R.string.language_vi) -> LanguageManager.Language.VI
//                                else -> LanguageManager.Language.EN
//                            }
//                            LanguageManager.saveSelectLanguage(this, select)
//                            if (sConfigData?.thirdOpen == FLAG_OPEN)
//                                MainActivity.reStart(this)
//                            else
//                                GameActivity.reStart(this)
//                        }
//                    })
//            }

        }

        menu_odds_type.setOnClickListener {
            context?.let {
                showBottomSheetDialog("",
                    viewModel.getOddTypeStatusSheetList(it),
                    viewModel.getDeafaultOddTypeStatusSheetData(it),
                    StatusSheetAdapter.ItemCheckedListener { _, data ->
                        when (data.code) {
                            OddsType.EU.code -> viewModel.saveOddsType(OddsType.EU)
                            OddsType.HK.code -> viewModel.saveOddsType(OddsType.HK)
                            OddsType.MYS.code -> viewModel.saveOddsType(OddsType.MYS)
                            OddsType.IDN.code -> viewModel.saveOddsType(OddsType.IDN)
                            else -> viewModel.saveOddsType(OddsType.EU)
                        }
                    })
            }
        }
    }

    private fun setupVersion() {
        tv_version.text = getString(R.string.label_version, BuildConfig.VERSION_NAME)
    }

    private fun getOddsType() {
        viewModel.getOddsType()
    }

    private fun updateUIVisibility(isCreditAccount: Boolean) {
        //其他投注記錄 信用盤 或 第三方關閉 隱藏
        menu_other_bet_record.visibility =
            if (isCreditAccount || sConfigData?.thirdOpen != FLAG_OPEN) {
                View.GONE
            } else {
                View.VISIBLE
            }

        menu_member_level.visibility = if (sConfigData?.thirdOpen != FLAG_OPEN) {
            View.GONE
        } else {
            View.VISIBLE
        }
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

    private fun updateOddsType(oddsType: OddsType) {
        menu_odds_type.text = getString(oddsType.res)
    }
}