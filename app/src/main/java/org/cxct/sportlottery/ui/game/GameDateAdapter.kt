package org.cxct.sportlottery.ui.game

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_game_date.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.game.data.Date

class GameDateAdapter(private val gameDateListener: GameDateListener) :
    RecyclerView.Adapter<GameDateAdapter.ViewHolder>() {

    var data = listOf<Date>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        holder.bind(item, gameDateListener)
    }

    override fun getItemCount(): Int = data.size

    class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: Date, gameDateListener: GameDateListener) {
            itemView.game_date.text = item.display
            itemView.isSelected = item.isSelected
            itemView.setOnClickListener {
                gameDateListener.onClick(item)
            }
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

class GameDateListener(val clickListener: (date: Date) -> Unit) {
    fun onClick(date: Date) = clickListener(date)
}
