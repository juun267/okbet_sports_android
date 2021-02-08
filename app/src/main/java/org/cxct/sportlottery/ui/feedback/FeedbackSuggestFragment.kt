package org.cxct.sportlottery.ui.feedback

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment.findNavController
import kotlinx.android.synthetic.main.fragment_feedback_suggest.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.finance.FinanceFragmentDirections

class FeedbackSuggestFragment : BaseFragment<FeedbackViewModel>(FeedbackViewModel::class) {

    private val navController by lazy {
        view?.findNavController()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_feedback_suggest, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initButton()
        initView()
    }

    private fun initView() {
        btn_sugession.isSelected = true
        btn_record.isSelected = false
    }

    private fun initButton() {
        btn_submit.setOnClickListener {
            navController?.navigate(R.id.action_feedbackSuggestFragment_to_feedbackSubmitFragment)
        }
        btn_record.setOnClickListener {
            navController?.navigate(R.id.action_feedbackSuggestFragment_to_feedbackRecordListFragment)
        }
    }

}