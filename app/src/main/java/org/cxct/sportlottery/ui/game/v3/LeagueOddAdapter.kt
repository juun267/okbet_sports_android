package org.cxct.sportlottery.ui.game.v3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_game_league_odd_1x2.view.*
import kotlinx.android.synthetic.main.itemview_game_league_odd_hdp_ou.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.PlayType
import org.cxct.sportlottery.network.odds.list.MatchOdd

class LeagueOddAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var data = listOf<MatchOdd>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var playType: PlayType = PlayType.OU_HDP
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var leagueOddListener: LeagueOddListener? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (playType) {
            PlayType.OU_HDP -> ViewHolderHdpOu.from(parent)
            else -> ViewHolder1x2.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = data[position]

        when (holder) {
            is ViewHolderHdpOu -> holder.bind(item, leagueOddListener)
            is ViewHolder1x2 -> holder.bind(item, leagueOddListener)
        }
    }

    override fun getItemCount(): Int = data.size


    class ViewHolderHdpOu private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: MatchOdd, leagueOddListener: LeagueOddListener?) {
            itemView.game_name_home.text = item.matchInfo?.homeName
            itemView.game_name_away.text = item.matchInfo?.awayName

            itemView.match_live.setOnClickListener {
                leagueOddListener?.onClickLive(item)
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolderHdpOu {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_game_league_odd_hdp_ou, parent, false)

                return ViewHolderHdpOu(view)
            }
        }
    }

    class ViewHolder1x2 private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: MatchOdd, leagueOddListener: LeagueOddListener?) {
            itemView.game_name_home_1x2.text = item.matchInfo?.homeName
            itemView.game_name_away_1x2.text = item.matchInfo?.awayName

            itemView.match_live_1x2.setOnClickListener {
                leagueOddListener?.onClickLive(item)
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder1x2 {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_game_league_odd_1x2, parent, false)

                return ViewHolder1x2(view)
            }
        }
    }
}

class LeagueOddListener(val clickListener: (item: MatchOdd) -> Unit) {
    fun onClickLive(item: MatchOdd) = clickListener(item)
}