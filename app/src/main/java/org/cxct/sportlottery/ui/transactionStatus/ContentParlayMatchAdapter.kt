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
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.*

class ContentParlayMatchAdapter :
    ListAdapter<MatchOdd, RecyclerView.ViewHolder>(ContentDiffCallBack()) {
    var gameType: String = ""
    var oddsType: OddsType = OddsType.EU
    fun setupMatchData(gameType: String, oddsType: OddsType, dataList: List<MatchOdd>) {
        this.gameType = gameType
        this.oddsType = oddsType
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
                holder.bind(gameType, data, oddsType)
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

        fun bind(gameTypeName: String, data: MatchOdd, oddsType: OddsType) {
            itemView.apply {
                content_play.text = "$gameTypeName ${data.playCateName}"

                val odd = when (oddsType) {
                    OddsType.HK -> data.hkOdds
                    else -> data.odds
                }

                val oddsTypeStr = when (data.oddsType) {
                    OddsType.HK.code -> "(" + context.getString(OddsType.HK.res) + ")"
                    OddsType.MYS.code -> "(" + context.getString(OddsType.MYS.res) + ")"
                    OddsType.IDN.code -> "(" + context.getString(OddsType.IDN.res) + ")"
                    else -> "(" + context.getString(OddsType.EU.res) + ")"
                }

                parlay_play_content.setPlayContent(
                    data.playName,
                    data.spread,
                    TextUtil.formatForOdd(odd),
                    oddsTypeStr
                )

                parlay_play_time.text = TimeUtil.timeFormat(data.startTime, TimeUtil.YMD_HM_FORMAT)
                content_league.text = data.leagueName
                content_home_name.text = data.homeName
                content_away_name.text = data.awayName
                content_date.setDateNoYear(data.startTime)

            }
        }
    }
}