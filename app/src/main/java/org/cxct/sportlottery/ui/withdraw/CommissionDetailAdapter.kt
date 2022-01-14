package org.cxct.sportlottery.ui.withdraw

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.withdraw.uwcheck.CheckList
import org.cxct.sportlottery.util.ArithUtil
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.TimeUtil

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
                tvDeductMoney.text = TextUtil.formatCommissionMoney(zero.minus(data.deductMoney ?: 0))
                tvTime.text = TimeUtil.stampToDateHMS(data.addTime?.toLong() ?: 0)

                tvRequiredValidBetsMoney.apply {
                    text = TextUtil.formatCommissionMoney(data.validCheckAmount?.toLong()?.toDouble() ?: 0)
                    setTextColor(ContextCompat.getColor(context, if(data.validCheckAmount ?: 0.0 < 0.0) R.color.colorRed else R.color.colorBlackLight))
                }

                tvSuccessedBetsMoney.apply {
                    text = TextUtil.formatCommissionMoney(data.finishValidAmount?.toLong()?.toDouble() ?: 0)
                    setTextColor(ContextCompat.getColor(context, if(data.finishValidAmount ?: 0.0 < 0.0) R.color.colorRed else R.color.colorBlackLight))
                }

                tvDeductMoney.apply {
                    text = TextUtil.formatCommissionMoney(zero.minus(data.deductMoney ?: 0))
                    setTextColor(ContextCompat.getColor(context, if(zero.minus(data.deductMoney ?: 0) < 0.0) R.color.colorRed else R.color.colorBlackLight))
                }

                when(data.isPass){
                    1 ->{
                        tvCheckStatus.apply {
                            text = this.context.getString(R.string.commissiom_completed)
                            setTextColor(ContextCompat.getColor(context, R.color.colorGreen))
                        }
                    }
                    else ->{
                        tvCheckStatus.apply {
                            text = this.context.getString(R.string.commission_not_completed)
                            setTextColor(ContextCompat.getColor(context, R.color.colorRed))
                        }
                    }
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
        var tvDeductMoney: TextView = itemView.findViewById(R.id.tv_deduct_money)
        var tvTime: TextView = itemView.findViewById(R.id.tv_time)
        var tvRequiredValidBetsMoney: TextView = itemView.findViewById(R.id.tv_required_valid_bets_money)
        var tvSuccessedBetsMoney: TextView = itemView.findViewById(R.id.tv_successed_bets_money)
        var tvCheckStatus: TextView = itemView.findViewById(R.id.tv_check_status)
    }

}