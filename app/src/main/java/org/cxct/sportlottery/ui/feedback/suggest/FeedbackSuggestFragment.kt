package org.cxct.sportlottery.ui.feedback.suggest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.fragment_feedback_suggest.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.feedback.FeedbackViewModel
import org.cxct.sportlottery.util.setTitleLetterSpacing

/**
 * @app_destination 建議反饋-問題與建議
 */
class FeedbackSuggestFragment : BaseFragment<FeedbackViewModel>(FeedbackViewModel::class) {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.showToolbar(true)
        viewModel.setToolbarName(getString(R.string.feedback))
        return inflater.inflate(R.layout.fragment_feedback_suggest, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initButton()
    }

    private fun initView() {
        tv_description.text = String.format(getString(R.string.feedback_description_money), sConfigData?.systemCurrency)
    }

    private fun initButton() {
        btn_submit.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_feedbackSuggestFragment_to_feedbackSubmitFragment)
        }
        btn_submit.setTitleLetterSpacing()
    }

}