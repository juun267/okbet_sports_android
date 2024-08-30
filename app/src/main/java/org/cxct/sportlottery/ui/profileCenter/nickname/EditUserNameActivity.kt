package org.cxct.sportlottery.ui.profileCenter.nickname


import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.hideLoading
import org.cxct.sportlottery.common.extentions.loading
import org.cxct.sportlottery.databinding.ActivityEditUsernameBinding
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.common.dialog.CustomAlertDialog
import org.cxct.sportlottery.util.VerifyConstUtil
import org.cxct.sportlottery.util.setBtnEnable
import org.cxct.sportlottery.view.boundsEditText.ExtendedEditText
import org.cxct.sportlottery.view.boundsEditText.TextFormFieldBoxes
import org.cxct.sportlottery.view.checkRegisterListener

class EditUserNameActivity: BaseActivity<ModifyProfileInfoViewModel, ActivityEditUsernameBinding>() {

    override fun pageName() = "修改真实姓名页面"

    override fun onInitView() {
        setStatusbar(R.color.color_232C4F_FFFFFF, true)
        binding.toolBar.setOnBackPressListener { finish() }
        binding.toolBar.titleText = getString(R.string.real_name)
        initEditText()
        initObserver()
        viewModel.loading.observe(this) {
            if (it)
                loading()
            else
                hideLoading()
        }
    }

    private fun initEditText()=binding.run {
        eetFirstName.checkRegisterListener { checkInput(eetFirstName, etFirstName, true) }
        eedtMiddleName.checkRegisterListener { checkInput(eedtMiddleName, edtMiddleName, !cbNoMiddleName.isChecked) }
        eedtLastName.checkRegisterListener { checkInput(eedtLastName, edtLastName, true) }
        binding.btnConfirm.setBtnEnable(false)
        binding.btnConfirm.setOnClickListener { change() }
        cbNoMiddleName.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {
                eedtMiddleName.setText("N/A")
                eedtMiddleName.isEnabled = false
                edtMiddleName.isEnabled = false
            } else {
                eedtMiddleName.setText("")
                eedtMiddleName.isEnabled = true
                edtMiddleName.isEnabled = true
            }
            edtMiddleName.setError(null, true)
            resetConfirmEnable()
        }
    }

    private fun resetConfirmEnable()=binding.run {
        binding.btnConfirm.setBtnEnable((eetFirstName.text.toString().isNotEmpty() && !etFirstName.isOnError)
                && (eedtLastName.text.toString().isNotEmpty() && !edtLastName.isOnError)
                && (eedtMiddleName.text.toString().isNotEmpty() && !edtMiddleName.isOnError))
    }

    private fun checkInput(editText: ExtendedEditText, textFormFieldBoxes: TextFormFieldBoxes, needCheck: Boolean = true) {

        if (needCheck) {
            val inputString = editText.text.toString()
            if (inputString.isEmpty()) {
                textFormFieldBoxes.setError(getString(R.string.error_input_empty), false)
            } else {
                textFormFieldBoxes.setError(if (VerifyConstUtil.verifyFullName2(inputString)) "" else getString(R.string.N280), false)
            }
        }

        resetConfirmEnable()
    }

    private fun change()=binding.run {
        val firstName = eetFirstName.text.toString()
        val middelName = eedtMiddleName.text.toString()
        val lastName = eedtLastName.text.toString()
        viewModel.editUserName(firstName, middelName, lastName)
    }

    private fun initObserver() {
        viewModel.userNameChangeResult.observe(this) {
            if (it.first) {
                finish()
            } else {
                showErrorDialog(it.second)
            }
        }
    }

    private fun showErrorDialog(errorMsg: String?) {
        val dialog = CustomAlertDialog()
        dialog.setMessage(errorMsg)
        dialog.setNegativeButtonText(null)
        dialog.setCanceledOnTouchOutside(false)
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, null)
    }
}