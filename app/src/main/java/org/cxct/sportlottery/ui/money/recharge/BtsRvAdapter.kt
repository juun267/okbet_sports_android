package org.cxct.sportlottery.ui.money.recharge

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_listview_bank_card_tick.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.visible

class BtsRvAdapter(private val dataList: MutableList<SelectBank>, private val clickListener: BankAdapterListener): RecyclerView.Adapter<BtsRvAdapter.ViewHolder>(){

    var selectedPosition = 0

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: SelectBank) {
            if (item.bankIcon != null){
                itemView.iv_bank_icon.setImageResource(item.bankIcon ?: 0)
                itemView.iv_bank_icon.visible()
            } else{
                itemView.iv_bank_icon.setImageResource(android.R.color.transparent)
                itemView.iv_bank_icon.gone()
            }
            itemView.tv_bank_card.text = item.bankName ?: ""
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.item_listview_bank_card_tick, parent, false)
                return ViewHolder(view)
            }
        }
    }

    data class SelectBank(var bankName: String?, var bankIcon: Int?)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        holder.bind(item)

        if (holder.adapterPosition == selectedPosition){
            holder.itemView.ll_select_bank_card.setBackgroundColor(ContextCompat.getColor(holder.itemView.context,
                R.color.color_E8EFFD))
            holder.itemView.img_tick.visibility = View.VISIBLE
        }
        else{
            holder.itemView.ll_select_bank_card.setBackgroundColor(ContextCompat.getColor(holder.itemView.context,
                R.color.color_bbbbbb_ffffff))
            holder.itemView.img_tick.visibility = View.INVISIBLE
        }

        holder.itemView.ll_select_bank_card.setOnClickListener {
            if (selectedPosition != position) {
                selectedPosition = position
                notifyDataSetChanged()
                clickListener.onClick(item,position)
            }
        }
    }

    override fun getItemCount() = dataList.size

    class BankAdapterListener(val listener: (bankCard: SelectBank, position: Int) -> Unit) {
        fun onClick(bankCard: SelectBank, position: Int) =
            listener(bankCard, position)
    }
}