package org.cxct.sportlottery.ui.feedback.suggest

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.fragment_feedback_submit.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.feedback.FeedbackViewModel

class FeedbackSubmitFragment : BaseFragment<FeedbackViewModel>(FeedbackViewModel::class) {

    private val navController by lazy {
        view?.findNavController()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.showToolbar(true)
        return inflater.inflate(R.layout.fragment_feedback_submit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initButton()
        initDataLive()
    }

    private fun initButton() {
        btn_submit.setOnClickListener {
            if (!et_content.text.isNullOrEmpty()){
                viewModel.fbSave(et_content.text.toString())
            }
        }
    }

    private fun initDataLive() {
        //2/5問了 說直接跳回第一頁 沒給彈窗UI也沒說要跳提示
        viewModel.feedBackBaseResult.observe(this.viewLifecycleOwner, {
            val dialog = CustomAlertDialog(requireActivity()).apply {
                setTitle(getString(R.string.prompt))
                setMessage(getString(R.string.feedback_submit_succeed))
                setNegativeButtonText(null)
            }
            dialog.show()
            navController?.navigate(R.id.action_feedbackSubmitFragment_to_feedbackSuggestFragment)
        })
    }

}