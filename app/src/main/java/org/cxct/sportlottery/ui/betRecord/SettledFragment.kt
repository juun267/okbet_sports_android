package org.cxct.sportlottery.ui.betRecord

import android.annotation.SuppressLint
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
import org.cxct.sportlottery.util.ArithUtil
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.view.loadMore
import org.cxct.sportlottery.view.onClick

class SettledFragment:BindingFragment<AccountHistoryViewModel,FragmentSettledBinding>() {
    private val mAdapter=RecyclerUnsettledAdapter()

    override fun onInitView(view: View) =binding.run {
        viewModel.settledStartTime=TimeUtil.getTodayStartTimeStamp()
        viewModel.settledEndTime=TimeUtil.getTodayEndTimeStamp()

        recyclerSettled.layoutManager=LinearLayoutManager(requireContext())
        recyclerSettled.adapter=mAdapter

        mAdapter.setOnItemChildClickListener { _, view, position ->
            val data= mAdapter.data[position]
            when(view.id){
                //打印点击
                R.id.tvOrderPrint->{
                    val dialog = PrintDialog(requireContext())
                    dialog.tvPrintClickListener = { it1 ->
                        if (it1?.isNotEmpty() == true) {
                            val orderNo = data.orderNo
                            val orderTime = data.betConfirmTime
                            val requestBet = RemarkBetRequest(orderNo, it1, orderTime.toString())
                            viewModel.remarkBetLiveData.observeForever {
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
                            viewModel.reMarkBet(requestBet)
                        }
                    }
                    dialog.show()
                }
            }
        }

        empty.tvReturn.onClick {
            requireActivity().finish()
        }

        tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
            @SuppressLint("NotifyDataSetChanged")
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewModel.pageSettledIndex=1
                mAdapter.data.clear()
                mAdapter.notifyDataSetChanged()
                when(tab.position){
                    0->{
                        viewModel.settledStartTime=TimeUtil.getTodayStartTimeStamp()
                        viewModel.settledEndTime=TimeUtil.getTodayEndTimeStamp()
                    }
                    1->{
                        viewModel.settledStartTime=TimeUtil.getDefaultTimeStamp(1).startTime!!.toLong()
                        viewModel.settledEndTime=TimeUtil.getDefaultTimeStamp(1).endTime!!.toLong()
                    }
                    2->{
                        viewModel.settledStartTime=TimeUtil.getDefaultTimeStamp(7).startTime!!.toLong()
                        viewModel.settledEndTime=TimeUtil.getDefaultTimeStamp(7).endTime!!.toLong()
                    }
                    3->{
                        viewModel.settledStartTime=TimeUtil.getDefaultTimeStamp(30).startTime!!.toLong()
                        viewModel.settledEndTime=TimeUtil.getDefaultTimeStamp(30).endTime!!.toLong()
                    }
                    4->{
                        viewModel.settledStartTime=TimeUtil.getDefaultTimeStamp(90).startTime!!.toLong()
                        viewModel.settledEndTime=TimeUtil.getDefaultTimeStamp(30).startTime!!.toLong()
                    }
                }
                viewModel.getSettledList()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        recyclerSettled.loadMore {
            viewModel.getSettledList()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onInitData() {
        viewModel.pageSettledIndex=1
        mAdapter.data.clear()
        mAdapter.notifyDataSetChanged()
        getSettledData()

    }

    private fun getSettledData(){
        //获取结算数据
        loading()
        viewModel.getSettledList()
        viewModel.settledData.observe(this){
            hideLoading()
            if(it.isEmpty()&&viewModel.pageSettledIndex<=2){
                binding.empty.emptyView.visible()
                binding.recyclerSettled.gone()
                return@observe
            }
            initBetValue()
            binding.empty.emptyView.gone()
            binding.recyclerSettled.visible()
            mAdapter.addData(it)
        }
        viewModel.responseFailed.observe(this){
            hideLoading()
            mAdapter.setList(arrayListOf())
            binding.empty.emptyView.visible()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initBetValue(){
        //总盈亏
        binding.tvReward.text="$showCurrencySign ${TextUtil.format(viewModel.totalReward)}"
        //总有效投注
        binding.tvTotalValue.text="$showCurrencySign ${TextUtil.format(viewModel.totalEfficient)}"
        //总投注额
        binding.tvTotalBet.text="$showCurrencySign ${TextUtil.format(viewModel.totalBet)}"
    }

}