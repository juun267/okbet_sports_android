package org.cxct.sportlottery.ui.finance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.view_item_recharge_log.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.money.list.Row

class RechargeLogAdapter : RecyclerView.Adapter<RechargeLogAdapter.ViewHolder>() {

    var data = listOf<Row>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        holder.bind(item)
    }

    override fun getItemCount(): Int = data.size

    class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: Row) {
            itemView.rech_log_date.text = item.rechDateStr
            itemView.rech_log_time.text = item.rechTimeStr
            itemView.rech_log_amount.text = item.displayMoney
            itemView.rech_log_type.text = item.rechName
            itemView.rech_log_state.text = item.rechState
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.view_item_recharge_log, parent, false)

                return ViewHolder(view)
            }
        }
    }
}