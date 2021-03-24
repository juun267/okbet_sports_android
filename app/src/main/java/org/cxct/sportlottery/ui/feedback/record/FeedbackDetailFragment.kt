package org.cxct.sportlottery.ui.feedback.record

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_feedback_detail.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.feedback.FeedbackViewModel


class FeedbackDetailFragment : BaseFragment<FeedbackViewModel>(FeedbackViewModel::class) {

    val adapter by lazy {
        viewModel.userID?.let { FeedbackListDetailAdapter(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.showToolbar(false)
        viewModel.fbQueryDetail()
        return inflater.inflate(R.layout.fragment_feedback_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initDataLive()
        initButton()
    }

    private fun initButton() {
        btn_submit.setOnClickListener {
            if (!et_content.text.isNullOrEmpty()){
                viewModel.fbReply(et_content.text.toString())
                view?.findNavController()?.navigate(R.id.action_feedbackDetailFragment_to_feedbackRecordListFragment)
            }
        }
    }

    private fun initRecyclerView() {
        rv_content.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        rv_content.adapter = adapter
    }

    private fun initDataLive() {
        viewModel.feedbackDetail.observe(this.viewLifecycleOwner, Observer {
            val listData = it ?: return@Observer
            adapter?.data = listData
        })
    }

}