package org.cxct.sportlottery.ui.feedback.record

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_feedback_detail.*
import kotlinx.android.synthetic.main.fragment_feedback_detail.et_content
import kotlinx.android.synthetic.main.view_submit_with_text_count.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.feedback.FeedbackViewModel
import org.cxct.sportlottery.util.countTextAmount


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
        initView()
        initRecyclerView()
        initDataLive()
        initButton()
    }

    private fun initView() {
        tv_input_count.text = String.format("%d / 500", 0)
        et_content.countTextAmount {
            tv_input_count.text = String.format("%d / 500", it)
        }
    }

    private fun initButton() {
        btn_submit.setOnClickListener {
            if (!et_content.text.isNullOrEmpty()){
                viewModel.fbReply(et_content.text.toString())
            } else {
                ll_error.visibility = View.VISIBLE
                tv_input_count.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorRedDark))
            }
        }
    }

    private fun initRecyclerView() {
        rv_content.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        rv_content.adapter = adapter
    }

    private fun initDataLive() {
        viewModel.feedbackDetail.observe(viewLifecycleOwner,  {
            et_content.text.clear()
            adapter?.data = it ?: mutableListOf()
            rv_content.scrollToPosition((adapter?.itemCount ?: 0)-1)
        })
    }

}