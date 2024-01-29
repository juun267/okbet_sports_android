package org.cxct.sportlottery.ui.betRecord

import android.annotation.SuppressLint
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.FragmentSettledBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.bet.settledDetailList.RemarkBetRequest
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.betRecord.accountHistory.AccountHistoryViewModel
import org.cxct.sportlottery.ui.betRecord.adapter.RecyclerUnsettledAdapter
import org.cxct.sportlottery.ui.betRecord.dialog.PrintDialog
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.RefreshHelper
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.view.BetEmptyView
import org.cxct.sportlottery.view.rumWithSlowRequest
import java.util.*

/**
 * 已结单列表
 */
class SettledFragment : BaseFragment<AccountHistoryViewModel, FragmentSettledBinding>() {
    private val mAdapter = RecyclerUnsettledAdapter()
    private val oneDay = 60 * 60 * 24 * 1000
    private val refreshHelper by lazy { RefreshHelper.of(binding.recyclerSettled, this, false) }
    var pageIndex=1
    var hasMore=true
    //已结单数据 开始时间
    var startTime:Long?=0L
    //已结单数据 结束时间
    var endTime:Long?=0L
    //总盈亏
    var totalReward:Double=0.0
    //总投注额
    var totalBet:Double=0.0
    //有效投注额
    var totalEfficient:Double=0.0
    override fun onInitView(view: View) = binding.run {
        //默认搜索时间   今天
        startTime = TimeUtil.getTodayStartTimeStamp()
        endTime = TimeUtil.getTodayEndTimeStamp()

        recyclerSettled.layoutManager = LinearLayoutManager(requireContext())
        recyclerSettled.adapter = mAdapter

        refreshHelper.setLoadMoreListener(object : RefreshHelper.LoadMore {
            override fun onLoadMore(pageIndex: Int, pageSize: Int) {
                if (hasMore) {
                    getSettledData()
                }else{
                    refreshHelper.finishLoadMoreWithNoMoreData()
                }
            }
        })
//        mAdapter.enableDefaultEmptyView(requireContext())
        mAdapter.setEmptyView(BetEmptyView(requireContext()).apply { center() })

        //item打印点击
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
                                dialog.dismiss()
                                val newUrl = Constants.getPrintReceipt(
                                    requireContext(),
                                    it.remarkBetResult?.uniqNo,
                                    orderTime.toString(),
                                    it1
                                )
                                JumpUtil.toExternalWeb(requireContext(), newUrl)
                            }
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
                    3 -> {
                        //其他  最近90天内除开最近30天的前60天
                        startTime = dateSearchBar.startTime
                        endTime = dateSearchBar.endTime
                    }
                }
                dateSearchBar.isVisible = tab.position==3
                //刷新数据
                recyclerSettled.gone()
                getSettledData()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
        dateSearchBar.setOnClickSearchListener {
            if((dateSearchBar.endTime?:0)-(dateSearchBar.startTime?:0)>7*24*3600*1000){
                ToastUtil.showToast(requireContext(),R.string.P274)
                return@setOnClickSearchListener
            }
            pageIndex=1
            startTime = dateSearchBar.startTime
            endTime = dateSearchBar.endTime
            //刷新数据
            recyclerSettled.gone()
            getSettledData()
        }
    }

    override fun onInitData() {
        initObservable()
        pageIndex = 1
        mAdapter.setNewInstance(null)
        loading()
        getSettledData()
    }

    private fun initObservable() {
        viewModel.settledResult.observe(this) {
            hideLoading()
            if (it==null){
                totalBet=0.0
                totalReward=0.0
                totalEfficient=0.0
                initBetValue()
                binding.recyclerSettled.visible()
                return@observe
            }
            if (it.success) {
                pageIndex++
                it.rows?.let {
                    if(it.isEmpty()){
                        hasMore=false
                    }
                    refreshHelper.finishWithSuccess()
                    binding.recyclerSettled.visible()
                    if (pageIndex == 2) {
                        mAdapter.setList(it)
                    } else {
                        mAdapter.addData(it)
                    }
                }
                totalBet=it.other?.totalAmount?:0.0
                totalReward=it.other?.win?:0.0
                totalEfficient=it.other?.valueBetAmount?:0.0
                initBetValue()
            } else {
                ToastUtil.showToast(requireContext(), it.msg)
            }
        }
    }

    private fun getSettledData() {
        //获取结算数据
        rumWithSlowRequest(viewModel){
            viewModel.getSettledList(pageIndex,startTime,endTime)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initBetValue() {
        //总盈亏
        binding.tvReward.text = "$showCurrencySign ${TextUtil.formatMoney(totalReward,2)}"
        //总有效投注
        binding.tvTotalValue.text = "$showCurrencySign ${TextUtil.formatMoney(totalEfficient,2)}"
        //总投注额
        binding.tvTotalBet.text = "$showCurrencySign ${TextUtil.formatMoney(totalBet,2)}"
    }

}