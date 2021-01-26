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
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.bet.record.BetRecordActivity
import org.cxct.sportlottery.ui.finance.FinanceActivity
import org.cxct.sportlottery.ui.home.MainActivity
import org.cxct.sportlottery.ui.home.MainViewModel
import org.cxct.sportlottery.ui.infoCenter.InfoCenterActivity
import org.cxct.sportlottery.ui.money.recharge.MoneyRechargeActivity
import org.cxct.sportlottery.ui.profileCenter.nickname.ChangeNicknameActivity
import org.cxct.sportlottery.ui.profileCenter.profile.ProfileActivity
import org.cxct.sportlottery.ui.withdraw.BankActivity
import org.cxct.sportlottery.ui.withdraw.WithdrawActivity
import org.cxct.sportlottery.util.ArithUtil
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.ToastUtil
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

class ProfileCenterActivity : BaseActivity<MainViewModel>(MainViewModel::class) {
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
    }

    private fun setupBackButton() {
        btn_back.setOnClickListener {
            finish()
        }
    }

    private fun setupEditNickname() {
        btn_edit_nickname.setOnClickListener {
            startActivity(Intent(this, ChangeNicknameActivity::class.java))
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

        btn_finance.setOnClickListener {
            startActivity(Intent(this, FinanceActivity::class.java))
        }
    }

    private fun setupWithdrawButton() {
        btn_withdraw.setOnClickListener {
            startActivity(Intent(this, WithdrawActivity::class.java))
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

        //資金明細
        btn_fund_detail.setOnClickListener {
            startActivity(Intent(this, FinanceActivity::class.java))
        }

        //投注記錄
        btn_bet_record.setOnClickListener {
            startActivity(Intent(this, BetRecordActivity::class.java))
        }

        //提款設置
        btn_withdrawal_setting.setOnClickListener {
            startActivity(Intent(this, BankActivity::class.java))
        }

        //消息中心
        btn_news_center.setOnClickListener {
            startActivity(Intent(this, InfoCenterActivity::class.java))
        }

        //建議反饋
        btn_feedback.setOnClickListener {
            //TODO 建議反饋
        }

        //優惠活動
        btn_promotion.setOnClickListener {
            val testFlag = viewModel.userInfo.value?.testFlag
            if (testFlag == TestFlag.NORMAL.index)
                JumpUtil.toInternalWeb(this, Constants.getPromotionUrl(viewModel.token), getString(R.string.promotion))
            else
                ToastUtil.showToastInCenter(this, getString(R.string.message_guest_no_permission))
        }

        //代理加盟
        btn_agent.setOnClickListener {
            //TODO 代理加盟
        }

        //幫助中心
        btn_help_center.setOnClickListener {
            //TODO 幫助中心
        }

        //在線客服
        btn_online_service.setOnClickListener {
            JumpUtil.toOnlineService(this)
        }
    }


    private fun getUserInfo() {
        viewModel.getUserInfo()
    }

    private fun initObserve() {
        viewModel.userMoney.observe(this, Observer {
            refreshMoneyHideLoading()
            tv_account_balance.text = ArithUtil.toMoneyFormat(it)
        })

        viewModel.userInfo.observe(this, Observer {
            updateUI(it)
        })
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(userInfo: UserInfo?) {

        Glide.with(this)
            .load(userInfo?.iconUrl)
            .apply(RequestOptions().placeholder(R.drawable.ic_head))
            .into(iv_head) //載入頭像

        tv_user_nickname.text = viewModel.sayHello() + userInfo?.userName
        btn_edit_nickname.visibility =
            if (userInfo?.setted == FLAG_NICKNAME_IS_SET) View.GONE else View.VISIBLE
        tv_user_id.text = userInfo?.userId?.toString()
    }
}