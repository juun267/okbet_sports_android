package org.cxct.sportlottery.ui.profileCenter.otherBetRecord

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_other_bet_record.*
import kotlinx.android.synthetic.main.view_status_spinner.view.*
import kotlinx.android.synthetic.main.view_total_record.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.setMoneyColor
import org.cxct.sportlottery.util.setProfitFormat
import java.util.*

/**
 * @app_destination 其他投注
 */
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
                findNavController().navigate(
                    OtherBetRecordFragmentDirections.actionOtherBetRecordFragmentToOtherBetRecordDetailFragment(
                        status_selector.selectedTag.toString(),
                        TimeUtil.timeFormat(data.statDate, TimeUtil.YMD_FORMAT)
                    )
                )
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
        initOnclick()
        initObserver()
    }

    private fun initView() {
        status_selector.tv_name.setTextColor(
            ContextCompat.getColor(
                status_selector.tv_name.context,
                R.color.color_6C7BA8_6C7BA8
            )
        )
        rv_record.apply {
            adapter = rvAdapter
            addOnScrollListener(recyclerViewOnScrollListener)
        }
    }

    private fun initOnclick() {
        date_search_bar.timeZone = TimeZone.getTimeZone(TimeUtil.TIMEZONE_DEFAULT)
        date_search_bar.setOnClickSearchListener {
            viewModel.queryFirstOrders(1, date_search_bar.startTime.toString(), date_search_bar.endTime.toString(), status_selector.selectedTag.toString())
        }

        iv_scroll_to_top.setOnClickListener {
            rv_record.smoothScrollToPosition(0)
        }

    }

    private fun initObserver() {
//        viewModel.loading.observe(viewLifecycleOwner) {
//            if (it)
//                loading()
//            else
//                hideLoading()
//        }

        viewModel.thirdGamesResult.observe(viewLifecycleOwner) {
            status_selector.setItemData((it ?: listOf()) as MutableList<StatusSheetData>)
        }

        viewModel.recordResult.observe(viewLifecycleOwner) {
            it?.t?.apply {
                rvAdapter.addFooterAndSubmitList(viewModel.recordDataList, viewModel.isLastPage)

                layout_total.apply {
                    tv_total_number.text = (totalCount ?: 0).toString().plus(
                        if (LanguageManager.getSelectLanguage(context) == LanguageManager.Language.ZH)
                            " ${getString(R.string.bet_count)}"
                        else ""
                    )
                    tv_total_bet_profit.setProfitFormat(totalWin, isTotal = true)
                    tv_total_bet_profit.setMoneyColor(totalWin ?: 0.0)
                }

            }
        }

    }

}
