package org.cxct.sportlottery.ui.finance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.view_item_recharge_log.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.money.list.Row
import org.cxct.sportlottery.ui.finance.df.Status

class RechargeLogAdapter : RecyclerView.Adapter<RechargeLogAdapter.ViewHolder>() {

    var data = listOf<Row>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var rechargeLogListener: RechargeLogListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        holder.bind(item, rechargeLogListener)
    }

    override fun getItemCount(): Int = data.size

    class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: Row, rechargeLogListener: RechargeLogListener?) {
            itemView.rech_log_date.text = item.rechDateStr
            itemView.rech_log_time.text = item.rechTimeStr
            itemView.rech_log_amount.text = item.displayMoney
            itemView.rech_log_type.text = item.rechName
            itemView.rech_log_state.text = item.rechState
            itemView.setOnClickListener {
                rechargeLogListener?.onClick(item)
            }

            setupStateTextColor(item)
        }

        private fun setupStateTextColor(item: Row) {
            when (item.status) {
                Status.PROCESSING.code -> {
                    itemView.rech_log_state.setTextColor(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.textGray
                        )
                    )
                }
                Status.SUCCESS.code -> {
                    itemView.rech_log_state.setTextColor(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.green_blue
                        )
                    )
                }

                Status.FAILED.code -> {
                    itemView.rech_log_state.setTextColor(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.orangeRed
                        )
                    )
                }
            }

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

class RechargeLogListener(val clickListener: (row: Row) -> Unit) {
    fun onClick(row: Row) = clickListener(row)
}