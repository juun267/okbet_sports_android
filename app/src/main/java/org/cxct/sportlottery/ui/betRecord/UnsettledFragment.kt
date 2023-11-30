package org.cxct.sportlottery.ui.betRecord

import android.annotation.SuppressLint
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.FragmentUnsettledBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.bet.settledDetailList.RemarkBetRequest
import org.cxct.sportlottery.network.service.order_settlement.Status
import org.cxct.sportlottery.ui.base.BindingFragment
import org.cxct.sportlottery.ui.betRecord.accountHistory.AccountHistoryViewModel
import org.cxct.sportlottery.ui.betRecord.adapter.RecyclerUnsettledAdapter
import org.cxct.sportlottery.ui.betRecord.dialog.PrintDialog
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.BetEmptyView
import org.cxct.sportlottery.view.loadMore
import org.cxct.sportlottery.view.rumWithSlowRequest

/**
 * 未结单列表
 */
class UnsettledFragment : BindingFragment<AccountHistoryViewModel, FragmentUnsettledBinding>() {
    private var mAdapter = RecyclerUnsettledAdapter()
    private val oneDay = 60 * 60 * 24 * 1000
    private val refreshHelper by lazy { RefreshHelper.of(binding.recyclerUnsettled, this, false) }

    var pageIndex=1
    var hasMore=true
    var startTime:Long?=null
    var endTime:Long?=null

    override fun onInitView(view: View) = binding.run {
        startTime = TimeUtil.getTodayStartTimeStamp()
        endTime = TimeUtil.getTodayEndTimeStamp()
        recyclerUnsettled.layoutManager = LinearLayoutManager(requireContext())
        recyclerUnsettled.adapter = mAdapter
        refreshHelper.setLoadMoreListener(object : RefreshHelper.LoadMore {
            override fun onLoadMore(pageIndex: Int, pageSize: Int) {
                if (hasMore) {
                    getUnsettledData()
                }else{
                    refreshHelper.finishLoadMoreWithNoMoreData()
                }
            }
        })
        mAdapter.setEmptyView(BetEmptyView(requireContext()))
        mAdapter.setOnItemChildClickListener { _, view, position ->
            val data = mAdapter.data[position]
            when (view.id) {
                //打印点击
                R.id.tvOrderPrint -> {
                    val dialog = PrintDialog(requireContext())
                    dialog.tvPrintClickListener = { it1 ->
                        if (it1?.isNotEmpty() == true) {
                            val orderNo = data.orderNo
                            val orderTime = data.betConfirmTime
                            val requestBet = RemarkBetRequest(orderNo, it1, orderTime.toString())
                            viewModel.observerRemarkBetLiveData {
                                hideLoading()
                                dialog.dismiss()
                                val newUrl =
                                    Constants.getPrintReceipt(
                                        requireContext(),
                                        it.remarkBetResult?.uniqNo,
                                        orderTime.toString(),
                                        it1
                                    )
                                JumpUtil.toExternalWeb(requireContext(), newUrl)
                            }
                            loading()
                            viewModel.reMarkBet(requestBet)
                        }
                    }
                    dialog.show()
                }
            }
        }
        //tab切换
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onTabSelected(tab: TabLayout.Tab) {
                loading()
                pageIndex = 1
                hasMore = true
                mAdapter.setNewInstance(null)
                when (tab.position) {
                    0 -> {
                        //今天
                        startTime = TimeUtil.getTodayStartTimeStamp()
                        endTime = TimeUtil.getTodayEndTimeStamp()
                    }

                    1 -> {
                        //昨天
                        startTime = TimeUtil.getDefaultTimeStamp(1).startTime!!.toLong()
                        endTime = startTime!! + oneDay - 1
                    }

                    2 -> {
                        //7天
                        startTime = TimeUtil.getDefaultTimeStamp(6).startTime!!.toLong()
                        endTime = TimeUtil.getTodayEndTimeStamp()
                    }
                    4 -> {
                        //其他  最近90天内除开最近30天的前60天
                        startTime = TimeUtil.getDefaultTimeStamp(89).startTime!!.toLong()
                        endTime = TimeUtil.getDefaultTimeStamp(30).startTime!!.toLong()
                    }
                }
                //刷新数据
                recyclerUnsettled.gone()
                getUnsettledData()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        //待成立倒计时结束刷新数据
        mAdapter.setOnCountTime {
            pageIndex = 1
            recyclerUnsettled.postDelayed({
                getUnsettledData()
            },600)
        }

        viewModel.settlementNotificationMsg.observe(viewLifecycleOwner) { event ->
            val it = event.getContentIfNotHandled() ?: return@observe
            if (it.status == Status.UN_DONE.code || it.status == Status.CANCEL.code) {
               recyclerUnsettled.postDelayed({
                 pageIndex = 1
                 getUnsettledData()
            },500)
            }
        }
    }

    override fun onInitData() {
        initObservable()
        getUnsettledData()
    }
    private fun initObservable() {
        viewModel.unSettledResult.observe(this) {
            hideLoading()
            if (it==null){
                binding.recyclerUnsettled.visible()
                return@observe
            }
            if (it.success) {
                pageIndex++
                it.rows?.let {
                    if(it.isEmpty()){
                        hasMore=false
                    }
                    refreshHelper.finishWithSuccess()
                    binding.recyclerUnsettled.visible()
                    if (pageIndex == 2) {
                        mAdapter.setList(list = it)
                    } else {
                        mAdapter.addData(newData = it)
                    }
                }

            } else {
                ToastUtil.showToast(requireContext(), it.msg)
            }
        }
    }
    private fun getUnsettledData() {
        mAdapter.setList(arrayListOf())
        //获取结算数据
        rumWithSlowRequest(viewModel){
            viewModel.getUnsettledList(pageIndex,startTime,endTime)
        }
    }
}