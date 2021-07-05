package org.cxct.sportlottery.ui.game.common

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.button_odd.view.*
import kotlinx.android.synthetic.main.itemview_game_league_odd_1x2.view.*
import kotlinx.android.synthetic.main.itemview_game_league_odd_hdp_ou.view.*
import kotlinx.android.synthetic.main.itemview_league_odd_v4.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.OUType
import org.cxct.sportlottery.network.common.PlayType
import org.cxct.sportlottery.network.common.SportType
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.list.BetStatus
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.odds.list.Odd
import org.cxct.sportlottery.network.odds.list.OddState
import org.cxct.sportlottery.ui.game.widget.OddButton
import org.cxct.sportlottery.ui.menu.OddsType
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

    var oddsType: OddsType = OddsType.EU
        set(value) {
            if (value != field) {
                field = value
                notifyDataSetChanged()
            }
        }

    var isTimerEnable = false
        set(value) {
            if (value != field) {
                field = value
                notifyDataSetChanged()
            }
        }

    var leagueOddListener: LeagueOddListener? = null

    private val oddStateRefreshListener by lazy {
        object : OddStateViewHolder.OddStateChangeListener {
            override fun refreshOddButton(odd: Odd) {
                notifyItemChanged(data.indexOf(data.find { matchOdd ->
                    matchOdd.odds.toList()
                        .find { map -> map.second.find { it == odd } != null } != null
                }))
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolderHdpOu.from(parent, oddStateRefreshListener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = data[position]
        val matchInfoList = data.mapNotNull {
            it.matchInfo
        }

        when (holder) {
            is ViewHolderHdpOu -> {
                holder.stopTimer()
                holder.bind(
                    matchType,
                    item,
                    leagueOddListener,
                    isTimerEnable,
                    oddsType,
                    matchInfoList
                )
            }
        }
    }

    override fun getItemCount(): Int = data.size

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)

        when (holder) {
            is ViewHolderTimer -> holder.stopTimer()
        }
    }

    class ViewHolderHdpOu private constructor(
        itemView: View,
        private val refreshListener: OddStateChangeListener
    ) : ViewHolderTimer(itemView) {

        fun bind(
            matchType: MatchType,
            item: MatchOdd,
            leagueOddListener: LeagueOddListener?,
            isTimerEnable: Boolean,
            oddsType: OddsType,
            matchInfoList: List<MatchInfo>
        ) {
            setupMatchInfo(item, matchType, matchInfoList, leagueOddListener)

            setupMatchTime(item, matchType, isTimerEnable)

//            setupOddButton(item, leagueOddListener, oddsType)
        }

        private fun setupMatchInfo(
            item: MatchOdd,
            matchType: MatchType,
            matchInfoList: List<MatchInfo>,
            leagueOddListener: LeagueOddListener?
        ) {
            itemView.league_odd_match_name_home.text = item.matchInfo?.homeName

            itemView.league_odd_match_name_away.text = item.matchInfo?.awayName

            itemView.league_odd_match_score_home.apply {
                visibility = if (matchType == MatchType.IN_PLAY) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

                text = (item.matchInfo?.homeScore ?: 0).toString()
            }

            itemView.league_odd_match_score_away.apply {
                visibility = if (matchType == MatchType.IN_PLAY) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

                text = (item.matchInfo?.awayScore ?: 0).toString()
            }

            itemView.league_odd_match_play_count.apply {
                text = item.matchInfo?.playCateNum.toString()

                setOnClickListener {
                    leagueOddListener?.onClickPlayType(item.matchInfo?.id, matchInfoList)
                }
            }

            itemView.league_odd_match_border_row1.setOnClickListener {
                leagueOddListener?.onClickPlayType(item.matchInfo?.id, matchInfoList)
            }

            itemView.league_odd_match_border_row2.setOnClickListener {
                leagueOddListener?.onClickPlayType(item.matchInfo?.id, matchInfoList)
            }
        }

        private fun setupMatchTime(
            item: MatchOdd,
            matchType: MatchType,
            isTimerEnable: Boolean
        ) {
            when (matchType) {
                MatchType.IN_PLAY -> {
                    listener = object : TimerListener {
                        override fun onTimerUpdate(timeMillis: Long) {
                            itemView.league_odd_match_time.text =
                                TimeUtil.timeFormat(timeMillis, "mm:ss")
                            item.leagueTime = (timeMillis / 1000).toInt()
                        }
                    }

                    updateTimer(
                        isTimerEnable,
                        item.leagueTime ?: 0,
                        item.matchInfo?.sportType == SportType.BASKETBALL
                    )
                }

                MatchType.AT_START -> {
                    listener = object : TimerListener {
                        override fun onTimerUpdate(timeMillis: Long) {
                            itemView.league_odd_match_time.text = String.format(
                                itemView.context.resources.getString(R.string.at_start_remain_minute),
                                TimeUtil.timeFormat(timeMillis, "mm")
                            )
                            item.matchInfo?.remainTime = timeMillis
                        }
                    }

                    item.matchInfo?.remainTime?.let { remainTime ->
                        updateTimer(
                            isTimerEnable,
                            (remainTime / 1000).toInt(),
                            true
                        )
                    }
                }

                else -> {
                    itemView.league_odd_match_time.text = item.matchInfo?.startTimeDisplay
                }
            }

            itemView.league_odd_match_status.text = when (matchType) {
                MatchType.IN_PLAY -> {
                    item.matchInfo?.statusName
                }
                else -> {
                    item.matchInfo?.startDateDisplay
                }
            }

            itemView.league_odd_match_remain_time_icon.visibility =
                if (matchType == MatchType.AT_START) {
                    View.VISIBLE
                } else {
                    View.INVISIBLE
                }
        }

        private fun setupOddButton(
            item: MatchOdd,
            leagueOddListener: LeagueOddListener?,
            oddsType: OddsType,
        ) {
            val sportType = item.matchInfo?.sportType

            val oddListHDP = when (sportType) {
                SportType.TENNIS -> {
                    item.odds[PlayType.SET_HDP.code]
                }
                SportType.BASKETBALL -> {
                    item.odds[PlayType.HDP_INCL_OT.code]
                }
                else -> {
                    item.odds[PlayType.HDP.code]
                }
            }

            val oddListOU = when (sportType) {
                SportType.BASKETBALL -> {
                    item.odds[PlayType.OU_INCL_OT.code]
                }
                else -> {
                    item.odds[PlayType.OU.code]
                }
            }

            val oddList1x2 = when (sportType) {
                SportType.BASKETBALL -> {
                    item.odds[PlayType.X12_INCL_OT.code]
                }
                else -> {
                    item.odds[PlayType.X12.code]
                }
            }

            itemView.match_play_type_column1.text = when (sportType) {
                SportType.FOOTBALL, SportType.BASKETBALL -> {
                    itemView.context.getString(R.string.ou_hdp_hdp_title)
                }
                SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> {
                    itemView.context.getString(R.string.ou_hdp_1x2_title)
                }
                else -> ""
            }

            itemView.match_play_type_column2.text = when (sportType) {
                SportType.FOOTBALL, SportType.BASKETBALL -> {
                    itemView.context.getString(R.string.ou_hdp_ou_title)
                }
                SportType.TENNIS -> {
                    itemView.context.getString(R.string.ou_hdp_hdp_title_tennis)
                }
                SportType.VOLLEYBALL, SportType.BADMINTON -> {
                    itemView.context.getString(R.string.ou_hdp_hdp_title_v_b)
                }
                else -> ""
            }

            itemView.match_odd_column1_home.apply {
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
                                    leagueOddListener?.onClickBet(
                                        item,
                                        odd,
                                        itemView.match_play_type_column1.text.toString(),
                                        item.matchInfo.homeName
                                    )
                                }
                            }
                        }

                        SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> {
                            if (oddList1x2 != null && oddList1x2.size >= 2) {
                                oddList1x2[0]?.let { odd ->
                                    leagueOddListener?.onClickBet(
                                        item,
                                        odd,
                                        itemView.match_play_type_column1.text.toString(),
                                        item.matchInfo.homeName
                                    )
                                }
                            }
                        }
                    }
                }
            }

            itemView.match_odd_column1_away.apply {
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
                                    leagueOddListener?.onClickBet(
                                        item,
                                        odd,
                                        itemView.match_play_type_column1.text.toString(),
                                        item.matchInfo.awayName
                                    )
                                }
                            }
                        }

                        SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> {
                            if (oddList1x2 != null && oddList1x2.size >= 2) {
                                oddList1x2[1]?.let { odd ->
                                    leagueOddListener?.onClickBet(
                                        item,
                                        odd,
                                        itemView.match_play_type_column1.text.toString(),
                                        item.matchInfo.awayName
                                    )
                                }
                            }
                        }
                    }
                }
            }

            itemView.match_odd_column2_home.apply {
                playType = when (sportType) {
                    SportType.FOOTBALL, SportType.BASKETBALL -> {
                        PlayType.OU
                    }
                    SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> {
                        PlayType.HDP
                    }
                    else -> null
                }

                ouType = when (sportType) {
                    SportType.FOOTBALL, SportType.BASKETBALL -> {
                        OUType.O_TYPE
                    }
                    else -> null
                }

                isSelected = when (sportType) {
                    SportType.FOOTBALL, SportType.BASKETBALL -> {
                        oddListOU?.get(0)?.isSelected ?: false
                    }
                    SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> {
                        oddListHDP?.get(0)?.isSelected ?: false
                    }
                    else -> {
                        false
                    }
                }

                betStatus = when (sportType) {
                    SportType.FOOTBALL, SportType.BASKETBALL -> {
                        if (oddListOU == null || oddListOU.size < 2) {
                            BetStatus.LOCKED.code
                        } else {
                            oddListOU[0]?.status ?: BetStatus.LOCKED.code
                        }
                    }
                    SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> {
                        if (oddListHDP == null || oddListHDP.size < 2) {
                            BetStatus.LOCKED.code
                        } else {
                            oddListHDP[0]?.status ?: BetStatus.LOCKED.code
                        }
                    }
                    else -> {
                        null
                    }
                }

                this@ViewHolderHdpOu.setupOddState(
                    this, when (sportType) {
                        SportType.FOOTBALL, SportType.BASKETBALL -> {
                            oddListOU?.get(0)
                        }
                        SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> {
                            oddListHDP?.get(0)
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
                                oddListOU?.get(0)?.oddState = OddState.SAME.state
                            }
                            SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> {
                                oddListHDP?.get(0)?.oddState = OddState.SAME.state
                            }
                        }
                    }
                }

                odd_ou_top_text.text = if (oddListOU == null || oddListOU.size < 2) {
                    ""
                } else {
                    oddListOU[0]?.spread
                }

                odd_ou_bottom_text.text = when {
                    (oddListOU != null && oddListOU.size >= 2 && oddsType == OddsType.EU) -> {
                        oddListOU[0]?.odds?.let {
                            TextUtil.formatForOdd(it)
                        }
                    }
                    (oddListOU != null && oddListOU.size >= 2 && oddsType == OddsType.HK) -> {
                        oddListOU[0]?.hkOdds?.let {
                            TextUtil.formatForOdd(it)
                        }
                    }
                    else -> ""
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

                setOnClickListener {
                    when (sportType) {
                        SportType.FOOTBALL, SportType.BASKETBALL -> {
                            if (oddListOU != null && oddListOU.size >= 2) {
                                oddListOU[0]?.let { odd ->
                                    leagueOddListener?.onClickBet(
                                        item,
                                        odd,
                                        itemView.match_play_type_column2.text.toString(),
                                        resources.getString(R.string.odd_button_ou_o)
                                    )
                                }
                            }
                        }

                        SportType.VOLLEYBALL, SportType.BADMINTON, SportType.TENNIS -> {
                            if (oddListHDP != null && oddListHDP.size >= 2) {
                                oddListHDP[0]?.let { odd ->
                                    leagueOddListener?.onClickBet(
                                        item,
                                        odd,
                                        itemView.match_play_type_column2.text.toString(),
                                        item.matchInfo.homeName
                                    )
                                }
                            }
                        }
                    }
                }
            }

            itemView.match_odd_column2_away.apply {
                playType = when (sportType) {
                    SportType.FOOTBALL, SportType.BASKETBALL -> {
                        PlayType.OU
                    }
                    SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> {
                        PlayType.HDP
                    }
                    else -> null
                }

                ouType = when (sportType) {
                    SportType.FOOTBALL, SportType.BASKETBALL -> {
                        OUType.U_TYPE
                    }
                    else -> null
                }

                isSelected = when (sportType) {
                    SportType.FOOTBALL, SportType.BASKETBALL -> {
                        oddListOU?.get(1)?.isSelected ?: false
                    }
                    SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> {
                        oddListHDP?.get(1)?.isSelected ?: false
                    }
                    else -> {
                        false
                    }
                }

                betStatus = when (sportType) {
                    SportType.FOOTBALL, SportType.BASKETBALL -> {
                        if (oddListOU == null || oddListOU.size < 2) {
                            BetStatus.LOCKED.code
                        } else {
                            oddListOU[1]?.status ?: BetStatus.LOCKED.code
                        }
                    }
                    SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> {
                        if (oddListHDP == null || oddListHDP.size < 2) {
                            BetStatus.LOCKED.code
                        } else {
                            oddListHDP[1]?.status ?: BetStatus.LOCKED.code
                        }
                    }
                    else -> {
                        null
                    }
                }

                this@ViewHolderHdpOu.setupOddState(
                    this, when (sportType) {
                        SportType.FOOTBALL, SportType.BASKETBALL -> {
                            oddListOU?.get(1)
                        }
                        SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> {
                            oddListHDP?.get(1)
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
                                oddListOU?.get(1)?.oddState = OddState.SAME.state
                            }
                            SportType.TENNIS, SportType.VOLLEYBALL, SportType.BADMINTON -> {
                                oddListHDP?.get(1)?.oddState = OddState.SAME.state
                            }
                        }
                    }
                }

                odd_ou_top_text.text = if (oddListOU == null || oddListOU.size < 2) {
                    ""
                } else {
                    oddListOU[1]?.spread
                }

                odd_ou_bottom_text.text = when {
                    (oddListOU != null && oddListOU.size >= 2 && oddsType == OddsType.EU) -> {
                        oddListOU[1]?.odds?.let {
                            TextUtil.formatForOdd(it)
                        }
                    }
                    (oddListOU != null && oddListOU.size >= 2 && oddsType == OddsType.HK) -> {
                        oddListOU[1]?.hkOdds?.let {
                            TextUtil.formatForOdd(it)
                        }
                    }
                    else -> ""
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

                setOnClickListener {
                    when (sportType) {
                        SportType.FOOTBALL, SportType.BASKETBALL -> {
                            if (oddListOU != null && oddListOU.size >= 2) {
                                oddListOU[1]?.let { odd ->
                                    leagueOddListener?.onClickBet(
                                        item,
                                        odd,
                                        itemView.match_play_type_column2.text.toString(),
                                        resources.getString(R.string.odd_button_ou_u)
                                    )
                                }
                            }
                        }

                        SportType.VOLLEYBALL, SportType.BADMINTON, SportType.TENNIS -> {
                            if (oddListHDP != null && oddListHDP.size >= 2) {
                                oddListHDP[1]?.let { odd ->
                                    leagueOddListener?.onClickBet(
                                        item,
                                        odd,
                                        itemView.match_play_type_column2.text.toString(),
                                        item.matchInfo.awayName
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup, refreshListener: OddStateChangeListener): ViewHolderHdpOu {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_league_odd_v4, parent, false)

                return ViewHolderHdpOu(view, refreshListener)
            }
        }

        override val oddStateChangeListener: OddStateChangeListener
            get() = refreshListener
    }

    abstract class ViewHolderTimer(itemView: View) : OddStateViewHolder(itemView) {
        interface TimerListener {
            fun onTimerUpdate(timeMillis: Long)
        }

        protected var listener: TimerListener? = null

        private var timer: Timer? = null

        fun updateTimer(isTimerEnable: Boolean, startTime: Int, isDecrease: Boolean) {

            when (isTimerEnable) {
                true -> {
                    var timeMillis = startTime * 1000L

                    Handler(Looper.getMainLooper()).post {
                        listener?.onTimerUpdate(timeMillis)
                    }

                    timer = Timer()
                    timer?.schedule(object : TimerTask() {
                        override fun run() {
                            when (isDecrease) {
                                true -> {
                                    timeMillis -= 1000
                                }
                                false -> {
                                    timeMillis += 1000
                                }
                            }

                            if (timeMillis > 0) {
                                Handler(Looper.getMainLooper()).post {
                                    listener?.onTimerUpdate(timeMillis)
                                }
                            }
                        }
                    }, 1000L, 1000L)
                }

                false -> {
                    stopTimer()
                }
            }
        }

        fun stopTimer() {
            timer?.cancel()
            timer = null
        }
    }
}

class LeagueOddListener(
    val clickListenerLive: (item: MatchOdd) -> Unit,
    val clickListenerPlayType: (matchId: String?, matchInfoList: List<MatchInfo>) -> Unit,
    val clickListenerBet: (matchOdd: MatchOdd, odd: Odd, playCateName: String, playName: String) -> Unit
) {
    fun onClickLive(item: MatchOdd) =
        clickListenerLive(item)

    fun onClickPlayType(matchId: String?, matchInfoList: List<MatchInfo>) =
        clickListenerPlayType(matchId, matchInfoList)

    fun onClickBet(matchOdd: MatchOdd, odd: Odd, playCateName: String = "", playName: String = "") =
        clickListenerBet(matchOdd, odd, playCateName, playName)
}