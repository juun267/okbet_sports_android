package org.cxct.sportlottery.ui.game.v3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_game_match_odd.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.list.MatchOdd

class LeagueOddAdapter : RecyclerView.Adapter<LeagueOddAdapter.ViewHolder>() {

    var data = listOf<MatchOdd>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var leagueOddListener: LeagueOddListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        holder.bind(item, leagueOddListener)
    }

    override fun getItemCount(): Int = data.size

    class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: MatchOdd, leagueOddListener: LeagueOddListener?) {
            itemView.game_name_home.text = item.matchInfo?.homeName
            itemView.game_name_away.text = item.matchInfo?.awayName
            itemView.match_live.setOnClickListener {
                leagueOddListener?.onClickLive(item)
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_game_match_odd, parent, false)

                return ViewHolder(view)
            }
        }
    }
}

class LeagueOddListener(val clickListener: (item: MatchOdd) -> Unit) {
    fun onClickLive(item: MatchOdd) = clickListener(item)
}