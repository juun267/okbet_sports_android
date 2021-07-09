package org.cxct.sportlottery.ui.transactionStatus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_parlay_match.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.MatchOdd

class ContentParlayMatchAdapter : ListAdapter<MatchOdd, RecyclerView.ViewHolder>(ContentDiffCallBack()) {
    var gameType: String = ""
    fun setupMatchData(gameType: String, dataList: List<MatchOdd>) {
        this.gameType = gameType
        submitList(dataList)
    }

    class ContentDiffCallBack : DiffUtil.ItemCallback<MatchOdd>() {
        override fun areItemsTheSame(oldItem: MatchOdd, newItem: MatchOdd): Boolean {
            return oldItem.oddsId == newItem.oddsId
        }

        override fun areContentsTheSame(oldItem: MatchOdd, newItem: MatchOdd): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ParlayMatchViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = getItem(holder.adapterPosition)
        when (holder) {
            is ParlayMatchViewHolder -> {
                holder.bind(gameType, data)
            }
        }
    }

    class ParlayMatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(viewGroup: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(viewGroup.context)
                val view = layoutInflater.inflate(R.layout.content_parlay_match, viewGroup, false)
                return ParlayMatchViewHolder(view)
            }
        }

        fun bind(gameTypeName: String, data: MatchOdd) {
            itemView.apply {
                content_play.text = "$gameTypeName ${data.playCateName}"
                content_league.text = data.leagueName
                content_home_name.text = data.homeName
                content_away_name.text = data.awayName
                content_spread.text = data.spread
                content_spread_team.text = data.playName
                content_odds.text = data.odds.toString() //TODO 盤口類型配置
            }
        }
    }
}