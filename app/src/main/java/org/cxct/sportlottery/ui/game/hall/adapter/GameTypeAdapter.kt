package org.cxct.sportlottery.ui.game.hall.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_game_type2.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.game.data.Date

class GameTypeAdapter : RecyclerView.Adapter<GameTypeAdapter.ViewHolder>() {

    var data = listOf<Date>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var gameTypeListener: GameTypeListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        holder.bind(item, gameTypeListener)
    }

    override fun getItemCount(): Int = data.size

    class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: Date, gameTypeListener: GameTypeListener?) {
            itemView.game_type_item_text.text = item.display
            itemView.isSelected = item.isSelected
            itemView.setOnClickListener {
                gameTypeListener?.onClick(item)
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_game_type2, parent, false)

                return ViewHolder(view)
            }
        }
    }
}

class GameTypeListener(val clickListener: (date: Date) -> Unit) {
    fun onClick(date: Date) = clickListener(date)
}