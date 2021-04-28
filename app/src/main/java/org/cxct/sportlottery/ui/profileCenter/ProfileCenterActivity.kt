package org.cxct.sportlottery.ui.profileCenter

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.listener.OnResultCallbackListener
import kotlinx.android.synthetic.main.activity_profile_center.*
import kotlinx.android.synthetic.main.toast_top_bet_result.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.db.entity.UserInfo
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.uploadImg.UploadImgRequest
import org.cxct.sportlottery.repository.FLAG_NICKNAME_IS_SET
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.TestFlag
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseOddButtonActivity
import org.cxct.sportlottery.ui.feedback.FeedbackMainActivity
import org.cxct.sportlottery.ui.finance.FinanceActivity
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.helpCenter.HelpCenterActivity
import org.cxct.sportlottery.ui.infoCenter.InfoCenterActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterActivity
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.ui.money.recharge.MoneyRechargeActivity
import org.cxct.sportlottery.ui.profileCenter.changePassword.SettingPasswordActivity
import org.cxct.sportlottery.ui.profileCenter.changePassword.SettingPasswordActivity.Companion.PWD_PAGE
import org.cxct.sportlottery.ui.profileCenter.money_transfer.MoneyTransferActivity
import org.cxct.sportlottery.ui.profileCenter.nickname.ModifyProfileInfoActivity
import org.cxct.sportlottery.ui.profileCenter.nickname.ModifyType
import org.cxct.sportlottery.ui.profileCenter.otherBetRecord.OtherBetRecordActivity
import org.cxct.sportlottery.ui.profileCenter.profile.AvatarSelectorDialog
import org.cxct.sportlottery.ui.profileCenter.profile.ProfileActivity
import org.cxct.sportlottery.ui.profileCenter.sportRecord.BetRecordActivity
import org.cxct.sportlottery.ui.withdraw.BankActivity
import org.cxct.sportlottery.ui.withdraw.WithdrawActivity
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.ToastUtil
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException

