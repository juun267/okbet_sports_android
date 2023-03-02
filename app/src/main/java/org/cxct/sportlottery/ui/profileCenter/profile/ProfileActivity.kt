package org.cxct.sportlottery.ui.profileCenter.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.listener.OnResultCallbackListener
import kotlinx.android.synthetic.main.activity_profile.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.db.entity.UserInfo
import org.cxct.sportlottery.network.index.config.VerifySwitchType
import org.cxct.sportlottery.network.uploadImg.UploadImgRequest
import org.cxct.sportlottery.network.withdraw.uwcheck.ValidateTwoFactorRequest
import org.cxct.sportlottery.repository.FLAG_NICKNAME_IS_SET
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.common.CustomSecurityDialog
import org.cxct.sportlottery.ui.game.ServiceDialog
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.profileCenter.authbind.AuthActivity
import org.cxct.sportlottery.ui.profileCenter.cancelaccount.CancelAccountActivity
import org.cxct.sportlottery.ui.profileCenter.changePassword.SettingPasswordActivity
import org.cxct.sportlottery.ui.profileCenter.identity.VerifyIdentityActivity
import org.cxct.sportlottery.ui.profileCenter.identity.VerifyIdentityDialog
import org.cxct.sportlottery.ui.profileCenter.nickname.ModifyProfileInfoActivity
import org.cxct.sportlottery.ui.profileCenter.nickname.ModifyProfileInfoActivity.Companion.MODIFY_INFO
import org.cxct.sportlottery.ui.profileCenter.nickname.ModifyType
import org.cxct.sportlottery.util.*
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException

/**
 * @app_destination 个人设置
 */
class ProfileActivity : BaseSocketActivity<ProfileModel>(ProfileModel::class) {

    //簡訊驗證彈窗
    private var customSecurityDialog: CustomSecurityDialog? = null
    //KYC驗證彈窗
    private var kYCVerifyDialog: CustomSecurityDialog? = null

    enum class VerifiedType(val value: Int) {
        NOT_YET(0), PASSED(1), VERIFYING(2), VERIFIED_FAILED(3)
    }

    enum class SecurityCodeEnterType(val value: Int) {
        REALNAME(0), PW(1)
    }

    var securityCodeEnter = SecurityCodeEnterType.REALNAME

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
                    this@ProfileActivity,
                    LocalUtils.getString(R.string.error_reading_file)
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
        setContentView(R.layout.activity_profile)

