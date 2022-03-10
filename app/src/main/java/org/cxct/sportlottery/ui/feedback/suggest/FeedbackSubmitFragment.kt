package org.cxct.sportlottery.ui.feedback.suggest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.fragment_feedback_submit.*
import kotlinx.android.synthetic.main.view_submit_with_text_count.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.feedback.FeedbackViewModel
import org.cxct.sportlottery.util.countTextAmount

class FeedbackSubmitFragment : BaseFragment<FeedbackViewModel>(FeedbackViewModel::class) {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.showToolbar(true)
        viewModel.setToolbarName(getString(R.string.feedback))
        return inflater.inflate(R.layout.fragment_feedback_submit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initButton()
        initDataLive()
    }

    private fun initView() {
        tv_input_count.text = String.format("%d / 500", 0)
        et_content.countTextAmount {
            tv_input_count.text = String.format("%d / 500", it)

            ll_error.visibility = if (it > 0) View.GONE else View.VISIBLE
            val textColor = if (it > 0) R.color.colorSilverDirk else R.color.colorRedDark
            tv_input_count.setTextColor(ContextCompat.getColor(tv_input_count.context, textColor))
        }
    }

    private fun initButton() {
        btn_submit.setOnClickListener {
            if (et_content.text.trim().isNotEmpty()) {
                viewModel.fbSave(et_content.text.toString())
            } else {
                ll_error.visibility = View.VISIBLE
                tv_input_count.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorRedDark))
            }
        }
    }

    private fun initDataLive() {
        //2/5問了 說直接跳回第一頁 沒給彈窗UI也沒說要跳提示
        viewModel.feedBackBaseResult.observe(viewLifecycleOwner, {
            it?.getContentIfNotHandled()?.let { result ->
                val msgContent = if (result.success) getString(R.string.feedback_submit_succeed) else result.msg
                val dialog = CustomAlertDialog(requireActivity()).apply {
                    setTitle(getString(R.string.prompt))
                    setMessage(msgContent)
                    setNegativeButtonText(null)
                }
                dialog.show(childFragmentManager, null)
                view?.findNavController()?.navigate(R.id.action_feedbackSubmitFragment_to_feedbackSuggestFragment)
            }
        })
    }

}