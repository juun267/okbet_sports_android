package org.cxct.sportlottery.ui.profileCenter.sportRecord

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_info_center.*
import kotlinx.android.synthetic.main.dialog_bottom_sheet_custom.view.*
import kotlinx.android.synthetic.main.fragment_sport_bet_record.*
import kotlinx.android.synthetic.main.fragment_sport_bet_record.iv_scroll_to_top
import kotlinx.android.synthetic.main.view_total_record.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.common.DividerItemDecorator
import org.cxct.sportlottery.ui.profileCenter.sportRecord.dialog.BetRecordDetailDialog
import org.cxct.sportlottery.util.setMoneyColor
import org.cxct.sportlottery.util.setMoneyFormat
import org.cxct.sportlottery.util.setProfitFormat


class SportBetRecordFragment : BaseFragment<BetRecordViewModel>(BetRecordViewModel::class) {

    private val statusAdapter = BottomSheetAdapter(BottomSheetAdapter.ItemCheckedListener {
        val cbAll = status_selector.bottomSheetView.checkbox_select_all
        cbAll.isChecked = viewModel.isAllCbChecked()
        status_selector.selectedText = viewModel.getBetStatus()
        status_selector.selectedTextColor = R.color.textColorDark
    })

    private val rvAdapter = BetRecordAdapter(ItemClickListener {
        it.let { data ->
            val detailDialog = BetRecordDetailDialog(data)
            detailDialog.show(parentFragmentManager, "BetRecordDetailDialog")
        }
    })

    private val recyclerViewOnScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {
        private fun scrollToTopControl(firstVisibleItemPosition: Int) {
            iv_scroll_to_top.apply {
                if (firstVisibleItemPosition > 0) {
                    if (alpha == 0f) {
                        alpha = 0f
                        visibility = View.VISIBLE
                        animate()
                            .alpha(1f)
                            .setDuration(300)
                            .setListener(null)
                    }
                } else {
                    if (alpha == 1f) {
                        alpha = 1f
                        animate()
                            .alpha(0f)
                            .setDuration(300)
                            .setListener(object : AnimatorListenerAdapter() {
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
        initBottomSheet()
        initOnclick()
        initObserver()
    }

    private fun initView() {
        layout_total.tv_total.text = getString(R.string.total_bet_amount)
        status_selector.setCloseBtnText(getString(R.string.cancel_select))
        status_selector.setAdapter(statusAdapter)
    }


    private fun initBottomSheet() {
        status_selector.isShowAllCheckBoxView = true
        statusAdapter.dataList = viewModel.betStatusList

        //item selected
        val cbAll = status_selector.bottomSheetView.checkbox_select_all

        //全選按鈕
        cbAll.setOnClickListener {
            if (cbAll.isChecked) {
                viewModel.addAllStatusList()
            } else {
                viewModel.clearStatusList()
            }

            status_selector.post {
                statusAdapter.notifyDataSetChanged()
            }
        }

        //取消選擇
        val tvCancel = status_selector.bottomSheetView.sheet_tv_close
        tvCancel.setOnClickListener {
            viewModel.clearStatusList()
            cbAll.isChecked = false
            statusAdapter.notifyDataSetChanged()
        }

    }


    private fun initOnclick() {

        ll_champion.setOnClickListener {
            btn_champion.isChecked = !btn_champion.isChecked
        }

        date_search_bar.setOnClickSearchListener {
            viewModel.searchBetRecord(btn_champion.isChecked, date_search_bar.startTime.toString(), date_search_bar.endTime.toString())
        }

        iv_scroll_to_top.setOnClickListener {
            rv_bet_record.smoothScrollToPosition(0)
        }


    }
    private fun initObserver() {

        viewModel.loading.observe(viewLifecycleOwner, {
            if (it) loading() else hideLoading()
        })

        viewModel.statusSearchEnable.observe(viewLifecycleOwner, {
            if (it == false) status_selector.selectedTextColor = R.color.red
        })

        viewModel.betRecordResult.observe(viewLifecycleOwner, {
            if (it.success) {
                rvAdapter.addFooterAndSubmitList(viewModel.recordDataList, viewModel.isLastPage)
                layout_total.apply {
                    tv_total_number.setMoneyFormat(it.other?.totalAmount ?: 0).toString()
                    tv_total_bet_profit.setProfitFormat(it.other?.win)
                    tv_total_bet_profit.setMoneyColor(it.other?.win ?: 0.0)
                }
            } else {
                Toast.makeText(context, it.msg, Toast.LENGTH_SHORT).show()
            }
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

