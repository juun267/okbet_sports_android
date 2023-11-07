package org.cxct.sportlottery.ui.profileCenter.changePassword

import android.app.Activity
import android.content.Intent
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.text_form_field_boxes_layout.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.ActivitySettingPasswordBinding
import org.cxct.sportlottery.network.user.UserInfo
import org.cxct.sportlottery.network.NetResult
import org.cxct.sportlottery.network.index.config.ConfigData
import org.cxct.sportlottery.repository.FLAG_IS_NEED_UPDATE_PAY_PW
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BindingActivity
import org.cxct.sportlottery.ui.login.foget.ForgetWaysActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.afterTextChanged
import org.cxct.sportlottery.view.boundsEditText.AsteriskPasswordTransformationMethod
import org.cxct.sportlottery.view.boundsEditText.ExtendedEditText

/**
 * @app_destination 密码设置
 */
class SettingPasswordActivity : BindingActivity<SettingPasswordViewModel, ActivitySettingPasswordBinding>() {

    companion object {
        const val PWD_PAGE = "PWD_PAGE"
    }

    enum class PwdPage { LOGIN_PWD, BANK_PWD }

    private var mPwdPage = PwdPage.LOGIN_PWD //登入密碼 or 提款密碼 page flag
    private var mUserInfo: UserInfo? = null

    private fun hasPassword() = viewModel.userInfo.value?.passwordSet == false

    override fun onInitView() {
        setStatusbar(R.color.color_232C4F_FFFFFF, true)
        initView()
        bindFinish(binding.toolBar.btnToolbarBack)
        setupTab()
        setupEditText()
        setupConfirmButton()
        initObserve()
    }

    private fun initView() = binding.run {

        tvForgetPassword.setOnClickListener {
            if (mPwdPage == PwdPage.BANK_PWD) {
                val phoneNo = UserInfoRepository.getPhoneNo()
                if (phoneNo.isEmptyStr()) {
                    phoneNumCheckDialog(this@SettingPasswordActivity, supportFragmentManager)
                    return@setOnClickListener
                }
                startActivity<VerifyPhoneNoActivity>(Pair("phone", phoneNo))
            } else {
                startActivity(ForgetWaysActivity::class.java)
            }
        }

        toolBar.tvToolbarTitle.setTitleLetterSpacing()
        toolBar.tvToolbarTitle.text = getString(R.string.setting_password)
        eetCurrentPassword.transformationMethod = AsteriskPasswordTransformationMethod()
        eetNewPassword.transformationMethod = AsteriskPasswordTransformationMethod()
        eetConfirmPassword.transformationMethod = AsteriskPasswordTransformationMethod()

        val bottomLineColorRes = R.color.color_80334266_E3E8EE
        etCurrentPassword.isVisible = hasPassword()
        etCurrentPassword.bottom_line.setBackgroundResource(bottomLineColorRes)
        etNewPassword.bottom_line.setBackgroundResource(bottomLineColorRes)
        etConfirmPassword.bottom_line.setBackgroundResource(bottomLineColorRes)
        updateButtonStatus(false)
        etCurrentPassword.setTransformationMethodEvent(eetCurrentPassword)
        etNewPassword.setTransformationMethodEvent(eetNewPassword)
        etConfirmPassword.setTransformationMethodEvent(eetConfirmPassword)
    }

    private fun setupTab() {
        binding.customTabLayout.setCustomTabSelectedListener { position ->
            if (position == 0) {
                mPwdPage = PwdPage.LOGIN_PWD
                updateCurrentPwdEditTextHint(mPwdPage, mUserInfo?.updatePayPw)
                cleanField()
                return@setCustomTabSelectedListener
            }

            if(position == 1) {
                mPwdPage = PwdPage.BANK_PWD
                updateCurrentPwdEditTextHint(mPwdPage, mUserInfo?.updatePayPw)
                cleanField()
            }
        }

        //初始顯示哪個 tab 頁面
        when (intent.getSerializableExtra(PWD_PAGE)) {
            PwdPage.BANK_PWD -> binding.customTabLayout.selectTab(1)
            else -> binding.customTabLayout.selectTab(0)
        }

//        binding.tvForgetPassword.isVisible = true //mPwdPage == PwdPage.BANK_PWD

    }

    private fun setupEditText() {
        //當失去焦點才去檢查 inputData
        setupEditTextFocusChangeEvent(binding.eetCurrentPassword) { viewModel.checkCurrentPwd(it) }
        setupEditTextFocusChangeEvent(binding.eetNewPassword) {
            viewModel.checkNewPwd(mPwdPage, binding.eetCurrentPassword.text.toString(), it)
        }
        setupEditTextFocusChangeEvent(binding.eetConfirmPassword) {
            viewModel.checkConfirmPwd(binding.eetNewPassword.text.toString(), it)
        }

        binding.eetCurrentPassword.afterTextChanged { viewModel.checkCurrentPwd(it) }
        binding.eetNewPassword.afterTextChanged {
            viewModel.checkNewPwd(mPwdPage, binding.eetCurrentPassword.text.toString(), it)
        }
        binding.eetConfirmPassword.afterTextChanged {
            viewModel.checkConfirmPwd(binding.eetNewPassword.text.toString(), it)
        }
    }

