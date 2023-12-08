package org.cxct.sportlottery.ui.finance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.view_item_account_history.view.*
import kotlinx.android.synthetic.main.view_item_recharge_log.view.rech_log_date
import kotlinx.android.synthetic.main.view_item_recharge_log.view.rech_log_time
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.money.list.SportBillResult
import org.cxct.sportlottery.ui.finance.df.AccountHistory
import org.cxct.sportlottery.util.TextUtil

class AccountHistoryAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class ItemType {
        ITEM, NO_DATA
    }

    var data = listOf<SportBillResult.Row>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var isFinalPage: Boolean = false
        set(value) {
            field = value
            notifyItemChanged(data.size)
        }
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
                holder.bind(item)
            }
           /* is NoDataViewHolder -> {
                holder.bind(isFinalPage, data.isNotEmpty())
            }*/
        }
    }

    override fun getItemCount(): Int = data.size + 1

    class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: SportBillResult.Row) {
            itemView.rech_log_date.text = item.rechDateStr
            itemView.rech_log_time.text = item.rechTimeStr
            itemView.rech_order_num.text = item.orderNo
            itemView.tvTypeName.text = itemView.context.getString(AccountHistory.getShowName(item.tranTypeName))
            itemView.rech_balance.text = TextUtil.format(item.balance)
            if(item.money<0){
                itemView.rech_amont.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.color_E44438_e44438
                    )
                )
                itemView.rech_amont.text =TextUtil.format(item.money)
            }else if(item.money>0){
                itemView.rech_amont.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.color_08dc6e_08dc6e
                    )
                )
                itemView.rech_amont.text = "+"+TextUtil.format(item.money)
            }else{
                itemView.rech_amont.text = TextUtil.format(item.money)
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.view_item_account_history, parent, false)

                return ViewHolder(view)
            }
        }
    }

    class NoDataViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(isFinalPage: Boolean, hasData: Boolean) {
            itemView.visibility = if (isFinalPage && hasData) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        companion object {
            fun from(parent: ViewGroup) =
                NoDataViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_footer_no_data, parent, false)
                )
        }
    }
}
