package org.cxct.sportlottery.ui.login.foget.reset

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.widget.EditText
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_rest_password.*
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
import org.cxct.sportlottery.view.boundsEditText.TextFormFieldBoxes

/**
 * @app_destination 忘记密码
 */
class ResetPasswordActivity: BaseActivity<ForgetViewModel>(ForgetViewModel::class) {

    companion object {

        fun start(activity: Activity, userName: String, byPhoneNumber: Boolean = true) {
            val intent = Intent(activity, ResetPasswordActivity::class.java)
            intent.putExtra("userName", userName)
            intent.putExtra("byPhoneNumber", byPhoneNumber)
            activity.startActivityForResult(intent, 100)
        }
    }

    private val binding by lazy { ActivityRestPasswordBinding.inflate(layoutInflater) }
    private val userName by lazy { "${intent.getSerializableExtra("userName")}" }
    private val byPhoneNumber by lazy { intent.getBooleanExtra("byPhoneNumber", true) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBarDarkFont()
        setContentView(binding.root)
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
            input.isNullOrEmpty() -> LocalUtils.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyPwd(input) ->
                LocalUtils.getString(R.string.error_register_password)
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

        fieldBox.setError(LocalUtils.getString(R.string.error_tips_confirm_password), false)
        return false
    }

    private fun onNext() {
        if (binding.clSuccess.isVisible) {
            finishWithOK()
            return
        }

        val confirmPassword = eet_confirm_password_forget.text.toString()
        val newPassword = eet_login_password_forget.text.toString()
        if (confirmPassword != newPassword) {
            ToastUtil.showToast(this, LocalUtils.getString(R.string.error_tips_confirm_password))
            return
        }

        hideSoftKeyboard()
        loading()
        binding.btnNext.setBtnEnable(false)
        val encodedPassword = MD5Util.MD5Encode(confirmPassword)

        if (byPhoneNumber) {
            viewModel.resetPassword(userName, encodedPassword, encodedPassword)
        } else {
            viewModel.resetPassWorkByEmail(userName, encodedPassword)
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