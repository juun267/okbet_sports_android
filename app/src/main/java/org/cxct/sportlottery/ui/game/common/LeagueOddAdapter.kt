package org.cxct.sportlottery.ui.game.common

import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
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
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.needCountStatus
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
                    matchOdd.oddsMap.toList()
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

            setupOddsButton(item, oddsType, leagueOddListener)

            setupQuickCategory(item, leagueOddListener)
        }

        private fun setupMatchInfo(
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
            }

            setStatusTextColor(matchType)

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

            val isIconVisible = if (item.matchInfo?.eps == 1) View.VISIBLE else View.GONE
            isIconVisible.let {
                itemView.league_odd_match_price_boost.visibility = it
                itemView.space_icon.visibility = it
            }
        }

        private fun setFtScoreText(matchType: MatchType, item: MatchOdd) {
            itemView.apply {
                setScoreTextAtFront(matchType, item)
                setCardText(matchType, item)
            }
        }

        private fun setBkScoreText(matchType: MatchType, item: MatchOdd) {
            itemView.apply {
                setScoreTextAtFront(matchType, item)
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

        private fun setStatusTextColor(matchType: MatchType) {
            val color =
                if (matchType == MatchType.IN_PLAY) R.color.colorRedDark else R.color.colorGray
            itemView.apply {
                league_odd_match_status.setTextColor(ContextCompat.getColor(this.context, color))
                league_odd_spt.setTextColor(ContextCompat.getColor(this.context, color))
                league_odd_match_time.setTextColor(ContextCompat.getColor(this.context, color))
            }
        }

        //賽制(5盤3勝 or /int)
        private fun setSptText(item: MatchOdd, matchType: MatchType) {
            item.matchInfo?.spt?.let {
                when (matchType) {
                    MatchType.IN_PLAY -> { //除0以外顯示
                        itemView.league_odd_spt.visibility = if (it > 0) View.VISIBLE else View.GONE
                        itemView.league_odd_spt.text = " / $it"
                    }

                    MatchType.EARLY, MatchType.PARLAY, MatchType.TODAY, MatchType.AT_START -> { //TODO: 串關尚未確定顯示邏輯(是否要判斷滾球做不同顯示?)
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

        private fun View.setScoreTextAtFront(matchType: MatchType, item: MatchOdd) {
            league_odd_match_score_home.apply {
                visibility = when {
                    matchType == MatchType.IN_PLAY || (matchType == MatchType.MY_EVENT && item.matchInfo?.isInPlay ?: false) -> View.VISIBLE
                    else -> View.GONE
                }
                text = (item.matchInfo?.homeScore ?: 0).toString()
            }

            league_odd_match_score_away.apply {
                visibility = when {
                    matchType == MatchType.IN_PLAY || (matchType == MatchType.MY_EVENT && item.matchInfo?.isInPlay ?: false) -> View.VISIBLE
                    else -> View.GONE
                }
                text = (item.matchInfo?.awayScore ?: 0).toString()
            }
        }

        private val isScoreTextVisible = { matchType: MatchType, item: MatchOdd ->
            when {
                matchType == MatchType.IN_PLAY ||
                        (matchType == MatchType.MY_EVENT && item.matchInfo?.isInPlay ?: false) -> View.VISIBLE
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

        private fun setupMatchTime(
            item: MatchOdd,
            matchType: MatchType,
            isTimerEnable: Boolean
        ) {
            when (matchType) {
                MatchType.IN_PLAY -> {
                    val socketValue = item.matchInfo?.socketMatchStatus

                    if (needCountStatus(socketValue)) {
                        itemView.league_odd_match_time.visibility = View.VISIBLE
                        listener = object : TimerListener {
                            override fun onTimerUpdate(timeMillis: Long) {
                                itemView.league_odd_match_time.text =
                                    TimeUtil.timeFormat(timeMillis, "mm:ss")
                                item.matchInfo?.leagueTime = (timeMillis / 1000).toInt()
                            }
                        }

                        updateTimer(
                            isTimerEnable,
                            item.matchInfo?.leagueTime ?: 0,
                            item.matchInfo?.gameType == GameType.BK.key
                        )

                    } else {
                        itemView.league_odd_match_time.visibility = View.GONE
                    }
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

                MatchType.MY_EVENT -> {
                    when {
                        item.matchInfo?.isInPlay ?: false -> {
                            item.matchInfo?.isAtStart = false
                            if (item.matchInfo?.gameType == GameType.FT.name || item.matchInfo?.gameType == GameType.BK.name) {
                                listener = object : TimerListener {
                                    override fun onTimerUpdate(timeMillis: Long) {
                                        itemView.league_odd_match_time.text =
                                            TimeUtil.timeFormat(timeMillis, "mm:ss")
                                        item.matchInfo.leagueTime = (timeMillis / 1000).toInt()
                                    }
                                }

                                updateTimer(
                                    isTimerEnable,
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

            itemView.league_odd_match_remain_time_icon.apply {
                visibility = when {
                    matchType == MatchType.AT_START -> View.VISIBLE
                    matchType == MatchType.TODAY -> View.VISIBLE
                    matchType == MatchType.MY_EVENT && item.matchInfo?.isAtStart == true -> View.VISIBLE
                    else -> View.INVISIBLE
                }
            }
        }

        private fun setStatusText(item: MatchOdd, matchType: MatchType) {
            itemView.league_odd_match_status.text = when {
                (matchType == MatchType.IN_PLAY &&
                        item.matchInfo?.status == 3 &&
                        (item.matchInfo.gameType == GameType.FT.name || item.matchInfo.gameType == GameType.BK.name || item.matchInfo.gameType == GameType.TN.name)) -> {
                    itemView.league_odd_spt.visibility = View.GONE
                    itemView.league_odd_match_time.visibility = View.GONE
                    itemView.context.getString(R.string.game_postponed)
                }

                matchType == MatchType.IN_PLAY || System.currentTimeMillis() > item.matchInfo?.startTime ?: 0 -> {
                    if (item.matchInfo?.statusName != null) {
                        itemView.league_odd_match_status.visibility = View.VISIBLE
                        (itemView.league_odd_match_status.layoutParams as LinearLayout.LayoutParams).marginEnd =
                            6
                        item.matchInfo.statusName
                    } else {
                        (itemView.league_odd_match_status.layoutParams as LinearLayout.LayoutParams).marginEnd = 0
                        return
                    }
                }

                matchType == MatchType.MY_EVENT -> {
                    when (item.matchInfo?.isInPlay) {
                        true -> item.matchInfo.statusName
                        else -> TimeUtil.timeFormat(item.matchInfo?.startTime, "MM/dd")
                    }
                }
                matchType == MatchType.AT_START -> {
                    itemView.league_odd_match_status.visibility = View.GONE
                    return
                }
                matchType == MatchType.TODAY -> {
                    itemView.context.getString(TimeUtil.setupDayOfWeekAndToday(item.matchInfo?.startTime))
                }

                else -> {
                    "${itemView.context.getString(TimeUtil.setupDayOfWeekAndToday(item.matchInfo?.startTime))} ${item.matchInfo?.startDateDisplay}"
                }
            }
        }

        private fun setupOddsButton(
            item: MatchOdd,
            oddsType: OddsType,
            leagueOddListener: LeagueOddListener?
        ) {

            itemView.league_odd_btn_pager_main.apply {
                this.adapter = OddButtonPagerAdapter(item.matchInfo).apply {

                    this.odds = item.oddsMap

                    this.oddsType = oddsType

                    this.listener = OddButtonListener { matchInfo, odd, playCateName ->
                        leagueOddListener?.onClickBet(matchInfo, odd, playCateName)
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
            }

            itemView.league_odd_btn_indicator_main.apply {

                visibility = if (item.oddsMap.size > 2) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

                setupWithViewPager2(itemView.league_odd_btn_pager_main)
            }
        }

        private fun setupQuickCategory(
            item: MatchOdd,
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
                    View.GONE
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

                item.quickPlayCateList?.forEach {
                    addView(RadioButton(context).apply {
                        text = it.name

                        id = it.hashCode()

                        setTextColor(
                            ContextCompat.getColorStateList(
                                context,
                                R.color.selector_tab_text_color
                            )
                        )

                        setButtonDrawable(R.drawable.selector_null)

                        setBackgroundResource(R.drawable.selector_tab)

                    }, LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    ).apply {
                        rightMargin =
                            itemView.context.resources.getDimensionPixelOffset(R.dimen.textSize20sp)
                    })

                    if (it.isSelected) {
                        check(it.hashCode())
                    }
                }

                setOnCheckedChangeListener { _, checkedId ->
                    item.quickPlayCateList?.forEach {
                        it.isSelected = (it.hashCode() == checkedId)
                        it.positionButtonPage = 0
                        it.positionButtonPairTab = 0
                    }

                    leagueOddListener?.onClickQuickCateTab(item.matchInfo?.id)
                }
            }

            when (item.quickPlayCateList?.find { it.isSelected }?.code) {
                QuickPlayCate.QUICK_OU.value, QuickPlayCate.QUICK_HDP.value -> {
                    setupQuickOddButtonPair(item, leagueOddListener)
                }

                QuickPlayCate.QUICK_CORNERS.value, QuickPlayCate.QUICK_PENALTY.value, QuickPlayCate.QUICK_ADVANCE.value -> {
                    setupQuickOddButtonPager(item, leagueOddListener)
                }

                QuickPlayCate.QUICK_EPS.value -> {
                    setupQuickOddButtonEps(item, leagueOddListener)
                }

                else -> {
                    invisibleOddButtons()
                }
            }
        }

        private fun setupQuickOddButtonPair(
            item: MatchOdd,
            leagueOddListener: LeagueOddListener?
        ) {
            val adapter by lazy {
                OddButtonPairAdapter(item.matchInfo).apply {
                    listener = OddButtonListener { matchInfo, odd, playCateName ->
                        leagueOddListener?.onClickBet(
                            matchInfo,
                            odd,
                            item.quickPlayCateList?.find { it.isSelected }?.name ?: playCateName
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

                check(
                    when {
                        (itemView.quick_odd_pair_tab_2.isVisible && item.quickPlayCateList?.find { it.isSelected }?.positionButtonPairTab == 1) -> R.id.quick_odd_pair_tab_2
                        else -> R.id.quick_odd_pair_tab_1
                    }
                )
            }
        }

        private fun setupQuickOddButtonPager(
            item: MatchOdd,
            leagueOddListener: LeagueOddListener?
        ) {
            itemView.league_odd_quick_odd_btn_pager.visibility = View.VISIBLE

            itemView.quick_odd_home.text = item.matchInfo?.homeName ?: ""

            itemView.quick_odd_away.text = item.matchInfo?.awayName ?: ""

            itemView.quick_odd_btn_pager_other.apply {
                this.adapter = OddButtonPagerAdapter(item.matchInfo).apply {

                    this.odds = item.quickPlayCateList?.find { it.isSelected }?.quickOdds ?: mapOf()

                    this.oddsType = oddsType

                    this.listener = OddButtonListener { matchInfo, odd, playCateName ->
                        leagueOddListener?.onClickBet(matchInfo, odd, playCateName)
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

        private fun setupQuickOddButtonEps(item: MatchOdd, leagueOddListener: LeagueOddListener?) {
            val adapter by lazy {
                OddButtonEpsAdapter(item.matchInfo).apply {
                    listener = OddButtonListener { matchInfo, odd, playCateName ->
                        leagueOddListener?.onClickBet(
                            matchInfo,
                            odd,
                            item.quickPlayCateList?.find { it.isSelected }?.name ?: playCateName
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
    val clickListenerPlayType: (matchId: String?, matchInfoList: List<MatchInfo>) -> Unit,
    val clickListenerBet: (matchInfo: MatchInfo?, odd: Odd, playCateName: String) -> Unit,
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
        playCateName: String = "",
    ) = clickListenerBet(matchInfo, odd, playCateName)

    fun onClickQuickCateTab(matchId: String?) = clickListenerQuickCateTab(matchId)

    fun onClickQuickCateClose() = clickListenerQuickCateClose()

    fun onClickFavorite(matchId: String?) = clickListenerFavorite(matchId)

    fun onClickStatistics(matchId: String?) = clickListenerStatistics(matchId)
}