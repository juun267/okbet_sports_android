package org.cxct.sportlottery.ui.money.withdraw

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.withdraw.uwcheck.CheckList
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.setMoneyFormat

class CommissionDetailAdapter: RecyclerView.Adapter<CommissionDetailAdapter.ItemViewHolder>() {

    private var mDataList: List<CheckList> = mutableListOf()
    enum class CommissionItemType{
        HEADER,ORIGINAL
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ItemViewHolder {
        return when(viewType){
            CommissionItemType.HEADER.ordinal ->{
                val itemLayout = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_money_get_commission_header, viewGroup, false)
                ItemViewHolder(itemLayout)
            }
            else ->{
                val itemLayout = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_money_get_commission, viewGroup, false)
                ItemViewHolder(itemLayout)
            }
        }
    }

    override fun onBindViewHolder(viewHolder: ItemViewHolder, position: Int) {
        try {
            val data = mDataList[position]
            val zero = 0.0
            viewHolder.run {
                tvId.text = data.orderNo
                tvDeductMoney.setMoneyFormat(zero.minus(data.deductMoney ?: 0))
                tvTime.text = TimeUtil.stampToDateHMS(data.addTime?.toLong() ?: 0)

                tvRequiredValidBetsMoney.apply {
                    setMoneyFormat(data.validCheckAmount?.toLong()?.toDouble() ?: 0.0)
                    setTextColor(ContextCompat.getColor(context, if(data.validCheckAmount ?: 0.0 < 0.0) R.color.color_E44438_e44438 else R.color.color_BBBBBB_333333))
                }

                tvSuccessedBetsMoney.apply {
                    setMoneyFormat(data.finishValidAmount?.toLong()?.toDouble() ?: 0.0)
                    setTextColor(ContextCompat.getColor(context, if(data.finishValidAmount ?: 0.0 < 0.0) R.color.color_E44438_e44438 else R.color.color_BBBBBB_333333))
                }

                tvDeductMoney.apply {
                    setMoneyFormat(zero.minus(data.deductMoney ?: 0))
                    setTextColor(ContextCompat.getColor(context, if(zero.minus(data.deductMoney ?: 0) < 0.0) R.color.color_E44438_e44438 else R.color.color_BBBBBB_333333))
                }

                when(data.isPass){
                    1 ->{
                        tvCheckStatus.apply {
                            text = this.context.getString(R.string.commissiom_completed)
                            setTextColor(ContextCompat.getColor(context, R.color.color_08dc6e_08dc6e))
                        }
                    }
                    else ->{
                        tvCheckStatus.apply {
                            text = this.context.getString(R.string.commission_not_completed)
                            setTextColor(ContextCompat.getColor(context, R.color.color_E44438_e44438))
                        }
                    }
                }

                sConfigData?.systemCurrencySign.let {
                    tvVnd.text = it
                    tvRequiredValidBetsVnd.text = it
                    tvSuccessedBetsVnd.text = it
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> {
                CommissionItemType.HEADER.ordinal
            }
            else -> {
                CommissionItemType.ORIGINAL.ordinal
            }
        }
    }

    fun setData(newDataList: List<CheckList>?) {
        mDataList = newDataList?: listOf()
        notifyDataSetChanged()
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvId: TextView = itemView.findViewById(R.id.tv_id)
        var tvStatus: TextView = itemView.findViewById(R.id.tv_status)
        var tvVnd: TextView = itemView.findViewById(R.id.tv_vnd)
        var tvRequiredValidBetsVnd: TextView = itemView.findViewById(R.id.tv_required_valid_bets_vnd)
        var tvSuccessedBetsVnd: TextView = itemView.findViewById(R.id.tv_successed_bets_vnd)
        var tvDeductMoney: TextView = itemView.findViewById(R.id.tv_deduct_money)
        var tvTime: TextView = itemView.findViewById(R.id.tv_time)
        var tvRequiredValidBetsMoney: TextView = itemView.findViewById(R.id.tv_required_valid_bets_money)
        var tvSuccessedBetsMoney: TextView = itemView.findViewById(R.id.tv_successed_bets_money)
        var tvCheckStatus: TextView = itemView.findViewById(R.id.tv_check_status)
    }

}