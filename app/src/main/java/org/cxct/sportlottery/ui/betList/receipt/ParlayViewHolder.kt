package org.cxct.sportlottery.ui.betList.receipt

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ItemParlayReceiptBinding
import org.cxct.sportlottery.network.bet.add.betReceipt.BetResult
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.ui.betRecord.ParlayType
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.setBetReceiptStatus
import org.cxct.sportlottery.util.setReceiptStatusColor
import timber.log.Timber

class ParlayViewHolder private constructor(val binding: ItemParlayReceiptBinding) :
    RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun from(viewGroup: ViewGroup): RecyclerView.ViewHolder {
            val binding = ItemParlayReceiptBinding.inflate( LayoutInflater.from(viewGroup.context), viewGroup, false)
            return ParlayViewHolder(binding)
        }
    }

    fun bind(
        itemData: BetResult,
        oddsType: OddsType,
        betParlay: List<ParlayOdd>?,
        interfaceStatusChangeListener: BetReceiptDiffAdapter.InterfaceStatusChangeListener?,
        position: Int
    ) = binding.run {
        val context = binding.root.context

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
        tvWinnableAmountTitle.text =
            context.getString(R.string.bet_receipt_win_quota_with_sign_max)
        tvBetAmountTitle.text =
            context.getString(R.string.bet_receipt_bet_quota_with_sign_money)
        itemData.matchOdds?.firstOrNull()?.apply {
            itemData.parlayType?.let { parlayTypeCode ->
                tvPlayNameParlay.text =
                    ParlayType.getParlayStringRes(parlayTypeCode)?.let { context.getString(it) }
                        ?: ""
            }
        }
        tvMultiplier.text = " x ${betParlay?.find { itemData.parlayType == it.parlayType }?.num ?: 1}"

        if (position == 0) { //僅有第一項item的上方，要顯示組合成串關的資料(matchOdds)
            rvShowSinglesList.isVisible = true
            rvShowSinglesList.apply {
                layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                adapter = BetReceiptDiffForParlayShowSingleAdapter().apply {
                    submit(
                        itemData.matchOdds ?: listOf(),
                        betParlayList ?: listOf(),
                        matchType
                    )
                }
            }
        } else {
            rvShowSinglesList.isVisible = false
        }


        val number = betParlay?.find { itemData.parlayType == it.parlayType }?.num ?: 0
        tvBetAmount.text = "$currencySign ${itemData.stake?.let { TextUtil.formatForOdd(it * number) }}"
        tvWinnableAmount.text = "$currencySign ${TextUtil.formatForOdd(itemData.winnable ?: 0.0)}"

        if (itemData.status != 0){
            tvBetStatus.setBetReceiptStatus(itemData.status)
            tvBetStatus.setReceiptStatusColor(itemData.status)
            tvBetStatus.visible()
        }else{
            tvBetStatus.gone()
        }

        //"status": 7 顯示賠率已改變
        Timber.d("parlayViewHolderCurrentStatus: ${itemData.status}")
        if (itemData.status == 7)
            interfaceStatusChangeListener?.onChange(itemData.code)
    }
}