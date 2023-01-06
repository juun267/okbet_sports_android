package org.cxct.sportlottery.ui.transactionStatus

import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_parlay_match.view.*
import kotlinx.android.synthetic.main.content_parlay_match.view.content_play
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.MatchOdd
import org.cxct.sportlottery.util.*

class ContentParlayMatchAdapter(val status: Int) :
    ListAdapter<MatchOdd, RecyclerView.ViewHolder>(ContentDiffCallBack()) {
    var gameType: String = ""
    var betConfirmTime: Long? = 0
    var matchType: String? = null
    fun setupMatchData(gameType: String, dataList: List<MatchOdd>, betConfirmTime: Long?, matchType: String?) {
        this.gameType = gameType
        this.betConfirmTime = betConfirmTime
        this.matchType = matchType
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
                holder.bind(gameType, data, position, betConfirmTime, status, matchType)
            }
        }
    }

    class ParlayMatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(viewGroup: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(viewGroup.context)
                val view = layoutInflater.inflate(R.layout.content_parlay_match, viewGroup, false)
                view.findViewById<TextView>(R.id.content_play).setCompoundDrawablesRelative(null, null, null, null)
                return ParlayMatchViewHolder(view)
            }
        }

        fun bind(gameType: String, data: MatchOdd, position: Int, betConfirmTime: Long?, status: Int, matchType: String?) {
            itemView.apply {
                topLine.isVisible = position != 0
//                content_play.text = "$gameTypeName ${data.playCateName}"
                //篮球 滚球 全场让分【欧洲盘】
                content_play.setGameType_MatchType_PlayCateName_OddsType(
                    gameType,
                    matchType,
                    data.playCateName,
                    data.oddsType
                )

//                tv_team_names.setTeamNames(15, data.homeName, data.awayName)
                title_team_name_parlay.setTeamsNameWithVS(data.homeName, data.awayName)

                parlay_play_content.setPlayContent(
                    data.playName,
                    data.spread,
                    TextUtil.formatForOdd(data.odds)
                )

                parlay_play_time.text = TimeUtil.timeFormat(data.startTime, TimeUtil.DM_HM_FORMAT)
                itemView.iv_country.setSvgDrawable(data.categoryIcon)
                content_league.text = data.leagueName
//                if (position == 0) {
//                    if(betConfirmTime?.toInt() != 0){
//                        val leftTime = betConfirmTime?.minus(TimeUtil.getNowTimeStamp())
//                        object : CountDownTimer(leftTime ?: 0, 1000) {
//
//                            override fun onTick(millisUntilFinished: Long) {
//                                tv_count_down_parley.text =
//                                    "${TimeUtil.longToSecond(millisUntilFinished)} ${context.getString(R.string.sec)}"
//                            }
//
//                            override fun onFinish() {
//                                tv_count_down_parley.text =
//                                    "0 ${context.getString(R.string.sec)}"
//                                if (status == BetRecordType.UNSETTLEMENT.code.firstOrNull()) {
//                                    tv_count_down_parley.visibility = View.GONE
//                                }
//                            }
//                        }.start()
//                    }else{
//                        tv_count_down_parley.visibility = View.GONE
//                    }
//                } else {
//                    tv_count_down_parley.visibility = View.GONE
//                }
//                if (data.rtScore?.isNotEmpty() == true) tv_score_parlay.text = "(${data.rtScore})"
            }
        }
    }
}