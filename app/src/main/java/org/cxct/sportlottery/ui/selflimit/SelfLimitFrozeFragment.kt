package org.cxct.sportlottery.ui.selflimit

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentSelfLimitFrozeBinding
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.game.publicity.GamePublicityActivity
import org.cxct.sportlottery.ui.login.afterTextChanged
import org.cxct.sportlottery.ui.main.MainActivity

@SuppressLint("SetTextI18n")
class SelfLimitFrozeFragment : BaseFragment<SelfLimitViewModel>(SelfLimitViewModel::class),
    View.OnClickListener {

    private lateinit var binding: FragmentSelfLimitFrozeBinding

    override fun onClick(v: View?) {
        when (v) {
            binding.llImportant -> {
                val dialog = SelfLimitFrozeImportantDialog(false)
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
        binding = FragmentSelfLimitFrozeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initObserve()
        initEditText()
        initDataLive()
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
                    it.isBlank() -> viewModel.setFrozeEditTextError(true)
                    it.toLong() in 1..999 -> viewModel.setFrozeEditTextError(false)
                    else -> viewModel.setFrozeEditTextError(true)
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
        binding.tvUsTime.text = "‧" + getString(R.string.text_us_east_time)
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
    }

    private fun submit() {
        CustomAlertDialog(requireContext()).apply {
            setTitle(this@SelfLimitFrozeFragment.getString(R.string.self_limit_confirm))
            setMessage(this@SelfLimitFrozeFragment.getString(R.string.self_limit_confirm_content))
            setPositiveButtonText(this@SelfLimitFrozeFragment.getString(R.string.btn_confirm))
            setNegativeButtonText(this@SelfLimitFrozeFragment.getString(R.string.btn_cancel))
            setPositiveClickListener {
                viewModel.setFroze(binding.etFrozeDay.text.toString().toInt())
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
        viewModel.frozeResult.observe(viewLifecycleOwner, {

            if (it.success) {
                val dialog = CustomAlertDialog(requireActivity()).apply {
                    setTitle(this@SelfLimitFrozeFragment.getString(R.string.self_limit_confirm))
                    setMessage(this@SelfLimitFrozeFragment.getString(R.string.self_limit_confirm_done))
                    setNegativeButtonText(null)
                    setCancelable(false)
                    setPositiveButtonText(this@SelfLimitFrozeFragment.getString(R.string.btn_confirm))
                    setPositiveClickListener {
                        dismiss()
                        viewModel.doLogoutCleanUser {
                            run {
                                if (sConfigData?.thirdOpen == FLAG_OPEN)
                                    MainActivity.reStart(MultiLanguagesApplication.appContext)
                                else
                                    GamePublicityActivity.reStart(MultiLanguagesApplication.appContext)
                            }
                        }
                    }
                }
                dialog.show(childFragmentManager, null)
            }

        })
    }

}