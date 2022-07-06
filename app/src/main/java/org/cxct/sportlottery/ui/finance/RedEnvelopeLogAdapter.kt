package org.cxct.sportlottery.ui.finance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.view_item_recharge_log.view.*
import kotlinx.android.synthetic.main.view_item_recharge_log.view.rech_log_amount
import kotlinx.android.synthetic.main.view_item_recharge_log.view.rech_log_date
import kotlinx.android.synthetic.main.view_item_recharge_log.view.rech_log_time
import kotlinx.android.synthetic.main.view_item_recharge_log.view.rech_log_type
import kotlinx.android.synthetic.main.view_item_red_envelope_log.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.money.list.RedEnvelopeRow
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.util.Event

class RedEnvelopeLogAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class ItemType {
        ITEM, NO_DATA
    }

    var data = listOf<RedEnvelopeRow>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var isFinalPage: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var redEnvelopeLogListener: RedEnvelopeLogListener? = null

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
                holder.bind(item, redEnvelopeLogListener)
            }
            is NoDataViewHolder -> {
                holder.bind(isFinalPage, data.isNotEmpty())
            }
        }
    }

    override fun getItemCount(): Int = data.size + 1

    class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: RedEnvelopeRow, redEnvelopeLogListener: RedEnvelopeLogListener?) {

            itemView.apply {
                rech_log_date.text = item.rechDateStr
                rech_log_time.text = item.rechTimeStr
                rech_log_order_no.text = item.orderNo
                rech_log_amount.text = "${sConfigData?.systemCurrencySign} ${item.money}"
                rech_log_type.text = item.tranTypeDisplay
            }

            itemView.setOnClickListener {
                redEnvelopeLogListener?.onClick(Event(item))
            }
        }


        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.view_item_red_envelope_log, parent, false)

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
            fun from(parent: ViewGroup) = NoDataViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_footer_no_data, parent, false
                )
            )
        }
    }
}

class RedEnvelopeLogListener(val clickListener: (row: Event<RedEnvelopeRow>) -> Unit) {
    fun onClick(row: Event<RedEnvelopeRow>) = clickListener(row)

}