package org.cxct.sportlottery.ui.profileCenter.sportRecord

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_sport_bet_record.*
import kotlinx.android.synthetic.main.view_total_record.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.common.DividerItemDecorator
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.ui.profileCenter.sportRecord.dialog.BetRecordDetailDialog
import org.cxct.sportlottery.util.setMoneyColor
import org.cxct.sportlottery.util.setMoneyFormat
import org.cxct.sportlottery.util.setProfitFormat


class SportBetRecordFragment : BaseFragment<BetRecordViewModel>(BetRecordViewModel::class) {

    private val betStatusList by lazy {
        listOf(StatusSheetData("01234567", context?.getString(R.string.all_order)),
               StatusSheetData("01", context?.getString(R.string.not_settled_order)),
               StatusSheetData("234567", context?.getString(R.string.settled_order)))
    }

    private val rvAdapter = BetRecordAdapter(ItemClickListener {
        it.let { data ->
            val detailDialog = BetRecordDetailDialog(data)
            detailDialog.show(parentFragmentManager, "BetRecordDetailDialog")
        }
    })

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
                viewModel.getNextPage(visibleItemCount, firstVisibleItemPosition, totalItemCount)
                scrollToTopControl(firstVisibleItemPosition)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel.searchBetRecord()
        return inflater.inflate(R.layout.fragment_sport_bet_record, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initRv()
        initOnclick()
        initObserver()
    }

    private fun initView() {
        layout_total.tv_total.text = getString(R.string.total_bet_money_colon)
        status_selector.setCloseBtnText(getString(R.string.bottom_sheet_close))
        status_selector.dataList = betStatusList
    }

    private fun initOnclick() {

        ll_champion.setOnClickListener {
            btn_champion.isChecked = !btn_champion.isChecked
        }

        date_search_bar.setOnClickSearchListener {
            //每次查詢，清除資料重載，list 才會置頂
            rvAdapter.addFooterAndSubmitList(null, true)
            viewModel.searchBetRecord(btn_champion.isChecked, date_search_bar.startTime.toString(), date_search_bar.endTime.toString(), status_selector.selectedTag)
        }

        iv_scroll_to_top.setOnClickListener {
            rv_bet_record.smoothScrollToPosition(0)
        }


    }

    private fun initObserver() {

        viewModel.loading.observe(viewLifecycleOwner, {
            if (it) loading() else hideLoading()
        })

        viewModel.betRecordResult.observe(viewLifecycleOwner, {
            if (it.success) {
                rvAdapter.addFooterAndSubmitList(viewModel.recordDataList, viewModel.isLastPage)
                layout_total.apply {
                    tv_total_number.setMoneyFormat(it.other?.totalAmount ?: 0.0).toString()
                    tv_total_bet_profit.setProfitFormat(it.other?.win, true)
                    tv_total_bet_profit.setMoneyColor(it.other?.win ?: 0.0)
                }
            } else {
                Toast.makeText(context, it.msg, Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.oddsType.observe(viewLifecycleOwner, {
            rvAdapter.oddsType = it
        })

    }

    private fun initRv() {
        rv_bet_record.apply {
            adapter = rvAdapter
            addItemDecoration(DividerItemDecorator(ContextCompat.getDrawable(context, R.drawable.divider_gray)))
            addOnScrollListener(recyclerViewOnScrollListener)
        }

    }

}

