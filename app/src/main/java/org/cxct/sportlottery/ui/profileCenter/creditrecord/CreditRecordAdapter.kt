package org.cxct.sportlottery.ui.profileCenter.creditrecord

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_credit_record.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.user.credit.Row

class CreditRecordAdapter : RecyclerView.Adapter<CreditRecordAdapter.ItemViewHolder>() {

    var data = listOf<Row>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    class ItemViewHolder private constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        fun bind(item: Row) {
            itemView.apply {
                credit_record_time.text = item.period
                credit_record_amount_all.text = item.formatCreditBalance
                credit_record_amount_remain.text = item.formatBalance
                credit_record_amount_settle.apply {
                    text = when (item.status) {
                        SettleStatus.UN_SETTLE.status, SettleStatus.SETTLE.status -> {
                            item.formatReward
                        }
                        else -> {
                            context.getString(R.string.credit_record_status_unsettle)
                        }
                    }

                    setTextColor(
                        ContextCompat.getColor(
                            itemView.context, when (item.status) {
                                SettleStatus.UN_SETTLE.status, SettleStatus.SETTLE.status -> {
                                    when {
                                        (item.reward != null && item.reward > 0) -> R.color.color_08dc6e_08dc6e
                                        (item.reward != null && item.reward < 0) -> R.color.color_E44438_e44438
                                        else -> R.color.color_A3A3A3_666666
                                    }
                                }
                                else -> {
                                    R.color.color_317FFF_1463cf
                                }
                            }
                        )
                    )
                }
                credit_record_remark.text = item.remark ?: ""
            }
        }

        companion object {
            fun from(parent: ViewGroup): ItemViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.itemview_credit_record, parent, false)

                return ItemViewHolder(view)
            }
        }
    }
}