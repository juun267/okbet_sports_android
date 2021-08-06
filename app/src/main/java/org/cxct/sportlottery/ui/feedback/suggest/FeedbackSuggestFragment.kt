package org.cxct.sportlottery.ui.feedback.suggest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.fragment_feedback_suggest.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.feedback.FeedbackViewModel

class FeedbackSuggestFragment : BaseFragment<FeedbackViewModel>(FeedbackViewModel::class) {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.showToolbar(true)
        viewModel.setToolbarName(getString(R.string.feedback_toolbar_title))
        return inflater.inflate(R.layout.fragment_feedback_suggest, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initButton()
    }

    private fun initButton() {
        btn_submit.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_feedbackSuggestFragment_to_feedbackSubmitFragment)
        }
    }

}