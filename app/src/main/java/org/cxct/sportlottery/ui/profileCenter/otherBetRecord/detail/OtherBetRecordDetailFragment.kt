package org.cxct.sportlottery.ui.profileCenter.otherBetRecord.detail

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_other_bet_record_detail.*
import kotlinx.android.synthetic.main.fragment_other_bet_record_detail.iv_scroll_to_top
import kotlinx.android.synthetic.main.fragment_other_bet_record_detail.layout_total
import kotlinx.android.synthetic.main.fragment_other_bet_record_detail.rv_record
import kotlinx.android.synthetic.main.view_total_record.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.profileCenter.otherBetRecord.OtherBetRecordViewModel
import org.cxct.sportlottery.ui.profileCenter.sportRecord.search.result.setMoneyColor
import org.cxct.sportlottery.ui.profileCenter.sportRecord.search.result.setMoneyFormat

class OtherBetRecordDetailFragment : BaseSocketFragment<OtherBetRecordViewModel>(OtherBetRecordViewModel::class) {

    private val recyclerViewOnScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {

        private fun scrollToTopControl(firstVisibleItemPosition: Int) {
            iv_scroll_to_top.apply {
                if (firstVisibleItemPosition > 0) {
                    if (alpha == 0f) {
                        alpha = 0f
                        visibility = View.VISIBLE
                        animate().alpha(1f).setDuration(300).setListener(null)
                    }
                } else {
                    if (alpha == 1f) {
                        alpha = 1f
                        animate().alpha(0f).setDuration(300).setListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {
                                    visibility = View.GONE
                                }
                            })
                    }
                }
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
        viewModel.querySecondOrders()
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

        viewModel.recordDetailResult.observe(viewLifecycleOwner) {
            it?.t?.apply {
                viewModel.isLastPage = (rvAdapter.itemCount >= (totalCount ?: 0))
                rvAdapter.addFooterAndSubmitList(viewModel.recordDetailDataList, viewModel.isLastPage)

                layout_total.apply {
                    tv_total_number.text = (totalCount ?: 0).toString()
                    tv_total_bet_profit.setMoneyFormat(totalWin)
                    tv_total_bet_profit.setMoneyColor(totalWin ?: 0.0)
                }

            }
        }

    }

}
