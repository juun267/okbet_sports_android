package org.cxct.sportlottery.ui.login.foget.reset

import android.app.Activity
import android.content.Intent
import android.text.method.HideReturnsTransformationMethod
import android.widget.EditText
import androidx.core.view.isVisible
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.ActivityRestPasswordBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.login.foget.ForgetViewModel
import org.cxct.sportlottery.view.checkRegisterListener
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.boundsEditText.AsteriskPasswordTransformationMethod
import org.cxct.sportlottery.view.boundsEditText.LoginFormFieldView

/**
 * @app_destination 忘记密码
 */
class ResetPasswordActivity: BaseActivity<ForgetViewModel,ActivityRestPasswordBinding>(ForgetViewModel::class) {

    override fun pageName() = "重置密码页面"

    companion object {

        fun start(activity: Activity, userName: String,
                  phoneNumber: String?,
                  code: String?,
                  ways: Int = 1) {
            val intent = Intent(activity, ResetPasswordActivity::class.java)
            intent.putExtra("userName", userName)
            intent.putExtra("phoneNumber", phoneNumber)
            intent.putExtra("code", code)
            intent.putExtra("ways", ways)
            activity.startActivityForResult(intent, 100)
        }
    }

    private val userName by lazy { "${intent.getSerializableExtra("userName")}" }
    private val ways by lazy { intent.getIntExtra("ways", 1) }
    private val phone by lazy { intent.getStringExtra("phoneNumber") }
    private val code by lazy { intent.getStringExtra("code") }

    override fun onInitView() {
        setStatusBarDarkFont()
        initView()
        initObserver()
    }

    private fun initView() = binding.run {
        bindFinish(btnBack)
        clLiveChat.setServiceClick(supportFragmentManager)
        btnNext.setOnClickListener { onNext() }
        btnBackToLogin.setOnClickListener { onNext() }
        etLoginPassword.endIconImageButton.setOnClickListener { resetInputTransformationMethod(etLoginPassword, eetLoginPasswordForget) }
        etConfirmPasswordForget.endIconImageButton.setOnClickListener { resetInputTransformationMethod(etConfirmPasswordForget, eetConfirmPasswordForget) }
        eetLoginPasswordForget.checkRegisterListener {
            btnNext.setBtnEnable(checkInput(etLoginPassword, eetLoginPasswordForget, etConfirmPasswordForget, eetConfirmPasswordForget))
        }
        eetConfirmPasswordForget.checkRegisterListener {
            btnNext.setBtnEnable(checkInput(etConfirmPasswordForget, eetConfirmPasswordForget, etLoginPassword, eetLoginPasswordForget))
        }
        bottomLiences.tvLicense.text = Constants.copyRightString
    }

    private fun resetInputTransformationMethod(fieldBox: LoginFormFieldView, editText: EditText) {
        if (fieldBox.endIconResourceId == R.drawable.ic_eye_open) {
            editText.transformationMethod = AsteriskPasswordTransformationMethod()
            fieldBox.setEndIcon(R.drawable.ic_eye_close)
        } else {
            fieldBox.setEndIcon(R.drawable.ic_eye_open)
            editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
        }

        fieldBox.hasFocus = true
        editText.setSelection(editText.text.toString().length)
    }

    private fun checkInput(fieldBox: LoginFormFieldView,
                           editText: EditText,
                           otherFieldBox: LoginFormFieldView,
                           otherEditText: EditText): Boolean {

        val input = editText.text.toString()
        val otherInput = otherEditText.text.toString()

        val msg = when {
            input.isNullOrEmpty() -> getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyPwd(input) -> getString(R.string.error_register_password)
            else -> null
        }

        if (!msg.isEmptyStr()) {
            fieldBox.setError(msg, false)
            return false
        }

        // 两个输入框都输入合法再次比较两次内容是否相同
        if (!VerifyConstUtil.verifyPwd(otherInput)) {
            fieldBox.setError(null, false)
            return false
        }

        if (input == otherInput) {
            otherFieldBox.setError(null, false)
            fieldBox.setError(null, false)
            return true
        }

        fieldBox.setError(getString(R.string.error_tips_confirm_password), false)
        return false
    }

    private fun onNext() {
        if (binding.clSuccess.isVisible) {
             startLogin()
            return
        }

        val confirmPassword = binding.eetConfirmPasswordForget.text.toString()
        val newPassword = binding.eetLoginPasswordForget.text.toString()
        if (confirmPassword != newPassword) {
            ToastUtil.showToast(this, getString(R.string.error_tips_confirm_password))
            return
        }

        hideSoftKeyboard()
        loading()
        binding.btnNext.setBtnEnable(false)
        val encodedPassword = MD5Util.MD5Encode(confirmPassword)
        when(ways){
            1-> viewModel.resetPassword(userName, encodedPassword, encodedPassword, phone, code)
            2-> viewModel.resetPassWorkByEmail(userName, encodedPassword)
            3-> code?.let { viewModel.updatePasswordBySafeQuestion(userName, it,encodedPassword) }
        }
    }

    private fun initObserver() = viewModel.run {

        resetPasswordResult.observe(this@ResetPasswordActivity) {
            hideLoading()
            binding.btnNext.setBtnEnable(true)
            if (it?.success != true) {
                val msg = it?.ResetPasswordData?.msg ?: it?.msg
                if (!msg.isEmptyStr()) {
                    ToastUtil.showToast(this@ResetPasswordActivity, msg)
                }
                return@observe
            }

            onResetSuccess(userName)
        }
        updatePasswordResultEvent.observe(this@ResetPasswordActivity) {
            hideLoading()
            binding.btnNext.setBtnEnable(true)
            if (it.succeeded()){
                onResetSuccess(userName)
            }else{
                val msg = it?.getData()?.msg ?: it?.msg
                if (!msg.isNullOrEmpty()){
                    ToastUtil.showToast(this@ResetPasswordActivity, msg)
                }
            }
        }
    }

    private fun onResetSuccess(userName: String) = binding.run {
        tvResetSucceed.text = getString(R.string.change_password)+"!"
        clSuccess.visible()
        clPassword.gone()
        clLiveChat.gone()
        btnNext.gone()
        setResult(Activity.RESULT_OK)
    }

}