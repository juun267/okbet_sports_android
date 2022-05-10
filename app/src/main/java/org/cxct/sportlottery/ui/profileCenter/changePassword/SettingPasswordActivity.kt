package org.cxct.sportlottery.ui.profileCenter.changePassword

import android.os.Bundle
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_setting_password.*
import kotlinx.android.synthetic.main.view_base_tool_bar_no_drawer.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.db.entity.UserInfo
import org.cxct.sportlottery.network.user.updateFundPwd.UpdateFundPwdResult
import org.cxct.sportlottery.network.user.updatePwd.UpdatePwdResult
import org.cxct.sportlottery.repository.FLAG_IS_NEED_UPDATE_PAY_PW
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.login.LoginEditText
import org.cxct.sportlottery.util.setTitleLetterSpacing

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
        setupEditTextFocusChangeEvent(et_current_password) { viewModel.checkCurrentPwd(it) }
        setupEditTextFocusChangeEvent(et_new_password) {
            viewModel.checkNewPwd(
                mPwdPage,
                et_current_password.getText(),
                it
            )
        }
        setupEditTextFocusChangeEvent(et_confirm_password) {
            viewModel.checkConfirmPwd(
                et_new_password.getText(),
                it
            )
        }
    }

    private fun setupEditTextFocusChangeEvent(editText: LoginEditText, listener: (String) -> Unit) {
        editText.setEditTextOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus)
                listener.invoke(editText.getText())
        }
    }

    private fun setupConfirmButton() {
        btn_confirm.setOnClickListener {
            checkInputData()
        }
    }

    private fun checkInputData() {
        viewModel.checkInputField(
            mPwdPage,
            et_current_password.getText(),
            et_new_password.getText(),
            et_confirm_password.getText()
        )
    }

    private fun initObserve() {

        viewModel.currentPwdError.observe(this, Observer {
            et_current_password.setError(it)
        })

        viewModel.newPwdError.observe(this, Observer {
            et_new_password.setError(it)
        })

        viewModel.confirmPwdError.observe(this, Observer {
            et_confirm_password.setError(it)
        })

        viewModel.updatePwdResult.observe(this, Observer {
            updateUiWithResult(it)
        })

        viewModel.updateFundPwdResult.observe(this, Observer {
            updateUiWithResult(it)
        })

        viewModel.userInfo.observe(this, Observer {
            mUserInfo = it
            updateCurrentPwdEditTextHint(mPwdPage, mUserInfo?.updatePayPw)
        })
    }

    private fun updateUiWithResult(updatePwdResult: UpdatePwdResult?) {
        hideLoading()
        if (updatePwdResult?.success == true) {
            showPromptDialog(
                getString(R.string.prompt),
                getString(R.string.update_login_pwd)
            ) { finish() }
        } else {
            val errorMsg = updatePwdResult?.msg ?: getString(R.string.unknown_error)
            showErrorPromptDialog(getString(R.string.prompt), errorMsg) {}
        }
    }

    private fun updateUiWithResult(updateFundPwdResult: UpdateFundPwdResult?) {
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
            et_current_password.setTitle(getString(R.string.current_login_password))
            et_current_password.setHint(getString(R.string.hint_current_login_password))
            et_new_password.setHint(getString(R.string.hint_register_password))
        } else {

            if (updatePayPw == FLAG_IS_NEED_UPDATE_PAY_PW) {
                et_current_password.setTitle(getString(R.string.current_login_password))
                et_current_password.setHint(getString(R.string.hint_current_login_password))
            } else {
                et_current_password.setTitle(getString(R.string.current_withdrawal_password))
                et_current_password.setHint(getString(R.string.hint_current_withdrawal_password))
            }
            et_new_password.setHint(getString(R.string.hint_withdrawal_new_password))
        }
    }

    private fun cleanField() {
        et_current_password.setText(null)
        et_current_password.setError(null)

        et_new_password.setText(null)
        et_new_password.setError(null)

        et_confirm_password.setText(null)
        et_confirm_password.setError(null)
    }
}