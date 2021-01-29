package org.cxct.sportlottery.ui.finance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.view_item_recharge_log.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.withdraw.list.Row
import org.cxct.sportlottery.ui.finance.df.CheckStatus

class WithdrawLogAdapter : RecyclerView.Adapter<WithdrawLogAdapter.ViewHolder>() {

    var data = listOf<Row>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var withdrawLogListener: WithdrawLogListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        holder.bind(item, withdrawLogListener)
    }

    override fun getItemCount(): Int = data.size

    class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: Row, withdrawLogListener: WithdrawLogListener?) {
            itemView.rech_log_date.text = item.withdrawDate
            itemView.rech_log_time.text = item.withdrawTime
            itemView.rech_log_amount.text = item.displayMoney
            itemView.rech_log_type.text = item.withdrawType
            itemView.rech_log_state.text = item.withdrawState

            itemView.setOnClickListener {
                withdrawLogListener?.onClick(item)
            }

            setupStateTextColor(item)
        }

        private fun setupStateTextColor(item: Row) {
            when (item.checkStatus) {
                CheckStatus.PROCESSING.code -> {
                    itemView.rech_log_state.setTextColor(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.textGray
                        )
                    )
                }
                CheckStatus.PASS.code -> {
                    itemView.rech_log_state.setTextColor(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.green_blue
                        )
                    )
                }

                CheckStatus.UN_PASS.code -> {
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

class WithdrawLogListener(val clickListener: (row: Row) -> Unit) {
    fun onClick(row: Row) = clickListener(row)
}