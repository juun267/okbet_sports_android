package org.cxct.sportlottery.ui.profileCenter.money_transfer.record


import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentMoneyTransferRecordBinding
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.common.adapter.StatusSheetData
import org.cxct.sportlottery.ui.profileCenter.money_transfer.MoneyTransferViewModel

/**
 * @app_destination 转换记录
 */
class MoneyTransferRecordFragment : BaseFragment<MoneyTransferViewModel,FragmentMoneyTransferRecordBinding>() {

    private val recyclerViewOnScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {

        private fun scrollToTopControl(firstVisibleItemPosition: Int) {

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

    private val rvAdapter by lazy {
        MoneyTransferRecordAdapter(ItemClickListener {
            MoneyRecordDetailDialog.newInstance(it).show(parentFragmentManager)
        })
    }

    override fun onInitView(view: View) {
        initView()
        initOnclick()
        initObserver()
        viewModel.getThirdGames()
        viewModel.queryTransfers()
        viewModel.setToolbarName(getString(R.string.account_transfer))
    }

    private fun initView()=binding.run {
        rvRecord.adapter = rvAdapter
        rvRecord.addOnScrollListener(recyclerViewOnScrollListener)
        selectorTransferStatus.setItemData(viewModel.statusList as MutableList<StatusSheetData>)
        selectorTransferStatus.setSelectCode(viewModel.statusList.first().code)
    }

    private fun initOnclick()=binding.run {
        dateRangeSelector.setOnClickSearchListener  {
            viewModel.queryTransfers(startTime = dateRangeSelector.startTime.toString(),
                endTime = dateRangeSelector.endTime.toString(),
                firmTypeIn = selectorInPlat.selectedTag,
                firmTypeOut = selectorOutPlat.selectedTag,
                status = selectorTransferStatus.selectedTag)
        }

        selectorOutPlat.setOnItemSelectedListener {
//            viewModel.filterRecordList(MoneyTransferViewModel.PLAT.IN_PLAT, it.showName)
            selectorOutPlat.setSelectCode(it.code)
            if (selectorInPlat?.selectedCode == it.code) {
                selectorInPlat.dataList.first { item -> it.code != item.code }.let {
                    selectorInPlat.setSelectCode(it.code)
                }
            }
        }

        selectorInPlat.setOnItemSelectedListener {
//            viewModel.filterRecordList(MoneyTransferViewModel.PLAT.OUT_PLAT, it.showName)
            selectorInPlat.setSelectCode(it.code)
            if (selectorOutPlat?.selectedCode == it.code) {
                selectorOutPlat.dataList.first { item -> it.code != item.code }.let {
                    selectorOutPlat.setSelectCode(it.code)
                }
            }
        }
    }

    private fun initObserver() {
        viewModel.queryTransfersResult.observe(viewLifecycleOwner) {
            rvAdapter.addFooterAndSubmitList(viewModel.recordDataList, viewModel.isLastPage)
            if (!binding.rvRecord.canScrollVertically(1) && !rvAdapter.currentList.isNullOrEmpty()) {
                binding.tvNoDataTips.visibility = View.VISIBLE
            } else {
                binding.tvNoDataTips.visibility = View.GONE
            }
        }

        viewModel.recordInPlatSheetList.observe(viewLifecycleOwner) {
            binding.selectorInPlat.setItemData(it as MutableList<StatusSheetData>)
            binding.selectorInPlat.setSelectCode(it.first().code)
        }

        viewModel.recordOutPlatSheetList.observe(viewLifecycleOwner) {
            binding.selectorOutPlat.setItemData(it as MutableList<StatusSheetData>)
            binding.selectorOutPlat.setSelectCode(it.first().code)
        }
    }

}
