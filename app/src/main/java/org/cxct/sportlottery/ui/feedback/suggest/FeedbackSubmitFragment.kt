package org.cxct.sportlottery.ui.feedback.suggest

import android.view.View
import androidx.core.content.ContextCompat
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentFeedbackSubmitBinding
import org.cxct.sportlottery.ui.base.BindingFragment
import org.cxct.sportlottery.ui.feedback.FeedbackViewModel
import org.cxct.sportlottery.util.countTextAmount
import org.cxct.sportlottery.util.setTitleLetterSpacing
/**
 * @app_destination 意见反馈-填写建议
 */
class FeedbackSubmitFragment : BindingFragment<FeedbackViewModel,FragmentFeedbackSubmitBinding>() {

    override fun onInitView(view: View) {
        viewModel.showToolbar(true)
        viewModel.setToolbarName(getString(R.string.feedback))
        initView()
        initButton()
    }

    private fun initView()=binding.run{
        tvInputCount.text = String.format("%d / 500", 0)
        etContent.countTextAmount {
            tvInputCount.text = String.format("%d / 500", it)
            llError.visibility = if (it > 0) View.GONE else View.VISIBLE
            val textColor = if (it > 0) R.color.color_616161_b4b4b4 else R.color.color_F75452_E23434
            tvInputCount.setTextColor(ContextCompat.getColor(tvInputCount.context, textColor))
        }
    }

    private fun initButton()=binding.run {
        btnSubmit.setOnClickListener {
            if (etContent.text.trim().isNotEmpty()) {
                viewModel.fbSave(etContent.text.toString())
            } else {
                llError.visibility = View.VISIBLE
                tvInputCount.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_F75452_E23434))
            }
        }
        btnSubmit.setTitleLetterSpacing()
    }

}