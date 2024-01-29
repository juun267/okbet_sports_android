package org.cxct.sportlottery.ui.betRecord.detail

import android.annotation.SuppressLint
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentBetDetails2Binding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.bet.list.Row
import org.cxct.sportlottery.network.bet.settledDetailList.RemarkBetRequest
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.betRecord.ParlayType
import org.cxct.sportlottery.ui.betRecord.accountHistory.AccountHistoryViewModel
import org.cxct.sportlottery.ui.betRecord.adapter.RecyclerDetailBetAdapter
import org.cxct.sportlottery.ui.betRecord.dialog.PrintDialog
import org.cxct.sportlottery.util.JumpUtil

class BetDetailsFragment2 : BaseFragment<AccountHistoryViewModel, FragmentBetDetails2Binding>() {
    private var mAdapter :RecyclerDetailBetAdapter?=null

    override fun onInitView(view: View) = binding.run {
        recyclerUnsettled.layoutManager = LinearLayoutManager(requireContext())
    }

    @SuppressLint("SetTextI18n")
    override fun onInitData() {
        super.onInitData()
        val row = arguments?.get("data") as Row?

//        row.parlayComsDetailVOs
        row?.let {
            mAdapter = RecyclerDetailBetAdapter(row)
            binding.recyclerUnsettled.adapter = mAdapter



            mAdapter!!.setOnItemChildClickListener { _, view, position ->
//                val data = mAdapter!!.data[position]
                when (view.id) {
                    //打印点击
                    R.id.tvOrderPrint -> {
                        val dialog = PrintDialog(requireContext())
                        dialog.tvPrintClickListener = { it1 ->
                            if (it1?.isNotEmpty() == true) {
                                val orderNo = it.orderNo
                                val orderTime = it.betConfirmTime
                                val requestBet = RemarkBetRequest(orderNo, it1, orderTime.toString())
                                viewModel.observerRemarkBetLiveData {
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

            mAdapter!!.setList(it.parlayComsDetailVOs)
            val parlayString = StringBuffer()
            ParlayType.getParlayStringRes(row.parlayType)?.let { parlayTypeStringResId ->
                parlayString.append(requireContext().getString(R.string.bet_record_parlay))
                parlayString.append("(")
                parlayString.append(requireContext().getString(parlayTypeStringResId))
                parlayString.append(") -")
                parlayString.append(GameType.getGameTypeString(requireContext(), row.gameType))
            }
            binding.tvParlay.text =parlayString.toString()
//            binding.tvBetTotal.text=" ₱ ${TextUtil.format(row.totalAmount)}"
        }



    }
}