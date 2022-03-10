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
                when {
                    it.isBlank() -> viewModel.setBetEditTextError(true)
                    it.toLong() in (sConfigData?.perBetMinAmount?.toLong()
                        ?: 0)..(sConfigData?.perBetMaxAmount?.toLong()
                        ?: 0) -> viewModel.setBetEditTextError(false)
                    else -> viewModel.setBetEditTextError(true)
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.llImportant -> {
                val dialog = SelfLimitFrozeImportantDialog(requireContext(), true)
                dialog.setCanceledOnTouchOutside(true)
                dialog.setCancelable(true)
                dialog.show()
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
        binding.tvLimit.text = String.format(
            getString(R.string.self_limit_per_bet_limit_user_limit),
            TextUtil.formatMoney(sConfigData?.perBetMinAmount?.toDouble() ?: 0.0),
            TextUtil.formatMoney(sConfigData?.perBetMaxAmount?.toDouble() ?: 0.0),
            sConfigData?.systemCurrency
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
            setTitle(getString(R.string.self_limit_fix_confirm))
            setMessage(getString(R.string.self_limit_fix_confirm_content))
            setPositiveButtonText(getString(R.string.btn_confirm))
            setNegativeButtonText(getString(R.string.btn_cancel))
            setPositiveClickListener {
                viewModel.setPerBetLimit(binding.etMount.text.toString().toInt())
                dismiss()
            }
            setNegativeClickListener {
                dismiss()
            }
            setCanceledOnTouchOutside(false)
            setCancelable(false) //不能用系統 BACK 按鈕關閉 dialog
            show(childFragmentManager, null)
        }
    }

    private fun initDataLive() {
        viewModel.perBetLimitResult.observe(this.viewLifecycleOwner, {
            if (it.success) {
                val dialog = CustomAlertDialog(requireActivity()).apply {
                    setTitle(getString(R.string.self_limit_fix_confirm))
                    setMessage(getString(R.string.self_limit_fix_confirm_done))
                    setNegativeButtonText(null)
                    setPositiveButtonText(getString(R.string.btn_confirm))
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