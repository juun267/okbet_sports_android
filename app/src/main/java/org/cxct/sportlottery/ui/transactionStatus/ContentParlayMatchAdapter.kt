package org.cxct.sportlottery.ui.transactionStatus

import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_parlay_match.view.*
import kotlinx.android.synthetic.main.content_parlay_match.view.content_play
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.MatchOdd
import org.cxct.sportlottery.ui.game.BetRecordType
import org.cxct.sportlottery.util.*

class ContentParlayMatchAdapter(val status: Int) :
    ListAdapter<MatchOdd, RecyclerView.ViewHolder>(ContentDiffCallBack()) {
    var gameType: String = ""
    var betConfirmTime: Long? = 0
    fun setupMatchData(gameType: String, dataList: List<MatchOdd>, betConfirmTime: Long?) {
        this.gameType = gameType
        this.betConfirmTime = betConfirmTime
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
                holder.bind(gameType, data, position, betConfirmTime, status)
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

        fun bind(gameTypeName: String, data: MatchOdd, position: Int, betConfirmTime: Long?, status: Int) {
            itemView.apply {
                content_play.text = "$gameTypeName ${data.playCateName}"

//                val oddsTypeStr = when (data.oddsType) {
//                    OddsType.HK.code -> "(" + context.getString(OddsType.HK.res) + ")"
//                    OddsType.MYS.code -> "(" + context.getString(OddsType.MYS.res) + ")"
//                    OddsType.IDN.code -> "(" + context.getString(OddsType.IDN.res) + ")"
//                    else -> "(" + context.getString(OddsType.EU.res) + ")"
//                }

                parlay_play_content.setPlayContent(
                    data.playName,
                    data.spread,
                    TextUtil.formatForOdd(data.odds)
                )

                parlay_play_time.text = TimeUtil.timeFormat(data.startTime, TimeUtil.YMD_HM_FORMAT)
                content_league.text = data.leagueName
                content_home_name.text = data.homeName
                content_away_name.text = data.awayName
//                content_date.setDateNoYear(data.startTime)
                content_date.visibility = View.GONE
                if (position == 0) {
                    if(betConfirmTime?.toInt() != 0){
                        val leftTime = betConfirmTime?.minus(TimeUtil.getNowTimeStamp())
                        object : CountDownTimer(leftTime ?: 0, 1000) {

                            override fun onTick(millisUntilFinished: Long) {
                                tv_count_down_parley.text =
                                    "${TimeUtil.longToSecond(millisUntilFinished)} ${context.getString(R.string.sec)}"
                            }

                            override fun onFinish() {
                                tv_count_down_parley.text =
                                    "0 ${context.getString(R.string.sec)}"
                                if (status == BetRecordType.UNSETTLEMENT.code.firstOrNull()) {
                                    tv_count_down_parley.visibility = View.GONE
                                }
                            }
                        }.start()
                    }else{
                        tv_count_down_parley.visibility = View.GONE
                    }
                } else {
                    tv_count_down_parley.visibility = View.GONE
                }
                if (data.rtScore?.isNotEmpty() == true)
                    tv_score_parlay.text = String.format(
                        context.getString(R.string.brackets),
                        data.rtScore
                    )

            }
        }
    }
}