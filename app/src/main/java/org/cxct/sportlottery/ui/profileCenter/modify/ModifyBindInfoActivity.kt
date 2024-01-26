package org.cxct.sportlottery.ui.profileCenter.modify

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.InputType
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.ActivityModifyBindInfoBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.base.BindingActivity
import org.cxct.sportlottery.ui.login.VerifyCodeDialog
import org.cxct.sportlottery.view.checkEmail
import org.cxct.sportlottery.view.checkPhoneNum
import org.cxct.sportlottery.view.checkSMSCode
import org.cxct.sportlottery.ui.profileCenter.nickname.ModifyType
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.DrawableCreator


// 验证绑定的手机号或者邮箱
class ModifyBindInfoActivity: BindingActivity<BindInfoViewModel,ActivityModifyBindInfoBinding>() {

    companion object {
        fun start(context: Activity, @ModifyType modifyType:  Int, requestCode: Int, phone: String?, email: String?, oldInfo: String? = null) {
            val intent = Intent(context, ModifyBindInfoActivity::class.java)
            intent.putExtra("MODIFY_INFO", modifyType)
            intent.putExtra("phone", phone)
            intent.putExtra("email", email)
            intent.putExtra("oldInfo", oldInfo)
            context.startActivityForResult(intent, requestCode)
        }
    }

    private val phone by lazy { intent.getStringExtra("phone") }
    private val email by lazy { intent.getStringExtra("email") }
    private val oldInfo by lazy { intent.getStringExtra("oldInfo") }
    private val modifyType by lazy { intent.getIntExtra("MODIFY_INFO", ModifyType.PhoneNumber) }
    private var inputPhoneNoOrEmail: String? = null // 输入的手机号或者邮箱，不为空即为输入的号码格式正确
    private var smsCode: String? = null // 短信或者邮箱验证码
    private var userName: String? = null

    private inline fun isPhoneWaysVerify() = !phone.isEmptyStr()                // 手机号验证
    private inline fun isModifyPhone() = modifyType == ModifyType.PhoneNumber    // 是否修手机号
    private inline fun dontNeedVerify() = phone.isEmptyStr() && email.isEmptyStr()

    private inline fun isReset() = !oldInfo.isEmptyStr()
    private fun getTitleText(): String {
        return if (isReset()) {
            getString(if (isModifyPhone()) R.string.edit_phone_no else R.string.edit_email)
        } else {
            getString(if (isModifyPhone()) R.string.set_phone_no else R.string.set_email)
        }
    }

    override fun onInitView() {
        setStatusBarDarkFont()
        setContentView(binding.root)
        initObserve()
        initView()
    }

    private fun createCircleDrawable(strokeColor: Int, strokeWidth: Float, solidColor: Int, wh: Float): Drawable {
        return DrawableCreator.Builder()
            .setCornersRadius(wh)
            .setSizeWidth(wh)
            .setSizeHeight(wh)
            .setSolidColor(solidColor)
            .setStrokeWidth(strokeWidth)
            .setStrokeColor(strokeColor)
            .build()
    }

    private fun setProgressVerify() = binding.run {
        val wh = 30.dp.toFloat()
        val padding = 4.dp.toFloat()
        val blue = getColor(R.color.color_025BE8)
        val gray = getColor(R.color.color_A7B2C4)
        tvOneText.setTextColor(blue)
        tvTwoText.setTextColor(gray)
        tvThreeText.setTextColor(gray)
        vPart1.setBackgroundColor(blue)
        vPart2.setBackgroundResource(R.color.color_C9CFD7)
        val disableDrawble = createCircleDrawable(Color.TRANSPARENT, padding, getColor(R.color.color_C9CFD7), wh)
        tvOne.background = createCircleDrawable(getColor(R.color.color_4c025BE8), padding, blue, wh)
        tvTwo.background = disableDrawble
        tvThree.background = disableDrawble
    }

    private fun setProgressEdit() = binding.run {
        val wh = 30.dp.toFloat()
        val padding = 4.dp.toFloat()
        val blue = getColor(R.color.color_025BE8)
        tvOneText.setTextColor(getColor(R.color.color_414655))
        tvTwoText.setTextColor(blue)
        tvThreeText.setTextColor(getColor(R.color.color_A7B2C4))
        vPart1.setBackgroundColor(getColor(R.color.color_1EB65B))
        vPart2.setBackgroundColor(getColor(R.color.color_C9CFD7))
        tvOne.text = ""
        tvOne.setBackgroundResource(R.drawable.ic_circle_finished)
        tvTwo.background = createCircleDrawable(getColor(R.color.color_4c025BE8), padding, blue, wh)
        tvThree.background = createCircleDrawable(Color.TRANSPARENT, padding, getColor(R.color.color_C9CFD7), wh)
    }

