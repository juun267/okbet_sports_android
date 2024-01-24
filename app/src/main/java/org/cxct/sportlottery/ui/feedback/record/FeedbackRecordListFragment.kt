package org.cxct.sportlottery.ui.feedback.record

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentFeedbackRecordListBinding
import org.cxct.sportlottery.ui.base.BindingFragment
import org.cxct.sportlottery.ui.common.adapter.StatusSheetData
import org.cxct.sportlottery.ui.feedback.FeedbackViewModel
import org.cxct.sportlottery.view.DividerItemDecorator

/**
 * @app_destination 意见反馈-反馈记录
 */
class FeedbackRecordListFragment : BindingFragment<FeedbackViewModel,FragmentFeedbackRecordListBinding>() {

    private val statusList by lazy {
        listOf(StatusSheetData(viewModel.allStatusTag, context?.getString(R.string.all_status)), StatusSheetData("0", context?.getString(R.string.feedback_not_reply_yet)), StatusSheetData("1", context?.getString(R.string.feedback_replied)))
    }
    private val recyclerViewOnScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {
        private fun scrollToTopControl(firstVisibleItemPosition: Int) {
            binding.ivScrollToTop.apply {
                when {
                    firstVisibleItemPosition > 0 && alpha == 0f -> {
                        visibility = View.GONE
                        animate().alpha(1f).setDuration(300).setListener(null)
                    }
                    firstVisibleItemPosition <= 0 && alpha == 1f -> {
                        animate().alpha(0f).setDuration(300).setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                visibility = View.GONE
                            }
                        })
                    }
                }
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            recyclerView.layoutManager?.let {
                val firstVisibleItemPosition: Int = (it as LinearLayoutManager).findFirstVisibleItemPosition()
                viewModel.getFbQueryList(isReload = false, currentTotalCount = adapter?.itemCount ?: 0)
                scrollToTopControl(firstVisibleItemPosition)
            }
        }
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
    override fun onInitView(view: View) {
        viewModel.showToolbar(true)
        viewModel.getFbQueryList(isReload = true, currentTotalCount = 0)//首次進來跟點選查詢都重新撈資料
        viewModel.setToolbarName(getString(R.string.feedback))
        initView()
        initButton()
        initRecyclerView()
        initObserve()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    private fun initView() {
        binding.statusSelector.setItemData(statusList as MutableList<StatusSheetData>)
    }

    private fun initButton()=binding.run {
        ivScrollToTop.setOnClickListener {
            rvPayType.smoothScrollToPosition(0)
        }

        dateRangeSelector.setOnClickSearchListener {
            viewModel.getFbQueryList(
                dateRangeSelector.startTime.toString(),
                dateRangeSelector.endTime.toString(), statusSelector.selectedTag,
                true, 0)//首次進來跟點選查詢都重新撈資料
        }
    }

    private fun initRecyclerView()=binding.rvPayType.run {
        layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        adapter = adapter
        addItemDecoration(DividerItemDecorator(ContextCompat.getDrawable(context, R.drawable.recycleview_decoration)))
        addOnScrollListener(recyclerViewOnScrollListener)
    }

    private fun initObserve() {
        viewModel.feedbackList.observe(viewLifecycleOwner) {
            val listData = it ?: return@observe
            adapter?.data = listData
            if ( !binding.rvPayType.canScrollVertically(1)&&!listData.isNullOrEmpty()){
                binding.tvNoData.visibility = View.VISIBLE
            }else{
                binding.tvNoData.visibility = View.INVISIBLE
            }

            if (listData.size == 0) {
                binding.tvNoData.visibility = View.VISIBLE
                binding.rvPayType.visibility = View.GONE
            } else {
                binding.tvNoData.visibility = View.GONE
                binding.rvPayType.visibility = View.VISIBLE
            }
        }

        viewModel.isFinalPage.observe(viewLifecycleOwner) {
            adapter?.isFinalPage = true
        }
    }




}