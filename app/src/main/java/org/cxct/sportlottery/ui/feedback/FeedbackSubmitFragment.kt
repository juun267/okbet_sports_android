package org.cxct.sportlottery.ui.feedback

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.fragment_feedback_submit.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFragment

class FeedbackSubmitFragment : BaseFragment<FeedbackViewModel>(FeedbackViewModel::class) {

    private val navController by lazy {
        view?.findNavController()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_feedback_submit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initButton()
        initDataLive()
    }

    private fun initDataLive() {
        //2/5問了 說直接跳回第一頁 沒給彈窗UI也沒說要跳提示
        viewModel.viewStatus.observe(this.viewLifecycleOwner, Observer {
            navController?.navigate(R.id.action_feedbackSubmitFragment_to_feedbackSuggestFragment)
        })
    }
    
    private fun initView() {
        btn_sugession.isSelected = true
        btn_record.isSelected = false
    }

    private fun initButton() {
        btn_submit.setOnClickListener {
            if (!et_content.text.isNullOrEmpty()){
                viewModel.fbSave(et_content.text.toString())
                navController?.navigate(R.id.action_feedbackSubmitFragment_to_feedbackSuggestFragment)
            }
        }
        btn_record.setOnClickListener {
            navController?.navigate(R.id.action_feedbackSubmitFragment_to_feedbackRecordListFragment)
        }
    }

}