package org.cxct.sportlottery.ui.selflimit

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.text.isDigitsOnly
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.enums.PassVerifyEnum
import org.cxct.sportlottery.common.extentions.hideSoftKeyboard
import org.cxct.sportlottery.databinding.FragmentSelfLimitFrozeBinding
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.common.dialog.CustomAlertDialog
import org.cxct.sportlottery.ui.common.dialog.CustomPasswordVerifyDialog
import org.cxct.sportlottery.view.afterTextChanged
import org.cxct.sportlottery.ui.maintab.MainTabActivity

/**
 * @app_destination 自我禁制-帳號登入限制
 */
@SuppressLint("SetTextI18n")
class SelfLimitFrozeFragment : BaseFragment<SelfLimitViewModel,FragmentSelfLimitFrozeBinding>(),
    View.OnClickListener {

    private var minFrozeDay = sConfigData?.minFrozeDay ?: 0

    override fun onClick(v: View?) {
        when (v) {
            binding.llImportant -> {
                val dialog = SelfLimitFrozeImportantDialog()
                dialog.arguments = Bundle().apply { putBoolean("isBet", false) }
                dialog.setCanceledOnTouchOutside(true)
                dialog.isCancelable = true
                dialog.show(childFragmentManager, null)
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
        initObserve()
        initEditText()
        resetView()
    }

    private fun resetView() {
        binding.llSelfFrozeLimit.isSelected = false
        binding.tvError.visibility = View.GONE
        binding.btnConfirm.isEnabled = false
    }

    private fun initEditText() {
        binding.etFrozeDay.apply {
            afterTextChanged {
                when {
                    it.isNullOrEmpty() -> {
                        viewModel.setFrozeEditTextError(false)
                    }
                    !it.isDigitsOnly() || it.toLong() > 999 -> {
                        binding.tvError.text =
                            getString(R.string.self_limit_error_format, minFrozeDay, 999)
                        viewModel.setFrozeEditTextError(true)
                    }
                    it.toLong() < minFrozeDay -> {
                        binding.tvError.text =
                            getString(R.string.more_than_days_format, minFrozeDay)
                        viewModel.setFrozeEditTextError(true)
                    }
                    else -> {
                        viewModel.setFrozeEditTextError(false)
                    }
                }
            }
            onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
                binding.llSelfFrozeLimit.isSelected = hasFocus
            }
        }
    }

    private fun initView() {
        binding.llImportant.setOnClickListener(this)
        binding.btnConfirm.setOnClickListener(this)
        binding.tvUsTime.text = getString(R.string.text_us_east_time)
        if (minFrozeDay > 0)
            binding.etFrozeDay.hint = getString(R.string.pls_enter_days_format, minFrozeDay)
        binding.tvError.text = getString(R.string.more_than_days_format, minFrozeDay)
    }

    private fun initObserve() {
        viewModel.isFrozeEditTextError.observe(this.viewLifecycleOwner) { showError ->
            if (showError) {
                binding.llSelfFrozeLimit.isActivated = true
                binding.tvError.visibility = View.VISIBLE
            } else {
                binding.llSelfFrozeLimit.isActivated = false
                binding.tvError.visibility = View.GONE
            }

            binding.btnConfirm.isEnabled = !showError
        }

        viewModel.frozeResult.observe(viewLifecycleOwner) {
            if (it.success) {
                val dialog = CustomAlertDialog(requireActivity()).apply {
                    isCancelable = false
                    setTitle(this@SelfLimitFrozeFragment.getString(R.string.self_limit_confirm))
                    setMessage(this@SelfLimitFrozeFragment.getString(R.string.self_limit_confirm_done))
                    setNegativeButtonText(null)
                    setPositiveButtonText(this@SelfLimitFrozeFragment.getString(R.string.btn_confirm))
                    setPositiveClickListener {
                        dismiss()
                        viewModel.doLogoutCleanUser {
                            run {
//                                if (sConfigData?.thirdOpen == FLAG_OPEN)
//                                    MainActivity.reStart(MultiLanguagesApplication.appContext)
//                                else
                                MainTabActivity.reStart(MultiLanguagesApplication.appContext)
                            }
                        }
                    }
                }
                dialog.show(childFragmentManager, null)
            }
        }
    }

    private fun submit() {
        requireActivity().hideSoftKeyboard()
        CustomPasswordVerifyDialog.newInstance(PassVerifyEnum.FROZE, inputValue = binding.etFrozeDay.text.toString())
            .show(childFragmentManager, CustomPasswordVerifyDialog::class.java.simpleName)
    }

}