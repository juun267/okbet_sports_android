package org.cxct.sportlottery.ui.profileCenter.otherBetRecord.detail

import android.view.View
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentOtherBetRecordDetailBinding
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.profileCenter.otherBetRecord.OtherBetRecordViewModel
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.setMoneyColor
import org.cxct.sportlottery.util.setProfitFormat

/**
 * @app_destination 其他投注详情
 */
class OtherBetRecordDetailFragment : BaseFragment<OtherBetRecordViewModel,FragmentOtherBetRecordDetailBinding>() {


    private val args: OtherBetRecordDetailFragmentArgs by navArgs()

    private val recyclerViewOnScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (!recyclerView.canScrollVertically(-1)) {
                binding.ivScrollToTop.visibility = View.GONE
            } else {
                binding.ivScrollToTop.visibility = View.VISIBLE
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

    override fun onInitView(view: View) {
        initRv()
        initOnclick()
        initObserver()
        viewModel.querySecondOrders(args.platCode, args.todayDate)
    }

    private fun initRv() {
        binding.rvRecord.apply {
            adapter = rvAdapter
            addOnScrollListener(recyclerViewOnScrollListener)
        }
    }

    private fun initOnclick() {
        binding.ivScrollToTop.setOnClickListener {
            binding.rvRecord.smoothScrollToPosition(0)
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

                binding.layoutTotal.apply {
                    tvTotalNumber.text = (totalCount ?: 0).toString()
                        .plus(
                            if (LanguageManager.getSelectLanguage(context) == LanguageManager.Language.ZH)
                                " ${getString(R.string.bet_count)}"
                            else ""
                        )
                    tvTotalBetProfit.setProfitFormat(totalWin, isTotal = true)
                    tvTotalBetProfit.setMoneyColor(totalWin ?: 0.0)
                }

            }
        }

        viewModel.lastPage.observe(viewLifecycleOwner) {
            binding.tvNoData.text = if (it) getString(R.string.no_data) else ""
        }
    }

}
