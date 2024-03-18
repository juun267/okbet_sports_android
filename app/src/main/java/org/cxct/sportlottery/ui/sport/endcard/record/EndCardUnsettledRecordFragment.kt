package org.cxct.sportlottery.ui.sport.endcard.record

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.databinding.FragmentEndcardUnsettledRecordBinding
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.sport.endcard.EndCardActivity
import org.cxct.sportlottery.ui.sport.endcard.EndCardVM
import org.cxct.sportlottery.ui.sport.endcard.dialog.EndCardBetDialog
import org.cxct.sportlottery.util.RefreshHelper
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.view.rumWithSlowRequest

class EndCardUnsettledRecordFragment: BaseFragment<EndCardVM,FragmentEndcardUnsettledRecordBinding>() {

    private val refreshHelper by lazy { RefreshHelper.of(binding.rvBetRecord, this, true,true) }
    private val recordAdapter by lazy { EndCardRecordAdapter() }
    private var currentPage = 1
    override fun onInitView(view: View) {
        initRecordList()
    }
    override fun onBindViewStatus(view: View) {
        super.onBindViewStatus(view)
        initObservable()
        getUnsettledData(0)
    }
    private fun initRecordList(){
        refreshHelper.setRefreshListener {
            getUnsettledData(0)
        }
        refreshHelper.setLoadMoreListener(object : RefreshHelper.LoadMore {
            override fun onLoadMore(pageIndex: Int, pageSize: Int) {
                getUnsettledData(currentPage+1)
            }
        })
        binding.rvBetRecord.apply {
            layoutManager = LinearLayoutManager(context,RecyclerView.VERTICAL,false)
            recordAdapter.setOnItemClickListener { adapter, view, position ->
                (activity as? EndCardActivity)?.showRecordDetail(recordAdapter.getItem(position))
            }
            recordAdapter.setOnItemChildClickListener { adapter, view, position ->
                EndCardBetDialog().show(childFragmentManager)
            }
            adapter = recordAdapter
        }
    }
    private fun initObservable() {
        viewModel.unSettledResult.observe(this){
            refreshHelper.finishRefresh()
            refreshHelper.finishLoadMore()
            if (it.success) {
                currentPage = it.page
                it.rows?.let { newData->
                    if (currentPage == 1) {
                        recordAdapter.setList(newData)
                    } else {
                        recordAdapter.addData(newData)
                    }
                    refreshHelper.setLoadMoreEnable(recordAdapter.itemCount < it.total?:0)
                }
            } else {
                ToastUtil.showToast(requireContext(), it.msg)
            }
        }
    }
    private fun getUnsettledData(pageIndex:Int) {
        //获取结算数据
        rumWithSlowRequest(viewModel){
            viewModel.getUnsettledList(pageIndex,null,null)
        }
    }
}