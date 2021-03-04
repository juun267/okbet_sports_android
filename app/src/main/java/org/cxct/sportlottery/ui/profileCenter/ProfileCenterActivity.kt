package org.cxct.sportlottery.ui.profileCenter

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.activity_profile_center.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.db.entity.UserInfo
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.repository.FLAG_NICKNAME_IS_SET
import org.cxct.sportlottery.repository.TestFlag
import org.cxct.sportlottery.ui.profileCenter.sportRecord.BetRecordActivity
import org.cxct.sportlottery.ui.base.BaseNoticeActivity
import org.cxct.sportlottery.ui.feedback.FeedbackMainActivity
import org.cxct.sportlottery.ui.finance.FinanceActivity
import org.cxct.sportlottery.ui.helpCenter.HelpCenterActivity
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.ui.infoCenter.InfoCenterActivity
import org.cxct.sportlottery.ui.money.recharge.MoneyRechargeActivity
import org.cxct.sportlottery.ui.profileCenter.changePassword.SettingPasswordActivity
import org.cxct.sportlottery.ui.profileCenter.changePassword.SettingPasswordActivity.Companion.PWD_PAGE
import org.cxct.sportlottery.ui.profileCenter.money_transfer.MoneyTransferActivity
import org.cxct.sportlottery.ui.profileCenter.otherBetRecord.OtherBetRecordActivity
import org.cxct.sportlottery.ui.profileCenter.nickname.ModifyProfileInfoActivity
import org.cxct.sportlottery.ui.profileCenter.nickname.ModifyType
import org.cxct.sportlottery.ui.profileCenter.profile.ProfileActivity
import org.cxct.sportlottery.ui.withdraw.BankActivity
import org.cxct.sportlottery.ui.withdraw.WithdrawActivity
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.ToastUtil

class ProfileCenterActivity :
    BaseNoticeActivity<ProfileCenterViewModel>(ProfileCenterViewModel::class) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_center)

        setupBackButton()
        setupEditNickname()
        setupBalance()
        setupRechargeButton()
        setupWithdrawButton()
        setupLogout()
        setupMoreButtons()
        getUserInfo()
        initObserve()
        initSocketObserver()
    }

    private fun setupBackButton() {
        btn_back.setOnClickListener {
            finish()
        }
    }

    private fun setupEditNickname() {
        btn_edit_nickname.setOnClickListener {
            startActivity(Intent(this@ProfileCenterActivity, ModifyProfileInfoActivity::class.java).apply {
                putExtra(ModifyProfileInfoActivity.MODIFY_INFO, ModifyType.NickName)
            })
        }
    }

    private fun setupBalance() {
        getMoney()
        btn_refresh_money.setOnClickListener {
            getMoney()
        }
    }

    private fun setupRechargeButton() {
        btn_recharge.setOnClickListener {
            startActivity(Intent(this, MoneyRechargeActivity::class.java))
        }
    }

    private fun setupWithdrawButton() {
        btn_withdraw.setOnClickListener {
            viewModel.withdrawCheckPermissions()
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
        btn_logout.setOnClickListener {
            viewModel.logout()
            run {
                MainActivity.reStart(this)
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

        //體育投注記錄
        btn_sport_bet_record.setOnClickListener {
            startActivity(Intent(this, BetRecordActivity::class.java))
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
            val testFlag = viewModel.userInfo.value?.testFlag
            if (testFlag == TestFlag.NORMAL.index)
                JumpUtil.toInternalWeb(this, Constants.getPromotionUrl(viewModel.token), getString(R.string.promotion))
            else
                ToastUtil.showToastInCenter(this, getString(R.string.message_guest_no_permission))
        }

        //幫助中心
        btn_help_center.setOnClickListener {
            startActivity(Intent(this, HelpCenterActivity::class.java))
        }

        //建議反饋
        btn_feedback.setOnClickListener {
            startActivity(Intent(this, FeedbackMainActivity::class.java))
        }
    }


    private fun getUserInfo() {
        viewModel.getUserInfo()
    }

    private fun initObserve() {
        viewModel.userMoney.observe(this, Observer {
            refreshMoneyHideLoading()
            tv_account_balance.text = it ?: ""
        })

        viewModel.userInfo.observe(this, Observer {
            updateUI(it)
        })

        viewModel.needToUpdateWithdrawPassword.observe(this, Observer {
            if (it == true) {
                SettingTipsDialog(this, SettingTipsDialog.SettingTipsDialogListener {
                    startActivity(Intent(this, SettingPasswordActivity::class.java).apply { putExtra(PWD_PAGE, SettingPasswordActivity.PwdPage.BANK_PWD) })
                }).apply {
                    setTipsTitle(R.string.withdraw_setting)
                    setTipsContent(R.string.please_setting_withdraw_password)
                    show(supportFragmentManager, "")
                }
            } else if (it == false) {
                viewModel.checkProfileInfoComplete()
            }
        })

        viewModel.needToCompleteProfileInfo.observe(this, Observer {
            if (it == true) {
                SettingTipsDialog(this, SettingTipsDialog.SettingTipsDialogListener {
                    startActivity(Intent(this, ProfileActivity::class.java))
                }).apply {
                    setTipsTitle(R.string.withdraw_setting)
                    setTipsContent(R.string.please_complete_profile_info)
                    show(supportFragmentManager, "")
                }
            } else if (it == false) {
                viewModel.checkBankCardPermissions()
            }
        })

        viewModel.needToBindBankCard.observe(this, Observer {
            if (it == true) {
                SettingTipsDialog(this, SettingTipsDialog.SettingTipsDialogListener {
                    startActivity(Intent(this, BankActivity::class.java))
                }).apply {
                    setTipsTitle(R.string.withdraw_setting)
                    setTipsContent(R.string.please_setting_bank_card)
                    show(supportFragmentManager, "")
                }
            } else {
                startActivity(Intent(this, WithdrawActivity::class.java))
            }
        })

        viewModel.settingNeedToUpdateWithdrawPassword.observe(this, Observer {
            if (it == true) {
                showPromptDialog(getString(R.string.withdraw_setting), getString(R.string.please_setting_withdraw_password)) {
                    startActivity(Intent(this, SettingPasswordActivity::class.java).apply { putExtra(PWD_PAGE, SettingPasswordActivity.PwdPage.BANK_PWD) })
                }
            } else if (it == false) {
                startActivity(Intent(this, BankActivity::class.java))
            }
        })
    }

    private fun initSocketObserver() {
        receiver.userMoney.observe(this, Observer {
            val formatMoney = it?.let {
                TextUtil.format(it)
            }

            tv_account_balance.text = formatMoney ?: ""
        })
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(userInfo: UserInfo?) {

        Glide.with(this)
            .load(userInfo?.iconUrl)
            .apply(RequestOptions().placeholder(R.drawable.img_avatar_default))
            .into(iv_head) //載入頭像

        tv_user_nickname.text = if (userInfo?.nickName.isNullOrEmpty()) {
            userInfo?.userName
        } else {
            userInfo?.nickName
        }

        btn_edit_nickname.visibility =
            if (userInfo?.setted == FLAG_NICKNAME_IS_SET) View.GONE else View.VISIBLE
        tv_user_id.text = userInfo?.userId?.toString()
    }
}