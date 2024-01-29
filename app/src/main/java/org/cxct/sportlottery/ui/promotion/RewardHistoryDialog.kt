package org.cxct.sportlottery.ui.promotion

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.hideLoading
import org.cxct.sportlottery.common.extentions.setOnClickListeners
import org.cxct.sportlottery.databinding.DialogRewardHistoryBinding
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.util.RefreshHelper
import org.cxct.sportlottery.view.BetEmptyView

class RewardHistoryDialog(val activityId: String): BaseDialog<MainHomeViewModel,DialogRewardHistoryBinding>() {

    init {
        setStyle(R.style.FullScreen)
    }
    private val adapter by lazy { RewardHistoryAdapter(activityId) }
    private val refreshHelper by lazy { RefreshHelper.of(binding.rvRecord, this, false) }
    private var currentPage: Int = 1
    private var totalCount: Int = 0

    override fun onInitView() {
        initView()
        initObserve()
        loadData()
    }

    private fun initView()=binding.run {
        refreshHelper.setLoadMoreListener(object : RefreshHelper.LoadMore {
            override fun onLoadMore(pageIndex: Int, pageSize: Int) {
                if (adapter.itemCount<totalCount) {
                    currentPage++
                    loadData()
                }else{
                    refreshHelper.finishLoadMoreWithNoMoreData()
                }
            }
        })
        setOnClickListeners(ivClose,btnConfirm){
            dismiss()
        }
        adapter.setEmptyView(BetEmptyView(requireContext()).apply { center() })
        rvRecord.layoutManager = LinearLayoutManager(requireContext(),RecyclerView.VERTICAL,false)
        rvRecord.adapter = adapter
    }

    private fun initObserve() = viewModel.run {
        rewardRecord.observe(viewLifecycleOwner) {
            requireActivity().hideLoading()
            totalCount = it.totalCount
            val datas = it.data
            if(datas.isNullOrEmpty()){
                refreshHelper.finishLoadMoreWithNoMoreData()
            }else{
                if (currentPage==1){
                    adapter.setList(datas)
                }else{
                    adapter.addData(datas)
                }
                if (adapter.itemCount<totalCount) {
                    refreshHelper.finishLoadMore()
                }else{
                    refreshHelper.finishLoadMoreWithNoMoreData()
                }
            }
        }
    }
    private fun loadData(){
        viewModel.activityRecord(activityId,currentPage)
    }

}