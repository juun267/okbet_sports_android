package org.cxct.sportlottery.ui.game.betList.receipt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_parlay_receipt.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.network.bet.add.betReceipt.BetResult
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.ui.transactionStatus.ParlayType
import org.cxct.sportlottery.util.LocalUtils
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.setBetReceiptStatus
import org.cxct.sportlottery.util.setReceiptStatusColor
import timber.log.Timber

class ParlayViewHolder private constructor(itemView: View) :
    RecyclerView.ViewHolder(itemView) {
    companion object {
        fun from(viewGroup: ViewGroup): RecyclerView.ViewHolder {
            val layoutInflater = LayoutInflater.from(viewGroup.context)
            val view = layoutInflater.inflate(R.layout.item_parlay_receipt, viewGroup, false)
            return ParlayViewHolder(view)
        }
    }

    fun bind(
        itemData: BetResult,
        firstItem: Boolean,
        oddsType: OddsType,
        betParlay: List<ParlayOdd>?,
        interfaceStatusChangeListener: BetReceiptDiffAdapter.InterfaceStatusChangeListener?,
        position: Int
    ) = itemView.run {

        if (itemData.isFailed()) {
            tvBetResutStatu.setTextColor(context.getColor(R.color.color_E23434))
            tvBetResutStatu.setText(R.string.bet_info_add_bet_failed)
            tvBetResutStatu.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_bet_failed, 0, 0, 0)
            flBetOrder.setBackgroundResource(R.drawable.bg_fail)
        } else {
            tvBetResutStatu.setTextColor(context.getColor(R.color.color_1EB65B))
            tvBetResutStatu.setText(R.string.bet_info_add_bet_success)
            tvBetResutStatu.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_bet_success, 0, 0, 0)
            flBetOrder.setBackgroundResource(R.drawable.bg_successful)
        }

        val currencySign = showCurrencySign
        itemData.apply {

            tv_winnable_amount_title.text =
                LocalUtils.getString(R.string.bet_receipt_win_quota_with_sign_max)
            tv_bet_amount_title.text =
                LocalUtils.getString(R.string.bet_receipt_bet_quota_with_sign_money)

            matchOdds?.firstOrNull()?.apply {
                parlayType?.let { parlayTypeCode ->
                    tv_play_name_parlay.text =
                        ParlayType.getParlayStringRes(parlayTypeCode)?.let { context.getString(it) }
                            ?: ""
                }
            }
            tv_multiplier.text = " x ${betParlay?.find { parlayType == it.parlayType }?.num ?: 1}"

            if (position == 0) { //僅有第一項item的上方，要顯示組合成串關的資料(matchOdds)
                rv_show_singles_list.isVisible = true
                rv_show_singles_list.apply {
                    layoutManager =
                        LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                    adapter = BetReceiptDiffForParlayShowSingleAdapter().apply {
                        submit(
                            matchOdds ?: listOf(),
                            betParlayList ?: listOf(),
                            matchType
                        )
                    }
                }
            } else {
                rv_show_singles_list.isVisible = false
            }

            //不顯示 "@ 組合賠率"
//                    tv_match_at.visibility = if (firstItem) View.VISIBLE else View.GONE
//                    tv_match_odd_parlay.apply {
//                        visibility = if (firstItem) View.VISIBLE else View.GONE
//                        text = (betParlay?.getOrNull(0)
//                            ?.let { parlayOdd ->
//                                TextUtil.formatForOdd(
//                                    getOdds(
//                                        parlayOdd,
//                                        oddsType
//                                    )
//                                )
//                            }) ?: "0.0"
//                    }

            val number = betParlay?.find { parlayType == it.parlayType }?.num ?: 0
            tv_bet_amount.text = "$currencySign ${itemData.stake?.let { TextUtil.formatForOdd(it * number) }}"
            tv_winnable_amount.text = "$currencySign ${TextUtil.formatForOdd(winnable ?: 0.0)}"

            if (status != 0){
                tv_bet_status.setBetReceiptStatus(status)
                tv_bet_status.setReceiptStatusColor(status)
                tv_bet_status.visible()
            }else{
                tv_bet_status.gone()
            }


            //"status": 7 顯示賠率已改變
            Timber.d("parlayViewHolderCurrentStatus: $status")
            if (status == 7)
                interfaceStatusChangeListener?.onChange(code)


        }
    }
}