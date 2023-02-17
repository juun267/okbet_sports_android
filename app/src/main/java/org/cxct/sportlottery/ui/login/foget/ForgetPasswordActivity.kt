package org.cxct.sportlottery.ui.login.foget

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.HideReturnsTransformationMethod
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.gyf.immersionbar.ImmersionBar
import kotlinx.android.synthetic.main.activity_forget_password.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.transfer_pay_fragment.*
import kotlinx.android.synthetic.main.view_status_bar.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityForgetPasswordBinding
import org.cxct.sportlottery.network.index.forgetPassword.SendSmsResult
import org.cxct.sportlottery.network.index.forgetPassword.ValidateUserResult
import org.cxct.sportlottery.network.index.validCode.ValidCodeResult
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.game.ServiceDialog
import org.cxct.sportlottery.ui.login.checkRegisterListener
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.widget.boundsEditText.AsteriskPasswordTransformationMethod
import java.util.*

/**
 * @app_destination 忘记密码
 */
class ForgetPasswordActivity :BaseActivity<ForgetViewModel>(ForgetViewModel::class) {

    private lateinit var binding: ActivityForgetPasswordBinding
    private var mSmsTimer: Timer? = null
    private var page = 0
    private var state = 1
    private var userName: String? = null
    private var mIdentity: String? = null
    private  var secs: Int = 120
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ImmersionBar.with(this)
            .statusBarDarkFont(true)
            .transparentStatusBar()
            .statusBarView(v_statusbar)
            .fitsSystemWindows(false)
            .init()
        binding = ActivityForgetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        setPage()
        setupBackButton()
        initObserve()
        viewModel.focusChangeCheckAllInputComplete(page)
        viewModel.smsCheckComplete()
        updateValidCode()
    }

    fun initView(){
        binding.eetAccountForget.checkRegisterListener { viewModel.checkAccount(it) }
        binding.eetVerificationCodeForget.checkRegisterListener { viewModel.checkValidCode(it) }
        binding.eetPhoneNum.checkRegisterListener{viewModel.checkPhone(it)}
        binding.eetSmsCode.checkRegisterListener { viewModel.checkSecurityCode(it) }
        binding.eetLoginPasswordForget.transformationMethod =
            AsteriskPasswordTransformationMethod()
        binding.eetConfirmPasswordForget.transformationMethod =
            AsteriskPasswordTransformationMethod()
        binding.eetLoginPasswordForget.checkRegisterListener { viewModel.checkPassword(it) }
        binding.eetConfirmPasswordForget.checkRegisterListener { viewModel.checkConfirmPassword(eet_login_password_forget.text.toString(),it) }

        binding.ivReturn.setOnClickListener { updateValidCode() }
        binding.btnSendSms.setOnClickListener {
            //先校验手机号码
//            viewModel.getSendSms(phone = eet_phone_num.text.toString(),/*userName = eet_account_forget.text.toString()*/)
        }
        binding.btnPut.setOnClickListener{
            if (page ==3){
                //提交修改密码的逻辑
              //  startActivity(Intent(this@ForgetPasswordActivity, LoginActivity::class.java))
                finish()
            }
            if (page<3){
                if (page == 0){
                    mIdentity?.let {
                        viewModel.checkValidateUser(validCode = eet_verification_code_forget.text.toString(),
                            userName = eet_account_forget.text.toString(),
                            validCodeIdentity = mIdentity!!
                        )
                    }
                    return@setOnClickListener
                }
                if (page ==1){
                   viewModel.getCheckPhone(
                        phone = eet_phone_num.text.toString(),
                        validCode = eet_sms_code.text.toString()
                    )
                    return@setOnClickListener

                }
                if (page == 2){
                    val confirmPassword = eet_confirm_password_forget.text.toString()
                    val newPassword = eet_login_password_forget.text.toString()
                    userName?.let { userName ->
                        viewModel.resetPassword(userName = userName,
                            confirmPassword = MD5Util.MD5Encode(confirmPassword),
                            newPassword = MD5Util.MD5Encode(newPassword))
                    }
                    return@setOnClickListener
                }
                page++
                setPage()
                viewModel.focusChangeCheckAllInputComplete(page)
            }
        }

        binding.apply {
            etLoginPassword.endIconImageButton.setOnClickListener {
                if (etLoginPassword.endIconResourceId == R.drawable.ic_eye_open){
                    eetLoginPasswordForget.transformationMethod =
                        AsteriskPasswordTransformationMethod()
                    etLoginPassword.setEndIcon(R.drawable.ic_eye_close)
                } else {
                    etLoginPassword.setEndIcon(R.drawable.ic_eye_open)
                    eetLoginPasswordForget.transformationMethod =
                        HideReturnsTransformationMethod.getInstance()
                }
                etLoginPassword.hasFocus = true
                eetLoginPasswordForget.setSelection(eetLoginPasswordForget.text.toString().length)
            }

            etConfirmPasswordForget.endIconImageButton.setOnClickListener {
                if (etConfirmPasswordForget.endIconResourceId == R.drawable.ic_eye_open){
                    eetConfirmPasswordForget.transformationMethod =
                        AsteriskPasswordTransformationMethod()
                    etConfirmPasswordForget.setEndIcon(R.drawable.ic_eye_close)
                } else {
                    etConfirmPasswordForget.setEndIcon(R.drawable.ic_eye_open)
                    eetConfirmPasswordForget.transformationMethod =
                        HideReturnsTransformationMethod.getInstance()
                }
                etConfirmPasswordForget.hasFocus = true
                eetConfirmPasswordForget.setSelection(eetConfirmPasswordForget.text.toString().length)
            }
        }
        binding.clLiveChat.setOnClickListener {
            val serviceUrl = sConfigData?.customerServiceUrl
            val serviceUrl2 = sConfigData?.customerServiceUrl2
            when {
                !serviceUrl.isNullOrBlank() && !serviceUrl2.isNullOrBlank() -> {
                    ServiceDialog().show(supportFragmentManager, null)
                }
                serviceUrl.isNullOrBlank() && !serviceUrl2.isNullOrBlank() -> {
                    JumpUtil.toExternalWeb(this@ForgetPasswordActivity, serviceUrl2)
                }
                !serviceUrl.isNullOrBlank() && serviceUrl2.isNullOrBlank() -> {
                    JumpUtil.toExternalWeb(this@ForgetPasswordActivity, serviceUrl)
                }
            }
        }
    }
    //数据回调
    @SuppressLint("SetTextI18n")
    private fun initObserve() {
        viewModel.accountMsg.observe(this){
            if (it.first == null) {
                viewModel.checkAccountExist(binding.eetAccountForget.text.toString())
                return@observe
            } else {
                binding.etAccount.setError(
                    it.first,
                    false
                )
            }
        }
        viewModel.accountCodeMsg.observe(this) {
            binding.etVerificationCode.setError(it.first, false)
        }
        viewModel.validDateResult.observe(this) {
            updateUiWithResult(it)
        }
        viewModel.smsResult.observe(this) {
            updateUiWithResult(it)
        }
        viewModel.validCodeResult.observe(this) {
            updateUiWithResult(it)
        }
        viewModel.smsCodeResult.observe(this) {
            it?.let { result->
                if (!result.success){
                    if (result.code == 2765||result.code == 2766){
                        binding.etPhone.setError(result.msg,false)
                    }else{
                        binding.etSmsValidCode.setError(result.msg,false)
                    }
                }else{
                    page++
                    setPage()
                }
                LogUtil.toJson(result)
            }
//            page++
//            setPage()
        }


        viewModel.resetPasswordResult.observe(this){
            if (it?.success == true){
                page++
                setPage()
                tv_user_name.text =  getString(R.string.member_name)+": "+it.ResetPasswordData?.userName
            }else{
                ToastUtil.showToast(this,it?.msg,Toast.LENGTH_LONG)
                return@observe
            }

        }
        viewModel.smsCheckComplete()
        viewModel.putEnable.observe(this){
            binding.btnPut.isEnabled = it
            if (it){
                binding.btnPut.alpha = 1.0f
            }else{
                binding.btnPut.alpha = 0.5f
            }
        }
        viewModel.smsEnable.observe(this){
            binding.btnSendSms.isEnabled = it
            if (it){
                binding.btnSendSms.alpha = 1.0f
            }else {
                binding.btnSendSms.alpha = 0.5f
            }
        }
        viewModel.phoneMsg.observe(this){
            binding.etPhone.setError(it.first, false)
        }
        viewModel.passwordMsg.observe(this){
            binding.etLoginPassword.setError(it.first,false)
        }
        viewModel.confirmPasswordMsg.observe(this){
            binding.etConfirmPasswordForget.setError(it.first,false)
        }
        viewModel.validateCodeMsg.observe(this){
            binding.etSmsValidCode.setError(it.first,false)
        }

        viewModel.focusChangeCheckAllInputComplete(page)
    }

    //返回键拦截
    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            when (page) {
                0-> {
                    finish()
                }
                3-> {
                   // startActivity(Intent(this@ForgetPasswordActivity, MainTabActivity::class.java))
                    finish()
                }
                else -> {
                    page--
                    setPage()
                    viewModel.focusChangeCheckAllInputComplete(page)
                }
            }

        }
    }
    //页面显示隐藏
    private fun setPage() {
        when (page) {
            0-> {
                binding.clAccount.visibility = View.VISIBLE
                binding.firstPager.visibility = View.GONE
                binding.clPassword.visibility = View.GONE
                binding.clSuccess.visibility = View.GONE
                binding.btnPut.text = getString(R.string.next_step)
            }
            1->{
                binding.firstPager.visibility = View.VISIBLE
                binding.clAccount.visibility = View.GONE
                binding.clPassword.visibility = View.GONE
                binding.clSuccess.visibility = View.GONE
//                adjustEnableLoginButton(false)
               // binding.labelRegister.text = getString(R.string.please_get_forget_password)
                binding.btnPut.text = getString(R.string.next_step)
            }
            2 ->{
                binding.clAccount.visibility = View.GONE
                binding.firstPager.visibility = View.GONE
                binding.clPassword.visibility = View.VISIBLE
                binding.clSuccess.visibility = View.GONE
                adjustEnableLoginButton(false)
              // binding.labelRegister.text = getString(R.string.please_set_forget_password)
                binding.btnPut.text = getString(R.string.submit)
            }
            3->{
                binding.clAccount.visibility = View.GONE
                binding.firstPager.visibility = View.GONE
                binding.clPassword.visibility = View.GONE
                binding.clLiveChat.visibility = View.GONE
                binding.clSuccess.visibility = View.VISIBLE
              //  binding.labelRegister.text = getString(R.string.please_set_forget_password)
                binding.btnPut.text = getString(R.string.to_back_login)
            }
        }
    }
    //发送验证码
    @SuppressLint("SetTextI18n")
    private fun updateUiWithResult(smsResult: SendSmsResult?) {
        binding.btnSendSms.isEnabled = true
        if (smsResult?.success == true) {
            userName = smsResult.ResetPasswordData?.userName
            state +=1
         //   binding.tvSmsSend.visibility = View.VISIBLE

//            val tipsContentBuilder = SpannableStringBuilder()
//            val stringSpan = SpannableString(getString(R.string.has_send_message_to_phone))
//            val phoneNum = SpannableString(" +63 ${TextUtil.maskPhoneNum(eet_phone_num.text.toString())}")
//            phoneNum.length.let {
//                phoneNum.setSpan(
//                    StyleSpan(Typeface.NORMAL),
//                    0,
//                    it,
//                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
//                phoneNum.setSpan(//字体颜色
//                    ForegroundColorSpan(resources.getColor(R.color.color_025BE8)),
//                    0,
//                    it,
//                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
//                phoneNum.setSpan( //字体大小
//                    AbsoluteSizeSpan(14, true),
//                    0,
//                    it,
//                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
//            }
      //      binding.tvSmsSend.text = tipsContentBuilder.append(stringSpan).append(phoneNum)
            showSmeTimer300()
        } else {
         //   binding.tvSmsSend.visibility = View.GONE
            //做异常处理
            if (smsResult?.code == 2765||smsResult?.code == 2766){
                binding.etPhone.setError(smsResult.msg,false)
            }else{
                binding.etSmsValidCode.setError(smsResult?.msg,false)
            }
           // binding.etSmsValidCode.setError(smsResult?.msg,false)
        }
    }

