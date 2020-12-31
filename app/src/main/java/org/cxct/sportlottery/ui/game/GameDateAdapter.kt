package org.cxct.sportlottery.ui.game

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_game_date.view.*
import org.cxct.sportlottery.R

class GameDateAdapter : RecyclerView.Adapter<GameDateAdapter.ViewHolder>() {

    var data = listOf<Pair<String, Boolean>>()
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

        fun bind(item: Pair<String, Boolean>) {
            itemView.game_date.text = item.first
            itemView.isSelected = item.second
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_game_date, parent, false)

                return ViewHolder(view)
            }
        }
    }
}
