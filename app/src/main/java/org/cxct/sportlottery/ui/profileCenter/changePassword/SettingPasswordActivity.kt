package org.cxct.sportlottery.ui.profileCenter.changePassword

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_setting_password.*
import kotlinx.android.synthetic.main.text_form_field_boxes_layout.view.*
import kotlinx.android.synthetic.main.view_base_tool_bar_no_drawer.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.user.UserInfo
import org.cxct.sportlottery.network.NetResult
import org.cxct.sportlottery.repository.FLAG_IS_NEED_UPDATE_PAY_PW
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.view.afterTextChanged
import org.cxct.sportlottery.util.setTitleLetterSpacing
import org.cxct.sportlottery.view.boundsEditText.AsteriskPasswordTransformationMethod
import org.cxct.sportlottery.view.boundsEditText.ExtendedEditText

/**
 * @app_destination 密码设置
 */
class SettingPasswordActivity :
    BaseSocketActivity<SettingPasswordViewModel>(SettingPasswordViewModel::class) {
    companion object {
        const val PWD_PAGE = "PWD_PAGE"
    }

    enum class PwdPage { LOGIN_PWD, BANK_PWD }

    private var mPwdPage = PwdPage.LOGIN_PWD //登入密碼 or 提款密碼 page flag
    private var mUserInfo: UserInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusbar(R.color.color_232C4F_FFFFFF, true)
        setContentView(R.layout.activity_setting_password)

        initView()
        setupBackButton()
        setupTab()
        setupEditText()
        setupConfirmButton()
        initObserve()
    }

    private fun initView() {
        tv_toolbar_title.setTitleLetterSpacing()
        tv_toolbar_title.text = getString(R.string.setting_password)
        eet_current_password.transformationMethod =
            AsteriskPasswordTransformationMethod()
        eet_new_password.transformationMethod =
            AsteriskPasswordTransformationMethod()
        eet_confirm_password.transformationMethod =
            AsteriskPasswordTransformationMethod()

        val bottomLineColorRes = R.color.color_80334266_E3E8EE
        et_current_password.isVisible = viewModel.userInfo.value?.passwordSet == false
        et_current_password.bottom_line.setBackgroundResource(bottomLineColorRes)
        et_new_password.bottom_line.setBackgroundResource(bottomLineColorRes)
        et_confirm_password.bottom_line.setBackgroundResource(bottomLineColorRes)
        updateButtonStatus(false)

        et_current_password.endIconImageButton.setOnClickListener {
            if (et_current_password.endIconResourceId == R.drawable.ic_eye_open) {
                eet_current_password.transformationMethod =
                    AsteriskPasswordTransformationMethod()
                et_current_password.setEndIcon(R.drawable.ic_eye_close)
            } else {
                et_current_password.setEndIcon(R.drawable.ic_eye_open)
                eet_current_password.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
            }
            et_current_password.hasFocus = true
            eet_current_password.setSelection(eet_current_password.text.toString().length)
        }

        et_new_password.endIconImageButton.setOnClickListener {
            if (et_new_password.endIconResourceId == R.drawable.ic_eye_open) {
                eet_new_password.transformationMethod =
                    AsteriskPasswordTransformationMethod()
                et_new_password.setEndIcon(R.drawable.ic_eye_close)
            } else {
                et_new_password.setEndIcon(R.drawable.ic_eye_open)
                eet_new_password.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
            }
            et_new_password.hasFocus = true
            eet_new_password.setSelection(eet_new_password.text.toString().length)
        }
        et_confirm_password.endIconImageButton.setOnClickListener {
            if (et_confirm_password.endIconResourceId == R.drawable.ic_eye_open) {
                eet_confirm_password.transformationMethod =
                    AsteriskPasswordTransformationMethod()
                et_confirm_password.setEndIcon(R.drawable.ic_eye_close)
            } else {
                et_confirm_password.setEndIcon(R.drawable.ic_eye_open)
                eet_confirm_password.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
            }
            et_confirm_password.hasFocus = true
            eet_confirm_password.setSelection(eet_confirm_password.text.toString().length)
        }

    }

    private fun setupBackButton() {
        btn_toolbar_back.setOnClickListener {
            finish()
        }
    }

    private fun setupTab() {
        custom_tab_layout.setCustomTabSelectedListener { position ->
            when (position) {
                0 -> {
                    mPwdPage = PwdPage.LOGIN_PWD
                    updateCurrentPwdEditTextHint(mPwdPage, mUserInfo?.updatePayPw)
                    cleanField()
                }

                1 -> {
                    mPwdPage = PwdPage.BANK_PWD
                    updateCurrentPwdEditTextHint(mPwdPage, mUserInfo?.updatePayPw)
                    cleanField()
                }
            }
        }

        //初始顯示哪個 tab 頁面
        when (intent.getSerializableExtra(PWD_PAGE)) {
            PwdPage.BANK_PWD -> custom_tab_layout.selectTab(1)
            else -> custom_tab_layout.selectTab(0)
        }

    }

    private fun setupEditText() {
        //當失去焦點才去檢查 inputData
        setupEditTextFocusChangeEvent(eet_current_password) { viewModel.checkCurrentPwd(it) }
        setupEditTextFocusChangeEvent(eet_new_password) {
            viewModel.checkNewPwd(
                mPwdPage,
                eet_current_password.getText().toString(),
                it
            )
        }
        setupEditTextFocusChangeEvent(eet_confirm_password) {
            viewModel.checkConfirmPwd(
                eet_new_password.getText().toString(),
                it
            )
        }

        eet_current_password.afterTextChanged {
            viewModel.checkCurrentPwd(it)
        }
        eet_new_password.afterTextChanged {
            viewModel.checkNewPwd(
                mPwdPage,
                eet_current_password.text.toString(),
                it
            )
        }
        eet_confirm_password.afterTextChanged {
            viewModel.checkConfirmPwd(
                eet_new_password.text.toString(),
                it
            )
        }
    }

    private fun setupEditTextFocusChangeEvent(
        editText: ExtendedEditText,
        listener: (String) -> Unit
    ) {

        editText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus)
                listener.invoke(editText.getText().toString())
        }
    }

    private fun setupConfirmButton() {
        btn_confirm.setOnClickListener {
            checkInputData()
        }
        btn_confirm.setTitleLetterSpacing()
    }

    private fun checkInputData() {
        viewModel.checkInputField(
            mPwdPage,
            eet_current_password.getText().toString(),
            eet_new_password.getText().toString(),
            eet_confirm_password.getText().toString()
        )
    }

    private fun initObserve() {
        viewModel.submitEnable.observe(this) {
            updateButtonStatus(it)
        }

        viewModel.currentPwdError.observe(this, Observer {
            et_current_password.setError(it, false)
        })

        viewModel.newPwdError.observe(this, Observer {
            et_new_password.setError(it, false)
        })

        viewModel.confirmPwdError.observe(this, Observer {
            et_confirm_password.setError(it, false)
        })

        viewModel.updatePwdResult.observe(this, Observer {
            updateUiWithResult(it)
        })

        viewModel.updateFundPwdResult.observe(this, Observer {
            updateFundPwdUiWithResult(it)
        })

        viewModel.userInfo.observe(this, Observer {
            mUserInfo = it
            updateCurrentPwdEditTextHint(mPwdPage, mUserInfo?.updatePayPw)
            et_current_password.isVisible = it?.passwordSet == false
        })
    }

    private fun updateUiWithResult(updatePwdResult: NetResult?) {
        hideLoading()
        if (updatePwdResult?.success == true) {
            showPromptDialog(
                getString(R.string.prompt),
                getString(R.string.tips_login_password_success)
            ) { finish() }
        } else {
            val errorMsg = updatePwdResult?.msg ?: getString(R.string.unknown_error)
            showErrorPromptDialog(getString(R.string.prompt), errorMsg) {}
        }
    }

    private fun updateFundPwdUiWithResult(updateFundPwdResult: NetResult?) {
        hideLoading()
        if (updateFundPwdResult?.success == true) {
            showPromptDialog(
                getString(R.string.prompt),
                getString(R.string.update_withdrawal_pwd)
            ) { finish() }
        } else {
            val errorMsg = updateFundPwdResult?.msg ?: getString(R.string.unknown_error)
            showErrorPromptDialog(getString(R.string.prompt), errorMsg) {}
        }
    }

    private fun updateCurrentPwdEditTextHint(pwdPage: PwdPage, updatePayPw: Int?) {
        if (pwdPage == PwdPage.LOGIN_PWD) {
            et_current_password.labelText = getString(R.string.current_login_password)
            et_current_password.setHintText(getString(R.string.hint_current_login_password))
            et_new_password.setHintText(getString(R.string.hint_register_password))
        } else {

            if (updatePayPw == FLAG_IS_NEED_UPDATE_PAY_PW) {
                et_current_password.labelText = getString(R.string.current_login_password)
                et_current_password.setHintText(getString(R.string.hint_current_login_password))
            } else {
                et_current_password.labelText = getString(R.string.current_withdrawal_password)
                et_current_password.setHintText(getString(R.string.hint_current_withdrawal_password))
            }
            et_new_password.setHintText(getString(R.string.hint_withdrawal_new_password))
        }
    }

    private fun cleanField() {
        eet_current_password.text = null
        et_current_password.setError(null, false)

        eet_new_password.setText(null)
        et_new_password.setError(null, false)

        eet_confirm_password.setText(null)
        et_confirm_password.setError(null, false)
    }

    private fun updateButtonStatus(isEnable: Boolean) {
        if (isEnable) {
            btn_confirm.isEnabled = true
            btn_confirm.alpha = 1.0f
        } else {
            btn_confirm.isEnabled = false
            btn_confirm.alpha = 0.5f
        }
    }

}