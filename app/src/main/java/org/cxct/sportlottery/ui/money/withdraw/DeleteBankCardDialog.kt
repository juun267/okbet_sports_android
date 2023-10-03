package org.cxct.sportlottery.ui.money.withdraw

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogDeleteBankcardBinding
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.login.VerifyCodeDialog
import org.cxct.sportlottery.util.CountDownUtil
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.util.setBtnEnable
import org.cxct.sportlottery.view.boundsEditText.AsteriskPasswordTransformationMethod
import org.cxct.sportlottery.view.checkRegisterListener


class DeleteBankCardDialog(private val phoneNo: String,
                           private val onResult: (String, String) -> Unit): BaseDialog<WithdrawViewModel>(WithdrawViewModel::class) {

    override fun setDefaulStyle() {  }

    private lateinit var binding: DialogDeleteBankcardBinding
    private var countDownGoing = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogDeleteBankcardBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        //不分手机上弹窗宽度会撑满，需重新设置下左右间距



        initWindowAttr()
        initPassWordInputStyle()
        initEditTextObserver()
        initObserver()
        setUpBtn()
    }

    private fun initWindowAttr() {
        dialog!!.window!!.setBackgroundDrawable(null)
    }


    private fun initObserver() {
        viewModel.onEmsCodeSended.observe(this) {
            hideLoading()

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
            fieldBoxes.hasFocus = true
            editText.setSelection(editText.text.toString().length)
        }
    }

    private fun initEditTextObserver() = binding.run {
        eetWithdrawalPassword.checkRegisterListener {
            resetConfirmEnable()
            if (it.length == 4) {
                btnSend.setBtnEnable(!countDownGoing)
//                etWithdrawalPassword.setError(null, false)
            } else {
                btnSend.setBtnEnable(false)
//                etWithdrawalPassword.setError(
//                    getString(R.string.hint_please_enter_withdraw_password),
//                    false
//                )
            }
        }

        eetSmsCode.checkRegisterListener {
            resetConfirmEnable()
//            if (it.length == 4) {
//                etSmsValidCode.setError(null, false)
//            } else {
//                etSmsValidCode.setError(getString(R.string.hint_verification_code_by_sms), false)
//            }
        }
    }


    private fun resetConfirmEnable() = binding.run {
        tvConfirm.isEnabled = eetWithdrawalPassword.text.toString().length == 4 && eetSmsCode.text.toString().length == 4
    }

    private fun setUpBtn() = binding.run {
        btnSend.setBtnEnable(false)
        tvCancel.setOnClickListener { dismiss() }
        btnSend.setOnClickListener {
            val verifyCodeDialog = VerifyCodeDialog()
            verifyCodeDialog.callBack = { identity, validCode ->
                loading()
                viewModel.senEmsCode(phoneNo, "$identity", validCode)
            }
            verifyCodeDialog.show(childFragmentManager, null)
        }

        tvConfirm.setOnClickListener {
            dismiss()
            val pwd = eetWithdrawalPassword.text.toString()
            val code = eetSmsCode.text.toString()
            onResult(pwd, code)
        }
    }









}