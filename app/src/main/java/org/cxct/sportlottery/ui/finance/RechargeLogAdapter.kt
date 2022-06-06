package org.cxct.sportlottery.ui.finance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.view_item_recharge_log.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.money.list.Row
import org.cxct.sportlottery.ui.finance.df.RechType
import org.cxct.sportlottery.ui.finance.df.Status
import org.cxct.sportlottery.util.Event
import org.cxct.sportlottery.util.setRecordStatus

class RechargeLogAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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

    var rechargeLogListener: RechargeLogListener? = null

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
                holder.bind(item, rechargeLogListener)
            }
            is NoDataViewHolder -> {
                holder.bind(isFinalPage, data.isNotEmpty())
            }
        }
    }

    override fun getItemCount(): Int = data.size + 1

    class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: Row, rechargeLogListener: RechargeLogListener?) {
            itemView.rech_log_date.text = item.rechDateStr
            itemView.rech_log_time.text = item.rechTimeStr
            itemView.rech_log_amount.text = item.displayMoney
            itemView.rech_log_type.text = when (item.rechType) {
                RechType.ONLINE_PAYMENT.type -> itemView.context.getString(R.string.recharge_channel_online)
                RechType.ADMIN_ADD_MONEY.type -> itemView.context.getString(R.string.recharge_channel_admin)
                RechType.CFT.type -> itemView.context.getString(R.string.recharge_channel_cft)
                RechType.WEIXIN.type -> itemView.context.getString(R.string.recharge_channel_weixin)
                RechType.ALIPAY.type -> itemView.context.getString(R.string.recharge_channel_alipay)
                RechType.BANK_TRANSFER.type -> itemView.context.getString(R.string.recharge_channel_bank)
                RechType.CRYPTO.type -> itemView.context.getString(R.string.recharge_channel_crypto)
                RechType.GCASH.type -> itemView.context.getString(R.string.recharge_channel_gcash)
                RechType.GRABPAY.type -> itemView.context.getString(R.string.recharge_channel_grabpay)
                RechType.PAYMAYA.type -> itemView.context.getString(R.string.recharge_channel_paymaya)
                else -> ""
            }
            itemView.rech_log_state.setRecordStatus(item.status)
            itemView.setOnClickListener {
                rechargeLogListener?.onClick(Event(item))
            }

            setupStateTextColor(item)
        }

        private fun setupStateTextColor(item: Row) {
            when (item.status) {
                Status.SUCCESS.code -> {
                    itemView.rech_log_state.setTextColor(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.color_08dc6e_08dc6e
                        )
                    )
                }

                Status.FAILED.code -> {
                    itemView.rech_log_state.setTextColor(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.color_E44438_e44438
                        )
                    )
                }

                else -> {
                    itemView.rech_log_state.setTextColor(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.color_909090_666666
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

class RechargeLogListener(val clickListener: (row: Event<Row>) -> Unit) {
    fun onClick(row: Event<Row>) = clickListener(row)
}