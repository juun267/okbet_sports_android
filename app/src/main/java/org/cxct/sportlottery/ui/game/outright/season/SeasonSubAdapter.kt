package org.cxct.sportlottery.ui.game.outright.season

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_sub_league.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.outright.season.Season

class SeasonSubAdapter(private val seasonSubListener: SeasonSubListener?) :
    RecyclerView.Adapter<SeasonSubAdapter.ViewHolder>() {
    var data = listOf<Season>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        holder.bind(item, seasonSubListener)
    }

    override fun getItemCount(): Int = data.size

    class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: Season, seasonSubListener: SeasonSubListener?) {
            itemView.sub_league_name.text = item.name
            itemView.sub_league_count.text = item.num.toString()

            itemView.setOnClickListener {
                seasonSubListener?.onClick(item)
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_sub_league, parent, false)

                return ViewHolder(view)
            }
        }
    }

    class SeasonSubListener(val clickListener: (item: Season) -> Unit) {
        fun onClick(item: Season) = clickListener(item)
    }
}