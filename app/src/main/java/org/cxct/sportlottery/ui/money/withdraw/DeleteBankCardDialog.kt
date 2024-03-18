package org.cxct.sportlottery.ui.money.withdraw

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.hideLoading
import org.cxct.sportlottery.common.extentions.loading
import org.cxct.sportlottery.databinding.DialogDeleteBankcardBinding
import org.cxct.sportlottery.repository.StaticData
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.util.CountDownUtil
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.util.setBtnEnable
import org.cxct.sportlottery.util.showCaptchaDialog
import org.cxct.sportlottery.view.boundsEditText.AsteriskPasswordTransformationMethod
import org.cxct.sportlottery.view.checkRegisterListener
import org.cxct.sportlottery.view.checkSMSCode
import org.cxct.sportlottery.view.isVisible
import splitties.bundle.put


class DeleteBankCardDialog: BaseDialog<WithdrawViewModel,DialogDeleteBankcardBinding>() {

    companion object{
        fun newInstance(phoneNo: String)=DeleteBankCardDialog().apply {
            arguments = Bundle().apply {
                put("phoneNo",phoneNo)
            }
        }
    }
    init {
        marginHorizontal=20.dp
    }
    private val phoneNo by lazy { requireArguments().getString("phoneNo")!! }
    private var countDownGoing = false

    override fun onInitView() {
        initPassWordInputStyle()
        initEditTextObserver()
        initObserver()
        setUpBtn()
    }


    private fun initObserver() {
        viewModel.onEmsCodeSended.observe(this) {
            requireActivity().hideLoading()

            if (it?.success == true) {
                CountDownUtil.smsCountDown(lifecycleScope, {
                    binding.btnSend.setBtnEnable(false)
                    countDownGoing = true
                }, {
                    binding.btnSend.setBtnEnable(false)
                    binding.btnSend.text = "${it}s"
                }, {
                    binding.btnSend.setBtnEnable(binding.eetWithdrawalPassword.text.toString().length == 4)
                    binding.btnSend.text = getString(R.string.send)
                    countDownGoing = false
                })
            } else {
                it?.msg?.let { msg -> ToastUtil.showToast(context, msg) }
            }
        }
    }


    private fun initPassWordInputStyle() {
        binding.blockSmsValidCode.isVisible = StaticData.isNeedOTPBank()
        val fieldBoxes = binding.etWithdrawalPassword
        val editText = binding.eetWithdrawalPassword
        fieldBoxes.endIconImageButton.setOnClickListener {
            if (fieldBoxes.endIconResourceId == R.drawable.ic_eye_open) {
                editText.transformationMethod = AsteriskPasswordTransformationMethod()
                fieldBoxes.setEndIcon(R.drawable.ic_eye_close)
            } else {
                fieldBoxes.setEndIcon(R.drawable.ic_eye_open)
                editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
            }
            fieldBoxes.setHasFocus(true)
            editText.setSelection(editText.text.toString().length)
        }
    }

    private fun initEditTextObserver() = binding.run {
        eetWithdrawalPassword.checkRegisterListener {
            resetConfirmEnable()
            etWithdrawalPassword.setError(getErrorMsg(it.length), false)
        }

        eetSmsCode.checkSMSCode(etSmsValidCode) {
            resetConfirmEnable()
        }
    }

    private fun getErrorMsg(length: Int): String? {
        return if (length == 0) {
            getString(R.string.error_input_empty)
        } else {
            null
        }
    }

    private fun resetConfirmEnable() = binding.run {
        tvConfirm.isEnabled = eetWithdrawalPassword.text.toString().length == 4 && (!StaticData.isNeedOTPBank() || eetSmsCode.text.toString().length == 4)
        tvConfirm.setBtnEnable(tvConfirm.isEnabled)
    }

    private fun setUpBtn() = binding.run {
        tvCancel.setOnClickListener { dismiss() }
        btnSend.setOnClickListener {
            showCaptchaDialog(childFragmentManager) { identity, validCode ->
                requireActivity().loading(null)
                viewModel.senEmsCode(phoneNo, "$identity", validCode)
            }
        }

        tvConfirm.setOnClickListener {
            val pwd = eetWithdrawalPassword.text.toString()

            if (pwd.isEmpty()) {
                etWithdrawalPassword.setError(getString(R.string.error_input_empty),false)
                return@setOnClickListener
            }
            if (pwd.length != 4) {
                etWithdrawalPassword.setError(getString(R.string.error_withdraw_password),false)
                return@setOnClickListener
            }

            val code = eetSmsCode.text.toString()
            if (binding.blockSmsValidCode.isVisible()) {
                if (code.isEmpty()) {
                    etSmsValidCode.setError(getString(R.string.P218),false)
                    return@setOnClickListener
                }
                if (code.length != 4) {
                    etSmsValidCode.setError(getString(R.string.sms_code_length_error),false)
                    return@setOnClickListener
                }

            }

            dismiss()
            (requireParentFragment() as? BankListFragment)?.apply {
                loading()
                viewModel.deleteBankCard(it.id.toString(), pwd, code)
            }
        }
    }
}