package org.cxct.sportlottery.ui.game

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_game_type.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.SportType
import org.cxct.sportlottery.network.sport.Sport

class GameTypeAdapter(private val gameTypeListener: GameTypeListener) :
    RecyclerView.Adapter<GameTypeAdapter.ViewHolder>() {
    var data = listOf<Sport>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        holder.bind(item, gameTypeListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: Sport, gameTypeListener: GameTypeListener) {
            itemView.type_game_count.text = item.num.toString()

            itemView.type_game_text.text = item.name

            when (item.code) {
                SportType.FOOTBALL.code -> {
                    itemView.type_game_img.setImageResource(R.drawable.selector_gametype_row_ft)
                }
                SportType.BASKETBALL.code -> {
                    itemView.type_game_img.setImageResource(R.drawable.selector_gametype_row_bk)
                }
                SportType.BADMINTON.code -> {
                    itemView.type_game_img.setImageResource(R.drawable.selector_gametype_row_bm)
                }
                SportType.TENNIS.code -> {
                    itemView.type_game_img.setImageResource(R.drawable.selector_gametype_row_tn)
                }
                SportType.VOLLEYBALL.code -> {
                    itemView.type_game_img.setImageResource(R.drawable.selector_gametype_row_vb)
                }
            }

            itemView.isSelected = item.isSelected

            itemView.setOnClickListener {
                gameTypeListener.onClick(item)
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_game_type, parent, false)

                return ViewHolder(view)
            }
        }
    }
}

class GameTypeListener(val clickListener: (sport: Sport) -> Unit) {
    fun onClick(sport: Sport) = clickListener(sport)
}