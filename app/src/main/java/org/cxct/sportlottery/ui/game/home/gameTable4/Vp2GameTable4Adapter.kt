package org.cxct.sportlottery.ui.game.home.gameTable4

import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.button_odd.view.*
import kotlinx.android.synthetic.main.home_game_table_item_4.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.interfaces.OnSelectItemListener
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayType
import org.cxct.sportlottery.network.common.SportType
import org.cxct.sportlottery.network.odds.list.BetStatus
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.odds.list.Odd
import org.cxct.sportlottery.network.odds.list.OddState
import org.cxct.sportlottery.ui.game.common.OddStateViewHolder
import org.cxct.sportlottery.ui.game.widget.OddButton
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.TimeUtil
import java.util.*

class Vp2GameTable4Adapter(val dataList: List<MatchOdd>, val oddsType: OddsType, val matchType: MatchType) : RecyclerView.Adapter<Vp2GameTable4Adapter.ViewHolderHdpOu>() {

    var onClickOddListener: OnClickOddListener? = null

    var onClickMatchListener: OnSelectItemListener<MatchOdd>? = null //賽事畫面跳轉

    private val mOddStateRefreshListener by lazy {
        object : OddStateViewHolder.OddStateChangeListener {
            override fun refreshOddButton(odd: Odd) {
                notifyItemChanged(dataList.indexOf(dataList.find { matchOdd ->
                    matchOdd.odds.toList()
                        .find { map -> map.second.find { it == odd } != null } != null
                }))
            }
        }
    }

    private val mTimerMap = mutableMapOf<Int, Timer?>()

    fun stopAllTimer() {
        mTimerMap.forEach {
            val timer = it.value
            timer?.cancel()
        }
        mTimerMap.clear()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderHdpOu {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.home_game_table_item_4, parent, false)
        return ViewHolderHdpOu(view)
    }

