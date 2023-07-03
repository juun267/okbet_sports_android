package org.cxct.sportlottery.ui.betRecord

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.FragmentUnsettledBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.bet.settledDetailList.RemarkBetRequest
import org.cxct.sportlottery.ui.base.BindingFragment
import org.cxct.sportlottery.ui.betRecord.accountHistory.AccountHistoryViewModel
import org.cxct.sportlottery.ui.betRecord.adapter.RecyclerUnsettledAdapter
import org.cxct.sportlottery.ui.betRecord.dialog.PrintDialog
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.view.BetEmptyView
import org.cxct.sportlottery.view.loadMore

/**
 * 未结单列表
 */
class UnsettledFragment : BindingFragment<AccountHistoryViewModel, FragmentUnsettledBinding>() {
    private var mAdapter = RecyclerUnsettledAdapter()


    override fun onInitView(view: View) = binding.run {
        recyclerUnsettled.layoutManager = LinearLayoutManager(requireContext())
        recyclerUnsettled.adapter = mAdapter
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
                            viewModel.remarkBetLiveData.observeForever {
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
        //空视图点击
//        empty.emptyView.onClick {
//            requireActivity().finish()
//        }

        //加载更多
        recyclerUnsettled.loadMore {
            viewModel.getUnsettledList()
        }

        //待成立倒计时结束刷新数据
        mAdapter.setOnCountTime {
            viewModel.pageIndex = 1
            recyclerUnsettled.postDelayed({
                viewModel.getUnsettledList()
            },500)
        }

    }

    private fun initObserve() {
        //网络请求失败
        viewModel.responseFailed.observe(this) {
            hideLoading()
            if(viewModel.unsettledDataEvent.value!=null){
                mAdapter.setList(viewModel.unsettledDataEvent.value)
            }else{
                mAdapter.setList(arrayListOf())
            }
        }
        //未接单数据监听
        viewModel.unsettledDataEvent.observe(this) {
            hideLoading()
            binding.recyclerUnsettled.visible()
            if(viewModel.pageIndex == 2){
                mAdapter.setList(it)
            }else{
                mAdapter.addData(it)
            }
        }

    }


    override fun onInitData() {
        Log.e("dachang","onInitData")
        refData()
        initObserve()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun refData() {
        loading()
        viewModel.pageIndex = 1
        mAdapter.setList(arrayListOf())
        mAdapter.notifyDataSetChanged()
        //获取未结算数据
        viewModel.getUnsettledList()
    }


}