    private fun setProgressSuccess() = binding.run {
        val green = getColor(R.color.color_1EB65B)
        vPart1.setBackgroundColor(green)
        vPart2.setBackgroundColor(green)
        tvOne.text = ""
        tvTwo.text = ""
        tvThree.text = ""
        tvOne.setBackgroundResource(R.drawable.ic_circle_finished)
        tvTwo.setBackgroundResource(R.drawable.ic_circle_finished)
        tvThree.setBackgroundResource(R.drawable.ic_circle_finished)

        val black = getColor(R.color.color_414655)
        tvOneText.setTextColor(black)
        tvTwoText.setTextColor(black)
        tvThreeText.setTextColor(black)

        tvStatusText.text = if (isReset()) {
            getString(if (isModifyPhone()) R.string.N866_1 else R.string.N867_1)
        } else {
            getString(if (isModifyPhone()) R.string.N866 else R.string.N867)
        }

        setViewGone(inputForm, blockSmsValidCode, clLiveChat)
        setViewVisible(tvStatusText, tvBackText)
        resetCountDown()

        bindFinish(binding.btnNext)
        binding.btnNext.setText(R.string.btn_sure)
    }

    private val onCheckInput = { it: String? ->
        inputPhoneNoOrEmail = it
        onNewSMSStatus()
    }

    // 验证手机号或邮箱
    private fun setVerifyLayout() = binding.run {
        setProgressVerify()
        btnNext.setOnClickListener { toVerify() }

        if (isPhoneWaysVerify()) {
            changeEdittextInputType(true)
            eetInputForm.checkPhoneNum(inputForm, onCheckInput)
        } else {
            changeEdittextInputType(false)
            eetInputForm.checkEmail(inputForm, onCheckInput)
        }
        eetInputForm.isEnabled = false
        eetInputForm.setText(if (isPhoneWaysVerify()) phone else email)
        tvTwoText.setText(if (isModifyPhone()) R.string.new_phone_no else R.string.new_email)
    }

    // 修改手机号或邮箱
    private fun setModifyLayout() = binding.run {
        setProgressEdit()
        bindResetClick()
        clearInput()
        inputPhoneNoOrEmail = null
        eetInputForm.isEnabled = true
        eetInputForm.requestFocus()

        if (isModifyPhone()) {
            changeEdittextInputType(true)
            eetInputForm.checkPhoneNum(inputForm, onCheckInput)
        } else {
            changeEdittextInputType(false)
            eetInputForm.checkEmail(inputForm, onCheckInput)
        }
    }

    private fun clearInput() = binding.run  {
        eetInputForm.setText("")
        eetSmsCode.setText("")
        inputForm.setError(null, false)
        etSmsValidCode.setError(null, false)
        resetCountDown()
    }

    private fun resetCountDown() = binding.run   {
        btnSendSms.tag?.let { (it as CoroutineScope).cancel() }
        btnSendSms.tag = null
    }

    private fun changeEdittextInputType(isPhone: Boolean) = binding.run {
        if (isPhone) {
            inputForm.setIconSignifier(R.drawable.ic_mobile_gray)
            inputForm.labelText = getString(R.string.mobile)
            eetInputForm.hint = getString(if (isReset()) R.string.new_phone_no else R.string.phone_number)
            eetInputForm.maxEms = 11
            eetInputForm.filters = arrayOf<InputFilter>(LengthFilter(11))
            eetInputForm.inputType = InputType.TYPE_CLASS_NUMBER
        } else {
            inputForm.setIconSignifier(R.drawable.ic_email_gray)
            inputForm.labelText = getString(R.string.e_mail)
            eetInputForm.hint = getString(if (isReset()) R.string.new_email else R.string.email_address)
            eetInputForm.filters = arrayOf<InputFilter>(LengthFilter(50))
            eetInputForm.inputType = InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS
        }
    }

