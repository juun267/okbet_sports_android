package org.cxct.sportlottery.ui.game.v3

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.button_odd.view.*
import kotlinx.android.synthetic.main.itemview_game_league_odd_1x2.view.*
import kotlinx.android.synthetic.main.itemview_game_league_odd_hdp_ou.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.OUType
import org.cxct.sportlottery.network.common.PlayType
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.odds.list.Odd
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.TimeUtil
import java.util.*

class LeagueOddAdapter(private val matchType: MatchType) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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

            is ViewHolderHdpOu -> holder.bind(matchType, item, data as MutableList<MatchOdd>,leagueOddListener)
            is ViewHolder1x2 -> holder.bind(matchType, item,data as MutableList<MatchOdd>,leagueOddListener)
        }
    }

    override fun getItemCount(): Int = data.size

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)

        when (holder) {
            is ViewHolderTimer -> holder.stopTimer()
        }
    }

    class ViewHolderHdpOu private constructor(itemView: View) : ViewHolderTimer(itemView) {
        fun bind(matchType: MatchType, item: MatchOdd,gameCardList:MutableList<MatchOdd>, leagueOddListener: LeagueOddListener?) {

            setupMatchInfo(item, matchType)

            setupOddButton(item, leagueOddListener)

            itemView.match_live.setOnClickListener {
                leagueOddListener?.onClickLive(item,gameCardList)
            }
        }

        private fun setupMatchInfo(item: MatchOdd, matchType: MatchType) {
            itemView.match_play_type_count.text = item.matchInfo?.playCateNum.toString()

            itemView.game_name_home.text = item.matchInfo?.homeName
            itemView.game_name_away.text = item.matchInfo?.awayName

            if (matchType == MatchType.IN_PLAY) {
                itemView.game_score_home.text = (item.matchInfo?.homeScore ?: 0).toString()
                itemView.game_score_away.text = (item.matchInfo?.awayScore ?: 0).toString()
                itemView.match_status.text = item.matchInfo?.statusName

                listener = object : TimerListener {
                    override fun onTimerUpdate(timeMillis: Long) {
                        itemView.match_time.text = TimeUtil.timeFormat(timeMillis, "mm:ss")
                    }
                }

                updateTimer(item.leagueTime ?: 0)
            } else {
                itemView.match_status.text = item.matchInfo?.startDateDisplay
                itemView.match_time.text = item.matchInfo?.startTimeDisplay
            }
        }

        private fun setupOddButton(item: MatchOdd, leagueOddListener: LeagueOddListener?) {
            val oddListHDP = item.odds[PlayType.HDP.code]
            val oddListOU = item.odds[PlayType.OU.code]

            itemView.match_odd_hdp_home.apply {

                playType = PlayType.HDP

                visibility = if (oddListHDP == null || oddListHDP.size < 2) {
                    View.INVISIBLE
                } else {
                    View.VISIBLE
                }

                isSelected = oddListHDP?.get(0)?.isSelected ?: false

                betStatus = oddListHDP?.get(0)?.status

                oddStatus = oddListHDP?.get(0)?.oddState

                odd_hdp_top_text.text = if (oddListHDP == null || oddListHDP.size < 2) {
                    ""
                } else {
                    oddListHDP[0]?.spread
                }

                odd_hdp_bottom_text.text = if (oddListHDP == null || oddListHDP.size < 2) {
                    ""
                } else {
                    oddListHDP[0]?.odds?.let { TextUtil.formatForOdd(it) }
                }

                setOnClickListener {
                    if (oddListHDP != null && oddListHDP.size >= 2) {
                        oddListHDP[0]?.let { odd ->
                            leagueOddListener?.onClickBet(item, PlayType.HDP.code, odd)
                        }
                    }
                }
            }

            itemView.match_odd_hdp_away.apply {

                playType = PlayType.HDP

                visibility = if (oddListHDP == null || oddListHDP.size < 2) {
                    View.INVISIBLE
                } else {
                    View.VISIBLE
                }

                isSelected = oddListHDP?.get(1)?.isSelected ?: false

                betStatus = oddListHDP?.get(1)?.status

                oddStatus = oddListHDP?.get(1)?.oddState

                odd_hdp_top_text.text = if (oddListHDP == null || oddListHDP.size < 2) {
                    ""
                } else {
                    oddListHDP[1]?.spread
                }

                odd_hdp_bottom_text.text = if (oddListHDP == null || oddListHDP.size < 2) {
                    ""
                } else {
                    oddListHDP[1]?.odds?.let { TextUtil.formatForOdd(it) }
                }

                setOnClickListener {
                    if (oddListHDP != null && oddListHDP.size >= 2) {
                        oddListHDP[1]?.let { odd ->
                            leagueOddListener?.onClickBet(item, PlayType.HDP.code, odd)
                        }
                    }
                }
            }

            itemView.match_odd_ou_home.apply {

                playType = PlayType.OU

                ouType = OUType.O_TYPE

                visibility = if (oddListOU == null || oddListOU.size < 2) {
                    View.INVISIBLE
                } else {
                    View.VISIBLE
                }

                isSelected = oddListOU?.get(0)?.isSelected ?: false

                betStatus = oddListOU?.get(0)?.status

                oddStatus = oddListOU?.get(0)?.oddState

                odd_ou_top_text.text = if (oddListOU == null || oddListOU.size < 2) {
                    ""
                } else {
                    oddListOU[0]?.spread
                }

                odd_ou_bottom_text.text = if (oddListOU == null || oddListOU.size < 2) {
                    ""
                } else {
                    oddListOU[0]?.odds?.let { TextUtil.formatForOdd(it) }
                }

                setOnClickListener {
                    if (oddListOU != null && oddListOU.size >= 2) {
                        oddListOU[0]?.let { odd ->
                            leagueOddListener?.onClickBet(item, PlayType.OU.code, odd)
                        }
                    }
                }
            }

            itemView.match_odd_ou_away.apply {

                playType = PlayType.OU

                ouType = OUType.U_TYPE

                visibility = if (oddListOU == null || oddListOU.size < 2) {
                    View.INVISIBLE
                } else {
                    View.VISIBLE
                }

                isSelected = oddListOU?.get(1)?.isSelected ?: false

                betStatus = oddListOU?.get(1)?.status

                oddStatus = oddListOU?.get(1)?.oddState

                odd_ou_top_text.text = if (oddListOU == null || oddListOU.size < 2) {
                    ""
                } else {
                    oddListOU[1]?.spread
                }

                odd_ou_bottom_text.text = if (oddListOU == null || oddListOU.size < 2) {
                    ""
                } else {
                    oddListOU[1]?.odds?.let { TextUtil.formatForOdd(it) }
                }

                setOnClickListener {
                    if (oddListOU != null && oddListOU.size >= 2) {
                        oddListOU[1]?.let { odd ->
                            leagueOddListener?.onClickBet(item, PlayType.OU.code, odd)
                        }
                    }
                }
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

    class ViewHolder1x2 private constructor(itemView: View) : ViewHolderTimer(itemView) {
        fun bind(matchType: MatchType, item: MatchOdd,gameCardList:MutableList<MatchOdd>, leagueOddListener: LeagueOddListener?) {

            setupMatchInfo(item, matchType)

            setupOddButton(item, leagueOddListener)

            itemView.match_live_1x2.setOnClickListener {
                leagueOddListener?.onClickLive(item,gameCardList)
            }
        }

        private fun setupMatchInfo(item: MatchOdd, matchType: MatchType) {
            itemView.match_play_type_count_1x2.text = item.matchInfo?.playCateNum.toString()

            itemView.game_name_home_1x2.text = item.matchInfo?.homeName
            itemView.game_name_away_1x2.text = item.matchInfo?.awayName

            if (matchType == MatchType.IN_PLAY) {
                itemView.game_score_home_1x2.text = (item.matchInfo?.homeScore ?: 0).toString()
                itemView.game_score_away_1x2.text = (item.matchInfo?.awayScore ?: 0).toString()
                itemView.match_status_1x2.text = item.matchInfo?.statusName

                listener = object : TimerListener {
                    override fun onTimerUpdate(timeMillis: Long) {
                        itemView.match_time_1x2.text = TimeUtil.timeFormat(timeMillis, "mm:ss")
                    }
                }

                updateTimer(item.leagueTime ?: 0)
            }
        }

        private fun setupOddButton(item: MatchOdd, leagueOddListener: LeagueOddListener?) {
            val oddList1X2 = item.odds[PlayType.X12.code]

            itemView.match_odd_1.apply {
                playType = PlayType.X12

                visibility = if (oddList1X2 == null || oddList1X2.size < 2) {
                    View.GONE
                } else {
                    View.VISIBLE
                }

                isSelected = oddList1X2?.get(0)?.isSelected ?: false

                betStatus = oddList1X2?.get(0)?.status

                oddStatus = oddList1X2?.get(0)?.oddState

                odd_1x2_top_text.text = "1"

                odd_1x2_bottom_text.text = if (oddList1X2 == null || oddList1X2.size < 2) {
                    ""
                } else {
                    oddList1X2[0]?.odds?.let { TextUtil.formatForOdd(it) }
                }

                setOnClickListener {
                    if (oddList1X2 != null && oddList1X2.size >= 2) {
                        oddList1X2[0]?.let { odd ->
                            leagueOddListener?.onClickBet(item, PlayType.X12.code, odd)
                        }
                    }
                }
            }

            itemView.match_odd_x.apply {
                playType = PlayType.X12

                visibility = if (oddList1X2 == null || oddList1X2.size < 3) {
                    View.GONE
                } else {
                    View.VISIBLE
                }

                isSelected = if (oddList1X2 != null && oddList1X2.size >= 3) {
                    oddList1X2[1]?.isSelected ?: false
                } else {
                    false
                }

                betStatus = if (oddList1X2 != null && oddList1X2.size >= 3) {
                    oddList1X2[1]?.status
                } else {
                    null
                }

                oddStatus = if (oddList1X2 != null && oddList1X2.size >= 3) {
                    oddList1X2[1]?.oddState
                } else {
                    null
                }

                odd_1x2_top_text.text = "X"

                odd_1x2_bottom_text.text = if (oddList1X2 == null || oddList1X2.size < 3) {
                    ""
                } else {
                    oddList1X2[1]?.odds?.let { TextUtil.formatForOdd(it) }
                }

                setOnClickListener {
                    if (oddList1X2 != null && oddList1X2.size >= 3) {
                        oddList1X2[1]?.let { odd ->
                            leagueOddListener?.onClickBet(item, PlayType.X12.code, odd)
                        }
                    }
                }
            }

            itemView.match_odd_2.apply {
                playType = PlayType.X12

                visibility = if (oddList1X2 == null || oddList1X2.size < 2) {
                    View.GONE
                } else {
                    View.VISIBLE
                }

                isSelected = if (oddList1X2 != null && oddList1X2.size == 2) {
                    oddList1X2[1]?.isSelected ?: false
                } else if (oddList1X2 != null && oddList1X2.size >= 3) {
                    oddList1X2[2]?.isSelected ?: false
                } else {
                    false
                }

                betStatus = if (oddList1X2 != null && oddList1X2.size == 2) {
                    oddList1X2[1]?.status
                } else if (oddList1X2 != null && oddList1X2.size >= 3) {
                    oddList1X2[2]?.status
                } else {
                    null
                }

                oddStatus = if (oddList1X2 != null && oddList1X2.size == 2) {
                    oddList1X2[1]?.oddState
                } else if (oddList1X2 != null && oddList1X2.size >= 3) {
                    oddList1X2[2]?.oddState
                } else {
                    null
                }

                odd_1x2_top_text.text = "2"

                odd_1x2_bottom_text.text = if (oddList1X2 == null || oddList1X2.size < 2) {
                    ""
                } else if (oddList1X2.size == 2) {
                    oddList1X2[1]?.odds?.let { TextUtil.formatForOdd(it) }
                } else {
                    oddList1X2[2]?.odds?.let { TextUtil.formatForOdd(it) }
                }

                setOnClickListener {
                    if (oddList1X2 != null && oddList1X2.size == 2) {
                        oddList1X2[1]?.let { odd ->
                            leagueOddListener?.onClickBet(item, PlayType.X12.code, odd)
                        }
                    } else if (oddList1X2 != null && oddList1X2.size >= 3) {
                        oddList1X2[2]?.let { odd ->
                            leagueOddListener?.onClickBet(item, PlayType.X12.code, odd)
                        }
                    }
                }
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

    abstract class ViewHolderTimer(itemView: View) : RecyclerView.ViewHolder(itemView) {
        interface TimerListener {
            fun onTimerUpdate(timeMillis: Long)
        }

        protected var listener: TimerListener? = null

        private var timer: Timer? = null

        fun updateTimer(leagueTime: Int) {
            var timeMillis = leagueTime * 1000L

            Handler(Looper.getMainLooper()).post {
                listener?.onTimerUpdate(timeMillis)
            }

            stopTimer()
            timer = Timer()
            timer?.schedule(object : TimerTask() {
                override fun run() {
                    timeMillis += 1000
                    Handler(Looper.getMainLooper()).post {
                        listener?.onTimerUpdate(timeMillis)
                    }
                }
            }, 1000L, 1000L)
        }

        fun stopTimer() {
            timer?.cancel()
        }
    }
}

class LeagueOddListener(
    val clickListenerLive: (item: MatchOdd,gameCardList: MutableList<MatchOdd>) -> Unit,
    val clickListenerBet: (matchOdd: MatchOdd, oddString: String, odd: Odd) -> Unit
) {
    fun onClickLive(item: MatchOdd,gameCardList: MutableList<MatchOdd>) = clickListenerLive(item,gameCardList)
    fun onClickBet(matchOdd: MatchOdd, oddString: String, odd: Odd) =
        clickListenerBet(matchOdd, oddString, odd)
}