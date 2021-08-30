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
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import kotlinx.android.synthetic.main.itemview_league_odd_v4.view.*
import kotlinx.android.synthetic.main.itemview_league_odd_v4.view.league_odd_btn_indicator_main
import kotlinx.android.synthetic.main.itemview_league_odd_v4.view.league_odd_btn_indicator_other
import kotlinx.android.synthetic.main.itemview_league_odd_v4.view.league_odd_btn_pager_main
import kotlinx.android.synthetic.main.itemview_league_odd_v4.view.league_odd_btn_pager_other
import kotlinx.android.synthetic.main.itemview_league_odd_v4.view.league_odd_match_border_row1
import kotlinx.android.synthetic.main.itemview_league_odd_v4.view.league_odd_match_border_row2
import kotlinx.android.synthetic.main.itemview_league_odd_v4.view.league_odd_match_favorite
import kotlinx.android.synthetic.main.itemview_league_odd_v4.view.league_odd_match_name_away
import kotlinx.android.synthetic.main.itemview_league_odd_v4.view.league_odd_match_name_home
import kotlinx.android.synthetic.main.itemview_league_odd_v4.view.league_odd_match_play_count
import kotlinx.android.synthetic.main.itemview_league_odd_v4.view.league_odd_match_price_boost
import kotlinx.android.synthetic.main.itemview_league_odd_v4.view.league_odd_match_remain_time_icon
import kotlinx.android.synthetic.main.itemview_league_odd_v4.view.league_odd_match_status
import kotlinx.android.synthetic.main.itemview_league_odd_v4.view.league_odd_match_time
import kotlinx.android.synthetic.main.itemview_league_odd_v4.view.league_odd_match_total_score_away
import kotlinx.android.synthetic.main.itemview_league_odd_v4.view.league_odd_match_total_score_home
import kotlinx.android.synthetic.main.itemview_league_odd_v4.view.league_odd_quick_away
import kotlinx.android.synthetic.main.itemview_league_odd_v4.view.league_odd_quick_button_border
import kotlinx.android.synthetic.main.itemview_league_odd_v4.view.league_odd_quick_button_bottom_margin
import kotlinx.android.synthetic.main.itemview_league_odd_v4.view.league_odd_quick_cate_border
import kotlinx.android.synthetic.main.itemview_league_odd_v4.view.league_odd_quick_cate_close
import kotlinx.android.synthetic.main.itemview_league_odd_v4.view.league_odd_quick_cate_divider
import kotlinx.android.synthetic.main.itemview_league_odd_v4.view.league_odd_quick_cate_tabs
import kotlinx.android.synthetic.main.itemview_league_odd_v4.view.league_odd_quick_home
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.ui.menu.OddsType
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
        return ViewHolderHdpOu.from(parent, oddStateRefreshListener, data.firstOrNull()?.matchInfo?.gameType)
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

            setupQuickCategory(item, leagueOddListener)

            setupOddsButton(item, oddsType, leagueOddListener)
        }

        private fun setupMatchInfo(
            item: MatchOdd,
            matchType: MatchType,
            matchInfoList: List<MatchInfo>,
            leagueOddListener: LeagueOddListener?
        ) {
            val isVbTn = (item.matchInfo?.gameType == GameType.VB.key || item.matchInfo?.gameType == GameType.TN.key)

            itemView.league_odd_match_name_home.text = item.matchInfo?.homeName

            itemView.league_odd_match_name_away.text = item.matchInfo?.awayName

            showStrongTeam(item)

            item.matchInfo?.eps?.let {
                if (it > 0) itemView.league_odd_eps.visibility = View.VISIBLE else
                    itemView.league_odd_eps.text = " /${it}"
            }

            if (isVbTn) setVbTnScoreText(matchType, item) else setScoreText(matchType, item)


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

            itemView.league_odd_match_border_row1.setOnClickListener {
                leagueOddListener?.onClickPlayType(item.matchInfo?.id, matchInfoList)
            }

            itemView.league_odd_match_border_row2.setOnClickListener {
                leagueOddListener?.onClickPlayType(item.matchInfo?.id, matchInfoList)
            }
            itemView.league_odd_match_price_boost.apply {
                this.visibility = if (item.matchInfo?.eps == 1) View.VISIBLE else View.GONE
            }
        }

        private fun setScoreText(matchType: MatchType, item: MatchOdd) {
            itemView.league_odd_match_total_score_home.apply {
                visibility = when {
                    matchType == MatchType.IN_PLAY || (matchType == MatchType.MY_EVENT && item.matchInfo?.isInPlay ?: false) -> View.VISIBLE
                    else -> View.GONE
                }
        //                text = (item.matchInfo?.totalHomeScore ?: 0).toString()
            }

            itemView.league_odd_match_total_score_away.apply {
                visibility = when {
                    matchType == MatchType.IN_PLAY || (matchType == MatchType.MY_EVENT && item.matchInfo?.isInPlay ?: false) -> View.VISIBLE
                    else -> View.GONE
                }

        //                text = (item.matchInfo?.totalAwayScore ?: 0).toString()
            }
        }

        private fun setVbTnScoreText(matchType: MatchType, item: MatchOdd) {

            itemView.apply {

                //hide
                league_odd_match_total_score_home.visibility = View.GONE
                league_odd_match_total_score_away.visibility = View.GONE

                //home
                league_odd_match_total_score_home_vb_tn.apply {
                    visibility = when {
                        matchType == MatchType.IN_PLAY || (matchType == MatchType.MY_EVENT && item.matchInfo?.isInPlay ?: false) -> View.VISIBLE
                        else -> View.GONE
                    }
                    text = (item.matchInfo?.homeScore ?: 0).toString()
                }

                league_odd_match_score_home.apply {
                    visibility = when {
                        matchType == MatchType.IN_PLAY || (matchType == MatchType.MY_EVENT && item.matchInfo?.isInPlay ?: false) -> View.VISIBLE
                        else -> View.GONE
                    }
                    text = (item.matchInfo?.homeScore ?: 0).toString()
                }

                league_odd_match_point_home.apply {
                    visibility = when {
                        matchType == MatchType.IN_PLAY || (matchType == MatchType.MY_EVENT && item.matchInfo?.isInPlay ?: false) -> View.VISIBLE
                        else -> View.GONE
                    }
                    text = (item.matchInfo?.homeScore ?: 0).toString()
                }

                //away
                league_odd_match_total_score_away_vb_tn.apply {
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

                league_odd_match_point_away.apply {
                    visibility = when {
                        matchType == MatchType.IN_PLAY || (matchType == MatchType.MY_EVENT && item.matchInfo?.isInPlay ?: false) -> View.VISIBLE
                        else -> View.GONE
                    }

                    text = (item.matchInfo?.awayScore ?: 0).toString()
                }

            }
        }

        private fun showStrongTeam(item: MatchOdd) {
            itemView.apply {
                val oddListHDP = when (item.matchInfo?.gameType) {
                    GameType.TN.key -> {
                        item.odds[PlayCate.SET_HDP.value]
                    }
                    GameType.BK.key -> {
                        item.odds[PlayCate.HDP_INCL_OT.value]
                    }
                    else -> {
                        item.odds[PlayCate.HDP.value]
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

                league_odd_match_total_score_home.apply { setTypeface(this.typeface, homeStrongType) }
                league_odd_match_name_home.apply { setTypeface(this.typeface, homeStrongType) }

                league_odd_match_total_score_away.apply { setTypeface(this.typeface, awayStrongType) }
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
                                        item.leagueTime = (timeMillis / 1000).toInt()
                                    }
                                }

                                updateTimer(
                                    isTimerEnable,
                                    item.leagueTime ?: 0,
                                    item.matchInfo?.gameType == GameType.BK.key
                                )
                            }
                        }
                        else -> {
                            //即將開賽
                            val timeMillis = TimeUtil.getRemainTime(item.matchInfo?.startTime)
                            if (timeMillis < 60 * 60 * 1000L) {
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
                            } else
                                itemView.league_odd_match_time.text =
                                    TimeUtil.timeFormat(item.matchInfo?.startTime, "HH:mm")
                        }
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
                MatchType.MY_EVENT -> {
                    when(item.matchInfo?.isInPlay) {
                        true -> item.matchInfo.statusName
                        else -> TimeUtil.timeFormat(item.matchInfo?.startTime, "MM/dd")
                    }
                }
                else -> {
                    item.matchInfo?.startDateDisplay
                }
            }

            itemView.league_odd_match_remain_time_icon.apply {
                visibility = when {
                    matchType == MatchType.AT_START -> View.VISIBLE
                    matchType == MatchType.MY_EVENT && item.matchInfo?.isAtStart == true -> View.VISIBLE
                    else -> View.INVISIBLE
                }
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
                    }

                    leagueOddListener?.onClickQuickCateTab(item.matchInfo?.id)
                }
            }

            itemView.league_odd_quick_button_border.apply {
                visibility = if (item.quickPlayCateList?.find { it.isSelected } == null) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            }

            itemView.league_odd_quick_button_bottom_margin.apply {
                visibility = if (item.quickPlayCateList?.find { it.isSelected } == null) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            }

            itemView.league_odd_quick_home.apply {
                visibility = if (item.quickPlayCateList?.find { it.isSelected } == null) {
                    View.GONE
                } else {
                    View.VISIBLE
                }

                text = item.matchInfo?.homeName
            }

            itemView.league_odd_quick_away.apply {
                visibility = if (item.quickPlayCateList?.find { it.isSelected } == null) {
                    View.GONE
                } else {
                    View.VISIBLE
                }

                text = item.matchInfo?.awayName
            }

            itemView.league_odd_btn_pager_other.apply {
                visibility = if (item.quickPlayCateList?.find { it.isSelected } == null) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            }

            itemView.league_odd_btn_indicator_other.apply {
                visibility = if (item.quickPlayCateList?.find { it.isSelected } == null) {
                    View.GONE
                } else {
                    View.VISIBLE
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

                    this.odds = item.odds

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

                visibility = if (item.odds.size > 2) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

                setupWithViewPager2(itemView.league_odd_btn_pager_main)
            }


            itemView.league_odd_btn_pager_other.apply {
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

            itemView.league_odd_btn_indicator_other.apply {

                visibility =
                    if (item.quickPlayCateList?.find { it.isSelected }?.quickOdds?.size ?: 0 > 2) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }

                setupWithViewPager2(itemView.league_odd_btn_pager_other)
            }
        }

        companion object {
            fun from(parent: ViewGroup, refreshListener: OddStateChangeListener, gameType: String?): ViewHolderHdpOu {
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
    val clickListenerFavorite: (matchId: String?) -> Unit
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
}