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
import org.cxct.sportlottery.view.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import splitties.activities.start

/**
 * @app_destination 通过手机号或者邮箱重置登录密码
 */
class ForgetPasswordActivity: BaseActivity<ForgetViewModel,ActivityForgetPassword2Binding>(ForgetViewModel::class)
 ,VerifyCallback {

    override fun pageName() = "密码找回验证手机号或者邮箱页面"

    companion object {
        const val WAY_PHONE = 1
        const val WAY_EMAIL = 2
        const val WAY_QUESTION = 3
        fun startByPhoneWays(context: Activity) = start(context, WAY_PHONE) // 1: 通过手机号方式修改密码
        fun startByEmailWays(context: Activity)  = start(context, WAY_EMAIL) // 2:通过邮箱方式修改密码
        fun startByQuestionWays(context: Activity)  = start(context, WAY_QUESTION) // 2:通过密保问题方式修改密码

        private fun start(context: Activity, ways: Int) {
            val intent = Intent(context, ForgetPasswordActivity::class.java)
            intent.putExtra("retrieveWays", ways)
            context.startActivityForResult(intent, 100)
        }
    }

    private val ways by lazy { intent.getIntExtra("retrieveWays", 1) }
    private var inputPhoneNo: String? = null // 输入的手机号，不为空即为输入的号码格式正确
    private var inputEmail: String? = null // 输入的邮箱，不为空即为输入的号码格式正确
    private var inputUserName: String? = null // 输入的用户名/手机号/邮箱，不为空即为输入格式正确
    private var smsCode: String? = null // 短信或者邮箱验证码
    private var userName: String? = null
    private var phoneWithUserName: String? = null // userName对应的手机号

    override fun onInitView() {
        setStatusBarDarkFont()
        initView()
        initObserve()
        EventBusUtil.targetLifecycle(this)
    }

    private fun initView() = binding.run {
        bindFinish(btnBack)
        clLiveChat.setServiceClick(supportFragmentManager)
        bottomLiences.tvLicense.text = Constants.copyRightString
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

        when(ways){
            WAY_PHONE->{
                etEMail.gone()
                etUsername.gone()
                eetPhoneNum.checkPhoneNum(etPhone) {
                    inputPhoneNo = it
                    onNewSMSStatus()
                }
            }
            WAY_EMAIL->{
                etPhone.gone()
                etUsername.gone()
                eetEMail.checkEmail(etEMail) {
                    inputEmail = it
                    onNewSMSStatus()
                }
            }
            WAY_QUESTION->{
                etPhone.gone()
                etEMail.gone()
                blockSmsValidCode.gone()
                eetUsername.excludeInputChar("#")
                eetUsername.checkUserName(etUsername) {
                    inputUserName = it
                    onNewSMSStatus()
                }
            }
        }
    }

    private fun setNextBtnStatus() {
        binding.btnPut.setBtnEnable(smsCode != null
                && (inputPhoneNo != null || inputEmail != null)
                && (userName != null && inputPhoneNo == phoneWithUserName))
    }

    private fun onNewSMSStatus()  {
        if (ways== WAY_QUESTION){
            binding.btnPut.setBtnEnable(!inputUserName.isNullOrEmpty())
        }else{
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
    }

    private fun sendCode(identity: String?, validCode: String) = binding.btnSendSms.run {
        loading()
        setBtnEnable(false)
        when(ways){
            WAY_PHONE-> viewModel.getSendSms("$inputPhoneNo", "$identity", validCode)
            WAY_EMAIL-> viewModel.sendEmail("$inputEmail", "$identity", validCode)
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
        hideSoftKeyboard()
        when (ways) {
            WAY_PHONE -> {
                ResetPasswordActivity.start(
                    this@ForgetPasswordActivity,
                    "$userName",
                    inputPhoneNo,
                    smsCode,
                    1
                )
            }
            WAY_EMAIL -> {
                loading()
                viewModel.checkEmailCode("$inputEmail", "$smsCode")
            }
            WAY_QUESTION -> {
                showCaptchaDialog()
            }
        }
    }

    private fun initObserve() = viewModel.run {
        smsResult.observe(this@ForgetPasswordActivity) { updateUiWithResult(it) }
        userQuestionEvent.observe(this@ForgetPasswordActivity){
            if (it.succeeded()){
                ForgetPasswordQuestionActivity.start(this@ForgetPasswordActivity, inputUserName!!, it.getData()?.safeQuestion!!)
            }else{
                showErrorPromptDialog(it.msg){}
            }
        }
    }

    //发送验证码的回调
    private fun updateUiWithResult(smsResult: SendSmsResult?) {
        hideLoading()
        if (smsResult?.success == true) {
            when{
                smsResult.t!= null->{
                    dealWithSmsResult(smsResult.t)
                }
                smsResult.rows?.size == 1->{
                    dealWithSmsResult(smsResult.rows[0])
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

    private fun dealWithSmsResult(resetPasswordData: ResetPasswordData){
        userName = resetPasswordData.userName
        phoneWithUserName = inputPhoneNo
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
                dealWithSmsResult(it)
            }
        }
    }

    override fun onVerifySucceed(identity: String, validCode: String, tag: String?) {
        with(binding) {
            if (ways== WAY_QUESTION){
                viewModel.getUserQuestion(inputUserName!!, identity, validCode)
            }else{
                sendCode(identity, validCode)
                eetSmsCode.setText("")
                binding.etSmsValidCode.setError(null, true)
            }
        }
    }

}