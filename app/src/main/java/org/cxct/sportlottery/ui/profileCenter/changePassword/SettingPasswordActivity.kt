package org.cxct.sportlottery.ui.profileCenter.changePassword

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_setting_password.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.db.entity.UserInfo
import org.cxct.sportlottery.network.user.updateFundPwd.UpdateFundPwdRequest
import org.cxct.sportlottery.network.user.updateFundPwd.UpdateFundPwdResult
import org.cxct.sportlottery.network.user.updatePwd.UpdatePwdRequest
import org.cxct.sportlottery.network.user.updatePwd.UpdatePwdResult
import org.cxct.sportlottery.repository.FLAG_IS_NEED_UPDATE_PAY_PW
import org.cxct.sportlottery.ui.base.BaseOddButtonActivity
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.util.MD5Util
import org.cxct.sportlottery.util.ToastUtil

class SettingPasswordActivity : BaseOddButtonActivity<SettingPasswordViewModel>(SettingPasswordViewModel::class) {
    companion object {
        const val PWD_PAGE = "PWD_PAGE"
    }

    enum class PwdPage { LOGIN_PWD, BANK_PWD }

    private var mPwdPage = PwdPage.LOGIN_PWD //登入密碼 or 提款密碼 page flag
    private var mUserInfo: UserInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_password)

        setupBackButton()
        setupTab()
        setupEditText()
        setupConfirmButton()
        initObserve()
    }

    private fun setupBackButton() {
        btn_back.setOnClickListener {
            finish()
        }
    }

    private fun setupTab() {
        tab_login_password.setOnClickListener {
            mPwdPage = PwdPage.LOGIN_PWD
            updateCurrentPwdEditTextHint(mPwdPage, mUserInfo?.updatePayPw)
            cleanField()
        }

        tab_withdrawal_password.setOnClickListener {
            mPwdPage = PwdPage.BANK_PWD
            updateCurrentPwdEditTextHint(mPwdPage, mUserInfo?.updatePayPw)
            cleanField()
        }

        //初始顯示哪個 tab 頁面
        when (intent.getSerializableExtra(PWD_PAGE)) {
            PwdPage.BANK_PWD -> tab_withdrawal_password.performClick()
            else -> tab_login_password.performClick()
        }
    }

    private fun setupEditText() {
        //當失去焦點才去檢查 inputData
        et_current_password.setEditTextOnFocusChangeListener { _: View, hasFocus: Boolean ->
            if (!hasFocus)
                checkInputData()
        }
        et_new_password.setEditTextOnFocusChangeListener { _: View, hasFocus: Boolean ->
            if (!hasFocus)
                checkInputData()
        }
        et_confirm_password.setEditTextOnFocusChangeListener { _: View, hasFocus: Boolean ->
            if (!hasFocus)
                checkInputData()
        }
    }

    private fun setupConfirmButton() {
        btn_confirm.setOnClickListener {
            if (checkInputData()) {
                when (mPwdPage) {
                    PwdPage.LOGIN_PWD -> updatePwd()
                    PwdPage.BANK_PWD -> updateFundPwd()
                }
            }
        }
    }

    private fun checkInputData(): Boolean {
        return viewModel.checkInputField(mPwdPage, this, et_current_password.getText(), et_new_password.getText(), et_confirm_password.getText())
    }

    private fun updatePwd() {
        val updatePwdRequest = UpdatePwdRequest(
            userId = mUserInfo?.userId ?: return,
            platformId = mUserInfo?.platformId ?: return,
            oldPassword = MD5Util.MD5Encode(et_current_password.getText()),
            newPassword = MD5Util.MD5Encode(et_new_password.getText())
        )

        loading()
        viewModel.updatePwd(updatePwdRequest)
    }

    private fun updateFundPwd() {
        val updateFundPwdRequest = UpdateFundPwdRequest(
            userId = mUserInfo?.userId ?: return,
            platformId = mUserInfo?.platformId ?: return,
            oldPassword = MD5Util.MD5Encode(et_current_password.getText()),
            newPassword = MD5Util.MD5Encode(et_new_password.getText())
        )

        loading()
        viewModel.updateFundPwd(updateFundPwdRequest)
    }

    private fun initObserve() {
        viewModel.passwordFormState.observe(this, Observer {
            val passwordState = it ?: return@Observer
            et_current_password.setError(passwordState.currentPwdError)
            et_new_password.setError(passwordState.newPwdError)
            et_confirm_password.setError(passwordState.confirmPwdError)
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
            showPromptDialog(getString(R.string.prompt), getString(R.string.update_login_pwd)) { finish() }
        } else {
            val errorMsg = updatePwdResult?.msg ?: getString(R.string.unknown_error)
            showErrorPromptDialog(getString(R.string.prompt), errorMsg) {}
        }
    }

    private fun updateUiWithResult(updateFundPwdResult: UpdateFundPwdResult?) {
        hideLoading()
        if (updateFundPwdResult?.success == true) {
            showPromptDialog(getString(R.string.prompt), getString(R.string.update_withdrawal_pwd)) { finish() }
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

    private fun showErrorDialog(errorMsg: String?) {
        val dialog = CustomAlertDialog(this)
        dialog.setMessage(errorMsg)
        dialog.setNegativeButtonText(null)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.show()
    }
}