package org.cxct.sportlottery.ui.profileCenter.nickname

import android.os.Bundle
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_change_nickname.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.user.nickname.NicknameResult
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.common.CustomAlertDialog

class ChangeNicknameActivity : BaseActivity<NicknameModel>(NicknameModel::class) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_nickname)

        setupNickname()
        initButton()
        initObserve()
    }

    private fun setupNickname() {
        et_nickname.afterTextChanged {
            checkInputData()
        }
    }

    private fun initButton() {
        btn_back.setOnClickListener {
            finish()
        }

        btn_confirm.setOnClickListener {
            if (viewModel.nicknameFormState.value?.isDataValid == true) {
                editNickName()
            } else {
                checkInputData()
            }
        }
    }

    private fun checkInputData() {
        viewModel.nicknameDataChanged(this, et_nickname.getText())
    }

    private fun editNickName() {
        loading()
        viewModel.editNickName(et_nickname.getText())
    }

    private fun initObserve() {
        viewModel.nicknameFormState.observe(this, Observer {
            et_nickname.setError(it.nicknameError)
        })

        viewModel.nicknameResult.observe(this, Observer {
            updateUiWithResult(it)
        })
    }

    private fun updateUiWithResult(nicknameResult: NicknameResult?) {
        hideLoading()
        if (nicknameResult?.success == true) {
            finish()
        } else {
            val errorMsg = nicknameResult?.msg?: getString(R.string.unknown_error)
            showErrorDialog(errorMsg)
        }
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