    private fun setupEditTextFocusChangeEvent(
        editText: ExtendedEditText,
        listener: (String) -> Unit
    ) {
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) listener.invoke(editText.text.toString())
        }
    }

    private fun setupConfirmButton() {
        binding.btnConfirm.setOnClickListener { checkInputData() }
        binding.btnConfirm.setTitleLetterSpacing()
    }

    private fun checkInputData() {
        viewModel.checkInputField(
            mPwdPage,
            binding.eetCurrentPassword.text.toString(),
            binding.eetNewPassword.text.toString(),
            binding.eetConfirmPassword.text.toString()
        )
    }

    private fun initObserve() {
        viewModel.submitEnable.observe(this) {
            updateButtonStatus(it)
        }

        viewModel.currentPwdError.observe(this) {
            binding.etCurrentPassword.setError(it, false)
        }

        viewModel.newPwdError.observe(this) {
            binding.etNewPassword.setError(it, false)
        }

        viewModel.confirmPwdError.observe(this) {
            binding.etConfirmPassword.setError(it, false)
        }

        viewModel.updatePwdResult.observe(this) {
            updateUiWithResult(it)
        }

        viewModel.updateFundPwdResult.observe(this) {
            updateFundPwdUiWithResult(it)
        }

        viewModel.userInfo.observe(this) {
            mUserInfo = it
            updateCurrentPwdEditTextHint(mPwdPage, mUserInfo?.updatePayPw)
            binding.etCurrentPassword.isVisible = it?.passwordSet == false
        }
    }

    private fun updateUiWithResult(updatePwdResult: NetResult?) {
        hideLoading()
        if (updatePwdResult?.success == true) {
            showPromptDialog(getString(R.string.prompt), getString(R.string.tips_login_password_success)) { finish() }
        } else {
            showErrorPromptDialog(getString(R.string.prompt), updatePwdResult?.msg ?: getString(R.string.unknown_error)) {}
        }
    }

    private fun updateFundPwdUiWithResult(updateFundPwdResult: NetResult?) {
        hideLoading()
        if (updateFundPwdResult?.success == true) {
            showPromptDialog(getString(R.string.prompt), getString(R.string.update_withdrawal_pwd)) { finish() }
        } else {
            showErrorPromptDialog(getString(R.string.prompt), updateFundPwdResult?.msg ?: getString(R.string.unknown_error)) {}
        }
    }

    private fun updateCurrentPwdEditTextHint(pwdPage: PwdPage, updatePayPw: Int?) = binding.run {
        if (pwdPage == PwdPage.LOGIN_PWD) {
            etCurrentPassword.labelText = getString(R.string.current_login_password)
            etCurrentPassword.setHintText(getString(R.string.hint_current_login_password))
            etNewPassword.setHintText(getString(R.string.hint_register_password))
            return@run
        }

        if (updatePayPw == FLAG_IS_NEED_UPDATE_PAY_PW) {
            etCurrentPassword.labelText = getString(R.string.current_login_password)
            etCurrentPassword.setHintText(getString(R.string.hint_current_login_password))
        } else {
            etCurrentPassword.labelText = getString(R.string.current_withdrawal_password)
            etCurrentPassword.setHintText(getString(R.string.hint_current_withdrawal_password))
        }
        etNewPassword.setHintText(getString(R.string.hint_withdrawal_new_password))
    }

    private fun cleanField() = binding.run {
        if (eetCurrentPassword.transformationMethod !is AsteriskPasswordTransformationMethod) {
            eetCurrentPassword.transformationMethod = AsteriskPasswordTransformationMethod()
        }
        if (eetNewPassword.transformationMethod !is AsteriskPasswordTransformationMethod) {
            eetNewPassword.transformationMethod = AsteriskPasswordTransformationMethod()
        }
        if (eetConfirmPassword.transformationMethod !is AsteriskPasswordTransformationMethod) {
            eetConfirmPassword.transformationMethod = AsteriskPasswordTransformationMethod()
        }
        eetCurrentPassword.text = null
        eetNewPassword.text = null
        eetConfirmPassword.text = null
        etCurrentPassword.setError(null, false)
        etNewPassword.setError(null, false)
        etConfirmPassword.setError(null, false)
        etCurrentPassword.setEndIcon(R.drawable.ic_eye_close)
        etNewPassword.setEndIcon(R.drawable.ic_eye_close)
        etConfirmPassword.setEndIcon(R.drawable.ic_eye_close)
        tvForgetPassword.isVisible = true // mPwdPage == PwdPage.BANK_PWD && (true != sConfigData?.enableRetrieveWithdrawPassword?.isStatusOpen())
    }

    private fun updateButtonStatus(isEnable: Boolean) {
        binding.btnConfirm.setBtnEnable(isEnable)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            finishWithOK()
        }
    }

}