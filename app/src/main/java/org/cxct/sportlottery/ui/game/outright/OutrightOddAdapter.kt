package org.cxct.sportlottery.ui.game.outright

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_outright_odd.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.outright.odds.Winner


class OutrightOddAdapter : RecyclerView.Adapter<OutrightOddAdapter.ViewHolder>() {
    var data = listOf<Winner>()
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

        fun bind(item: Winner) {
            itemView.outright_name.text = item.spread
            itemView.outright_bet.text = item.odds.toString()
            itemView.outright_bet.setOnClickListener {
                it.isSelected = !it.isSelected
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_outright_odd, parent, false)

                return ViewHolder(view)
            }
        }
    }
}