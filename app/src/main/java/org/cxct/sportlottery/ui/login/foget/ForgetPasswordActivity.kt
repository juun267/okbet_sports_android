package org.cxct.sportlottery.ui.login.foget

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.HideReturnsTransformationMethod
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.gyf.immersionbar.ImmersionBar
import kotlinx.android.synthetic.main.activity_forget_password.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_register.eet_confirm_password
import kotlinx.android.synthetic.main.view_status_bar.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityForgetPasswordBinding
import org.cxct.sportlottery.network.index.forgetPassword.ForgetSmsResult
import org.cxct.sportlottery.network.index.forgetPassword.ResetPasswordResult
import org.cxct.sportlottery.network.index.sendSms.SmsResult
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.login.checkRegisterListener
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.util.LogUtil
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.util.observe
import org.cxct.sportlottery.widget.boundsEditText.AsteriskPasswordTransformationMethod
import java.util.*

class ForgetPasswordActivity :BaseActivity<ForgetViewModel>(ForgetViewModel::class) {

    private lateinit var binding: ActivityForgetPasswordBinding
    private var mSmsTimer: Timer? = null
    private var page = 1
    private var state = 1

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
    }

    fun initView(){
        binding.eetPhoneNum.checkRegisterListener{viewModel.checkPhone(it)}
        binding.eetSmsCode.checkRegisterListener { viewModel.checkSecurityCode(it) }
        binding.eetLoginPasswordForget.transformationMethod =
            AsteriskPasswordTransformationMethod()
        binding.eetConfirmPasswordForget.transformationMethod =
            AsteriskPasswordTransformationMethod()
        binding.eetLoginPasswordForget.checkRegisterListener { viewModel.checkPassword(it) }
        binding.eetConfirmPasswordForget.checkRegisterListener { viewModel.checkConfirmPassword(eet_login_password_forget.text.toString(),it) }
        binding.btnSendSms.setOnClickListener {
            //先校验手机号码
            viewModel.getSendSms(phoneNum = eet_phone_num.text.toString())
        }
        binding.btnPut.setOnClickListener{
            if (page ==3){
                //提交修改密码的逻辑
                startActivity(Intent(this@ForgetPasswordActivity, LoginActivity::class.java))
            }
            if (page<3){
                if (page ==1){
                   viewModel.getCheckPhone(
                        phone = eet_phone_num.text.toString(),
                        validCode = eet_sms_code.text.toString()
                    )
                    return@setOnClickListener

                }
                if (page == 2){
                    viewModel.resetPassword(phone = eet_phone_num.text.toString(),
                    confirmPassword = eet_confirm_password_forget.text.toString(),
                        newPassword = eet_login_password_forget.text.toString())
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
    }
    //数据回调
    private fun initObserve() {
        viewModel.smsResult.observe(this) {
            updateUiWithResult(it)
        }
        viewModel.smsCodeResult.observe(this){
            it?.let { result->
                if (!result.success){
                    binding.etSmsValidCode.setError(result.msg,false)
                }else{
                    page++
                    setPage()
                }
            }
        }
        viewModel.resetPasswordResult.observe(this){
            if (it?.success == true){
                page++
                setPage()
                tv_user_name.text =  it.ResetPasswordData?.userName
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
                1 -> {
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
            1->{
                binding.firstPager.visibility = View.VISIBLE
                binding.clPassword.visibility = View.GONE
                binding.clSuccess.visibility = View.GONE
                binding.labelRegister.text = getString(R.string.please_get_forget_password)
                binding.btnPut.text = getString(R.string.next_step)
            }
            2 ->{
                binding.firstPager.visibility = View.GONE
                binding.clPassword.visibility = View.VISIBLE
                binding.clSuccess.visibility = View.GONE
                binding.labelRegister.text = getString(R.string.please_set_forget_password)
                binding.btnPut.text = getString(R.string.submit)
            }
            3->{
                binding.firstPager.visibility = View.GONE
                binding.clPassword.visibility = View.GONE
                binding.clSuccess.visibility = View.VISIBLE
                binding.labelRegister.text = getString(R.string.please_set_forget_password)
                binding.btnPut.text = getString(R.string.to_back_login)
            }
        }
    }
    //发送验证码
    @SuppressLint("SetTextI18n")
    private fun updateUiWithResult(smsResult: SmsResult?) {
        binding.btnSendSms.isEnabled = true
        if (smsResult?.success == true) {
            state +=1
            binding.tvSmsSend.visibility = View.VISIBLE
            binding.tvSmsSend2.visibility = View.VISIBLE
            binding.tvSmsSend2.text = " +63 ${TextUtil.maskPhoneNum(eet_phone_num.text.toString())}"
            showSmeTimer300()
        } else {
            binding.tvSmsSend.visibility = View.GONE
            binding.tvSmsSend2.visibility = View.GONE
            //做异常处理
            binding.etSmsValidCode.setError(smsResult?.msg,false)
        }
    }
    //发送验证码开始倒计时
    private fun showSmeTimer300() {
        try {
            stopSmeTimer()
            var sec = 120
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
                                binding.btnSendSms.text = getString(R.string.get_phone_code)
                            }else{
                                binding.btnSendSms.text = getString(R.string.reget_phone_code)
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
                binding.btnSendSms.text = getString(R.string.get_phone_code)
            }else{
                binding.btnSendSms.text = getString(R.string.reget_phone_code)
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
}