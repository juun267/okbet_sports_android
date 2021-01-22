package org.cxct.sportlottery.ui.money.recharge

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.money.MoneyPayWayData
import org.cxct.sportlottery.util.MoneyManager

class MoneyBankTypeAdapter : RecyclerView.Adapter<MoneyBankTypeAdapter.ViewHolder>() {

    var data = mutableListOf<MoneyPayWayData>()
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

    override fun getItemCount() = data.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icBank: ImageView = itemView.findViewById(R.id.ic_bank)
        private val tvType: TextView = itemView.findViewById(R.id.tv_type)

        fun bind(item: MoneyPayWayData) {
            icBank.setImageResource(MoneyManager.getBankIcon(item.rechType))
            tvType.text = item.title
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.content_money_pay_type_rv, parent, false)
                return ViewHolder(view)
            }
        }
    }
}