package org.cxct.sportlottery.ui.login.foget

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.ForgetPwdSelectAccountEvent
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.ActivityForgetPassword2Binding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.index.forgetPassword.ResetPasswordData
import org.cxct.sportlottery.network.index.forgetPassword.SendSmsResult
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.login.VerifyCallback
import org.cxct.sportlottery.ui.login.foget.reset.ResetPasswordActivity
import org.cxct.sportlottery.ui.login.selectAccount.SelectAccountActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.checkEmail
import org.cxct.sportlottery.view.checkPhoneNum
import org.cxct.sportlottery.view.checkSMSCode
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import splitties.activities.start

/**
 * @app_destination 通过手机号或者邮箱重置登录密码
 */
class ForgetPasswordActivity: BaseActivity<ForgetViewModel,ActivityForgetPassword2Binding>(ForgetViewModel::class)
 ,VerifyCallback {

    companion object {

        fun startByPhoneWays(context: Activity) = start(context, 1) // 1: 通过手机号方式修改密码
        fun startByEmailWays(context: Activity)  = start(context, 2) // 2:通过邮箱方式修改密码

        private fun start(context: Activity, ways: Int) {
            val intent = Intent(context, ForgetPasswordActivity::class.java)
            intent.putExtra("retrieveWays", ways)
            context.startActivityForResult(intent, 100)
        }
    }

    private val ways by lazy { intent.getIntExtra("retrieveWays", 1) }
    private var inputPhoneNo: String? = null // 输入的手机号，不为空即为输入的号码格式正确
    private var inputEmail: String? = null // 输入的邮箱，不为空即为输入的号码格式正确
    private var smsCode: String? = null // 短信或者邮箱验证码
    private var userName: String? = null

    private inline fun isPhoneWays() = 1 == ways

    override fun onInitView() {
        setStatusBarDarkFont()
        initView()
        initObserve()
        EventBusUtil.targetLifecycle(this)
    }

    private fun initView() = binding.run {
        bindFinish(btnBack)
        clLiveChat.setServiceClick(supportFragmentManager)
        btnPut.setOnClickListener { next() }
//        btnSendSms.setOnClickListener { YidunCaptcha.validateAction(this@ForgetPasswordActivity2,this@ForgetPasswordActivity2, { sendCode() }) }

        btnSendSms.setOnClickListener {
            hideSoftKeyboard()
            showCaptchaDialog()
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
        bottomLiences.tvLicense.text = Constants.copyRightString
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
        CountDownUtil.smsCountDown(this@ForgetPasswordActivity.lifecycleScope,
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
        hideSoftKeyboard()
        if (isPhoneWays()) {
            viewModel.getCheckPhone("$inputPhoneNo", "$smsCode")
        } else {
            viewModel.checkEmailCode("$inputEmail", "$smsCode")
        }
    }

    protected open fun sendSMSCode() {

    }

    protected open fun sendEmailCode() {

    }

    private fun initObserve() = viewModel.run {
        smsResult.observe(this@ForgetPasswordActivity) { updateUiWithResult(it) }
        smsCodeResult.observe(this@ForgetPasswordActivity) { result-> // 验证短信验证码
            hideLoading()
            if (result == null) {
                return@observe
            }

            if (result.success){
                ResetPasswordActivity.start(this@ForgetPasswordActivity, "$userName", isPhoneWays())
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
            when{
                smsResult.t!= null->{
                    dealWithSmsResultata(smsResult.t)
                }
                smsResult.rows?.size == 1->{
                    dealWithSmsResultata(smsResult.rows[0])
                }
                smsResult.rows?.size == 2->{
                   start<SelectAccountActivity> {
                       putExtra(SelectAccountActivity.TYPE_SELECT,SelectAccountActivity.TYPE_FORGET)
                   }
                }
            }
            return
        }

        binding.btnSendSms.setBtnEnable(true)
        ToastUtil.showToast(this@ForgetPasswordActivity, smsResult?.msg, Toast.LENGTH_SHORT)
        //做异常处理
        if (smsResult?.code == 2765 || smsResult?.code == 2766) {
            binding.etPhone.setError(smsResult.msg,false)
        } else {
            binding.etSmsValidCode.setError(smsResult?.msg,false)
        }
    }
    fun dealWithSmsResultata(resetPasswordData: ResetPasswordData){
        userName = resetPasswordData.userName
        codeCountDown()
        val msg = resetPasswordData.msg
        if(!msg.isEmptyStr()) {
            ToastUtil.showToast(this@ForgetPasswordActivity, msg, Toast.LENGTH_SHORT)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            finishWithOK()
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSelectAccount(event: ForgetPwdSelectAccountEvent) {
        val result = viewModel.smsResult.value
        result?.rows?.let {
            it.first { it.vipType==(if(event.isVip) 1 else 0)}.let {
                dealWithSmsResultata(it)
            }
        }
    }

    override fun onVerifySucceed(identity: String, validCode: String, tag: String?) {
        with(binding) {
            sendCode(identity, validCode)
            eetSmsCode.requestFocus()
        }
    }

}