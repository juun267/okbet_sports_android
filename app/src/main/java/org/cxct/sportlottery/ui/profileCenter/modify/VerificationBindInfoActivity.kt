package org.cxct.sportlottery.ui.profileCenter.modify

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityForgetPassword2Binding
import org.cxct.sportlottery.extentions.bindFinish
import org.cxct.sportlottery.extentions.finishWithOK
import org.cxct.sportlottery.extentions.gone
import org.cxct.sportlottery.extentions.isEmptyStr
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.user.data.SendCodeRespnose
import org.cxct.sportlottery.network.index.forgetPassword.SendSmsResult
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.login.VerifyCodeDialog
import org.cxct.sportlottery.ui.login.checkEmail
import org.cxct.sportlottery.ui.login.checkPhoneNum
import org.cxct.sportlottery.ui.login.checkSMSCode
import org.cxct.sportlottery.ui.login.foget2.rest.ResetPasswordActivity
import org.cxct.sportlottery.ui.profileCenter.nickname.ModifyType
import org.cxct.sportlottery.util.CountDownUtil
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.util.setBtnEnable
import org.cxct.sportlottery.util.setServiceClick

// 验证绑定的手机号或者邮箱
class VerificationBindInfoActivity: BaseActivity<BindInfoViewModel>(BindInfoViewModel::class) {

    companion object {

        // 1: 验证手机号
        fun startByPhoneWays(context: Activity, requestCode: Int, phone: String) {
            start(context, ModifyType.PhoneNumber, requestCode, phone, null)
        }
        // 2:验证邮箱
        fun startByEmailWays(context: Activity, requestCode: Int, email: String) {
            start(context, ModifyType.Email, requestCode, null, email)
        }

        fun start(context: Activity, modifyType: @ModifyType Int, requestCode: Int, phone: String?, email: String?) {
            val intent = Intent(context, VerificationBindInfoActivity::class.java)
            intent.putExtra("MODIFY_INFO", modifyType)
            intent.putExtra("phone", phone)
            intent.putExtra("email", email)
            context.startActivityForResult(intent, requestCode)
        }
    }

    private val binding by lazy { ActivityForgetPassword2Binding.inflate(layoutInflater) }
    private val phone by lazy { intent.getStringExtra("phone") }
    private val email by lazy { intent.getStringExtra("email") }
    private val modifyType by lazy { intent.getIntExtra("MODIFY_INFO", ModifyType.PhoneNumber) }
    private var inputPhoneNo: String? = null // 输入的手机号，不为空即为输入的号码格式正确
    private var inputEmail: String? = null // 输入的邮箱，不为空即为输入的号码格式正确
    private var smsCode: String? = null // 短信或者邮箱验证码
    private var userName: String? = null

    private inline fun isPhoneWays() = !phone.isEmptyStr()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBarDarkFont()
        setContentView(binding.root)
        initView()
        initObserve()
        bindValue()
    }

    private fun bindValue() = binding.run {
        phone?.let {
            eetPhoneNum.setText(it)
            eetPhoneNum.isEnabled = false
        }

        email?.let {
            eetEMail.setText(it)
            eetEMail.isEnabled = false
        }
    }

    private fun initView() = binding.run {
        bindFinish(btnBack)
        clLiveChat.setServiceClick(supportFragmentManager)
        btnPut.setOnClickListener { next() }
//        btnSendSms.setOnClickListener { YidunCaptcha.validateAction(this@VerificationBindInfoActivity,this@VerificationBindInfoActivity, { sendCode() }) }

        btnSendSms.setOnClickListener {
            hideSoftKeyboard(this@VerificationBindInfoActivity)
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
        val phoneOrEmail = if (inputPhoneNo.isEmptyStr()) inputEmail else inputPhoneNo
        viewModel.sendSMSOrEmailCode("$phoneOrEmail", "$identity", validCode)
    }

    private fun codeCountDown() = binding.btnSendSms.run  {
        if (tag != null) {
            return@run
        }

        tag = this
        CountDownUtil.smsCountDown(this@VerificationBindInfoActivity,
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
        hideSoftKeyboard(this@VerificationBindInfoActivity)
        val phoneOrEmail = if (inputPhoneNo.isEmptyStr()) inputEmail else inputPhoneNo
        viewModel.verifyEmailOrPhoneCode("$phoneOrEmail", "$smsCode")
    }

    private fun initObserve() = viewModel.run {
        sendCodeResult.observe(this@VerificationBindInfoActivity) { updateUiWithResult(it) }
        verifyResult.observe(this@VerificationBindInfoActivity) { result-> // 验证短信验证码
            hideLoading()
            if (result == null) {
                return@observe
            }

            if (result.succeeded()){
                ModifyBindInfoActivity.start(this@VerificationBindInfoActivity, modifyType, "$userName")
                return@observe
            }

            if (result.code == 2765|| result.code == 2766) {
                if(inputPhoneNo.isEmptyStr()) {
                    binding.etPhone.setError(result.msg,false)
                } else {
                    binding.etEMail.setError(result.msg,false)
                }
            } else {
                binding.etSmsValidCode.setError(result.msg,false)
            }
        }
    }

    //发送验证码的回调
    private fun updateUiWithResult(smsResult: ApiResult<SendCodeRespnose>) {
        hideLoading()
        if (smsResult.succeeded()) {
            userName = smsResult.getData()?.userName
            codeCountDown()
//            CountDownUtil.targSMSTimeStamp()
            val msg = smsResult.getData()?.msg
            if(!msg.isEmptyStr()) {
                ToastUtil.showToast(this@VerificationBindInfoActivity, msg, Toast.LENGTH_SHORT)
            }
            return
        }

        binding.btnSendSms.setBtnEnable(true)
        ToastUtil.showToast(this@VerificationBindInfoActivity, smsResult?.msg, Toast.LENGTH_SHORT)
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