package org.cxct.sportlottery.ui.selflimit

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentSelfLimitBetBinding
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.util.TextUtil

/**
 * @app_destination 自我禁制-每次投注上限
 */
@SuppressLint("SetTextI18n")
class SelfLimitBetFragment : BaseFragment<SelfLimitViewModel>(SelfLimitViewModel::class),
    View.OnClickListener {

    private lateinit var binding: FragmentSelfLimitBetBinding
    private var textWatch: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            s.toString().let {
                var min = sConfigData?.perBetMinAmount ?: "0"
                if (min.isNullOrEmpty()) {min = "0"}
                var max = sConfigData?.perBetMaxAmount ?: "0"
                if (max.isNullOrEmpty()) {max = "0"}
                when {
                    it.isNullOrEmpty() -> viewModel.setBetEditTextError(true)
                    it.toLong() in (min.toLong())..(max.toLong()) -> viewModel.setBetEditTextError(false)
                    else -> viewModel.setBetEditTextError(true)
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.llImportant -> {
                val dialog = SelfLimitFrozeImportantDialog(true)
                dialog.setCanceledOnTouchOutside(true)
                dialog.isCancelable = true
                dialog.show(childFragmentManager, null)
            }
            binding.btnConfirm -> {
                submit()
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel.showToolbar(true)
        viewModel.setToolbarName(getString(R.string.self_limit))
        binding = FragmentSelfLimitBetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initEditText()
        initObserve()
        initDataLive()
        resetView()
    }

    private fun resetView() {
        binding.etMount.removeTextChangedListener(textWatch)
        binding.etMount.setText("")
        binding.llSelfLimit.isSelected = false
        binding.tvError.visibility = View.GONE
        binding.btnConfirm.isEnabled = false
        initEditText()
    }

    private fun initEditText() {
        binding.etMount.apply {
            addTextChangedListener(textWatch)
            onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
                binding.llSelfLimit.isSelected = hasFocus
            }
        }
    }

    private fun initView() {
        binding.llImportant.setOnClickListener(this)
        binding.btnConfirm.setOnClickListener(this)
        if (viewModel.userInfo.value?.perBetLimit == null) {
            binding.tvPerBetLimit.text = String.format(
                getString(R.string.self_limit_per_bet_limit_user),
                getString(R.string.self_limit_per_bet_limit_user_none)
            )
        } else {
            binding.tvPerBetLimit.text = String.format(
                getString(R.string.self_limit_per_bet_limit_user),
                TextUtil.formatMoney(viewModel.userInfo.value?.perBetLimit?.toDouble() ?: 0.0)
            ) + " " + sConfigData?.systemCurrency
        }

        val perBetMinAmount = if (sConfigData?.perBetMinAmount.isNullOrEmpty()) 0.0 else sConfigData?.perBetMinAmount?.toDouble() ?: 0.0
        val perBetMaxAmount = if (sConfigData?.perBetMaxAmount.isNullOrEmpty()) 0.0 else sConfigData?.perBetMaxAmount?.toDouble() ?: 0.0
        binding.tvLimit.text = String.format(
            getString(R.string.self_limit_per_bet_limit_user_limit),
            TextUtil.formatMoney(perBetMinAmount),
            TextUtil.formatMoney(perBetMaxAmount),
            sConfigData?.systemCurrency ?: " "
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

            binding.btnConfirm.isEnabled = !showError
        }
    }

    private fun submit() {
        CustomAlertDialog(requireContext()).apply {
            setTitle(this@SelfLimitBetFragment.getString(R.string.self_limit_fix_confirm))
            setMessage(this@SelfLimitBetFragment.getString(R.string.self_limit_fix_confirm_content))
            setPositiveButtonText(this@SelfLimitBetFragment.getString(R.string.btn_confirm))
            setNegativeButtonText(this@SelfLimitBetFragment.getString(R.string.btn_cancel))
            setPositiveClickListener {
                viewModel.setPerBetLimit(binding.etMount.text.toString().toInt())
                dismiss()
            }
            setNegativeClickListener {
                dismiss()
            }
            setCanceledOnTouchOutside(false)
            setCancelable(false) //不能用系統 BACK 按鈕關閉 dialog
        }.show(childFragmentManager, null)
    }

    private fun initDataLive() {
        viewModel.perBetLimitResult.observe(this.viewLifecycleOwner, {
            if (it.success) {
                val dialog = CustomAlertDialog(requireActivity()).apply {
                    setTitle(this@SelfLimitBetFragment.getString(R.string.self_limit_fix_confirm))
                    setMessage(this@SelfLimitBetFragment.getString(R.string.self_limit_fix_confirm_done))
                    setNegativeButtonText(null)
                    setPositiveButtonText(this@SelfLimitBetFragment.getString(R.string.btn_confirm))
                    setCancelable(false)
                    setPositiveClickListener {
                        updateBetLimit(binding.etMount.text.toString())
                        resetView()
                        viewModel.getUserInfo()
                        dismiss()
                    }
                }
                dialog.show(childFragmentManager, null)
            }

        })

        viewModel.userInfoResult.observe(viewLifecycleOwner, {
            binding.tvPerBetLimit.text =
                String.format(getString(R.string.self_limit_per_bet_limit_user), TextUtil.formatMoney(viewModel.userInfo.value?.perBetLimit!!) + sConfigData?.systemCurrency)
        })

    }

    private fun updateBetLimit(text: String) {
        binding.tvPerBetLimit.text = String.format(
            getString(R.string.self_limit_per_bet_limit_user),
            TextUtil.formatMoney(text.toDouble())
        ) + " " + sConfigData?.systemCurrency
    }

}