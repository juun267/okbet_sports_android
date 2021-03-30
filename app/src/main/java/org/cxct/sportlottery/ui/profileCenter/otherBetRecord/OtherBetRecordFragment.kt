package org.cxct.sportlottery.ui.profileCenter.otherBetRecord

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_info_center.*
import kotlinx.android.synthetic.main.fragment_feedback_record_list.*
import kotlinx.android.synthetic.main.fragment_other_bet_record.*
import kotlinx.android.synthetic.main.fragment_other_bet_record.date_search_bar
import kotlinx.android.synthetic.main.fragment_other_bet_record.iv_scroll_to_top
import kotlinx.android.synthetic.main.fragment_other_bet_record.layout_total
import kotlinx.android.synthetic.main.fragment_other_bet_record.status_selector
import kotlinx.android.synthetic.main.fragment_sport_bet_record.*
import kotlinx.android.synthetic.main.view_total_record.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.setMoneyColor
import org.cxct.sportlottery.util.setMoneyFormat
import org.cxct.sportlottery.util.setProfitFormat

class OtherBetRecordFragment : BaseSocketFragment<OtherBetRecordViewModel>(OtherBetRecordViewModel::class) {

    private val recyclerViewOnScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {

        private fun scrollToTopControl(firstVisibleItemPosition: Int) {
            iv_scroll_to_top.apply {
                when {
                    firstVisibleItemPosition > 0 && alpha == 0f -> {
                        visibility = View.VISIBLE
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
                val visibleItemCount: Int = it.childCount
                val totalItemCount: Int = it.itemCount
                val firstVisibleItemPosition: Int = (it as LinearLayoutManager).findFirstVisibleItemPosition()
                viewModel.getRecordNextPage(visibleItemCount, firstVisibleItemPosition, totalItemCount)
                scrollToTopControl(firstVisibleItemPosition)
            }
        }
    }

    private val rvAdapter by lazy {
        OtherBetRecordAdapter(ItemClickListener {
            it.let { data ->
                findNavController().navigate(OtherBetRecordFragmentDirections.actionOtherBetRecordFragmentToOtherBetRecordDetailFragment(TimeUtil.timeStampToDay(data.statDate)))
            }
        })
    }

    private val sheetAdapter by lazy { SheetAdapter(viewModel.allPlatTag, SheetAdapter.ItemCheckedListener { isChecked, data ->
        if (isChecked) {
            status_selector.selectedText = data.showName
            status_selector.selectedTag = data.firmType
            status_selector.dismiss()
        }
    })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        viewModel.getThirdGames()
        viewModel.queryFirstOrders()
        return inflater.inflate(R.layout.fragment_other_bet_record, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initRv()
        initOnclick()
        initObserver()
    }

    private fun initView() {
        status_selector.setAdapter(sheetAdapter)
    }

    private fun initRv() {
        rv_record.apply {
            adapter = rvAdapter
            addOnScrollListener(recyclerViewOnScrollListener)
        }
    }

    private fun initOnclick() {

        date_search_bar.setOnClickSearchListener {
            viewModel.queryFirstOrders(1, date_search_bar.startTime.toString(), date_search_bar.endTime.toString(), status_selector.selectedTag.toString())
        }

        iv_scroll_to_top.setOnClickListener {
            rv_record.smoothScrollToPosition(0)
        }

    }

    private fun initObserver() {
        viewModel.loading.observe(viewLifecycleOwner) {
            if (it)
                loading()
            else
                hideLoading()
        }

        viewModel.thirdGamesResult.observe(viewLifecycleOwner) {
            sheetAdapter.dataList = it ?: listOf()
        }

        viewModel.recordResult.observe(viewLifecycleOwner) {
            it?.t?.apply {
                rvAdapter.addFooterAndSubmitList(viewModel.recordDataList, viewModel.isLastPage)

                layout_total.apply {
                    tv_total_number.text = (totalCount ?: 0).toString()
                    tv_total_bet_profit.setProfitFormat(totalWin)
                    tv_total_bet_profit.setMoneyColor(totalWin ?: 0.0)
                }

            }
        }

    }

}
