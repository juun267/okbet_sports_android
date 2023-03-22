package org.cxct.sportlottery.ui.profileCenter.modify

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityModifyBindInfoBinding
import org.cxct.sportlottery.extentions.bindFinish
import org.cxct.sportlottery.extentions.gone
import org.cxct.sportlottery.extentions.isEmptyStr
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.user.data.SendCodeRespnose
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.login.VerifyCodeDialog
import org.cxct.sportlottery.ui.login.checkEmail
import org.cxct.sportlottery.ui.login.checkPhoneNum
import org.cxct.sportlottery.ui.login.checkSMSCode
import org.cxct.sportlottery.ui.profileCenter.nickname.ModifyType
import org.cxct.sportlottery.util.CountDownUtil
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.util.setBtnEnable
import org.cxct.sportlottery.widget.boundsEditText.TextFormFieldBoxes

// 修改或者绑定手机号、邮箱
class ModifyBindInfoActivity: BaseActivity<BindInfoViewModel>(BindInfoViewModel::class) {

    companion object {
        fun start(context: Context, modifyType: @ModifyType Int, userName: String) {
            val intent = Intent(context, ModifyBindInfoActivity::class.java)
            intent.putExtra("MODIFY_INFO", modifyType)
            intent.putExtra("userName", userName)
            context.startActivity(intent)
        }
    }

    private val modifyType by lazy { intent.getIntExtra("MODIFY_INFO", ModifyType.PhoneNumber) }
    private val binding by lazy { ActivityModifyBindInfoBinding.inflate(layoutInflater) }

    private var inputPhoneNo: String? = null // 输入的手机号，不为空即为输入的号码格式正确
    private var inputEmail: String? = null // 输入的邮箱，不为空即为输入的号码格式正确
    private var smsCode: String? = null // 短信或者邮箱验证码

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBarDarkFont()
        setContentView(binding.root)
        initView()
        initObserver()
    }

    private fun initView() = binding.run  {
        bindFinish(btnBack)
        btnPut.setOnClickListener { confirm() }
        btnSendSms.setOnClickListener {
            hideSoftKeyboard(this@ModifyBindInfoActivity)
            VerifyCodeDialog(callBack = { identity, validCode ->
                sendCode(identity, validCode)
                eetSmsCode.requestFocus()
            }).show(supportFragmentManager, null)
        }

        eetSmsCode.checkSMSCode(etSmsValidCode) {
            smsCode = it
            setNextBtnStatus()
        }

        if (modifyType == ModifyType.Email) {
            etPhone.gone()
            eetEMail.checkEmail(etEMail) {
                inputEmail = it
                btnSendSms.setBtnEnable(!inputEmail.isEmptyStr())
                setNextBtnStatus()
            }
            return@run
        }

        etEMail.gone()
        eetPhoneNum.checkPhoneNum(etPhone) {
            inputPhoneNo = it
            btnSendSms.setBtnEnable(!inputPhoneNo.isEmptyStr())
            setNextBtnStatus()
        }
    }

    private fun setNextBtnStatus() {
        binding.btnPut.setBtnEnable(smsCode != null && (inputPhoneNo != null || inputEmail != null))
    }

    private inline fun getPhoneOrEmail(): String {
        return "${if (inputPhoneNo.isEmptyStr()) inputEmail else inputPhoneNo}"
    }

    private fun sendCode(identity: String?, validCode: String) = binding.btnSendSms.run {
        loading()
        setBtnEnable(false)
        viewModel.sendSMSOrEmailCode(getPhoneOrEmail(), "$identity", validCode)
    }

    private fun confirm() {
        loading()
        viewModel.resetEmailOrPhone(getPhoneOrEmail(), "$smsCode")
    }

    private fun initObserver() = viewModel.run {
        sendCodeResult.observe(this@ModifyBindInfoActivity) { updateUiWithResult(it) }
        resetResult.observe(this@ModifyBindInfoActivity) {
            hideLoading()
            ToastUtil.showToast(this@ModifyBindInfoActivity, it.msg)
            if (it.succeeded()) {
                finish()
            }
        }
    }

    private fun codeCountDown() = binding.btnSendSms.run  {
        if (tag != null) {
            return@run
        }

        tag = this
        CountDownUtil.smsCountDown(this@ModifyBindInfoActivity,
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

    //发送验证码的回调
    private fun updateUiWithResult(smsResult: ApiResult<SendCodeRespnose>) {
        hideLoading()
        if (smsResult.succeeded()) {
            codeCountDown()
            val msg = smsResult.getData()?.msg
            if(!msg.isEmptyStr()) {
                ToastUtil.showToast(this@ModifyBindInfoActivity, msg, Toast.LENGTH_SHORT)
            }
            return
        }

        binding.btnSendSms.setBtnEnable(true)
        ToastUtil.showToast(this@ModifyBindInfoActivity, smsResult?.msg, Toast.LENGTH_SHORT)
        //做异常处理
        if (smsResult?.code == 2765 || smsResult?.code == 2766) {
            binding.etPhone.setError(smsResult.msg,false)
        } else {
            binding.etSmsValidCode.setError(smsResult?.msg,false)
        }
    }


}