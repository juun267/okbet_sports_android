package org.cxct.sportlottery.ui.finance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.view_item_finance.view.*
import org.cxct.sportlottery.R

class FinanceRecordAdapter : RecyclerView.Adapter<FinanceRecordAdapter.ViewHolder>() {
    var data = listOf<Pair<String, Int>>()
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

    override fun getItemCount(): Int = data.size

    class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: Pair<String, Int>) {
            itemView.tv_name.text = item.first
            itemView.iv_icon.setImageResource(item.second)
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.view_item_finance, parent, false)

                return ViewHolder(view)
            }
        }
    }
}