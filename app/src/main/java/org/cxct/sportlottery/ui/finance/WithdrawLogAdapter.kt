package org.cxct.sportlottery.ui.finance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_withdraw_log.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.withdraw.list.Row
import org.cxct.sportlottery.ui.finance.df.OrderState
import org.cxct.sportlottery.util.Event
import org.cxct.sportlottery.util.LocalUtils
import org.cxct.sportlottery.util.TextUtil

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
                tv_order_number.text = item.orderNo
                rech_log_amount.text = item.displayMoney
                tv_receive_amount.text = TextUtil.formatMoney(item.actualMoney ?: 0.0)
                //用于前端显示单单订单状态 1: 處理中 2:提款成功 3:提款失败 4：待投注站出款
                rech_log_state.apply {
                    when (item.orderState) {
                        OrderState.PROCESSING.code -> {
                            text = LocalUtils.getString(R.string.log_state_processing)
                            setTextColor(ContextCompat.getColor(itemView.context,
                                R.color.color_414655))
                        }
                        OrderState.SUCCESS.code -> {
                            text = LocalUtils.getString(R.string.L019)
                            setTextColor(ContextCompat.getColor(itemView.context,
                                R.color.color_1EB65B))
                        }
                        OrderState.FAILED.code -> {
                            text = LocalUtils.getString(R.string.N626)
                            setTextColor(ContextCompat.getColor(itemView.context,
                                R.color.color_E23434))
                        }
                        OrderState.PENGING.code -> {
                            text = LocalUtils.getString(R.string.N653)
                            setTextColor(ContextCompat.getColor(itemView.context,
                                R.color.color_414655))
                        }
                    }
                }
            }

            itemView.setOnClickListener {
                withdrawLogListener?.onClick(Event(item))
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.item_withdraw_log, parent, false)

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

class WithdrawLogListener(val clickListener: (row: Event<Row>) -> Unit) {
    fun onClick(row: Event<Row>) = clickListener(row)
}