    // 不需要验证手机号或者邮箱的情况下设置手机号或者邮箱
    private fun setBindInfoLayout() = binding.run {
        bindResetClick()
        clProgressLayout.gone()
        if (isModifyPhone()) {
            changeEdittextInputType(true)
            eetInputForm.checkPhoneNum(inputForm, onCheckInput)
        } else {
            changeEdittextInputType(false)
            eetInputForm.checkEmail(inputForm, onCheckInput)
        }
    }

    private fun initView() = binding.run {
        bindFinish(btnBack)
        clLiveChat.setServiceClick(supportFragmentManager)
        tvTitle.text = getTitleText()
        eetSmsCode.checkSMSCode(etSmsValidCode) {
            smsCode = it
            setNextBtnStatus()
        }
        btnSendSms.setOnClickListener {
            hideSoftKeyboard()
            showCaptchaDialog(supportFragmentManager)
                { identity, validCode ->
                    sendCode(identity, validCode)
                    eetSmsCode.requestFocus()
                }
        }
        if (dontNeedVerify()) { // 不需要验证直接设置
            setBindInfoLayout()
        } else {
            setVerifyLayout()
        }
        bottomLiences.tvLicense.text = Constants.copyRightString
    }

    private fun bindResetClick() {
        binding.btnNext.setOnClickListener {
            loading()
            viewModel.resetEmailOrPhone("$inputPhoneNoOrEmail", "$smsCode")
        }
    }

    private fun setNextBtnStatus() {
        binding.btnNext.setBtnEnable(smsCode != null && (inputPhoneNoOrEmail != null))
    }

    private fun onNewSMSStatus()  {

        val inputEffective = inputPhoneNoOrEmail != null

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
        viewModel.sendSMSOrEmailCode("$inputPhoneNoOrEmail", "$identity", validCode)
    }

    private fun codeCountDown() = binding.btnSendSms.run  {
        if (tag != null) {
            return@run
        }

        GlobalScope.launch(this@ModifyBindInfoActivity.lifecycleScope.coroutineContext) {
            tag = this
            CountDownUtil.smsCountDown(
                this,
                { setBtnEnable(false) },
                { text = "${it}s" },
                { onCountDownEnd() }
            )
        }
    }

    private fun onCountDownEnd() = binding.btnSendSms.run  {
        tag = null
        onNewSMSStatus()
        setTextColor(Color.WHITE)
        setText(R.string.send)
    }

    private fun toVerify() {
        loading()
        hideSoftKeyboard()
        viewModel.verifyEmailOrPhoneCode("$inputPhoneNoOrEmail", "$smsCode")
    }

    private fun initObserve() = viewModel.run {

        sendCodeResult.observe(this@ModifyBindInfoActivity) { smsResult-> // 发送验证码
            hideLoading()

            if (smsResult.succeeded()) {
                userName = smsResult.getData()?.userName
                ToastUtil.showToast(this@ModifyBindInfoActivity, smsResult.getData()?.msg)
                codeCountDown()
                return@observe
            }

            ToastUtil.showToast(this@ModifyBindInfoActivity, smsResult.msg)
            binding.btnSendSms.setBtnEnable(true)
            //做异常处理
//            if (smsResult?.code == 2765 || smsResult?.code == 2766) {
//                binding.inputForm.setError(smsResult.msg,false)
//            } else {
//                binding.etSmsValidCode.setError(smsResult?.msg,false)
//            }
        }

        verifyResult.observe(this@ModifyBindInfoActivity) { result-> // 验证短信验证码
            hideLoading()
            if (result.succeeded()){
                setModifyLayout()
                return@observe
            }

            ToastUtil.showToast(this@ModifyBindInfoActivity, result.msg)
//            if (result.code == 2765|| result.code == 2766) {
//                binding.inputForm.setError(result.msg,false)
//            } else {
//                binding.etSmsValidCode.setError(result.msg,false)
//            }
        }

        resetResult.observe(this@ModifyBindInfoActivity) {
            hideLoading()
            if (!it.second.succeeded()) {
                ToastUtil.showToast(this@ModifyBindInfoActivity, it.second.msg)
                return@observe
            }

            val intent = Intent()
            if (VerifyConstUtil.verifyPhone(it.first)) {
                intent.putExtra("phone", it.first)
            } else if (VerifyConstUtil.verifyMail(it.first)) {
                intent.putExtra("email", it.first)
            }
            setResult(Activity.RESULT_OK, intent)
            setProgressSuccess()
        }

    }

}