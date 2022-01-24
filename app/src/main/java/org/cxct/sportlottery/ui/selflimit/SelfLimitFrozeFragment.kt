package org.cxct.sportlottery.ui.selflimit

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.fragment_feedback_submit.*
import kotlinx.android.synthetic.main.view_submit_with_text_count.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentSelfLimitFrozeBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.feedback.FeedbackViewModel
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.countTextAmount

class SelfLimitFrozeFragment : BaseFragment<SelfLimitViewModel>(SelfLimitViewModel::class),
    View.OnClickListener {

    private lateinit var binding: FragmentSelfLimitFrozeBinding

    override fun onClick(v: View?) {
        when (v) {
            binding.llImportant -> {
                val dialog = CustomAlertDialog(requireContext())
                dialog.setTitle(getString(R.string.selfLimit_impotent))
                dialog.setMessage(getString(R.string.selfLimit_froze_impotent))
                dialog.setCanceledOnTouchOutside(true)
                dialog.setCancelable(true)
                dialog.setNegativeButtonText(null)
                dialog.setPositiveButtonText(null)
                dialog.setShowDivider(true)
                dialog.setGravity(Gravity.LEFT)
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
    ): View? {
        viewModel.showToolbar(true)
        viewModel.setToolbarName(getString(R.string.selfLimit))
        binding = FragmentSelfLimitFrozeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initDataLive()
    }

    private fun initView() {
        binding.llImportant.setOnClickListener(this)
        binding.btnConfirm.setOnClickListener(this)
    }

    private fun submit() {
        val dialog = CustomAlertDialog(requireContext()).apply {
            setTitle(getString(R.string.selfLimit_confirm))
            setMessage(getString(R.string.selfLimit_confirm_content))
            setPositiveButtonText(getString(R.string.btn_confirm))
            setNegativeButtonText(getString(R.string.btn_cancel))
            setPositiveClickListener(View.OnClickListener {
                viewModel.setFroze(binding.etFrozeDay.text.toString().toInt())
                dismiss()
            })
            setNegativeClickListener({
                dismiss()
            })
            setCanceledOnTouchOutside(false)
            setCancelable(false) //不能用系統 BACK 按鈕關閉 dialog
            show()
        }
    }

    private fun initDataLive() {
        viewModel.frozeResult.observe(viewLifecycleOwner, {
            if(it.success){
                val dialog = CustomAlertDialog(requireActivity()).apply {
                    setTitle(getString(R.string.selfLimit_confirm))
                    setMessage(getString(R.string.selfLimit_confirm_done))
                    setNegativeButtonText(null)
                    setPositiveButtonText(getString(R.string.btn_confirm))
                    setPositiveClickListener(View.OnClickListener {
                        dismiss()
                        viewModel.doLogoutCleanUser {
                            run {
                                if (sConfigData?.thirdOpen == FLAG_OPEN)
                                    MainActivity.reStart(requireContext())
                                else
                                    GameActivity.reStart(requireContext())
                            }
                        }
                    })
                }
                dialog.show()
            }

        })
    }

}