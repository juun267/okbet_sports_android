package org.cxct.sportlottery.ui.betRecord

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.FragmentUnsettledBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.bet.settledDetailList.RemarkBetRequest
import org.cxct.sportlottery.ui.base.BindingFragment
import org.cxct.sportlottery.ui.betRecord.accountHistory.AccountHistoryViewModel
import org.cxct.sportlottery.ui.betRecord.adapter.RecyclerUnsettledAdapter
import org.cxct.sportlottery.ui.betRecord.dialog.PrintDialog
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.view.loadMore
import org.cxct.sportlottery.view.onClick

class UnsettledFragment:BindingFragment<AccountHistoryViewModel,FragmentUnsettledBinding>() {
    private val mAdapter= RecyclerUnsettledAdapter()


    override fun onInitView(view: View) =binding.run {
        recyclerUnsettled.layoutManager=LinearLayoutManager(requireContext())
        recyclerUnsettled.adapter=mAdapter

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

        empty.emptyView.onClick {
            requireActivity().finish()
        }

        recyclerUnsettled.loadMore {
            viewModel.getUnsettledList()
        }
    }


    override fun onInitData() {
        getUnsettledData()
    }


    private fun getUnsettledData(){
        //获取未结算数据
        loading()
        viewModel.getUnsettledList()
        viewModel.unsettledData.observe(this){
            hideLoading()
            if(it.isEmpty()&&viewModel.pageIndex<=2){
                binding.empty.emptyView.visible()
                binding.recyclerUnsettled.gone()
                return@observe
            }
            binding.empty.emptyView.gone()
            binding.recyclerUnsettled.visible()
            mAdapter.addData(it)
        }
        viewModel.responseFailed.observe(this){
            hideLoading()
            mAdapter.setList(arrayListOf())
            binding.empty.emptyView.visible()
            binding.recyclerUnsettled.gone()
        }
    }
}