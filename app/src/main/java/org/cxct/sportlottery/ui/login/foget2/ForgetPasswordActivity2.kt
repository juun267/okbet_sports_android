package org.cxct.sportlottery.ui.login.foget2

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityForgetPassword2Binding
import org.cxct.sportlottery.common.extentions.bindFinish
import org.cxct.sportlottery.common.extentions.finishWithOK
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.network.index.forgetPassword.SendSmsResult
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.login.*
import org.cxct.sportlottery.ui.login.foget.ForgetViewModel
import org.cxct.sportlottery.ui.login.foget2.rest.ResetPasswordActivity
import org.cxct.sportlottery.util.CountDownUtil
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.util.setBtnEnable
import org.cxct.sportlottery.util.setServiceClick

/**
 * @app_destination 通过手机号或者邮箱重置登录密码
 */
class ForgetPasswordActivity2: BaseActivity<ForgetViewModel>(ForgetViewModel::class) {

    companion object {

        fun startByPhoneWays(context: Activity) = start(context, 1) // 1: 通过手机号方式修改密码
        fun startByEmailWays(context: Activity)  = start(context, 2) // 2:通过邮箱方式修改密码

        private fun start(context: Activity, ways: Int) {
            val intent = Intent(context, ForgetPasswordActivity2::class.java)
            intent.putExtra("retrieveWays", ways)
            context.startActivityForResult(intent, 100)
        }
    }

    private val binding by lazy { ActivityForgetPassword2Binding.inflate(layoutInflater) }
    private val ways by lazy { intent.getIntExtra("retrieveWays", 1) }
    private var inputPhoneNo: String? = null // 输入的手机号，不为空即为输入的号码格式正确
    private var inputEmail: String? = null // 输入的邮箱，不为空即为输入的号码格式正确
    private var smsCode: String? = null // 短信或者邮箱验证码
    private var userName: String? = null

    private inline fun isPhoneWays() = 1 == ways

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBarDarkFont()
        setContentView(binding.root)
        initView()
        initObserve()
    }

    private fun initView() = binding.run {
        bindFinish(btnBack)
        clLiveChat.setServiceClick(supportFragmentManager)
        btnPut.setOnClickListener { next() }
//        btnSendSms.setOnClickListener { YidunCaptcha.validateAction(this@ForgetPasswordActivity2,this@ForgetPasswordActivity2, { sendCode() }) }

        btnSendSms.setOnClickListener {
            hideSoftKeyboard(this@ForgetPasswordActivity2)
            VerifyCodeDialog(callBack = { identity, validCode ->
                sendCode(identity, validCode)
                eetSmsCode.requestFocus()
            }).show(supportFragmentManager, null)
        }

        eetSmsCode.checkSMSCode(etSmsValidCode) {
            smsCode = it
            setNextBtnStatus()
        }

        if (isPhoneWays()) {
            etEMail.gone()
            eetPhoneNum.checkPhoneNum(etPhone) {
                inputPhoneNo = it
                onNewSMSStatus()
            }
            return@run
        }

        etPhone.gone()
        eetEMail.checkEmail(etEMail) {
            inputEmail = it
            onNewSMSStatus()
        }
    }

    private fun setNextBtnStatus() {
        binding.btnPut.setBtnEnable(smsCode != null && (inputPhoneNo != null || inputEmail != null))
    }

    private fun onNewSMSStatus()  {

        val inputEffective = inputPhoneNo != null || inputEmail != null

        if (!inputEffective) {
            binding.btnSendSms.setBtnEnable(inputEffective)
            setNextBtnStatus()
            return
        }

        if (binding.btnSendSms.tag == null) {
            binding.btnSendSms.setBtnEnable(true)
        }
    }

    private fun sendCode(identity: String?, validCode: String) = binding.btnSendSms.run {
        loading()
        setBtnEnable(false)
        if (isPhoneWays()) {
            viewModel.getSendSms("$inputPhoneNo", "$identity", validCode)
        } else {
            viewModel.sendEmail("$inputEmail", "$identity", validCode)
        }
    }

    private fun codeCountDown() = binding.btnSendSms.run  {
        if (tag != null) {
            return@run
        }

        tag = this
        CountDownUtil.smsCountDown(this@ForgetPasswordActivity2,
            { setBtnEnable(false) },
            { text = "${it}s" },
            {
                tag = null
                setBtnEnable(true)
                setTextColor(Color.WHITE)
                setText(R.string.send)
            }
        )
    }

    private fun next() {
        loading()
        hideSoftKeyboard(this@ForgetPasswordActivity2)
        if (isPhoneWays()) {
            viewModel.getCheckPhone("$inputPhoneNo", "$smsCode")
        } else {
            viewModel.checkEmailCode("$inputEmail", "$smsCode")
        }
    }

    private fun initObserve() = viewModel.run {
        smsResult.observe(this@ForgetPasswordActivity2) { updateUiWithResult(it) }
        smsCodeResult.observe(this@ForgetPasswordActivity2) { result-> // 验证短信验证码
            hideLoading()
            if (result == null) {
                return@observe
            }

            if (result.success){
                ResetPasswordActivity.start(this@ForgetPasswordActivity2, "$userName", isPhoneWays())
                return@observe
            }

            if (result.code == 2765|| result.code == 2766) {
                binding.etPhone.setError(result.msg,false)
            } else {
                binding.etSmsValidCode.setError(result.msg,false)
            }
        }
    }

    //发送验证码的回调
    private fun updateUiWithResult(smsResult: SendSmsResult?) {
        hideLoading()
        if (smsResult?.success == true) {
            userName = smsResult.ResetPasswordData?.userName
            codeCountDown()
//            CountDownUtil.targSMSTimeStamp()
            val msg = smsResult.ResetPasswordData?.msg
            if(!msg.isEmptyStr()) {
                ToastUtil.showToast(this@ForgetPasswordActivity2, msg, Toast.LENGTH_SHORT)
            }
            return
        }

        binding.btnSendSms.setBtnEnable(true)
        ToastUtil.showToast(this@ForgetPasswordActivity2, smsResult?.msg, Toast.LENGTH_SHORT)
        //做异常处理
        if (smsResult?.code == 2765 || smsResult?.code == 2766) {
            binding.etPhone.setError(smsResult.msg,false)
        } else {
            binding.etSmsValidCode.setError(smsResult?.msg,false)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            finishWithOK()
        }
    }

}