//获取随机验证码
    private fun updateValidCode() {
        val data = viewModel.validCodeResult.value?.validCodeData
        viewModel.getValidCode(data?.identity)
        binding.eetVerificationCodeForget.apply {
            if (text.isNotBlank()) {
                text = null
            }
        }
    }
//图形验证码展示
    private fun updateUiWithResult(validCodeResult: ValidCodeResult?) {
        if (validCodeResult?.success == true) {
            val bitmap = BitmapUtil.stringToBitmap(validCodeResult.validCodeData?.img)
            Glide.with(this)
                .load(bitmap)
                .into(binding.ivVerification)
            mIdentity = validCodeResult?.validCodeData?.identity
        } else {
            updateValidCode()
            ToastUtil.showToastInCenter(
                this@ForgetPasswordActivity,
                getString(R.string.get_valid_code_fail_point)
            )
        }
    }
    private fun updateUiWithResult(validateUserResult: ValidateUserResult?){
        if(validateUserResult?.success == true) {
             validateUserResult.validData?.countDownSec?.let {
                 secs = it
                 page++
                 setPage()
            }
        }else{
            if (validateUserResult?.code==2751){
                viewModel._accountMsg.value = Pair(validateUserResult?.msg, false)
                binding.etAccount.setError(validateUserResult?.msg,false)
            }else{
                viewModel._accountCodeMsg.value = Pair(validateUserResult?.msg, false)
                binding.etVerificationCode.setError(validateUserResult?.msg,false)
            }
            viewModel.focusChangeCheckAllInputComplete(page)
        }
    }
    //发送验证码开始倒计时
    private fun showSmeTimer300() {
        try {
            stopSmeTimer()
            var sec = secs
            mSmsTimer = Timer()
            mSmsTimer?.schedule(object : TimerTask() {
                override fun run() {
                    Handler(Looper.getMainLooper()).post {
                        if (sec-- > 0) {
                            binding.btnSendSms.isEnabled = false
                            binding.btnSendSms.setBackgroundResource(R.drawable.bg_unennable_timer)
                            binding.btnSendSms.text = "${sec}s"
                            binding.btnSendSms.setTextColor(
                                ContextCompat.getColor(
                                    this@ForgetPasswordActivity,
                                    R.color.color_FFFFFF
                                )
                            )
                        } else {
                            stopSmeTimer()
                            binding.btnSendSms.isEnabled = true
                            binding.btnSendSms.setBackgroundResource(R.drawable.btn_send_sms)

                            if (state == 1){
                                binding.btnSendSms.text = getString(R.string.get_security_code)
                            }else{
                                binding.btnSendSms.text = getString(R.string.reget_phone_code_for)
                            }
                            binding.btnSendSms.setTextColor(Color.WHITE)
                        }
                    }
                }
            }, 0, 1000) //在 0 秒後，每隔 1000L 毫秒執行一次
        } catch (e: Exception) {
            e.printStackTrace()
            stopSmeTimer()
            binding.btnSendSms.isEnabled = true
            if (state == 1){
                binding.btnSendSms.text = getString(R.string.get_security_code)
            }else{
                binding.btnSendSms.text = getString(R.string.reget_phone_code_for)
            }

        }
    }

    private fun stopSmeTimer() {
        if (mSmsTimer != null) {
            mSmsTimer?.cancel()
            mSmsTimer = null
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        stopSmeTimer()
    }

    private fun adjustEnableLoginButton(isEnable: Boolean) {
        if (isEnable) {
            binding.btnPut.alpha = 1.0f
        }else{
            binding.btnPut.alpha = 0.5f
        }
        binding.btnPut.isEnabled = isEnable
    }
}