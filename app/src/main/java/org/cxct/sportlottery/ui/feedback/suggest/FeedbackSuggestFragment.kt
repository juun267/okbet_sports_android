package org.cxct.sportlottery.ui.feedback.suggest

import android.view.View
import androidx.navigation.findNavController
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentFeedbackSuggestBinding
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BindingFragment
import org.cxct.sportlottery.ui.feedback.FeedbackViewModel
import org.cxct.sportlottery.util.setTitleLetterSpacing

/**
 * @app_destination 建議反饋-問題與建議
 */
class FeedbackSuggestFragment : BindingFragment<FeedbackViewModel,FragmentFeedbackSuggestBinding>() {

    override fun onInitView(view: View) {
        viewModel.showToolbar(true)
        viewModel.setToolbarName(getString(R.string.feedback))
        initView()
        initButton()
    }


    private fun initView() {
        binding.tvDescription.text = String.format(getString(R.string.feedback_description_money), sConfigData?.systemCurrencySign)
    }

    private fun initButton()=binding.run {
        btnSubmit.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_feedbackSuggestFragment_to_feedbackSubmitFragment)
        }
        btnSubmit.setTitleLetterSpacing()
    }



}