package org.cxct.sportlottery.ui.selflimit

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.text.isDigitsOnly
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.PassVerifyEnum
import org.cxct.sportlottery.common.extentions.hideSoftKeyboard
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.common.extentions.toDoubleS
import org.cxct.sportlottery.common.extentions.toIntS
import org.cxct.sportlottery.databinding.FragmentSelfLimitBetBinding
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.common.dialog.CustomAlertDialog
import org.cxct.sportlottery.ui.common.dialog.CustomPasswordVerifyDialog
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.view.afterTextChanged
import org.cxct.sportlottery.view.checkRegisterListener

/**
 * @app_destination 自我禁制-每次投注上限
 */
@SuppressLint("SetTextI18n")
class SelfLimitBetFragment : BaseFragment<SelfLimitViewModel,FragmentSelfLimitBetBinding>(),
    View.OnClickListener {

    val perBetMinAmount =
        if (sConfigData?.perBetMinAmount.isNullOrEmpty()) 0 else sConfigData?.perBetMinAmount?.toIntS()
            ?: 0
    val perBetMaxAmount =
        if (sConfigData?.perBetMaxAmount.isNullOrEmpty()) 0 else sConfigData?.perBetMaxAmount?.toIntS()
            ?: 0

    override fun onClick(v: View?) {
        when (v) {
            binding.llImportant -> {
              SelfLimitFrozeImportantDialog.newInstance(true).show(childFragmentManager)
            }
            binding.btnConfirm -> {
                submit()
            }
        }
    }
    override fun onInitView(view: View) {
        viewModel.showToolbar(true)
        viewModel.setToolbarName(getString(R.string.self_limit))
        initView()
        initEditText()
        initObserve()
        resetView()
    }
    private fun resetView() {
        binding.etMount.setText("")
        binding.llSelfLimit.isSelected = false
        binding.tvError.visibility = View.GONE
        binding.btnConfirm.isEnabled = false
        initEditText()
    }

    private fun initEditText() {
        binding.etMount.apply{
            afterTextChanged {
                when {
                    it.isNullOrEmpty() -> {
                        viewModel.setBetEditTextError(false)
                    }
                    !it.isDigitsOnly() || it.toLong() > perBetMaxAmount -> {
                        viewModel.setFrozeEditTextError(true)
                    }
                    it.toLong() < perBetMinAmount -> {
                        viewModel.setFrozeEditTextError(true)
                    }
                    else -> viewModel.setBetEditTextError(false)
                }
        }
            onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
                binding.llSelfLimit.isSelected = hasFocus
            }
        }
    }

    private fun initView() {
        binding.tvError.text =
            getString(R.string.self_limit_error_format, perBetMinAmount, perBetMaxAmount)
        binding.llImportant.setOnClickListener(this)
        binding.btnConfirm.setOnClickListener(this)
        if (viewModel.userInfo.value?.perBetLimit == null) {
            binding.tvPerBetLimit.text = String.format(
                getString(R.string.self_limit_per_bet_limit_user),
                getString(R.string.self_limit_per_bet_limit_user_none),
                ""
            )
        } else {
            binding.tvPerBetLimit.text = String.format(
                getString(R.string.self_limit_per_bet_limit_user),
                sConfigData?.systemCurrencySign,
                TextUtil.formatMoney(viewModel.userInfo.value?.perBetLimit?.toDouble() ?: 0.0)
            ).apply {
                if (viewModel.userInfo.value?.perBetLimit?.toDouble() == 0.0){
                    String.format(
                        getString(R.string.self_limit_per_bet_limit_user),
                        "",
                        TextUtil.formatMoney(viewModel.userInfo.value?.perBetLimit?.toDouble() ?: "无")
                    )
                }
            }
        }

        binding.tvLimit.text = String.format(
            getString(R.string.self_limit_per_bet_limit_user_limit),  sConfigData?.systemCurrencySign ?: " ",
            TextUtil.formatMoney(perBetMinAmount),
            TextUtil.formatMoney(perBetMaxAmount)
        )
    }

    private fun initObserve() {
        viewModel.isBetEditTextError.observe(this.viewLifecycleOwner) { showError ->
            if (showError) {
                binding.llSelfLimit.isActivated = true
                binding.tvError.visibility = View.VISIBLE
            } else {
                binding.llSelfLimit.isActivated = false
                binding.tvError.visibility = View.GONE
            }
            binding.btnConfirm.isEnabled = !showError && binding.etMount.text.isNotEmpty()
        }

        viewModel.perBetLimitResult.observe(this.viewLifecycleOwner) {
            if (!it.success) {
                return@observe
            }

            val dialog = CustomAlertDialog().apply {
                isCancelable = false
                setTitle(this@SelfLimitBetFragment.getString(R.string.self_limit_fix_confirm))
                setMessage(this@SelfLimitBetFragment.getString(R.string.self_limit_fix_confirm_done))
                setNegativeButtonText(null)
                setPositiveButtonText(this@SelfLimitBetFragment.getString(R.string.btn_confirm))
                setPositiveClickListener {
                    updateBetLimit(binding.etMount.text.toString())
                    resetView()
                    viewModel.getUserInfo()
                    dismiss()
                }
            }
            dialog.show(childFragmentManager, null)
        }

        viewModel.userInfoResult.observe(viewLifecycleOwner) {
            binding.tvPerBetLimit.text =
                String.format(
                    getString(R.string.self_limit_per_bet_limit_user),
                    sConfigData?.systemCurrencySign,
                    TextUtil.formatMoney(viewModel.userInfo.value?.perBetLimit ?: 0)
                )
        }
    }

    private fun submit() {
        requireActivity().hideSoftKeyboard()
        val days = binding.etMount.text.toString()
        if (days.isEmptyStr()) {
            return
        }
        CustomPasswordVerifyDialog.newInstance(PassVerifyEnum.BET, inputValue = days)
            .show(childFragmentManager)
    }

    private fun updateBetLimit(text: String) {
        binding.tvPerBetLimit.text = String.format(
            getString(R.string.self_limit_per_bet_limit_user),
            sConfigData?.systemCurrencySign,
            TextUtil.formatMoney(text.toDoubleS())
        )
    }

}