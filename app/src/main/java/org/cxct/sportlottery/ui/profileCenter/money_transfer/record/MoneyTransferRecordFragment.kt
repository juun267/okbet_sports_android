package org.cxct.sportlottery.ui.profileCenter.money_transfer.record


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.component_date_range_new_selector.view.*
import kotlinx.android.synthetic.main.fragment_money_transfer_record.*
import kotlinx.android.synthetic.main.fragment_money_transfer_record.date_range_selector
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.clickDelay
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.common.adapter.StatusSheetData
import org.cxct.sportlottery.ui.profileCenter.money_transfer.MoneyTransferViewModel
import org.cxct.sportlottery.util.LogUtil

/**
 * @app_destination 转换记录
 */
class MoneyTransferRecordFragment : BaseSocketFragment<MoneyTransferViewModel>(MoneyTransferViewModel::class) {

    private val recyclerViewOnScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {

        private fun scrollToTopControl(firstVisibleItemPosition: Int) {
           /* iv_scroll_to_top.apply {
                when {
                    firstVisibleItemPosition > 0 && alpha == 0f -> {
                      //  visibility = View.VISIBLE
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
            }*/
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
//            if ( !recyclerView.canScrollVertically(1)){//1表示是否能向上滚动 false表示已经到底部 -1表示是否能向下滚动false表示已经到顶部
//                viewModel.queryTransfersResult.observe(this@MoneyTransferRecordFragment){
//                    if (it.rows.isNullOrEmpty()){
//                        tv_no_data_tips.visibility = View.GONE
//                    }else{
//                        tv_no_data_tips.visibility = View.VISIBLE
//                    }
//                }
//            }else{
//                tv_no_data_tips.visibility = View.GONE
//            }
        }
    }

    private val rvAdapter by lazy {
        MoneyTransferRecordAdapter(ItemClickListener {
            val detailDialog = MoneyRecordDetailDialog()
            detailDialog.arguments = Bundle().apply { putParcelable("data", it) }
            detailDialog.show(parentFragmentManager, null)
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel.getThirdGames()
        viewModel.queryTransfers()
        viewModel.setToolbarName(getString(R.string.account_transfer))

        return inflater.inflate(R.layout.fragment_money_transfer_record, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initOnclick()
        initObserver()
    }

    private fun initView() {
        rv_record.adapter = rvAdapter
        rv_record.addOnScrollListener(recyclerViewOnScrollListener)
        selector_transfer_status.setItemData(viewModel.statusList as MutableList<StatusSheetData>)
        selector_transfer_status.setSelectCode(viewModel.statusList.first().code)
    }

    private fun initOnclick() {
       /* iv_scroll_to_top.setOnClickListener {
            rv_record.smoothScrollToPosition(0)
        }*/

        date_range_selector.btn_search.clickDelay  {
            viewModel.queryTransfers(startTime = date_range_selector.startTime.toString(),
                endTime = date_range_selector.endTime.toString(),
                firmTypeIn = selector_in_plat.selectedTag,
                firmTypeOut = selector_out_plat.selectedTag,
                status = selector_transfer_status.selectedTag)
        }

        selector_out_plat.setOnItemSelectedListener {
//            viewModel.filterRecordList(MoneyTransferViewModel.PLAT.IN_PLAT, it.showName)
            selector_out_plat.setSelectCode(it.code)
            if (selector_in_plat?.selectedCode == it.code) {
                selector_in_plat.dataList.first { item -> it.code != item.code }.let {
                    selector_in_plat.setSelectCode(it.code)
                }
            }
        }

        selector_in_plat.setOnItemSelectedListener {
//            viewModel.filterRecordList(MoneyTransferViewModel.PLAT.OUT_PLAT, it.showName)
            selector_in_plat.setSelectCode(it.code)
            if (selector_out_plat?.selectedCode == it.code) {
                selector_out_plat.dataList.first { item -> it.code != item.code }.let {
                    selector_out_plat.setSelectCode(it.code)
                }
            }
        }
    }

    private fun initObserver() {
        viewModel.queryTransfersResult.observe(viewLifecycleOwner) {
            rvAdapter.addFooterAndSubmitList(viewModel.recordDataList, viewModel.isLastPage)
            if (!rv_record.canScrollVertically(1) && !rvAdapter.currentList.isNullOrEmpty()) {
                tv_no_data_tips.visibility = View.VISIBLE
            } else {
                tv_no_data_tips.visibility = View.GONE
            }
            LogUtil.d("tv_no_data_tips="+tv_no_data_tips.visibility)
        }

        viewModel.recordInPlatSheetList.observe(viewLifecycleOwner) {
            selector_in_plat.setItemData(it as MutableList<StatusSheetData>)
            selector_in_plat.setSelectCode(it.first().code)
        }

        viewModel.recordOutPlatSheetList.observe(viewLifecycleOwner) {
            selector_out_plat.setItemData(it as MutableList<StatusSheetData>)
            selector_out_plat.setSelectCode(it.first().code)
        }
    }

}
