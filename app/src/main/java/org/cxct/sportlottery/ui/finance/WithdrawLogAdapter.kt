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

class WithdrawLogAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class ItemType {
        ITEM, NO_DATA
    }

    var data = listOf<Row>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var isFinalPage: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var withdrawLogListener: WithdrawLogListener? = null

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            (data.size) -> {
                ItemType.NO_DATA.ordinal
            }
            else -> ItemType.ITEM.ordinal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.NO_DATA.ordinal -> NoDataViewHolder.from(parent)
            else -> ViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                val item = data[position]
                holder.bind(item, withdrawLogListener)
            }
            is NoDataViewHolder -> {
                holder.bind(isFinalPage)
            }
        }
    }

    override fun getItemCount(): Int = data.size + 1

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

    class NoDataViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(isFinalPage: Boolean) {
            itemView.visibility = if (isFinalPage) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        companion object {
            fun from(parent: ViewGroup) =
                NoDataViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_finance_no_data, parent, false)
                )
        }
    }
}

class WithdrawLogListener(val clickListener: (row: Row) -> Unit) {
    fun onClick(row: Row) = clickListener(row)
}