    override fun onBindViewHolder(holder: ViewHolderHdpOu, position: Int) {
        try {
            val data = dataList[position]
            holder.bind(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int = dataList.size

    override fun onViewRecycled(holder: ViewHolderHdpOu) {
        super.onViewRecycled(holder)
        holder.stopTimer()
    }

    inner class ViewHolderHdpOu(itemView: View) : OddStateViewHolder(itemView) {

        private var timer: Timer? = null

        fun bind(data: MatchOdd) {
            setupMatchInfo(data)
            setupTime(data)
            setupOddButton(data)

            //TODO simon test review 賠率 icon 顯示邏輯
            itemView.iv_match_in_play.visibility = if (matchType == MatchType.IN_PLAY) View.VISIBLE else View.GONE
//            itemView.iv_match_price.visibility = if () View.VISIBLE else View.GONE
//            itemView.iv_match_live.visibility = if () View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                onClickMatchListener?.onClick(data)
            }
        }

        private fun setupMatchInfo(data: MatchOdd) {
            itemView.apply {
                tv_game_name_home.text = data.matchInfo?.homeName
                tv_game_name_away.text = data.matchInfo?.awayName
                tv_match_play_type_count.text = data.matchInfo?.playCateNum?.toString()

                when (matchType) {
                    MatchType.IN_PLAY -> {
                        tv_game_type.text = context.getString(R.string.home_tab_in_play)

                        tv_game_score_home.visibility = View.VISIBLE
                        tv_game_score_away.visibility = View.VISIBLE
                        val homeScore = data.matchInfo?.homeScore ?: 0
                        val awayScore = data.matchInfo?.awayScore ?: 0
                        tv_game_score_home.text = "$homeScore"
                        tv_game_score_away.text = "$awayScore"

                        when {
                            homeScore > awayScore -> {
                                tv_game_score_home.setTypeface(
                                    tv_game_score_home.typeface,
                                    Typeface.BOLD
                                )
                                tv_game_score_away.setTypeface(
                                    tv_game_score_away.typeface,
                                    Typeface.NORMAL
                                )
                                tv_game_name_home.setTypeface(
                                    tv_game_name_home.typeface,
                                    Typeface.BOLD
                                )
                                tv_game_name_away.setTypeface(
                                    tv_game_name_away.typeface,
                                    Typeface.NORMAL
                                )
                            }
                            homeScore < awayScore -> {
                                tv_game_score_home.setTypeface(
                                    tv_game_score_home.typeface,
                                    Typeface.NORMAL
                                )
                                tv_game_score_away.setTypeface(
                                    tv_game_score_away.typeface,
                                    Typeface.BOLD
                                )
                                tv_game_name_home.setTypeface(
                                    tv_game_name_home.typeface,
                                    Typeface.NORMAL
                                )
                                tv_game_name_away.setTypeface(
                                    tv_game_name_away.typeface,
                                    Typeface.BOLD
                                )
                            }
                            else -> {
                                tv_game_score_home.setTypeface(
                                    tv_game_score_home.typeface,
                                    Typeface.NORMAL
                                )
                                tv_game_score_away.setTypeface(
                                    tv_game_score_away.typeface,
                                    Typeface.NORMAL
                                )
                                tv_game_name_home.setTypeface(
                                    tv_game_name_home.typeface,
                                    Typeface.NORMAL
                                )
                                tv_game_name_away.setTypeface(
                                    tv_game_name_away.typeface,
                                    Typeface.NORMAL
                                )
                            }
                        }
                    }
                    MatchType.AT_START -> {
                        tv_game_type.text = context.getString(R.string.home_tab_today)

                        tv_game_score_home.visibility = View.GONE
                        tv_game_score_away.visibility = View.GONE

                        tv_game_name_home.setTypeface(tv_game_name_home.typeface, Typeface.NORMAL)
                        tv_game_name_away.setTypeface(tv_game_name_away.typeface, Typeface.NORMAL)
                    }
                    else -> {
                    }
                }
            }
        }

        private fun setupTime(data: MatchOdd) {
            itemView.apply {
                when (matchType) {
                    MatchType.IN_PLAY -> {
                        val statusName = if (data.matchInfo?.statusName.isNullOrEmpty())
                            ""
                        else
                            data.matchInfo?.statusName + " "

                        when (data.matchInfo?.sportType) {
                            SportType.FOOTBALL -> { //足球
                                data.leagueTime?.let { leagueTime ->
                                    startTimer(leagueTime, false) { timeMillis ->
                                        val timeStr =
                                            statusName + TimeUtil.timeFormat(timeMillis, "mm:ss")
                                        tv_match_time.text = timeStr
                                        data.leagueTime = (timeMillis / 1000).toInt()
                                    }
                                }
                            }
                            SportType.BASKETBALL -> { //籃球
                                data.leagueTime?.let { leagueTime ->
                                    startTimer(leagueTime, true) { timeMillis ->
                                        val timeStr =
                                            statusName + TimeUtil.timeFormat(timeMillis, "mm:ss")
                                        tv_match_time.text = timeStr
                                        data.leagueTime = (timeMillis / 1000).toInt()
                                    }
                                }
                            }
                            else -> {
                                stopTimer()
                                tv_match_time.text = null
                            }
                        }
                    }

                    MatchType.AT_START -> {
                        val statusName = if (data.matchInfo?.startDateDisplay.isNullOrEmpty())
                            ""
                        else
                            data.matchInfo?.startDateDisplay + " "

                        data.matchInfo?.remainTime?.let { remainTime ->
                            startTimer((remainTime / 1000).toInt(), true) { timeMillis ->
                                val timeStr = statusName + String.format(
                                    itemView.context.resources.getString(R.string.at_start_remain_minute),
                                    TimeUtil.timeFormat(timeMillis, "mm")
                                )
                                tv_match_time.text = timeStr

                                data.matchInfo.remainTime = timeMillis
                            }
                        }
                    }

                    else -> {
                        stopTimer()
                        tv_match_time.text =
                            "${data.matchInfo?.startDateDisplay?: ""} ${data.matchInfo?.startTimeDisplay?: ""}"
                    }
                }
            }
        }

        private fun setupOddButton(data: MatchOdd) {
            itemView.apply {
                val sportType = data.matchInfo?.sportType

                tv_play_type.text = when (sportType) {
                    SportType.FOOTBALL, SportType.BASKETBALL -> context.getText(R.string.ou_hdp_hdp_title)
                    SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> context.getText(R.string.ou_hdp_1x2_title)
                    else -> ""
                }

                val oddListHDP = when (sportType) {
                    SportType.TENNIS -> {
                        data.odds[PlayType.SET_HDP.code]
                    }
                    SportType.BASKETBALL -> {
                        data.odds[PlayType.HDP_INCL_OT.code]
                    }
                    else -> {
                        data.odds[PlayType.HDP.code]
                    }
                }

                val oddList1x2 = when (sportType) {
                    SportType.BASKETBALL -> {
                        data.odds[PlayType.X12_INCL_OT.code]
                    }
                    else -> {
                        data.odds[PlayType.X12.code]
                    }
                }

                btn_match_odd1.apply {
                    playType = when (sportType) {
                        SportType.FOOTBALL, SportType.BASKETBALL -> {
                            PlayType.HDP
                        }
                        SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> {
                            PlayType.X12
                        }
                        else -> null
                    }

                    isSelected = when (sportType) {
                        SportType.FOOTBALL, SportType.BASKETBALL -> {
                            oddListHDP?.get(0)?.isSelected ?: false
                        }
                        SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> {
                            oddList1x2?.get(0)?.isSelected ?: false
                        }
                        else -> {
                            false
                        }
                    }

                    betStatus = when (sportType) {
                        SportType.FOOTBALL, SportType.BASKETBALL -> {
                            if (oddListHDP == null || oddListHDP.size < 2) {
                                BetStatus.LOCKED.code
                            } else {
                                oddListHDP[0]?.status ?: BetStatus.LOCKED.code
                            }
                        }
                        SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> {
                            if (oddList1x2 == null || oddList1x2.size < 2) {
                                BetStatus.LOCKED.code
                            } else {
                                oddList1x2[0]?.status ?: BetStatus.LOCKED.code
                            }
                        }
                        else -> {
                            null
                        }
                    }

                    this@ViewHolderHdpOu.setupOddState(
                        this, when (sportType) {
                            SportType.FOOTBALL, SportType.BASKETBALL -> {
                                oddListHDP?.get(0)
                            }
                            SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> {
                                oddList1x2?.get(0)
                            }
                            else -> {
                                null
                            }
                        }
                    )

                    onOddStatusChangedListener = object : OddButton.OnOddStatusChangedListener {
                        override fun onOddStateChangedFinish() {
                            when (sportType) {
                                SportType.FOOTBALL, SportType.BASKETBALL -> {
                                    oddListHDP?.get(0)?.oddState = OddState.SAME.state
                                }
                                SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> {
                                    oddList1x2?.get(0)?.oddState = OddState.SAME.state
                                }
                            }
                        }
                    }

                    odd_hdp_top_text.text = if (oddListHDP == null || oddListHDP.size < 2) {
                        ""
                    } else {
                        oddListHDP[0]?.spread
                    }

                    odd_hdp_bottom_text.text = when {
                        (oddListHDP != null && oddListHDP.size >= 2 && oddsType == OddsType.EU) -> {
                            oddListHDP[0]?.odds?.let {
                                TextUtil.formatForOdd(it)
                            }
                        }
                        (oddListHDP != null && oddListHDP.size >= 2 && oddsType == OddsType.HK) -> {
                            oddListHDP[0]?.hkOdds?.let {
                                TextUtil.formatForOdd(it)
                            }
                        }
                        else -> ""
                    }

                    odd_1x2_top_text.visibility = View.GONE

                    odd_1x2_bottom_text.text = when {
                        (oddList1x2 != null && oddList1x2.size >= 2 && oddsType == OddsType.EU) -> {
                            oddList1x2[0]?.odds?.let {
                                TextUtil.formatForOdd(it)
                            }
                        }
                        (oddList1x2 != null && oddList1x2.size >= 2 && oddsType == OddsType.HK) -> {
                            oddList1x2[0]?.hkOdds?.let {
                                TextUtil.formatForOdd(it)
                            }
                        }
                        else -> ""
                    }

                    setOnClickListener {
                        when (sportType) {
                            SportType.FOOTBALL, SportType.BASKETBALL -> {
                                if (oddListHDP != null && oddListHDP.size >= 2) {
                                    oddListHDP[0]?.let { odd ->
                                        onClickOddListener?.onClickBet(
                                            data,
                                            odd,
                                            itemView.tv_play_type.text.toString(),
                                            data.matchInfo.homeName
                                        )
                                    }
                                }
                            }

                            SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> {
                                if (oddList1x2 != null && oddList1x2.size >= 2) {
                                    oddList1x2[0]?.let { odd ->
                                        onClickOddListener?.onClickBet(
                                            data,
                                            odd,
                                            itemView.tv_play_type.text.toString(),
                                            data.matchInfo.homeName
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                btn_match_odd2.apply {
                    playType = when (sportType) {
                        SportType.FOOTBALL, SportType.BASKETBALL -> {
                            PlayType.HDP
                        }
                        SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> {
                            PlayType.X12
                        }
                        else -> null
                    }

                    isSelected = when (sportType) {
                        SportType.FOOTBALL, SportType.BASKETBALL -> {
                            oddListHDP?.get(1)?.isSelected ?: false
                        }
                        SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> {
                            oddList1x2?.get(1)?.isSelected ?: false
                        }
                        else -> {
                            false
                        }
                    }

                    betStatus = when (sportType) {
                        SportType.FOOTBALL, SportType.BASKETBALL -> {
                            if (oddListHDP == null || oddListHDP.size < 2) {
                                BetStatus.LOCKED.code
                            } else {
                                oddListHDP[1]?.status ?: BetStatus.LOCKED.code
                            }
                        }
                        SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> {
                            if (oddList1x2 == null || oddList1x2.size < 2) {
                                BetStatus.LOCKED.code
                            } else {
                                oddList1x2[1]?.status ?: BetStatus.LOCKED.code
                            }
                        }
                        else -> {
                            null
                        }
                    }

                    this@ViewHolderHdpOu.setupOddState(
                        this, when (sportType) {
                            SportType.FOOTBALL, SportType.BASKETBALL -> {
                                oddListHDP?.get(1)
                            }
                            SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> {
                                oddList1x2?.get(1)
                            }
                            else -> {
                                null
                            }
                        }
                    )

                    onOddStatusChangedListener = object : OddButton.OnOddStatusChangedListener {
                        override fun onOddStateChangedFinish() {
                            when (sportType) {
                                SportType.FOOTBALL, SportType.BASKETBALL -> {
                                    oddListHDP?.get(1)?.oddState = OddState.SAME.state
                                }
                                SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> {
                                    oddList1x2?.get(1)?.oddState = OddState.SAME.state
                                }
                            }
                        }
                    }

                    odd_hdp_top_text.text = if (oddListHDP == null || oddListHDP.size < 2) {
                        ""
                    } else {
                        oddListHDP[1]?.spread
                    }

                    odd_hdp_bottom_text.text = when {
                        (oddListHDP != null && oddListHDP.size >= 2 && oddsType == OddsType.EU) -> {
                            oddListHDP[1]?.odds?.let {
                                TextUtil.formatForOdd(it)
                            }
                        }
                        (oddListHDP != null && oddListHDP.size >= 2 && oddsType == OddsType.HK) -> {
                            oddListHDP[1]?.hkOdds?.let {
                                TextUtil.formatForOdd(it)
                            }
                        }
                        else -> ""
                    }

                    odd_1x2_top_text.visibility = View.GONE

                    odd_1x2_bottom_text.text = when {
                        (oddList1x2 != null && oddList1x2.size >= 2 && oddsType == OddsType.EU) -> {
                            oddList1x2[1]?.odds?.let {
                                TextUtil.formatForOdd(it)
                            }
                        }
                        (oddList1x2 != null && oddList1x2.size >= 2 && oddsType == OddsType.HK) -> {
                            oddList1x2[1]?.hkOdds?.let {
                                TextUtil.formatForOdd(it)
                            }
                        }
                        else -> ""
                    }

                    setOnClickListener {
                        when (sportType) {
                            SportType.FOOTBALL, SportType.BASKETBALL -> {
                                if (oddListHDP != null && oddListHDP.size >= 2) {
                                    oddListHDP[1]?.let { odd ->
                                        onClickOddListener?.onClickBet(
                                            data,
                                            odd,
                                            itemView.tv_play_type.text.toString(),
                                            data.matchInfo.awayName
                                        )
                                    }
                                }
                            }

                            SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> {
                                if (oddList1x2 != null && oddList1x2.size >= 2) {
                                    oddList1x2[1]?.let { odd ->
                                        onClickOddListener?.onClickBet(
                                            data,
                                            odd,
                                            itemView.tv_play_type.text.toString(),
                                            data.matchInfo.awayName
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        fun startTimer(
            startTime: Int,
            isDecrease: Boolean,
            timerListener: (timeMillis: Long) -> Unit
        ) {
            var timeMillis = startTime * 1000L

            mTimerMap[adapterPosition]?.cancel()
            stopTimer()

            timer = Timer()
            timer?.schedule(object : TimerTask() {
                override fun run() {
                    when {
                        timeMillis < 0 -> timeMillis = 0
                        isDecrease -> timeMillis -= 1000
                        !isDecrease -> timeMillis += 1000
                    }
                    Handler(Looper.getMainLooper()).post {
                        timerListener(timeMillis)
                    }
                }
            }, 1000L, 1000L)

            mTimerMap[adapterPosition] = timer
        }

        fun stopTimer() {
            timer?.cancel()
            timer = null
        }

        override val oddStateChangeListener: OddStateChangeListener
            get() = mOddStateRefreshListener
    }

}

interface OnClickOddListener {
    fun onClickBet(matchOdd: MatchOdd, odd: Odd, playCateName: String, playName: String)
}