class ProfileCenterActivity : BaseOddButtonActivity<ProfileCenterViewModel>(ProfileCenterViewModel::class) {

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
                ToastUtil.showToastInCenter(this@ProfileCenterActivity, getString(R.string.error_reading_file))
            }
        }

        override fun onCancel() {
            Timber.i("PictureSelector Cancel")
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_center)

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
        initSocketObserver()
    }

    override fun onResume() {
        super.onResume()
        getMoney()
    }

    private fun setupHeadButton() {
        iv_head.setOnClickListener {
            AvatarSelectorDialog(this, mSelectMediaListener).show(supportFragmentManager, null)
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
        btn_refresh_money.setOnClickListener {
            getMoney()
        }
    }

    private fun setupRechargeButton() {
        btn_recharge.setOnClickListener {
            viewModel.checkRechargeSystem()
        }
    }

    private fun setupWithdrawButton() {
        btn_withdraw.setOnClickListener {
            viewModel.checkWithdrawSystem()
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
            viewModel.doLogoutCleanUser {
                run {
                    MainActivity.reStart(this)
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

    private fun initBottomNav() {
        bottom_nav_view.selectedItemId = R.id.my_account_page
        bottom_nav_view.setOnNavigationItemSelectedListener {
            //20200303 紀錄：跳轉其他 Activity 頁面，不需要切換 BottomNav 選取狀態
            when (it.itemId) {
                R.id.home_page -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    false
                }
                R.id.game_page -> {
                    startActivity(Intent(this, GameActivity::class.java))
                    false
                }
                R.id.promotion_page -> {
                    JumpUtil.toInternalWeb(
                        this,
                        Constants.getPromotionUrl(viewModel.token),
                        getString(R.string.promotion)
                    )
                    false
                }
                R.id.chat_page -> {
                    false
                }
                R.id.my_account_page -> {
                    when (viewModel.userInfo.value?.testFlag) {
                        TestFlag.NORMAL.index -> {
                            startActivity(Intent(this, ProfileCenterActivity::class.java))
                        }
                        else -> { //遊客 //尚未登入
                            startActivity(Intent(this, RegisterActivity::class.java))
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

        viewModel.withdrawSystemOperation.observe(this, {
            val operation = it.getContentIfNotHandled()
            if (operation == false) {
                showPromptDialog(getString(R.string.prompt), getString(R.string.message_withdraw_maintain)) {}
            }
        })

        viewModel.rechargeSystemOperation.observe(this, {
            val operation = it.getContentIfNotHandled()
            if (operation == false)
                showPromptDialog(getString(R.string.prompt), getString(R.string.message_recharge_maintain)) {}
            else
                startActivity(Intent(this, MoneyRechargeActivity::class.java))

        })

        viewModel.needToUpdateWithdrawPassword.observe(this, Observer {
            it.getContentIfNotHandled()?.let { b ->
                if (b) {
                    showPromptDialog(getString(R.string.withdraw_setting), getString(R.string.please_setting_withdraw_password)) {
                        startActivity(Intent(this, SettingPasswordActivity::class.java).apply { putExtra(PWD_PAGE, SettingPasswordActivity.PwdPage.BANK_PWD) })
                    }
                } else {
                    viewModel.checkProfileInfoComplete()
                }
            }
        })

        viewModel.needToCompleteProfileInfo.observe(this, Observer {
            it.getContentIfNotHandled()?.let { b ->
                if (b) {
                    showPromptDialog(getString(R.string.withdraw_setting), getString(R.string.please_complete_profile_info)) {
                        startActivity(Intent(this, ProfileActivity::class.java))
                    }
                } else {
                    viewModel.checkBankCardPermissions()
                }
            }
        })

        viewModel.needToBindBankCard.observe(this, Observer {
            it.getContentIfNotHandled()?.let { b ->
                if (b) {
                    showPromptDialog(getString(R.string.withdraw_setting), getString(R.string.please_setting_money_card)) {
                        startActivity(Intent(this, BankActivity::class.java))
                    }
                } else {
                    startActivity(Intent(this, WithdrawActivity::class.java))
                }
            }
        })

        viewModel.settingNeedToUpdateWithdrawPassword.observe(this, Observer {
            it.getContentIfNotHandled()?.let { b ->
                if (b) {
                    showPromptDialog(getString(R.string.withdraw_setting), getString(R.string.please_setting_withdraw_password)) {
                        startActivity(Intent(this, SettingPasswordActivity::class.java).apply { putExtra(PWD_PAGE, SettingPasswordActivity.PwdPage.BANK_PWD) })
                    }
                } else if (!b) {
                    startActivity(Intent(this, BankActivity::class.java))
                }
            }
        })

        viewModel.settingNeedToCompleteProfileInfo.observe(this, {
            it.getContentIfNotHandled()?.let { b ->
                if (b) {
                    showPromptDialog(getString(R.string.withdraw_setting), getString(R.string.please_complete_profile_info)) {
                        startActivity(Intent(this, ProfileActivity::class.java))
                    }
                } else if (!b) {
                    startActivity(Intent(this, BankActivity::class.java))
                }
            }
        })

        viewModel.editIconUrlResult.observe(this, {
            val iconUrlResult = it?.getContentIfNotHandled()
            if (iconUrlResult?.success == true)
                showPromptDialog(getString(R.string.prompt), getString(R.string.save_avatar_success)) {}
            else
                iconUrlResult?.msg?.let { msg -> showErrorPromptDialog(msg) {} }
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

    private fun uploadImg(file: File) {
        val userId = viewModel.userInfo.value?.userId.toString()
        val uploadImgRequest = UploadImgRequest(userId, file, UploadImgRequest.PlatformCodeType.AVATAR)
        viewModel.uploadImage(uploadImgRequest)
    }

}