package org.cxct.sportlottery.ui.betRecord

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.FragmentSettledBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.bet.settledDetailList.RemarkBetRequest
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.ui.base.BindingFragment
import org.cxct.sportlottery.ui.betRecord.accountHistory.AccountHistoryViewModel
import org.cxct.sportlottery.ui.betRecord.adapter.RecyclerUnsettledAdapter
import org.cxct.sportlottery.ui.betRecord.dialog.PrintDialog
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.RefreshHelper
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.view.BetEmptyView
import org.cxct.sportlottery.view.loadMore

/**
 * 已结单列表
 */
class SettledFragment : BindingFragment<AccountHistoryViewModel, FragmentSettledBinding>() {
    private val mAdapter = RecyclerUnsettledAdapter()
    private val oneDay = 60 * 60 * 24 * 1000
    private val refreshHelper by lazy { RefreshHelper.of(binding.recyclerSettled, this, false) }
    override fun onInitView(view: View) = binding.run {
        //默认搜索时间   今天
        viewModel.settledStartTime = TimeUtil.getTodayStartTimeStamp()
        viewModel.settledEndTime = TimeUtil.getTodayEndTimeStamp()

        recyclerSettled.layoutManager = LinearLayoutManager(requireContext())
        recyclerSettled.adapter = mAdapter

        refreshHelper.setLoadMoreListener(object : RefreshHelper.LoadMore {
            override fun onLoadMore(pageIndex: Int, pageSize: Int) {
                if (viewModel.hasMore) {
                    viewModel.getSettledList()
                }else{
                    refreshHelper.finishLoadMoreWithNoMoreData()
                }
            }
        })
//        mAdapter.enableDefaultEmptyView(requireContext())
        mAdapter.setEmptyView(BetEmptyView(requireContext()))

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
                viewModel.pageSettledIndex = 1
                viewModel.hasMore = true
                mAdapter.setNewInstance(null)
                when (tab.position) {
                    0 -> {
                        //今天
                        viewModel.settledStartTime = TimeUtil.getTodayStartTimeStamp()
                        viewModel.settledEndTime = TimeUtil.getTodayEndTimeStamp()
                    }

                    1 -> {
                        //昨天
                        viewModel.settledStartTime =
                            TimeUtil.getDefaultTimeStamp(1).startTime!!.toLong()
                        viewModel.settledEndTime = viewModel.settledStartTime!! + oneDay - 1
                    }

                    2 -> {
                        //7天
                        viewModel.settledStartTime =
                            TimeUtil.getDefaultTimeStamp(6).startTime!!.toLong()
                        viewModel.settledEndTime = TimeUtil.getTodayEndTimeStamp()
                    }

                    3 -> {
                        //30天
                        viewModel.settledStartTime =
                            TimeUtil.getDefaultTimeStamp(29).startTime!!.toLong()
                        viewModel.settledEndTime = TimeUtil.getTodayEndTimeStamp()
                    }

                    4 -> {
                        //其他  最近90天内除开最近30天的前60天
                        viewModel.settledStartTime =
                            TimeUtil.getDefaultTimeStamp(89).startTime!!.toLong()
                        viewModel.settledEndTime =
                            TimeUtil.getDefaultTimeStamp(30).startTime!!.toLong()
                    }
                }
                //刷新数据
                recyclerSettled.gone()
                viewModel.getSettledList()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        //加载更多
//        recyclerSettled.loadMore {
//            if(viewModel.hasMore){
//                viewModel.getSettledList()
//            }
//        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onInitData() {
        viewModel.pageSettledIndex = 1
        mAdapter.setNewInstance(null)
        getSettledData()

    }

    private fun getSettledData() {
        //获取结算数据
        loading()
        viewModel.getSettledList()
        viewModel.settledData.observe(this) {
            hideLoading()
            initBetValue()
            refreshHelper.finishWithSuccess()
            binding.recyclerSettled.visible()
            if (viewModel.pageSettledIndex == 2) {
                mAdapter.setList(it)
            } else {
                mAdapter.addData(it)
            }
        }
        //网络失败
        viewModel.responseFailed.observe(this) {
            hideLoading()
            initBetValue()
            binding.recyclerSettled.visible()
        }
        viewModel.errorEvent.observe(this) {
            hideLoading()
            ToastUtil.showToast(requireContext(), it)
        }

    }

    @SuppressLint("SetTextI18n")
    private fun initBetValue() {
        //总盈亏
        binding.tvReward.text = "$showCurrencySign ${TextUtil.formatMoney(viewModel.totalReward,2)}"
        //总有效投注
        binding.tvTotalValue.text = "$showCurrencySign ${TextUtil.formatMoney(viewModel.totalEfficient,2)}"
        //总投注额
        binding.tvTotalBet.text = "$showCurrencySign ${TextUtil.formatMoney(viewModel.totalBet,2)}"
    }

}