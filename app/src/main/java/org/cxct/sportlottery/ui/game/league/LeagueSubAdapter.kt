package org.cxct.sportlottery.ui.game.league

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_sub_league.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.league.League

class LeagueSubAdapter(private val leagueSubListener: LeagueSubListener) : RecyclerView.Adapter<LeagueSubAdapter.ViewHolder>() {
    var data = listOf<League>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        holder.bind(item, leagueSubListener)
    }

    override fun getItemCount(): Int = data.size

    class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: League, leagueSubListener: LeagueSubListener) {
            itemView.sub_league_name.text = item.name
            itemView.sub_league_count.text = item.num.toString()

            itemView.item_sub_league.setOnClickListener {
                leagueSubListener.onClick(item)
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

    class LeagueSubListener(val clickListener: (item: League) -> Unit) {
        fun onClick(item: League) = clickListener(item)
    }
}