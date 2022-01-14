package org.cxct.sportlottery.ui.game.common

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import kotlinx.android.synthetic.main.itemview_league_odd_v4.view.*
import kotlinx.android.synthetic.main.view_quick_odd_btn_eps.view.*
import kotlinx.android.synthetic.main.view_quick_odd_btn_pager.view.*
import kotlinx.android.synthetic.main.view_quick_odd_btn_pair.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.odds.list.TimeCounting
import org.cxct.sportlottery.ui.component.overScrollView.OverScrollDecoratorHelper
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.needCountStatus
import java.util.*
import kotlin.collections.ArrayList

val PAYLOAD_SCORE_CHANGE = "payload_score_change"
val PAYLOAD_CLOCK_CHANGE = "payload_clock_change"
val PAYLOAD_ODDS_CHANGE = "payload_odds_change"


class LeagueOddAdapter(private val matchType: MatchType, private var itemData: List<MatchOdd>) :
    ListAdapter<MatchOdd,RecyclerView.ViewHolder>(ItemDiffCallback()) {
    //private val itemDataList = mutableListOf<MatchOdd>()

//    var data = listOf<MatchOdd>()
//        set(value) {
//            field = value
//            notifyDataSetChanged()
//        }

//    fun updateList(newList: List<MatchOdd>) {
//        val diffCallback = ItemDiffCallback(oldItem = itemData, newItem= newList)
//        val diffResult = DiffUtil.calculateDiff(diffCallback)
//        itemData.clear()
//        itemData.addAll(newList)
//        diffResult.dispatchUpdatesTo(this)
//    }

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

    var leagueOdd: LeagueOdd? = null

    private val oddStateRefreshListener by lazy {
        object : OddStateViewHolder.OddStateChangeListener {
            override fun refreshOddButton(odd: Odd) {
                notifyItemChanged(currentList.indexOf(currentList.find { matchOdd ->
                    matchOdd.oddsMap.toList()
                        .find { map -> map.second?.find { it == odd } != null } != null
                }))
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolderHdpOu.from(parent, oddStateRefreshListener)
    }


    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: List<Any>
    ) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            for (payload in payloads) {
                val item = getItem(position)
                val matchInfoList = currentList.mapNotNull {
                    it.matchInfo
                }
                val isTimerPause = item.matchInfo?.stopped == TimeCounting.STOP.value
                when (payload) {
                    PAYLOAD_SCORE_CHANGE -> {
                        (holder as ViewHolderHdpOu).setupMatchInfo(
                            item,
                            matchType,
                            matchInfoList,
                            leagueOddListener
                        )
                        (holder as ViewHolderHdpOu).setupMatchTime(item, matchType, isTimerEnable,isTimerPause)
                    }
                    PAYLOAD_CLOCK_CHANGE -> {
                        (holder as ViewHolderHdpOu).setupMatchTime(item, matchType, isTimerEnable,isTimerPause)
                    }
//                    PAYLOAD_ODDS_CHANGE -> {
//                        (holder as ViewHolderHdpOu).setupOddsButton(item, oddsType, leagueOddListener)
//                    }
                }
            }
        }
    }

    //[Martin] 我知道寫得很醜 但是socket更新太頻繁 只能用判斷式來判斷部分刷新
    //不要每次一有socket來 就重建整個View
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = currentList[position]
        val matchInfoList = currentList.mapNotNull {
            it.matchInfo
        }
        var viewHolder = holder as ViewHolderHdpOu
        val isTimerPause = item.matchInfo?.stopped == TimeCounting.STOP.value

        if(!viewHolder.itemView.hasTransientState()){
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
        } else if(updateType == PAYLOAD_SCORE_CHANGE){
            viewHolder.setupMatchInfo(
                item,
                matchType,
                matchInfoList,
                leagueOddListener
            )

            viewHolder.setupMatchTime(item, matchType, isTimerEnable,isTimerPause)
        }else if(updateType == PAYLOAD_CLOCK_CHANGE ){
            viewHolder.setupMatchInfo(
                item,
                matchType,
                matchInfoList,
                leagueOddListener
            )
            viewHolder.setupMatchTime(item, matchType, isTimerEnable,isTimerPause)
        }else if(updateType == PAYLOAD_ODDS_CHANGE){
            viewHolder.setupOddsButton(item, oddsType, leagueOddListener)
            viewHolder.setupMatchTime(item, matchType, isTimerEnable,isTimerPause)
        } else{
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
    }
    var updateType:String? = null
    fun submitList(list: List<MatchOdd>?,updateType :String?) {
        submitList(list)
        this.updateType = updateType
    }

    override fun submitList(list: List<MatchOdd>?) {
        super.submitList(if (list != null) ArrayList(list) else null)

    }


    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        updateType = null
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
            matchInfoList: List<MatchInfo>,

            ) {
            setupMatchInfo(item, matchType, matchInfoList, leagueOddListener)

            val isTimerPause = item.matchInfo?.stopped == TimeCounting.STOP.value
            setupMatchTime(item, matchType, isTimerEnable, isTimerPause)

            setupOddsButton(item, oddsType, leagueOddListener)
            setupQuickCategory(item, oddsType, leagueOddListener)
            itemView.setHasTransientState(true)
        }

        fun setupMatchInfo(
            item: MatchOdd,
            matchType: MatchType,
            matchInfoList: List<MatchInfo>,
            leagueOddListener: LeagueOddListener?
        ) {

            itemView.league_odd_match_name_home.text = item.matchInfo?.homeName

            itemView.league_odd_match_name_away.text = item.matchInfo?.awayName

            showStrongTeam(item)

            when (item.matchInfo?.gameType) {
                GameType.VB.key -> setVbScoreText(matchType, item)
                GameType.TN.key -> setTnScoreText(matchType, item)
                GameType.FT.key -> setFtScoreText(matchType, item)
                GameType.BK.key -> setBkScoreText(matchType, item)
                GameType.TT.key -> setVbScoreText(matchType, item)
                else -> setBkScoreText(matchType, item)//TODO Bill 這裡要等PM確認版型 SocketUpdateUtil
            }

            setStatusTextColor(item)

            itemView.league_odd_match_play_count.apply {
                text = item.matchInfo?.playCateNum.toString()

                setOnClickListener {
                    leagueOddListener?.onClickPlayType(item.matchInfo?.id, matchInfoList)
                }
            }

            itemView.league_odd_match_favorite.apply {
                isSelected = item.matchInfo?.isFavorite ?: false

                setOnClickListener {
                    leagueOddListener?.onClickFavorite(item.matchInfo?.id)
                }
            }

            itemView.league_odd_match_chart.apply {
                setOnClickListener {
                    leagueOddListener?.onClickStatistics(item.matchInfo?.id)
                }
            }

            itemView.league_odd_match_border_row1.setOnClickListener {
                leagueOddListener?.onClickPlayType(item.matchInfo?.id, matchInfoList)
            }

            itemView.league_odd_match_border_row2.setOnClickListener {
                leagueOddListener?.onClickPlayType(item.matchInfo?.id, matchInfoList)
            }

            itemView.league_odd_match_price_boost.isVisible = item.matchInfo?.eps == 1
            itemView.space2.isVisible = (item.matchInfo?.eps == 1 || item.matchInfo?.liveVideo == 1)
            itemView.iv_play.isVisible =
                item.matchInfo?.liveVideo == 1 && (matchType == MatchType.IN_PLAY || matchType == MatchType.MY_EVENT && item.matchInfo.isInPlay == true)

        }

        private fun setFtScoreText(matchType: MatchType, item: MatchOdd) {
            itemView.apply {
                setScoreTextAtFront(item)
                setCardText(matchType, item)
            }
        }

        private fun setBkScoreText(matchType: MatchType, item: MatchOdd) {
            itemView.apply {
                setScoreTextAtFront(item)
            }
        }

        private fun setVbScoreText(matchType: MatchType, item: MatchOdd) {
            itemView.apply {
                setAllScoreTextAtBottom(matchType, item)
                setScoreText(matchType, item)
                setSptText(item, matchType)
            }
        }

        private fun setTnScoreText(matchType: MatchType, item: MatchOdd) {

            itemView.apply {

                setAllScoreTextAtBottom(matchType, item)
                setScoreText(matchType, item)
                setPointText(matchType, item)
                setSptText(item, matchType)

            }
        }

        private fun setStatusTextColor(item: MatchOdd) {
            val color =
                if (item.matchInfo?.isInPlay == true) R.color.colorRedDark else R.color.colorGray
            itemView.apply {
                league_odd_match_status.setTextColor(ContextCompat.getColor(this.context, color))
                league_odd_spt.setTextColor(ContextCompat.getColor(this.context, color))
                league_odd_match_time.setTextColor(ContextCompat.getColor(this.context, color))
            }
        }

        //賽制(5盤3勝 or /int)
        @SuppressLint("SetTextI18n")
        private fun setSptText(item: MatchOdd, matchType: MatchType) {
            item.matchInfo?.spt?.let {
                when {
                    item.matchInfo.isInPlay == true -> { //除0以外顯示
                        itemView.league_odd_spt.visibility = if (it > 0) View.VISIBLE else View.GONE
                        itemView.league_odd_spt.text = " / $it"
                    }

                    matchType == MatchType.EARLY || matchType == MatchType.PARLAY || matchType == MatchType.TODAY || matchType == MatchType.AT_START || (matchType == MatchType.MY_EVENT && item.matchInfo.isInPlay == false)
                    -> {
                        if (it == 3 || it == 5) {//除3、5以外不顯示
                            itemView.league_spt.visibility = View.VISIBLE
                            itemView.league_spt.text = when (it) {
                                3 -> itemView.context.getString(R.string.spt_number_3_2)
                                5 -> itemView.context.getString(R.string.spt_number_5_3)
                                else -> ""
                            }
                        } else {
                            itemView.league_spt.visibility = View.GONE
                        }
                    }

                    else -> {
                    }
                }
            }
        }

        private fun View.setCardText(matchType: MatchType, item: MatchOdd) {
            league_odd_match_cards_home.apply {
                visibility = when {
                    (matchType == MatchType.IN_PLAY || (matchType == MatchType.MY_EVENT && item.matchInfo?.isInPlay ?: false))
                            && (item.matchInfo?.homeCards ?: 0 > 0) -> View.VISIBLE
                    else -> View.GONE
                }
                text = (item.matchInfo?.homeCards ?: 0).toString()
            }

            league_odd_match_cards_away.apply {
                visibility = when {
                    (matchType == MatchType.IN_PLAY || (matchType == MatchType.MY_EVENT && item.matchInfo?.isInPlay ?: false))
                            && (item.matchInfo?.awayCards ?: 0 > 0) -> View.VISIBLE
                    else -> View.GONE
                }
                text = (item.matchInfo?.awayCards ?: 0).toString()
            }
        }

        private fun View.setScoreTextAtFront(item: MatchOdd) {
            league_odd_match_score_home.apply {
                visibility = when (item.matchInfo?.isInPlay) {
                    true -> View.VISIBLE
                    else -> View.GONE
                }
                text = (item.matchInfo?.homeScore ?: 0).toString()
            }

            league_odd_match_score_away.apply {
                visibility = when (item.matchInfo?.isInPlay) {
                    true -> View.VISIBLE
                    else -> View.GONE
                }
                text = (item.matchInfo?.awayScore ?: 0).toString()
            }
        }

        private val isScoreTextVisible = { _: MatchType, item: MatchOdd ->
            when (item.matchInfo?.isInPlay) {
                true -> View.VISIBLE
                else -> View.GONE
            }
        }

        private fun View.setPointText(matchType: MatchType, item: MatchOdd) {
            league_odd_match_point_home_bottom.apply {
                visibility = isScoreTextVisible(matchType, item)
                text = (item.matchInfo?.homePoints ?: 0).toString()
            }

            league_odd_match_point_away_bottom.apply {
                visibility = isScoreTextVisible(matchType, item)
                text = (item.matchInfo?.awayPoints ?: 0).toString()
            }
        }

        private fun View.setScoreText(matchType: MatchType, item: MatchOdd) {
            league_odd_match_score_home_bottom.apply {
                visibility = isScoreTextVisible(matchType, item)
                text = (item.matchInfo?.homeScore ?: 0).toString()
            }

            league_odd_match_score_away_bottom.apply {
                visibility = isScoreTextVisible(matchType, item)
                text = (item.matchInfo?.awayScore ?: 0).toString()
            }
        }

        private fun View.setAllScoreTextAtBottom(matchType: MatchType, item: MatchOdd) {

            //hide front total score text
            league_odd_match_score_home.visibility = View.GONE
            league_odd_match_score_away.visibility = View.GONE

            league_odd_match_total_score_home_bottom.apply {
                visibility = isScoreTextVisible(matchType, item)
                text = (item.matchInfo?.homeTotalScore ?: 0).toString()
            }

            league_odd_match_total_score_away_bottom.apply {
                visibility = isScoreTextVisible(matchType, item)
                text = (item.matchInfo?.awayTotalScore ?: 0).toString()
            }
        }

        private fun showStrongTeam(item: MatchOdd) {
            itemView.apply {
                val oddListHDP = when (item.matchInfo?.gameType) {
                    GameType.TN.key -> {
                        item.oddsMap[PlayCate.SET_HDP.value]
                    }
                    GameType.BK.key -> {
                        item.oddsMap[PlayCate.HDP_INCL_OT.value]
                    }
                    else -> {
                        item.oddsMap[PlayCate.HDP.value]
                    }
                }
                val homeStrongType = if (oddListHDP?.getOrNull(0)?.spread?.contains("-") == true)
                    Typeface.BOLD
                else
                    Typeface.NORMAL

                val awayStrongType = if (oddListHDP?.getOrNull(1)?.spread?.contains("-") == true)
                    Typeface.BOLD
                else
                    Typeface.NORMAL

                league_odd_match_score_home.apply { setTypeface(this.typeface, homeStrongType) }
                league_odd_match_name_home.apply { setTypeface(this.typeface, homeStrongType) }

                league_odd_match_score_away.apply { setTypeface(this.typeface, awayStrongType) }
                league_odd_match_name_away.apply { setTypeface(this.typeface, awayStrongType) }
            }
        }

        fun setupMatchTime(
            item: MatchOdd,
            matchType: MatchType,
            isTimerEnable: Boolean,
            isTimerPause: Boolean
        ) {
            when {
                item.matchInfo?.isInPlay == true -> {
                    val socketValue = item.matchInfo.socketMatchStatus

                    if (needCountStatus(socketValue)) {
                        //會閃
                        //itemView.league_odd_match_time.visibility = View.VISIBLE
                        listener = object : TimerListener {
                            override fun onTimerUpdate(timeMillis: Long) {
                                if (timeMillis > 1000) {
                                    itemView.league_odd_match_time.text =
                                        TimeUtil.longToMmSs(timeMillis)
                                } else {
                                    itemView.league_odd_match_time.text =
                                        itemView.context.getString(R.string.time_null)
                                }
                                item.matchInfo.leagueTime = (timeMillis / 1000).toInt()
                            }
                        }


                        updateTimer(
                            isTimerEnable,
                            isTimerPause,
                            item.matchInfo.leagueTime ?: 0,
                            item.matchInfo.gameType == GameType.BK.key
                        )

                    } else {
                        //會閃
                        //itemView.league_odd_match_time.visibility = View.GONE
                    }
                }

                item.matchInfo?.isAtStart == true -> {
                    listener = object : TimerListener {
                        override fun onTimerUpdate(timeMillis: Long) {
                            itemView.league_odd_match_time.text = String.format(
                                itemView.context.resources.getString(R.string.at_start_remain_minute),
                                TimeUtil.longToMinute(timeMillis)
                            )
                            item.matchInfo.remainTime = timeMillis
                        }
                    }

                    item.matchInfo.remainTime?.let { remainTime ->
                        updateTimer(
                            isTimerEnable,
                            isTimerPause,
                            (remainTime / 1000).toInt(),
                            true
                        )
                    }
                }

                matchType == MatchType.MY_EVENT -> {
                    when {
                        item.matchInfo?.isInPlay ?: false -> {
                            item.matchInfo?.isAtStart = false
                            if (item.matchInfo?.gameType == GameType.FT.name || item.matchInfo?.gameType == GameType.BK.name) {
                                listener = object : TimerListener {
                                    override fun onTimerUpdate(timeMillis: Long) {
                                        if (timeMillis > 1000) {
                                            itemView.league_odd_match_time.text =
                                                TimeUtil.longToMmSs(timeMillis)
                                            item.matchInfo.leagueTime = (timeMillis / 1000).toInt()
                                        } else {
                                            itemView.league_odd_match_time.text =
                                                itemView.context.getString(R.string.time_null)
                                        }
                                    }
                                }

                                updateTimer(
                                    isTimerEnable,
                                    isTimerPause,
                                    item.matchInfo.leagueTime ?: 0,
                                    item.matchInfo.gameType == GameType.BK.key
                                )
                            }
                        }

                        item.matchInfo?.isAtStart ?: false -> {
                            item.matchInfo.apply {
                                this?.isAtStart = true
                                this?.remainTime = TimeUtil.getRemainTime(this?.startTime)
                            }
                            listener = object : TimerListener {
                                override fun onTimerUpdate(timeMillis: Long) {
                                    itemView.league_odd_match_time.text = String.format(
                                        itemView.context.resources.getString(R.string.at_start_remain_minute),
                                        TimeUtil.longToMmSs(timeMillis)
                                    )
                                    item.matchInfo?.remainTime = timeMillis
                                }
                            }

                            item.matchInfo?.remainTime?.let { remainTime ->
                                updateTimer(
                                    isTimerEnable,
                                    isTimerPause,
                                    (remainTime / 1000).toInt(),
                                    true
                                )
                            }
                        }

                        else -> {
                            //今日、早盤、串關
                            itemView.league_odd_match_time.text =
                                TimeUtil.timeFormat(item.matchInfo?.startTime, "HH:mm")
                        }
                    }
                }

                else -> {
                    itemView.league_odd_match_time.text = item.matchInfo?.startTimeDisplay

                }
            }

            setStatusText(item, matchType)
            setTextViewStatus(item, matchType)

            itemView.league_odd_match_remain_time_icon.apply {
                visibility = when {
                    item.matchInfo?.isAtStart == true -> View.VISIBLE
                    matchType == MatchType.TODAY -> View.VISIBLE
                    else -> View.INVISIBLE
                }
            }
        }

        private fun setStatusText(item: MatchOdd, matchType: MatchType) {
            itemView.league_odd_match_status.text = when {
                (matchType == MatchType.IN_PLAY
                        && item.matchInfo?.status == GameStatus.POSTPONED.code
                        && (item.matchInfo.gameType == GameType.FT.name || item.matchInfo.gameType == GameType.BK.name || item.matchInfo.gameType == GameType.TN.name)) -> {
                    itemView.context.getString(R.string.game_postponed)
                }

                matchType == MatchType.IN_PLAY || System.currentTimeMillis() > item.matchInfo?.startTime ?: 0 -> {
                    if (item.matchInfo?.statusName18n != null) {
                        item.matchInfo.statusName18n
                    } else {
                        return
                    }
                }

                matchType == MatchType.MY_EVENT -> {
                    when (item.matchInfo?.isInPlay) {
                        true -> item.matchInfo.statusName18n
                        else -> {
                            if (TimeUtil.isTimeToday(item.matchInfo?.startTime))
                                itemView.context.getString(TimeUtil.setupDayOfWeekAndToday(item.matchInfo?.startTime))
                            else
                                "${itemView.context.getString(TimeUtil.setupDayOfWeekAndToday(item.matchInfo?.startTime))} ${item.matchInfo?.startDateDisplay}"
                        }
                    }
                }

                matchType == MatchType.TODAY -> {
                    itemView.context.getString(TimeUtil.setupDayOfWeekAndToday(item.matchInfo?.startTime))
                }

                else -> {
                    "${itemView.context.getString(TimeUtil.setupDayOfWeekAndToday(item.matchInfo?.startTime))} ${item.matchInfo?.startDateDisplay}"
                }
            }
        }

        private fun setTextViewStatus(item: MatchOdd, matchType: MatchType) {
            when {
                (matchType == MatchType.IN_PLAY && item.matchInfo?.status == GameStatus.POSTPONED.code && (item.matchInfo.gameType == GameType.FT.name || item.matchInfo.gameType == GameType.BK.name || item.matchInfo.gameType == GameType.TN.name)) -> {
                    itemView.league_odd_spt.visibility = View.GONE
                    itemView.league_odd_match_time.visibility = View.GONE
                }

                matchType == MatchType.IN_PLAY || System.currentTimeMillis() > item.matchInfo?.startTime ?: 0 -> {
                    if (item.matchInfo?.statusName18n != null) {
                        itemView.league_odd_match_status.visibility = View.VISIBLE
                        (itemView.league_odd_match_status.layoutParams as LinearLayout.LayoutParams).marginEnd =
                            6
                    } else {
                        (itemView.league_odd_match_status.layoutParams as LinearLayout.LayoutParams).marginEnd =
                            0
                    }
                }

                matchType == MatchType.MY_EVENT -> {
                    when (item.matchInfo?.isInPlay) {
                        true -> item.matchInfo.statusName18n
                        else -> {
                            if (TimeUtil.isTimeToday(item.matchInfo?.startTime))
                                itemView.context.getString(TimeUtil.setupDayOfWeekAndToday(item.matchInfo?.startTime))
                            else
                                "${itemView.context.getString(TimeUtil.setupDayOfWeekAndToday(item.matchInfo?.startTime))} ${item.matchInfo?.startDateDisplay}"
                        }
                    }
                    when (item.matchInfo?.isAtStart) {
                        true -> itemView.league_odd_match_status.visibility = View.GONE
                    }
                }

                item.matchInfo?.isAtStart == true -> {
                    itemView.league_odd_match_status.visibility = View.GONE
                }
            }
        }

        fun setupOddsButton(
            item: MatchOdd,
            oddsType: OddsType,
            leagueOddListener: LeagueOddListener?
        ) {

            itemView.league_odd_btn_pager_main.apply {
                this.adapter =
                    OddButtonPagerAdapter(
                        item.matchInfo,
                        item.oddsSort,
                        item.playCateNameMap,
                        item.playCateMappingList
                    ).apply {

                        this.odds = item.oddsMap

                        this.oddsType = oddsType

                        this.listener =
                            OddButtonListener { matchInfo, odd, playCateCode, playCateName ->
                                leagueOddListener?.onClickBet(
                                    matchInfo,
                                    odd,
                                    playCateCode,
                                    playCateName,
                                    item.betPlayCateNameMap
                                )
                            }
                    }

                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageScrolled(
                        position: Int,
                        positionOffset: Float,
                        positionOffsetPixels: Int
                    ) {
                        super.onPageScrolled(position, positionOffset, positionOffsetPixels)

                        item.positionButtonPage = position
                    }
                })

                setCurrentItem(item.positionButtonPage, false)
                getChildAt(0)?.overScrollMode = View.OVER_SCROLL_NEVER //移除漣漪效果
                OverScrollDecoratorHelper.setUpOverScroll(
                    getChildAt(0) as RecyclerView,
                    OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL
                )
            }
            OverScrollDecoratorHelper.setUpOverScroll(itemView.league_odd_btn_pager_main)

            itemView.league_odd_btn_indicator_main.apply {

                visibility = if (item.oddsMap.size > 2) {
                    View.VISIBLE
                } else {
                    View.VISIBLE
                }

                setupWithViewPager2(itemView.league_odd_btn_pager_main)
            }
        }

        @SuppressLint("InflateParams")
        private fun setupQuickCategory(
            item: MatchOdd,
            oddsType: OddsType,
            leagueOddListener: LeagueOddListener?
        ) {
            itemView.league_odd_quick_cate_border.visibility =
                if (item.quickPlayCateList.isNullOrEmpty()) {
                    View.GONE
                } else {
                    View.VISIBLE
                }

            itemView.league_odd_quick_cate_divider.visibility =
                if (item.quickPlayCateList.isNullOrEmpty()) {
                    View.INVISIBLE
                } else {
                    View.VISIBLE
                }

            itemView.league_odd_quick_cate_close.apply {
                visibility = if (item.quickPlayCateList?.find { it.isSelected } == null) {
                    View.GONE
                } else {
                    View.VISIBLE
                }

                setOnClickListener {
                    leagueOddListener?.onClickQuickCateClose()
                }
            }
            itemView.league_odd_quick_cate_tabs.apply {
                visibility = if (item.quickPlayCateList.isNullOrEmpty()) {
                    View.GONE
                } else {
                    View.VISIBLE
                }

                item.quickPlayCateList?.sortedBy { it.sort }?.forEachIndexed { index, it ->
                    val inflater =
                        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val rb = inflater.inflate(R.layout.custom_radio_button, null) as RadioButton
                    addView(rb.apply {
                        text = it.nameMap?.get(LanguageManager.getSelectLanguage(context).key)
                            ?: it.name

                        id = it.hashCode()

                        setTextColor(
                            ContextCompat.getColorStateList(
                                context,
                                R.color.selector_tab_text_color
                            )
                        )

                        setButtonDrawable(R.drawable.selector_null)

                        setBackgroundResource(R.drawable.selector_tab)

                    })

                    if (it.isSelected) {
                        if (index > 3) {
                            itemView.scroll_view_rg.post {
                                itemView.scroll_view_rg.scrollTo(rb.left, 0)
                            }
                        }
                        rb.isChecked = true
                    }
                }

                setOnCheckedChangeListener { group, checkedId ->
                    item.quickPlayCateList?.forEach {
                        it.isSelected = (it.hashCode() == checkedId)
                        it.positionButtonPage = 0
                        it.positionButtonPairTab = 0
                    }

                    leagueOddListener?.onClickQuickCateTab(item.matchInfo?.id)
                }
            }

            when (item.quickPlayCateList?.find { it.isSelected }?.code) {
                QuickPlayCate.QUICK_OU.value, QuickPlayCate.QUICK_HDP.value, QuickPlayCate.QUICK_ADVANCE.value -> {
                    setupQuickOddButtonPair(item, oddsType, leagueOddListener)
                }

                QuickPlayCate.QUICK_CORNERS.value, QuickPlayCate.QUICK_PENALTY.value -> {
                    setupQuickOddButtonPager(item, oddsType, leagueOddListener)
                }

                QuickPlayCate.QUICK_EPS.value -> {
                    setupQuickOddButtonEps(item, oddsType, leagueOddListener)
                }

                else -> {
                    invisibleOddButtons()
                }
            }
        }

        private fun setupQuickOddButtonPair(
            item: MatchOdd,
            oddsType: OddsType,
            leagueOddListener: LeagueOddListener?
        ) {
            val adapter by lazy {
                OddButtonPairAdapter(item.matchInfo).apply {
                    this.oddsType = oddsType

                    listener = OddButtonListener { matchInfo, odd, playCateCode, playCateName ->
                        leagueOddListener?.onClickBet(
                            matchInfo,
                            odd,
                            playCateCode,
                            item.quickPlayCateList?.find { it.isSelected }?.name ?: playCateName,
                            item.betPlayCateNameMap
                        )
                    }
                }
            }

            val quickOdds = item.quickPlayCateList?.find { it.isSelected }?.quickOdds ?: mapOf()

            itemView.league_odd_quick_odd_btn_pair.visibility = View.VISIBLE

            itemView.quick_odd_pair_tab_1.apply {
                visibility =
                    if (quickOdds.keys.any { it == PlayCate.HDP.value || it == PlayCate.OU.value }
                        && !(quickOdds[PlayCate.HDP.value].isNullOrEmpty() && quickOdds[PlayCate.OU.value].isNullOrEmpty())) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
            }

            itemView.quick_odd_pair_tab_2.apply {
                visibility =
                    if (quickOdds.keys.any { it == PlayCate.HDP_1ST.value || it == PlayCate.OU_1ST.value }
                        && !(quickOdds[PlayCate.HDP_1ST.value].isNullOrEmpty() && quickOdds[PlayCate.OU_1ST.value].isNullOrEmpty())) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
            }

            itemView.quick_odd_pair_list.apply {
                this.adapter = adapter
            }

            itemView.quick_odd_pair_tab.apply {
                setOnCheckedChangeListener { _, checkedId ->
                    when (checkedId) {
                        R.id.quick_odd_pair_tab_1 -> {
                            item.quickPlayCateList?.find { it.isSelected }?.positionButtonPairTab =
                                0

                            adapter.odds =
                                quickOdds[PlayCate.HDP.value] ?: quickOdds[PlayCate.OU.value]
                                        ?: listOf()
                        }

                        R.id.quick_odd_pair_tab_2 -> {
                            item.quickPlayCateList?.find { it.isSelected }?.positionButtonPairTab =
                                1

                            adapter.odds = quickOdds[PlayCate.HDP_1ST.value]
                                ?: quickOdds[PlayCate.OU_1ST.value] ?: listOf()
                        }
                    }
                }
            }

            if (quickOdds.keys.any {
                    it == PlayCate.ADVANCE.value
                } && !(quickOdds[PlayCate.ADVANCE.value].isNullOrEmpty())) {
                adapter.odds = quickOdds[PlayCate.ADVANCE.value] ?: listOf()
            } else {
                itemView.quick_odd_pair_tab.check(
                    when {
                        (itemView.quick_odd_pair_tab_2.isVisible && item.quickPlayCateList?.find { it.isSelected }?.positionButtonPairTab == 1) -> R.id.quick_odd_pair_tab_2
                        else -> R.id.quick_odd_pair_tab_1
                    }
                )
            }
        }

        private fun setupQuickOddButtonPager(
            item: MatchOdd,
            oddsType: OddsType,
            leagueOddListener: LeagueOddListener?
        ) {
            itemView.league_odd_quick_odd_btn_pager.visibility = View.VISIBLE

            itemView.quick_odd_home.text = item.matchInfo?.homeName ?: ""

            itemView.quick_odd_away.text = item.matchInfo?.awayName ?: ""

            itemView.quick_odd_btn_pager_other.apply {
                this.adapter =
                    OddButtonPagerAdapter(
                        item.matchInfo,
                        item.oddsSort,
                        item.playCateNameMap,
                        item.playCateMappingList
                    ).apply {

                        this.odds = item.quickPlayCateList?.find { it.isSelected }?.quickOdds
                            ?: mutableMapOf()

                        this.oddsType = oddsType

                        this.listener =
                            OddButtonListener { matchInfo, odd, playCateCode, playCateName ->
                                leagueOddListener?.onClickBet(
                                    matchInfo,
                                    odd,
                                    playCateCode,
                                    playCateName,
                                    item.betPlayCateNameMap
                                )
                            }
                    }

                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageScrolled(
                        position: Int,
                        positionOffset: Float,
                        positionOffsetPixels: Int
                    ) {
                        super.onPageScrolled(position, positionOffset, positionOffsetPixels)

                        item.quickPlayCateList?.find { it.isSelected }?.positionButtonPage =
                            position
                    }
                })

                setCurrentItem(
                    item.quickPlayCateList?.find { it.isSelected }?.positionButtonPage ?: 0, false
                )
                getChildAt(0)?.overScrollMode = View.OVER_SCROLL_NEVER //移除漣漪效果
                OverScrollDecoratorHelper.setUpOverScroll(
                    getChildAt(0) as RecyclerView,
                    OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL
                )
            }

            itemView.quick_odd_btn_indicator_other.apply {
                visibility =
                    if (item.quickPlayCateList?.find { it.isSelected }?.quickOdds?.size ?: 0 > 2) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                setupWithViewPager2(itemView.quick_odd_btn_pager_other)
            }
        }

        private fun setupQuickOddButtonEps(
            item: MatchOdd,
            oddsType: OddsType,
            leagueOddListener: LeagueOddListener?
        ) {
            val adapter by lazy {
                OddButtonEpsAdapter(item.matchInfo).apply {
                    this.oddsType = oddsType

                    listener = OddButtonListener { matchInfo, odd, playCateCode, playCateName ->
                        leagueOddListener?.onClickBet(
                            matchInfo,
                            odd,
                            playCateCode,
                            item.quickPlayCateList?.find { it.isSelected }?.name ?: playCateName,
                            item.betPlayCateNameMap
                        )
                    }
                }
            }

            val quickOdds = item.quickPlayCateList?.find { it.isSelected }?.quickOdds ?: mapOf()

            itemView.league_odd_quick_odd_btn_eps.visibility = View.VISIBLE

            itemView.quick_odd_eps_list.apply {
                this.adapter = adapter.apply {
                    data = quickOdds[quickOdds.keys.firstOrNull()] ?: listOf()
                }
            }
        }

        private fun invisibleOddButtons() {
            itemView.league_odd_quick_odd_btn_pair.visibility = View.GONE
            itemView.league_odd_quick_odd_btn_pager.visibility = View.GONE
            itemView.league_odd_quick_odd_btn_eps.visibility = View.GONE
        }

        companion object {
            fun from(parent: ViewGroup, refreshListener: OddStateChangeListener): ViewHolderHdpOu {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view =
                    layoutInflater.inflate(R.layout.itemview_league_odd_v4, parent, false)

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

        fun updateTimer(
            isTimerEnable: Boolean,
            isTimerPause: Boolean,
            startTime: Int,
            isDecrease: Boolean
        ) {
            when (isTimerEnable) {
                false -> {
                    stopTimer()
                }

                true -> {
                    startTimer(isTimerPause, startTime, isDecrease)
                }

            }
        }

        private fun startTimer(isTimerPause: Boolean, startTime: Int, isDecrease: Boolean) {
            var timeMillis = startTime * 1000L
            stopTimer()
            Handler(Looper.getMainLooper()).post {
                listener?.onTimerUpdate(timeMillis)
            }

            timer = Timer()
            timer?.schedule(object : TimerTask() {
                override fun run() {
                    when (isDecrease) {
                        true -> {
                            if (!isTimerPause) timeMillis -= 1000
                        }
                        false -> {
                            if (!isTimerPause) timeMillis += 1000
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


        fun stopTimer() {
            timer?.cancel()
            timer = null
        }
    }

    //override fun getItemCount() = itemData.size
}

class LeagueOddListener(
    val clickListenerPlayType: (matchId: String?, matchInfoList: List<MatchInfo>) -> Unit,
    val clickListenerBet: (matchInfo: MatchInfo?, odd: Odd, playCateCode: String, playCateName: String, betPlayCateNameMap: Map<String?, Map<String?, String?>?>?) -> Unit,
    val clickListenerQuickCateTab: (matchId: String?) -> Unit,
    val clickListenerQuickCateClose: () -> Unit,
    val clickListenerFavorite: (matchId: String?) -> Unit,
    val clickListenerStatistics: (matchId: String?) -> Unit
) {
    fun onClickPlayType(matchId: String?, matchInfoList: List<MatchInfo>) =
        clickListenerPlayType(matchId, matchInfoList)

    fun onClickBet(
        matchInfo: MatchInfo?,
        odd: Odd,
        playCateCode: String,
        playCateName: String = "",
        betPlayCateNameMap: Map<String?, Map<String?, String?>?>?
    ) = clickListenerBet(matchInfo, odd, playCateCode, playCateName, betPlayCateNameMap)

    fun onClickQuickCateTab(matchId: String?) = clickListenerQuickCateTab(matchId)

    fun onClickQuickCateClose() = clickListenerQuickCateClose()

    fun onClickFavorite(matchId: String?) = clickListenerFavorite(matchId)

    fun onClickStatistics(matchId: String?) = clickListenerStatistics(matchId)
}
class ItemDiffCallback : DiffUtil.ItemCallback<MatchOdd>() {

    override fun getChangePayload(oldItem: MatchOdd, newItem: MatchOdd): Any? {
        return if (oldItem.matchInfo?.homeTotalScore != newItem.matchInfo?.homeTotalScore) PAYLOAD_SCORE_CHANGE
        else if(oldItem.matchInfo?.awayTotalScore != newItem.matchInfo?.awayTotalScore ) PAYLOAD_SCORE_CHANGE
        else if(oldItem.matchInfo?.awayPoints != newItem.matchInfo?.awayPoints) PAYLOAD_SCORE_CHANGE
        else if(oldItem.matchInfo?.homePoints != newItem.matchInfo?.homePoints ) PAYLOAD_SCORE_CHANGE
        else if(oldItem.matchInfo?.awayScore != newItem.matchInfo?.awayScore) PAYLOAD_SCORE_CHANGE
        else if(oldItem.matchInfo?.homeScore != newItem.matchInfo?.homeScore) PAYLOAD_SCORE_CHANGE
        else if(oldItem.leagueTime != newItem.leagueTime) PAYLOAD_CLOCK_CHANGE
        else null
    }

    override fun areItemsTheSame(oldItem: MatchOdd, newItem: MatchOdd): Boolean {
        return oldItem.matchInfo?.id == newItem.matchInfo?.id
    }

    override fun areContentsTheSame(oldItem: MatchOdd, newItem: MatchOdd): Boolean {
        return false
    }
}
