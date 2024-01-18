package org.cxct.sportlottery.ui.common.dialog

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.dialog_password_verify.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.PassVerifyEnum
import org.cxct.sportlottery.common.extentions.showErrorPromptDialog
import org.cxct.sportlottery.common.extentions.toIntS
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.selflimit.SelfLimitViewModel
import org.cxct.sportlottery.util.MD5Util

/**
 * @author kevin
 * @create 2022/6/6
 * @description
 */
class CustomPasswordVerifyDialog : BaseDialog<SelfLimitViewModel>(SelfLimitViewModel::class) {

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.dialog_password_verify, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initPasswordExitText()
        initObserve()
    }

    private fun initView() {
        when (arguments?.getSerializable(PASSWORD_VERIFY) as PassVerifyEnum) {
            PassVerifyEnum.FROZE -> {
                tv_title.text = getString(R.string.self_limit_confirm)
                tv_message.text = getString(R.string.self_limit_input_password_for_confirm)
            }
            PassVerifyEnum.BET -> {
                tv_title.text = getString(R.string.self_limit_fix_confirm)
                tv_message.text = getString(R.string.self_limit_input_password_for_modification)
            }
        }

        tv_submit.setOnClickListener {
            submit()
        }

        tv_close.setOnClickListener {
            dismiss()
        }
    }

    private fun initPasswordExitText() {
        /* 同login頁面設定*/
        tfb_password.endIconImageButton.setOnClickListener {
            if (tfb_password.endIconResourceId == R.drawable.ic_eye_open) {
                et_password.transformationMethod = PasswordTransformationMethod.getInstance()
                tfb_password.setEndIcon(R.drawable.ic_eye_close)
            } else {
                tfb_password.setEndIcon(R.drawable.ic_eye_open)
                et_password.transformationMethod = HideReturnsTransformationMethod.getInstance()
            }
            et_password.setSelection(et_password.text.toString().length)
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
        val password = et_password.text.toString()
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
            tfb_password.setError(getString(R.string.error_input_empty), false)
            return false
        }
        return true
    }

}