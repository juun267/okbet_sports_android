package org.cxct.sportlottery.ui.profileCenter.otherBetRecord.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_other_bet_record_detail.*
import kotlinx.android.synthetic.main.view_total_record.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.profileCenter.otherBetRecord.OtherBetRecordViewModel
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.setMoneyColor
import org.cxct.sportlottery.util.setProfitFormat

/**
 * @app_destination 其他投注详情
 */
class OtherBetRecordDetailFragment : BaseSocketFragment<OtherBetRecordViewModel>(OtherBetRecordViewModel::class) {


    private val args: OtherBetRecordDetailFragmentArgs by navArgs()

    private val recyclerViewOnScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (!recyclerView.canScrollVertically(-1)) {
                iv_scroll_to_top.visibility = View.GONE
            } else {
                iv_scroll_to_top.visibility = View.VISIBLE
            }
            recyclerView.layoutManager?.let {
                val visibleItemCount: Int = it.childCount
                val totalItemCount: Int = it.itemCount
                val firstVisibleItemPosition: Int = (it as LinearLayoutManager).findFirstVisibleItemPosition()
                viewModel.getRecordDetailNextPage(visibleItemCount, firstVisibleItemPosition, totalItemCount)

            }
        }
    }

    private val rvAdapter by lazy {
        OtherBetRecordDetailAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        viewModel.querySecondOrders(args.platCode, args.todayDate)
        return inflater.inflate(R.layout.fragment_other_bet_record_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRv()
        initOnclick()
        initObserver()
    }

    private fun initRv() {
        rv_record.apply {
            adapter = rvAdapter
            addOnScrollListener(recyclerViewOnScrollListener)
        }
    }

    private fun initOnclick() {
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

        viewModel.recordDetailResult.observe(viewLifecycleOwner) {
            it?.t?.apply {
                rvAdapter.addFooterAndSubmitList(viewModel.recordDetailDataList, viewModel.isLastPage)

                layout_total.apply {
                    tv_total_number.text = (totalCount ?: 0).toString()
                        .plus(
                            if (LanguageManager.getSelectLanguage(context) == LanguageManager.Language.ZH)
                                " ${getString(R.string.bet_count)}"
                            else ""
                        )
                    tv_total_bet_profit.setProfitFormat(totalWin, isTotal = true)
                    tv_total_bet_profit.setMoneyColor(totalWin ?: 0.0)
                }

            }
        }

        viewModel.lastPage.observe(viewLifecycleOwner) {
            tv_no_data.text = if (it) getString(R.string.no_data) else ""
        }
    }

}
