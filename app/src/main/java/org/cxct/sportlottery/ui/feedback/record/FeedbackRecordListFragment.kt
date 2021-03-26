package org.cxct.sportlottery.ui.feedback.record

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_feedback_record_list.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.common.DividerItemDecorator
import org.cxct.sportlottery.ui.feedback.FeedbackViewModel
import org.cxct.sportlottery.ui.profileCenter.otherBetRecord.SheetAdapter
import java.util.*

class FeedbackRecordListFragment : BaseFragment<FeedbackViewModel>(FeedbackViewModel::class) {

    private val sheetAdapter by lazy { SheetAdapter(viewModel.allStatusTag, SheetAdapter.ItemCheckedListener { isChecked, data ->
        if (isChecked) {
            status_selector.selectedText = data.showName
            status_selector.selectedTag = data.firmType
            status_selector.dismiss()
        }
    })
    }

    val adapter by lazy {
        context?.let {
            FeedbackListAdapter(it, FeedbackListAdapter.ItemClickListener { data ->
                view?.findNavController()?.navigate(R.id.action_feedbackRecordListFragment_to_feedbackDetailFragment)
                viewModel.dataID = data.id?.toLong()
                viewModel.feedbackCode = data.feedbackCode
            })
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.showToolbar(true)
        viewModel.getFbQueryList(isReload = true, currentTotalCount = 0)//首次進來跟點選查詢都重新撈資料
        return inflater.inflate(R.layout.fragment_feedback_record_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initButton()
        initRecyclerView()
        initObserve()
    }

    private fun initView() {
        sheetAdapter.dataList = viewModel.statusList
        status_selector.setAdapter(sheetAdapter)
    }

    private fun initButton() {
        date_range_selector.setOnClickSearchListener {
            viewModel.getFbQueryList(date_range_selector.startTime.toString(), date_range_selector.endTime.toString(), status_selector.selectedTag, true, 0)//首次進來跟點選查詢都重新撈資料
        }
    }

    private fun initRecyclerView() {
        rv_pay_type.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        rv_pay_type.adapter = adapter
        rv_pay_type.addItemDecoration(DividerItemDecorator(ContextCompat.getDrawable(rv_pay_type.context, R.drawable.divider_gray)))


        rv_pay_type.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!recyclerView.canScrollVertically(1)) {
                    viewModel.getFbQueryList(isReload = false, currentTotalCount = adapter?.itemCount ?: 0)
                }
            }
        })
    }

    private fun initObserve() {
        viewModel.feedbackList.observe(this.viewLifecycleOwner, Observer {
            val listData = it ?: return@Observer
            adapter?.data = listData

            if (listData.size == 0) {
                view_no_record.visibility = View.VISIBLE
                rv_pay_type.visibility = View.GONE
            } else {
                view_no_record.visibility = View.GONE
                rv_pay_type.visibility = View.VISIBLE
            }
        })
        viewModel.isFinalPage.observe(this.viewLifecycleOwner, Observer {
            adapter?.isFinalPage = true
        })
    }



}