        initView()
        initButton()
        initObserve()
        setupLogout()
    }

    private fun setupLogout() {
        btn_sign_out.setOnClickListener {
            viewModel.doLogoutAPI()
            viewModel.doLogoutCleanUser {
                run {
//                    if (sConfigData?.thirdOpen == FLAG_OPEN)
//                        MainActivity.reStart(this)
//                    else
                    MainTabActivity.reStart(this)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        getUserInfo()
    }

    private fun initView() {
        custom_tool_bar.titleText = LocalUtils.getString(R.string.profile_info)
        sConfigData?.apply {
            ll_qq_number.visibility = if (enableWithdrawQQ == FLAG_OPEN) View.VISIBLE else View.GONE
            ll_e_mail.visibility = if (enableWithdrawEmail == FLAG_OPEN) View.VISIBLE else View.GONE
            ll_phone_number.visibility =
                if (enableWithdrawPhone == FLAG_OPEN) View.VISIBLE else View.GONE
            ll_wechat.visibility =
                if (enableWithdrawWechat == FLAG_OPEN) View.VISIBLE else View.GONE
            ll_real_name.visibility =
                if (enableWithdrawFullName == FLAG_OPEN) View.VISIBLE else View.GONE
        }
        LogUtil.toJson(viewModel.userInfo.value)
        tv_pass_word.text =
            if (viewModel.userInfo.value?.passwordSet == true) LocalUtils.getString(R.string.set) else LocalUtils.getString(
                R.string.edit)
    }

    private fun initButton() {
        custom_tool_bar.setOnBackPressListener {
            finish()
        }

        //設定個人資訊頁面
        setupToInfoSettingPage()

        btn_head.setOnClickListener {
            AvatarSelectorDialog(this, mSelectMediaListener).show(supportFragmentManager, null)
        }
    }

    private fun setupToInfoSettingPage() {
        //真實姓名
        ll_real_name.setOnClickListener {
            securityCodeEnter = SecurityCodeEnterType.REALNAME
            viewModel.checkNeedToShowSecurityDialog()//檢查有需不需要簡訊認證
        }
        //暱稱
        btn_nickname.setOnClickListener { putExtraForProfileInfoActivity(ModifyType.NickName) }
        //密碼設置
        btn_pwd_setting.setOnClickListener {
            securityCodeEnter = SecurityCodeEnterType.PW
            viewModel.checkNeedToShowSecurityDialog()//檢查有需不需要簡訊認證
        }
        //登录授权
        lin_auth.setOnClickListener {
            startActivity(Intent(this@ProfileActivity, AuthActivity::class.java))
        }
        //QQ號碼
        ll_qq_number.setOnClickListener { putExtraForProfileInfoActivity(ModifyType.QQNumber) }
        //郵箱
        ll_e_mail.setOnClickListener { putExtraForProfileInfoActivity(ModifyType.Email) }
        //手機號碼
        ll_phone_number.setOnClickListener { putExtraForProfileInfoActivity(ModifyType.PhoneNumber) }
        //微信
        ll_wechat.setOnClickListener { putExtraForProfileInfoActivity(ModifyType.WeChat) }
        //實名制
        ll_verified.setOnClickListener {
            if (ll_verified.isEnabled)
                startActivity(Intent(this@ProfileActivity, VerifyIdentityActivity::class.java))
        }
        //注销账号
        ll_cancel_account.setOnClickListener{
            startActivity(Intent(this@ProfileActivity, CancelAccountActivity::class.java))
        }
    }

    private fun putExtraForProfileInfoActivity(modifyType: ModifyType) {
        startActivity(
            Intent(
                this@ProfileActivity,
                ModifyProfileInfoActivity::class.java
            ).apply { putExtra(MODIFY_INFO, modifyType) })
    }

    private fun updateAvatar(iconUrl: String?) {
        Glide.with(iv_head.context)
            .load(iconUrl)
            .apply(RequestOptions().placeholder(R.drawable.ic_person_avatar))
            .into(iv_head) //載入頭像
    }

    private fun uploadImg(file: File) {
        val userId = viewModel.userInfo.value?.userId.toString()
        val uploadImgRequest =
            UploadImgRequest(userId, file, UploadImgRequest.PlatformCodeType.AVATAR)
        viewModel.uploadImage(uploadImgRequest)
    }

    private fun initObserve() {

        viewModel.editIconUrlResult.observe(this) {
            val iconUrlResult = it?.getContentIfNotHandled()
            if (iconUrlResult?.success == true) {
                showPromptDialog(
                    LocalUtils.getString(R.string.prompt),
                    LocalUtils.getString(R.string.save_avatar_success)
                ) {}
            } else
                iconUrlResult?.msg?.let { msg -> showErrorPromptDialog(msg) {} }
        }

        viewModel.userInfo.observe(this) {
            updateAvatar(it?.iconUrl)
            tv_nickname.text = it?.nickName
            tv_member_account.text = it?.userName
            tv_id.text = it?.userId?.toString()
            tv_real_name.text = it?.fullName
            ll_verified.isVisible =
                sConfigData?.realNameWithdrawVerified == VerifySwitchType.OPEN.value || sConfigData?.realNameRechargeVerified == VerifySwitchType.OPEN.value
            tv_pass_word.text =
                if (it?.passwordSet == true) LocalUtils.getString(R.string.set) else LocalUtils.getString(R.string.edit)
            when (it?.verified) {
                VerifiedType.PASSED.value -> {
                    ll_verified.isEnabled = false
                    ll_verified.isClickable = false
                    tv_verified.text = LocalUtils.getString(R.string.kyc_passed)

                    icon_identity.visibility = View.GONE
                }
                VerifiedType.NOT_YET.value -> {
                    ll_verified.isEnabled = true
                    ll_verified.isClickable = true
                    tv_verified.text = LocalUtils.getString(R.string.kyc_unverified)

                    icon_identity.visibility = View.VISIBLE
                }
                VerifiedType.VERIFYING.value -> {
                    ll_verified.isEnabled = false
                    ll_verified.isClickable = false
                    tv_verified.text = LocalUtils.getString(R.string.kyc_unverified)

                    icon_identity.visibility = View.GONE
                }
                else -> {
                    ll_verified.isEnabled = true
                    ll_verified.isClickable = true
                    tv_verified.text = LocalUtils.getString(R.string.kyc_unverified)

                    icon_identity.visibility = View.VISIBLE
                }
            }

            if (it?.setted == FLAG_NICKNAME_IS_SET) {
                btn_nickname.isEnabled = false
                icon_arrow_nickname.visibility = View.GONE
            } else {
                btn_nickname.isEnabled = true
                icon_arrow_nickname.visibility = View.VISIBLE
            }

            it?.let { setWithdrawInfo(it) }
        }

        //是否顯示簡訊驗證彈窗
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
                } else if (!b) { //有手機號碼又不用驗證的狀態下
                    when (securityCodeEnter) {
                        SecurityCodeEnterType.REALNAME -> {
                            putExtraForProfileInfoActivity(ModifyType.RealName)
                        }
                        SecurityCodeEnterType.PW -> {
                            startActivity(
                                Intent(
                                    this@ProfileActivity,
                                    SettingPasswordActivity::class.java
                                )
                            )
                        }
                    }
                }
            }
        }

        //簡訊驗證失敗
        viewModel.errorMessageDialog.observe(this) {
            val errorMsg = it ?: LocalUtils.getString(R.string.unknown_error)
            CustomAlertDialog(this).apply {
                setMessage(errorMsg)
                setNegativeButtonText(null)
                setCanceledOnTouchOutside(false)
                setCancelable(false)
            }.show(supportFragmentManager, null)
        }

        //簡訊驗證成功
        viewModel.twoFactorSuccess.observe(this) {
            if (it == true) {
                customSecurityDialog?.dismiss()

                when (securityCodeEnter) {
                    SecurityCodeEnterType.REALNAME -> {
                        putExtraForProfileInfoActivity(ModifyType.RealName)
                    }
                    SecurityCodeEnterType.PW -> {
                        startActivity(
                            Intent(
                                this@ProfileActivity,
                                SettingPasswordActivity::class.java
                            )
                        )
                    }
                }
            }
        }

        //確認收到簡訊驗證碼
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

    private fun setWithdrawInfo(userInfo: UserInfo) {
        userInfo.apply {
            judgeImproveInfo(ll_real_name, tv_real_name, icon_real_name, fullName)
            judgeImproveInfo(ll_qq_number, tv_qq_number, icon_qq_number, qq)
            judgeImproveInfo(ll_e_mail, tv_e_mail, icon_e_mail, email)
            judgeImproveInfo(ll_phone_number, tv_phone_number, icon_phone_number, phone)
            judgeImproveInfo(ll_wechat, tv_we_chat, icon_wechat, wechat)
        }
    }

    private fun judgeImproveInfo(
        itemLayout: LinearLayout,
        tvInfo: TextView,
        iconModify: ImageView,
        infoData: String?
    ) {
        tvInfo.apply {
            if (infoData.isNullOrEmpty()) {
                text = LocalUtils.getString(R.string.need_improve)

                iconModify.visibility = View.VISIBLE
                itemLayout.isEnabled = true
            } else {
                text = infoData

                iconModify.visibility = View.GONE
                itemLayout.isEnabled = false
            }
            setTextColor(ContextCompat.getColor(this@ProfileActivity, R.color.color_939393_999999))
        }
    }

    private fun getUserInfo() {
        viewModel.getUserInfo()
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