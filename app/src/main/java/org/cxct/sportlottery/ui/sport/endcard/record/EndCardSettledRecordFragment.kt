package org.cxct.sportlottery.ui.sport.endcard.record

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.databinding.FragmentEndcardSettledRecordBinding
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.sport.endcard.EndCardActivity
import org.cxct.sportlottery.ui.sport.endcard.EndCardVM
import org.cxct.sportlottery.ui.sport.endcard.dialog.EndCardBetDialog
import org.cxct.sportlottery.util.RefreshHelper
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.view.BetEmptyView
import org.cxct.sportlottery.view.rumWithSlowRequest

class EndCardSettledRecordFragment: BaseFragment<EndCardVM, FragmentEndcardSettledRecordBinding>() {

    private val oneDay = 60 * 60 * 24 * 1000
    private val refreshHelper by lazy { RefreshHelper.of(binding.rvBetRecord, this, true,true) }
    private val recordAdapter by lazy { EndCardRecordAdapter().apply {
        setEmptyView(BetEmptyView(requireContext()).apply { center() })
    } }
    private var currentPage = 1
    private var startTime:Long?=null
    private var endTime:Long?=null
    override fun onInitView(view: View) {
        initTimeTab()
        initRecordList()
    }

    override fun onBindViewStatus(view: View) {
        super.onBindViewStatus(view)
        initObservable()
        reload()
    }

    private fun initTimeTab(){
        binding.rgDate.setOnCheckedChangeListener{ group, checkedId ->
            when(checkedId){
                binding.rbtnToday.id -> {
                    //今天
                    startTime = TimeUtil.getTodayStartTimeStamp()
                    endTime = TimeUtil.getTodayEndTimeStamp()
                }

                binding.rbtnYesterday.id -> {
                    //昨天
                    startTime = TimeUtil.getDefaultTimeStamp(1).startTime!!.toLong()
                    endTime = startTime!! + oneDay - 1
                }

                binding.rbtnLast.id -> {
                    //7天
                    startTime = TimeUtil.getDefaultTimeStamp(6).startTime!!.toLong()
                    endTime = TimeUtil.getTodayEndTimeStamp()
                }
            }
            resetListData()
            loading()
            getSettledData(1)
        }
        binding.rgDate.check(binding.rbtnToday.id)
    }
    private fun initRecordList(){
        refreshHelper.setRefreshListener {
            getSettledData(1)
        }
        refreshHelper.setLoadMoreListener(object : RefreshHelper.LoadMore {
            override fun onLoadMore(pageIndex: Int, pageSize: Int) {
                getSettledData(currentPage+1)
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
        viewModel.settledResult.observe(this){
            hideLoading()
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
                binding.tvTotalbet.text = (it.other?.totalAmount?:0.0).toString()
                binding.tvReward.text = (it.other?.win?:0.0).toString()
                binding.tvTotalValue.text = (it.other?.valueBetAmount?:0.0).toString()
            } else {
                ToastUtil.showToast(requireContext(), it.msg)
            }
        }
    }
    private fun getSettledData(pageIndex:Int) {
        //获取结算数据
        rumWithSlowRequest(viewModel){
            viewModel.getSettledList(pageIndex,startTime,endTime)
        }
    }
    private fun resetListData(){
        binding.tvTotalbet.text = "0.0"
        binding.tvReward.text = "0.0"
        binding.tvTotalValue.text = "0.0"
    }
    fun reload(){
        if (isAdded) {
            loading()
            getSettledData(1)
        }
    }
}