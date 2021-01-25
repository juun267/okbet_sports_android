package org.cxct.sportlottery.ui.money.recharge

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.infoCenter.InfoCenterData
import org.cxct.sportlottery.network.money.MoneyPayWayData
import org.cxct.sportlottery.ui.infoCenter.InfoCenterAdapter
import org.cxct.sportlottery.util.MoneyManager

class MoneyBankTypeAdapter(private val clickListener: ItemClickListener) : RecyclerView.Adapter<MoneyBankTypeAdapter.ViewHolder>() {

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
        holder.bind(item,clickListener)
    }

    override fun getItemCount() = data.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icBank: ImageView = itemView.findViewById(R.id.ic_bank)
        private val tvType: TextView = itemView.findViewById(R.id.tv_type)
        private val rootItem: ConstraintLayout =itemView.findViewById(R.id.rootItem)

        fun bind(item: MoneyPayWayData, clickListener: ItemClickListener) {
            icBank.setImageResource(MoneyManager.getBankIcon(item.image))
            tvType.text = item.title
            rootItem.setOnClickListener {
                clickListener.onClick(item)
            }
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

    class ItemClickListener(private val clickListener: (moneyPayWayData: MoneyPayWayData) -> Unit) {
        fun onClick(moneyPayWayData: MoneyPayWayData) = clickListener(moneyPayWayData)
    }
}