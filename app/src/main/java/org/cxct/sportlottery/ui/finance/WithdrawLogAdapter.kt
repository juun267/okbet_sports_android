package org.cxct.sportlottery.ui.finance

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.view_item_recharge_log.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.withdraw.list.Row
import org.cxct.sportlottery.ui.finance.df.CheckStatus
import org.cxct.sportlottery.ui.finance.df.UWType
import org.cxct.sportlottery.util.Event
import org.cxct.sportlottery.util.LocalUtils

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
        return ItemType.ITEM.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                val item = data[position]
                holder.bind(item, withdrawLogListener)
            }
            is NoDataViewHolder -> {
                holder.bind(isFinalPage, data.isNotEmpty())
                holder.itemView.isVisible = false
            }
        }
    }

    override fun getItemCount(): Int = data.size

    class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: Row, withdrawLogListener: WithdrawLogListener?) {

            itemView.apply {
                rech_log_date.text = item.withdrawDate
                rech_log_time.text = item.withdrawTime
                rech_log_amount.text = item.displayMoney
                rech_log_type.text = item.withdrawType
                //用于前端显示单单订单状态 1: 處理中 2:提款成功 3:提款失败 4：待投注站出款
                rech_log_state.text = when (item.orderState) {
                    1 -> LocalUtils.getString(R.string.log_state_processing)
                    2 -> LocalUtils.getString(R.string.L019)
                    3 -> LocalUtils.getString(R.string.N626)
                    4 -> LocalUtils.getString(R.string.N627)
                    else -> null
                }.apply {
                    Log.d("hjq", "rech_log_state=" + this)
                }
            }

            itemView.setOnClickListener {
                withdrawLogListener?.onClick(Event(item))
            }

            if (item.uwType == UWType.BETTING_STATION.type) {
                itemView.rech_log_type.setOnClickListener {
                    withdrawLogListener?.onBettingStationClick(item)
                }
            } else {
                itemView.rech_log_type.setOnClickListener(null)
            }

            setupStateTextColor(item)
        }

        private fun setupStateTextColor(item: Row) {
            when (item.checkStatus) {
                CheckStatus.PROCESSING.code -> {
                    itemView.rech_log_state.setTextColor(ContextCompat.getColor(itemView.context, R.color.color_909090_666666))
                }
                CheckStatus.PASS.code -> {
                    itemView.rech_log_state.setTextColor(ContextCompat.getColor(itemView.context, R.color.color_08dc6e_08dc6e))
                }

                CheckStatus.UN_PASS.code -> {
                    itemView.rech_log_state.setTextColor(ContextCompat.getColor(itemView.context, R.color.color_E44438_e44438))
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.view_item_recharge_log, parent, false)

                return ViewHolder(view)
            }
        }
    }
    //TODO 位置改动 这个后续要删除掉 暂时隐藏
    class NoDataViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(isFinalPage: Boolean, hasData: Boolean) {
            itemView.visibility = if (isFinalPage && hasData) {
                View.GONE
            } else {
                View.GONE
            }
        }

        companion object {
            fun from(parent: ViewGroup) = NoDataViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_footer_no_data, parent, false))
        }
    }
}

class WithdrawLogListener(val clickListener: (row: Event<Row>) -> Unit, val bettingStationClick: (row: Row) -> Unit) {
    fun onClick(row: Event<Row>) = clickListener(row)
    fun onBettingStationClick(row: Row) = bettingStationClick(row)
}