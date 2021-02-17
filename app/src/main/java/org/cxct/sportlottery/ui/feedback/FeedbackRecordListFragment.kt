package org.cxct.sportlottery.ui.feedback

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_feedback_record_list.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFragment

class FeedbackRecordListFragment : BaseFragment<FeedbackViewModel>(FeedbackViewModel::class) {

    private val navController by lazy {
        view?.findNavController()
    }

    val adapter by lazy {
        context?.let {
            FeedbackListAdapter(it, FeedbackListAdapter.ItemClickListener { data ->
                navController?.navigate(R.id.action_feedbackRecordListFragment_to_feedbackDetailFragment)
                viewModel.dataID = data.id?.toLong()
                viewModel.feedbackCode =data.feedbackCode
            })
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_feedback_record_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initView()
        initButton()
        initDataLive()
        initData()
    }

    private fun initData() {
        getList()
    }

    private fun initRecyclerView() {
        rv_pay_type.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        rv_pay_type.adapter = adapter
    }

    private fun initDataLive() {
        viewModel.feedbackList.observe(this.viewLifecycleOwner, Observer {
            val listData = it ?: return@Observer
            adapter?.data = listData
        })
    }

    private fun initView() {
        btn_sugession.isSelected = false
        btn_record.isSelected = true
    }

    private fun initButton() {
        btn_sugession.setOnClickListener {
            navController?.navigate(R.id.action_feedbackRecordListFragment_to_feedbackSuggestFragment)
        }
        btn_submit.setOnClickListener {
            getList()
        }
    }

    private fun getList() {
        viewModel.getFbQueryList()
    }
}