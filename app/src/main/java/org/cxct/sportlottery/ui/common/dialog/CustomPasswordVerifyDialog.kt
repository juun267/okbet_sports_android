package org.cxct.sportlottery.ui.common.dialog

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.PassVerifyEnum
import org.cxct.sportlottery.common.extentions.showErrorPromptDialog
import org.cxct.sportlottery.common.extentions.toIntS
import org.cxct.sportlottery.databinding.DialogPasswordVerifyBinding
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.selflimit.SelfLimitViewModel
import org.cxct.sportlottery.util.MD5Util

/**
 * @author kevin
 * @create 2022/6/6
 * @description
 */
class CustomPasswordVerifyDialog : BaseDialog<SelfLimitViewModel,DialogPasswordVerifyBinding>() {

    init {
        setStyle(R.style.CustomDialogStyle)
    }

    companion object {
        const val PASSWORD_VERIFY = "password-verify"
        const val INPUT_VALUE = "input-value"

        @JvmStatic
        fun newInstance(passVerifyEnum: PassVerifyEnum, inputValue: String? = null) = CustomPasswordVerifyDialog().apply {
            arguments = Bundle().apply {
                putSerializable(PASSWORD_VERIFY, passVerifyEnum)
                if (inputValue != null) putString(INPUT_VALUE, inputValue)
            }
        }
    }

    override fun onInitView() {
        initView()
        initPasswordExitText()
        initObserve()
    }

    private fun initView()=binding.run {
        when (arguments?.getSerializable(PASSWORD_VERIFY) as PassVerifyEnum) {
            PassVerifyEnum.FROZE -> {
                tvTitle.text = getString(R.string.self_limit_confirm)
                tvMessage.text = getString(R.string.self_limit_input_password_for_confirm)
            }
            PassVerifyEnum.BET -> {
                tvTitle.text = getString(R.string.self_limit_fix_confirm)
                tvMessage.text = getString(R.string.self_limit_input_password_for_modification)
            }
        }

        tvSubmit.setOnClickListener {
            submit()
        }

        tvClose.setOnClickListener {
            dismiss()
        }
    }

    private fun initPasswordExitText()=binding.run {
        /* 同login頁面設定*/
        tfbPassword.endIconImageButton.setOnClickListener {
            if (tfbPassword.endIconResourceId == R.drawable.ic_eye_open) {
                etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                tfbPassword.setEndIcon(R.drawable.ic_eye_close)
            } else {
                tfbPassword.setEndIcon(R.drawable.ic_eye_open)
                etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            }
            etPassword.setSelection(etPassword.text.toString().length)
        }
    }

    private fun initObserve() {
        viewModel.passwordVerifyResult.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let {
                if (!it.success) {
                    val title: Int = when (arguments?.getSerializable(PASSWORD_VERIFY) as PassVerifyEnum) {
                        PassVerifyEnum.FROZE -> {
                            R.string.self_limit_confirm
                        }
                        PassVerifyEnum.BET -> {
                            R.string.self_limit_fix_confirm
                        }
                    }
                    requireActivity().showErrorPromptDialog(getString(title), it.msg) {}
                } else {
                    dismiss()
                }
            }
        }
    }

    private fun submit() {
        val enum = arguments?.getSerializable(PASSWORD_VERIFY) as PassVerifyEnum
        val inputValue = arguments?.getString(INPUT_VALUE)
        val password = binding.etPassword.text.toString()
        when (enum) {
            PassVerifyEnum.FROZE -> {
                if (!checkPassword(password)) return
                inputValue?.let {
                    viewModel.passwordVerifyForFroze(MD5Util.MD5Encode(password), it.toIntS())
                }
            }
            PassVerifyEnum.BET -> {
                if (!checkPassword(password)) return
                inputValue?.let {
                    viewModel.passwordVerifyForLimitBet(MD5Util.MD5Encode(password), it.toIntS())
                }
            }
        }
    }

    private fun checkPassword(password: String): Boolean {
        if (password.isBlank()) {
            binding.tfbPassword.setError(getString(R.string.error_input_empty), false)
            return false
        }
        return true
    }

}