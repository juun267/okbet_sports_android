package org.cxct.sportlottery.ui.maintab.betdetails

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_bet_detail_match.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.MatchOdd
import org.cxct.sportlottery.network.bet.settledDetailList.MatchOddsVO
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.widget.onClick

class ContentParlayDetailAdapter(val status: Int) :
    ListAdapter<MatchOddsVO, RecyclerView.ViewHolder>(ContentDiffCallBack()) {
    var gameType: String = ""
    var matchType: String? = null
    fun setupMatchData(gameType: String, dataList: List<MatchOddsVO>,  matchType: String?) {
        this.gameType = gameType
        this.matchType = matchType
        submitList(dataList)
    }

    class ContentDiffCallBack : DiffUtil.ItemCallback<MatchOddsVO>() {
        override fun areItemsTheSame(oldItem: MatchOddsVO, newItem: MatchOddsVO): Boolean {
            return oldItem.oddsId == newItem.oddsId
        }

        override fun areContentsTheSame(oldItem: MatchOddsVO, newItem: MatchOddsVO): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ParlayMatchViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = getItem(holder.bindingAdapterPosition)
        when (holder) {
            is ParlayMatchViewHolder -> {
                holder.bind(gameType, data, position,  status, matchType)
            }
        }
    }

    class ParlayMatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(viewGroup: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(viewGroup.context)
                val view = layoutInflater.inflate(R.layout.item_bet_detail_match, viewGroup, false)
                return ParlayMatchViewHolder(view)
            }
        }

        fun bind(gameType: String, data: MatchOddsVO, position: Int, status: Int, matchType: String?) {
            itemView.apply {
                topLine.isVisible = position != 0
                //篮球 滚球 全场让分【欧洲盘】
                content_play.setGameType_MatchType_PlayCateName_OddsType(
                    gameType,
                    matchType,
                    data.playCateName,
                    data.oddsType
                )

                title_team_name_parlay.setTeamsNameWithVS(data.homeName, data.awayName)

                parlay_play_content.setPlayContent(
                    data.playName,
                    data.spread,
                    TextUtil.formatForOdd(data.odds)
                )

                parlay_play_time.text = TimeUtil.timeFormat(data.startTime, TimeUtil.DM_HM_FORMAT)
                itemView.iv_country.setSvgDrawable(data.categoryIcon)
                content_league.text = data.leagueName
            }
        }
    }
}