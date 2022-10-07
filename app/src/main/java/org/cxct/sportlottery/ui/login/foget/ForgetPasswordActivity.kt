package org.cxct.sportlottery.ui.login.foget

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_forget_password.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_register.eet_confirm_password
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityForgetPasswordBinding
import org.cxct.sportlottery.network.index.sendSms.SmsResult
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.login.checkRegisterListener
import org.cxct.sportlottery.util.observe
import java.util.*

class ForgetPasswordActivity :BaseActivity<ForgetViewModel>(ForgetViewModel::class) {

    private lateinit var binding: ActivityForgetPasswordBinding
    private var mSmsTimer: Timer? = null
    private var page = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        setPage()
        setupBackButton()
        initObserve()
    }

    fun initView(){
        binding.eetPhone.checkRegisterListener{viewModel.checkPhone(it)}
        binding.eetSmsValidCode.checkRegisterListener { viewModel.checkSecurityCode(it) }
        binding.eetLoginPassword.checkRegisterListener { viewModel.checkPassword(it) }
        binding.eetConfirmPasswordForget.checkRegisterListener { viewModel.checkConfirmPassword(it,confirmPassword = eet_confirm_password_forget.text.toString()) }
        binding.btnSendSms.setOnClickListener {
            //先校验手机号码
        }
        binding.btnPut.setOnClickListener{
            if (page ==2){
                //提交修改密码的逻辑
            }
            if (page<2){
                page++
                setPage()
                viewModel.focusChangeCheckAllInputComplete(page)
            }
        }
    }
    //数据回调
    private fun initObserve() {
        viewModel.smsResult.observe(this) {
            updateUiWithResult(it)
        }
        viewModel.putEnable.observe(this){
            binding.btnPut.isEnabled = it
            if (it){
                binding.btnPut.alpha = 1.0f
            }else{
                binding.btnPut.alpha = 0.5f
            }
        }
        viewModel.phoneMsg.observe(this){
            binding.etPhone.setError(it.first, false)
        }
        viewModel.passwordMsg.observe(this){
            binding.etLoginPassword.setError(it.first,false)
        }
        viewModel.confirmPasswordMsg.observe(this){
            binding.etConfirmPassword.setError(it.first,false)
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
                binding.btnPut.text = getString(R.string.next_step)
            }
            2 ->{binding.firstPager.visibility = View.GONE
                binding.clPassword.visibility = View.VISIBLE
                    binding.btnPut.text = getString(R.string.submit)
            }
        }
    }
    //发送验证码
    private fun updateUiWithResult(smsResult: SmsResult?) {
        binding.btnSendSms.isEnabled = true
        if (smsResult?.success == true) {
            showSmeTimer300()
        } else {
            //做异常处理
        }
    }
    //发送验证码开始倒计时
    private fun showSmeTimer300() {
        try {
            stopSmeTimer()

            var sec = 60
            mSmsTimer = Timer()
            mSmsTimer?.schedule(object : TimerTask() {
                override fun run() {
                    Handler(Looper.getMainLooper()).post {
                        if (sec-- > 0) {
                            binding.btnSendSms.isEnabled = false
                            binding.btnSendSms.text = "${sec}s"
                            binding.btnSendSms.setTextColor(
                                ContextCompat.getColor(
                                    this@ForgetPasswordActivity,
                                    R.color.color_AEAEAE_404040
                                )
                            )
                        } else {
                            stopSmeTimer()
                            binding.btnSendSms.isEnabled = true
                            binding.btnSendSms.text = getString(R.string.get_verification_code)
                            binding.btnSendSms.setTextColor(Color.WHITE)
                        }
                    }
                }
            }, 0, 1000) //在 0 秒後，每隔 1000L 毫秒執行一次
        } catch (e: Exception) {
            e.printStackTrace()

            stopSmeTimer()
            binding.btnSendSms.isEnabled = true
            binding.btnSendSms.text = getString(R.string.get_verification_code)
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