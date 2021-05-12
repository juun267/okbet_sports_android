package org.cxct.sportlottery.ui.odds

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_odds_detail_game_card.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.SportType
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.service.match_clock.MatchClockCO
import org.cxct.sportlottery.network.service.match_status_change.MatchStatusCO
import org.cxct.sportlottery.util.TimeUtil
import java.util.*

class OddsGameCardAdapter(
    private val context:Context?,
    private var matchId: String?,
    private val clickListener: ItemClickListener
) :
    RecyclerView.Adapter<OddsGameCardAdapter.ViewHolder>() {
    private var mSelectedPosition = -1
    private var mTimerMap = mutableMapOf<Int, Timer?>()

    var matchClockCOList: MutableList<MatchClockCO> = mutableListOf()
    var matchStatusCOList: MutableList<MatchStatusCO> = mutableListOf()


    var data: MutableList<MatchInfo?> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
            matchClockCOList = MutableList(data.size) { MatchClockCO(0, "", "", 0, 0, 0, 0, 0, 0, 0) }
            matchStatusCOList = MutableList(data.size) { MatchStatusCO("", 0, 0, 0, 0, 0, "", 0, 0, 0) }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater
            .inflate(R.layout.content_odds_detail_game_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        item?.let { holder.bind(it, position) }
        holder.itemView.isSelected = mSelectedPosition == position

        if (mSelectedPosition == -1)//首次進入用ID判斷
            holder.itemView.isSelected = matchId == item?.id

        holder.gameCard.setOnClickListener {
            if (position != mSelectedPosition) {
                mSelectedPosition = position
                stopAllTimer()
                mTimerMap = mutableMapOf()
                notifyDataSetChanged()

                item?.let { it -> clickListener.onClick(it) }
            }
        }
    }

    override fun getItemCount(): Int = data.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var timer: Timer? = null

        private val homeScore: TextView = itemView.findViewById(R.id.txv_home_score)
        private val homeName: TextView = itemView.findViewById(R.id.txv_home_name)

        private val awayScore: TextView = itemView.findViewById(R.id.txv_away_score)
        private val awayName: TextView = itemView.findViewById(R.id.txv_away_name)

        private val txvTime: TextView = itemView.findViewById(R.id.txv_time)
        private val txvStatus: TextView = itemView.findViewById(R.id.txv_status)
        val gameCard: ConstraintLayout = itemView.findViewById(R.id.ll_game_card)


        fun bind(item: MatchInfo, position: Int) {
            homeScore.text = (item.homeScore ?: 0).toString()
            homeName.text = item.homeName

            awayScore.text = (item.awayScore ?: 0).toString()
            awayName.text = item.awayName

            txvStatus.text =  getGameStatus(matchStatusCOList[position])

            setTimer(position, matchClockCOList[position])
        }

        fun stopTimer() {
            timer?.cancel()
        }

        private fun setTimer(position: Int, matchClockCO: MatchClockCO?) {
            mTimerMap[position]?.cancel()
            mTimerMap[position] = null
            stopTimer()
            if (matchClockCO?.stopped == 0) {//是否计时停止 1:是 ，0：否
                when (matchClockCO.gameType) {
                    "BK" -> {
                        if (matchClockCO.remainingTimeInPeriod == null) {
                            itemView.txv_time.text = null
                        } else {
                            itemView.txv_time.text = TimeUtil.timeFormat(
                                matchClockCO.remainingTimeInPeriod?.times(1000L),
                                "mm:ss"
                            )

                            timer = Timer()
                            timer?.schedule(object : TimerTask() {
                                override fun run() {
                                    Handler(Looper.getMainLooper()).post {
                                        matchClockCO.remainingTimeInPeriod =
                                            matchClockCO.remainingTimeInPeriod?.minus(1)
                                        itemView.txv_time.text =
                                            TimeUtil.timeFormat(
                                                matchClockCO.remainingTimeInPeriod?.times(
                                                    1000L
                                                ), "mm:ss"
                                            )
                                    }
                                }
                            }, 1000L, 1000L)
                        }
                    }
                    "FT" -> {
                        itemView.txv_time.text =
                            TimeUtil.timeFormat(matchClockCO.matchTime?.times(1000L), "mm:ss")

                        timer = Timer()
                        timer?.schedule(object : TimerTask() {
                            override fun run() {
                                Handler(Looper.getMainLooper()).post {
                                    matchClockCO.matchTime = matchClockCO.matchTime?.plus(1)
                                    itemView.txv_time.text =
                                        TimeUtil.timeFormat(
                                            matchClockCO.matchTime?.times(1000L),
                                            "mm:ss"
                                        )
                                }
                            }
                        }, 1000L, 1000L)
                    }
                    else -> {

                    }
                }
                mTimerMap[adapterPosition] = timer

            } else {
                itemView.txv_time.text = ""
            }
        }

        fun getGameStatus(matchStatusCO: MatchStatusCO?): String {
            var status = ""

            when (matchStatusCO?.gameType) {
                SportType.FOOTBALL.code -> {
                    when (matchStatusCO.status) {
                        6 -> {
                            status = context?.getString(R.string.first_half_game).toString()
                        }
                        7 -> {
                            status = context?.getString(R.string.second_half_game).toString()
                        }
                        41 -> {
                            status = context?.getString(R.string.add_time_first_plat).toString()
                        }
                        42 -> {
                            status = context?.getString(R.string.add_time_second_plat).toString()
                        }
                    }
                }
                SportType.BASKETBALL.code -> {
                    when (matchStatusCO.status) {
                        6 -> {
                            status = context?.getString(R.string.first_half_game).toString()
                        }
                        7 -> {
                            status = context?.getString(R.string.second_half_game).toString()
                        }
                        13 -> {
                            status = context?.getString(R.string.first_session).toString()
                        }
                        14 -> {
                            status = context?.getString(R.string.second_session).toString()
                        }
                        15 -> {
                            status = context?.getString(R.string.third_session).toString()
                        }
                        16 -> {
                            status = context?.getString(R.string.fourth_session).toString()
                        }
                        40 -> {
                            status = context?.getString(R.string.add_time).toString()
                        }
                    }
                }
                SportType.TENNIS.code, SportType.VOLLEYBALL.code, SportType.BADMINTON.code -> {
                    when (matchStatusCO.status) {
                        8 -> {
                            status = context?.getString(R.string.first_plat).toString()
                        }
                        9 -> {
                            status = context?.getString(R.string.second_plat).toString()
                        }
                        10 -> {
                            status = context?.getString(R.string.third_plat).toString()
                        }
                        11 -> {
                            status = context?.getString(R.string.fourth_plat).toString()
                        }
                        12 -> {
                            status = context?.getString(R.string.fifth_plat).toString()
                        }
                    }
                }
            }

            return status
        }

    }

    private fun stopAllTimer() {
        mTimerMap.forEach {
            val timer = it.value
            timer?.cancel()
        }
    }

    class ItemClickListener(private val clickListener: (oddsData: MatchInfo) -> Unit) {
        fun onClick(oddsData: MatchInfo) = clickListener(oddsData)
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        holder.stopTimer()
    }

    fun updateGameCard(matchClockCO: MatchClockCO?) {

        data.forEachIndexed { index, matchInfo ->
            if (matchInfo?.id == matchClockCO?.matchId) {
                matchClockCO?.let {
                    matchClockCOList[index] = matchClockCO
                }
                notifyItemChanged(index)
            }

        }
    }

    fun updateGameCard(matchStatusCO: MatchStatusCO?) {
        data.forEachIndexed { index, matchInfo ->
            if (matchInfo?.id == matchStatusCO?.matchId) {
                matchStatusCO?.let {
                    data[index]?.homeScore = it.homeScore
                    data[index]?.awayScore = it.awayScore
                    matchStatusCOList[index] = matchStatusCO

                }
                notifyItemChanged(index)
            }
        }
    }

}