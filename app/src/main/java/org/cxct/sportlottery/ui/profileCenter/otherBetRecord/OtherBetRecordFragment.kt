package org.cxct.sportlottery.ui.profileCenter.otherBetRecord

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentOtherBetRecordBinding
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.common.adapter.StatusSheetData
import org.cxct.sportlottery.util.*
import java.util.*

/**
 * @app_destination 其他投注
 */
class OtherBetRecordFragment : BaseFragment<OtherBetRecordViewModel,FragmentOtherBetRecordBinding>() {

    private val recyclerViewOnScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {

        private fun scrollToTopControl(firstVisibleItemPosition: Int) {
            binding.ivScrollToTop.apply {
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
                        binding.statusSelector.selectedTag.toString(),
                        TimeUtil.timeFormat(data.statDate, TimeUtil.YMD_FORMAT)
                    )
                )
            }
        })
    }

    override fun onInitView(view: View) {
        initView()
        initOnclick()
        viewModel.getThirdGames()
        viewModel.queryFirstOrders()
    }

    override fun onBindViewStatus(view: View) {
        super.onBindViewStatus(view)
        initObserver()
    }

    private fun initView()=binding.run {
        statusSelector.binding.tvName.setTextColor(
            ContextCompat.getColor(
                statusSelector.binding.tvName.context,
                R.color.color_6C7BA8_6C7BA8
            )
        )
        rvRecord.apply {
            adapter = rvAdapter
            addOnScrollListener(recyclerViewOnScrollListener)
        }
    }

    private fun initOnclick()=binding.run {
        dateSearchBar.timeZone = TimeZone.getTimeZone(TimeUtil.TIMEZONE_DEFAULT)
        dateSearchBar.setOnClickSearchListener {
            viewModel.queryFirstOrders(1, dateSearchBar.startTime.toString(), dateSearchBar.endTime.toString(), statusSelector.selectedTag.toString())
        }

        ivScrollToTop.setOnClickListener {
            rvRecord.smoothScrollToPosition(0)
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
            binding.statusSelector.setItemData((it ?: listOf()) as MutableList<StatusSheetData>)
        }

        viewModel.recordResult.observe(viewLifecycleOwner) {
            it?.t?.apply {
                rvAdapter.addFooterAndSubmitList(viewModel.recordDataList, viewModel.isLastPage)

                binding.layoutTotal.apply {
                    tvTotalNumber.text = (totalCount ?: 0).toString().plus(
                        if (LanguageManager.getSelectLanguage(context) == LanguageManager.Language.ZH)
                            " ${getString(R.string.bet_count)}"
                        else ""
                    )
                    tvTotalBetProfit.setProfitFormat(totalWin, isTotal = true)
                    tvTotalBetProfit.setMoneyColor(totalWin ?: 0.0)
                }

            }
        }

    }

}
