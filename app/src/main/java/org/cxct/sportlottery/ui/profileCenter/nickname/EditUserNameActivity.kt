package org.cxct.sportlottery.ui.profileCenter.nickname


import kotlinx.android.synthetic.main.activity_edit_username.*
import kotlinx.android.synthetic.main.layout_username.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityEditUsernameBinding
import org.cxct.sportlottery.network.common.BaseResult
import org.cxct.sportlottery.ui.base.BindingActivity
import org.cxct.sportlottery.ui.common.dialog.CustomAlertDialog
import org.cxct.sportlottery.util.VerifyConstUtil
import org.cxct.sportlottery.util.setBtnEnable
import org.cxct.sportlottery.view.boundsEditText.ExtendedEditText
import org.cxct.sportlottery.view.boundsEditText.TextFormFieldBoxes
import org.cxct.sportlottery.view.checkRegisterListener

class EditUserNameActivity: BindingActivity<ModifyProfileInfoViewModel, ActivityEditUsernameBinding>() {

    override fun onInitView() {
        setStatusbar(R.color.color_232C4F_FFFFFF, true)
        binding.toolBar.btnToolbarBack.setOnClickListener { finish() }
        binding.toolBar.tvToolbarTitle.text = getString(R.string.real_name)
        initEditText()
        initObserver()
        viewModel.loading.observe(this) {
            if (it)
                loading()
            else
                hideLoading()
        }
    }

    private fun initEditText() {
        eetFirstName.checkRegisterListener { checkInput(eetFirstName, etFirstName, true) }
        eedtMiddleName.checkRegisterListener { checkInput(eedtMiddleName, edtMiddleName, !cbNoMiddleName.isChecked) }
        eedtLastName.checkRegisterListener { checkInput(eedtLastName, edtLastName, true) }
        btnConfirm.setBtnEnable(false)
        btnConfirm.setOnClickListener { change() }
        cbNoMiddleName.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                eedtMiddleName.isEnabled = false
                eedtMiddleName.setText("")
            } else {
                eedtMiddleName.isEnabled = true
            }
        }
    }

    private fun checkInput(editText: ExtendedEditText, textFormFieldBoxes: TextFormFieldBoxes, needCheck: Boolean = true) {
        btnConfirm.setBtnEnable(eetFirstName.text.toString().isNotEmpty()
                && eedtLastName.text.toString().isNotEmpty()
                && (cbNoMiddleName.isChecked || eedtMiddleName.text.toString().isNotEmpty()))

        if (!needCheck) {
            return
        }

        val inputString = editText.text.toString()
        if (inputString.isEmpty()) {
            textFormFieldBoxes.setError(getString(R.string.error_input_empty), false)
        } else {
            textFormFieldBoxes.setError(if (VerifyConstUtil.verifyFullName(inputString)) "" else getString(R.string.error_input_has_blank), false)
        }
    }

    private fun change() {
        val firstName = eetFirstName.text.toString()
        val middelName = if (cbNoMiddleName.isChecked) null else eedtMiddleName.text.toString()
        val lastName = eedtLastName.text.toString()
        viewModel.editUserName(firstName, middelName, lastName)
    }

    private fun initObserver() {
        viewModel.withdrawInfoResult.observe(this) {
            updateUiWithResult(it)
        }
    }

    private fun updateUiWithResult(result: BaseResult?) {
        if (result?.success == true) {
            finish()
        } else {
            val errorMsg = result?.msg ?: getString(R.string.unknown_error)
            showErrorDialog(errorMsg)
        }
    }

    private fun showErrorDialog(errorMsg: String?) {
        val dialog = CustomAlertDialog(this)
        dialog.setMessage(errorMsg)
        dialog.setNegativeButtonText(null)
        dialog.setCanceledOnTouchOutside(false)
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, null)
    }
}