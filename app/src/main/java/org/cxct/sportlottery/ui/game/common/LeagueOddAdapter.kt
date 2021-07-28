package org.cxct.sportlottery.ui.game.common

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.button_odd_detail.view.*
import kotlinx.android.synthetic.main.itemview_league_odd_v4.view.*
import kotlinx.android.synthetic.main.view_odd_btn_column_v4.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.ui.game.PlayCateUtils
import org.cxct.sportlottery.network.odds.Odd
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

            setupOddsButton(
                item.matchInfo,
                item.odds,
                oddsType,
                leagueOddListener,
                itemView.odd_button_main
            )

            setupQuickCategory(item, leagueOddListener)

            setupOddsButton(
                item.matchInfo,
                item.quickPlayCateList?.find { it.isSelected }?.quickOdds?.mapValues {
                    it.value?.odds ?: listOf()
                },
                oddsType,
                leagueOddListener,
                itemView.odd_button_other
            )
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

        private fun setupOddsButton(
            matchInfo: MatchInfo?,
            odds: Map<String, List<Odd?>>?,
            oddsType: OddsType,
            leagueOddListener: LeagueOddListener?,
            viewGroup: ViewGroup
        ) {
            odds?.forEach {
                val view = LayoutInflater.from(itemView.context).inflate(
                    R.layout.view_odd_btn_column_v4,
                    itemView.odd_button_main,
                    false
                ).apply {

                    val playCateName = PlayCateUtils
                        .getPlayCateTitleResId(it.key, matchInfo?.gameType)?.let {
                            itemView.context.getString(it)
                        } ?: ""

                    this.odd_btn_type.text = playCateName

                    this.odd_btn_home.apply homeButtonSettings@{
                        when {
                            (it.value.size < 2) -> {
                                betStatus = BetStatus.LOCKED.code
                                return@homeButtonSettings
                            }
                            else -> {
                                betStatus = it.value[0]?.status
                            }
                        }

                        tv_name.apply {
                            visibility = when {
                                PlayCateUtils.getOUSeries().map { it.value }
                                    .contains(it.key) -> View.VISIBLE
                                else -> {
                                    when (!it.value[0]?.spread.isNullOrEmpty()) {
                                        true -> View.INVISIBLE
                                        false -> View.GONE
                                    }
                                }
                            }

                            text = when {
                                PlayCateUtils.getOUSeries().map { it.value }.contains(it.key) -> {
                                    itemView.context.getString(R.string.odd_button_ou_o)
                                }
                                else -> ""
                            }
                        }

                        tv_spread.apply {
                            visibility = when (!it.value[0]?.spread.isNullOrEmpty()) {
                                true -> View.VISIBLE
                                false -> {
                                    when {
                                        PlayCateUtils.getOUSeries().map { it.value }
                                            .contains(it.key) -> View.INVISIBLE
                                        else -> View.GONE
                                    }
                                }
                            }

                            text = it.value[0]?.spread ?: ""
                        }

                        tv_odds.text = when (oddsType) {
                            OddsType.EU -> it.value[0]?.odds.toString()
                            OddsType.HK -> it.value[0]?.hkOdds.toString()
                        }

                        this@ViewHolderHdpOu.setupOddState(this, it.value[0])

                        isSelected = it.value[0]?.isSelected ?: false

                        setOnClickListener { _ ->
                            it.value[0]?.let { odd ->

                                val playName = when {
                                    PlayCateUtils.getOUSeries().map { it.value }
                                        .contains(it.key) -> {
                                        itemView.context.getString(R.string.odd_button_ou_o)
                                    }
                                    else -> {
                                        matchInfo?.homeName
                                    }
                                } ?: ""

                                leagueOddListener?.onClickBet(
                                    matchInfo,
                                    odd,
                                    playCateName,
                                    playName
                                )
                            }
                        }
                    }

                    this.odd_btn_away.apply awayButtonSettings@{
                        when {
                            (it.value.size < 2) -> {
                                betStatus = BetStatus.LOCKED.code
                                return@awayButtonSettings
                            }
                            else -> {
                                betStatus = it.value[1]?.status
                            }
                        }

                        tv_name.apply {
                            visibility = when {
                                PlayCateUtils.getOUSeries().map { it.value }
                                    .contains(it.key) -> View.VISIBLE
                                else -> {
                                    when (!it.value[1]?.spread.isNullOrEmpty()) {
                                        true -> View.INVISIBLE
                                        false -> View.GONE
                                    }
                                }
                            }

                            text = when {
                                PlayCateUtils.getOUSeries().map { it.value }.contains(it.key) -> {
                                    itemView.context.getString(R.string.odd_button_ou_u)
                                }
                                else -> ""
                            }
                        }

                        tv_spread.apply {
                            visibility = when (!it.value[1]?.spread.isNullOrEmpty()) {
                                true -> View.VISIBLE
                                false -> {
                                    when {
                                        PlayCateUtils.getOUSeries().map { it.value }
                                            .contains(it.key) -> View.INVISIBLE
                                        else -> View.GONE
                                    }
                                }
                            }

                            text = it.value[1]?.spread ?: ""
                        }

                        tv_odds.text = when (oddsType) {
                            OddsType.EU -> it.value[1]?.odds.toString()
                            OddsType.HK -> it.value[1]?.hkOdds.toString()
                        }

                        this@ViewHolderHdpOu.setupOddState(this, it.value[1])

                        isSelected = it.value[1]?.isSelected ?: false

                        setOnClickListener { _ ->
                            it.value[1]?.let { odd ->

                                val playName = when {
                                    PlayCateUtils.getOUSeries().map { it.value }
                                        .contains(it.key) -> {
                                        itemView.context.getString(R.string.odd_button_ou_u)
                                    }
                                    else -> {
                                        matchInfo?.awayName
                                    }
                                } ?: ""

                                leagueOddListener?.onClickBet(
                                    matchInfo,
                                    odd,
                                    playCateName,
                                    playName
                                )
                            }
                        }
                    }

                    this.odd_btn_draw.apply drawButtonSettings@{
                        when {
                            (it.value.size < 3) -> {
                                betStatus = BetStatus.DEACTIVATED.code
                                return@drawButtonSettings
                            }
                            else -> {
                                betStatus = it.value[2]?.status
                            }
                        }

                        tv_name.apply {
                            text = itemView.context.getString(R.string.draw)
                            visibility = View.VISIBLE
                        }

                        tv_spread.apply {
                            visibility = View.INVISIBLE
                        }

                        tv_odds.text = when (oddsType) {
                            OddsType.EU -> it.value[2]?.odds.toString()
                            OddsType.HK -> it.value[2]?.hkOdds.toString()
                        }

                        this@ViewHolderHdpOu.setupOddState(this, it.value[2])

                        isSelected = it.value[2]?.isSelected ?: false

                        setOnClickListener { _ ->
                            it.value[2]?.let { odd ->
                                leagueOddListener?.onClickBet(
                                    matchInfo,
                                    odd,
                                    playCateName,
                                    itemView.context.getString(R.string.draw)
                                )
                            }
                        }
                    }
                }

                view?.let {
                    val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        rightMargin =
                            itemView.context.resources.getDimensionPixelOffset(R.dimen.textSize8sp)
                    }

                    viewGroup.addView(it, layoutParams)
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

            itemView.odd_button_other.apply {
                visibility = if (item.quickPlayCateList?.find { it.isSelected } == null) {
                    View.GONE
                } else {
                    View.VISIBLE
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
    val clickListenerPlayType: (matchId: String?, matchInfoList: List<MatchInfo>) -> Unit,
    val clickListenerBet: (matchInfo: MatchInfo?, odd: Odd, playCateName: String, playName: String) -> Unit,
    val clickListenerQuickCateTab: (matchId: String?) -> Unit,
    val clickListenerQuickCateClose: () -> Unit
) {
    fun onClickPlayType(matchId: String?, matchInfoList: List<MatchInfo>) =
        clickListenerPlayType(matchId, matchInfoList)

    fun onClickBet(
        matchInfo: MatchInfo?,
        odd: Odd,
        playCateName: String = "",
        playName: String = ""
    ) = clickListenerBet(matchInfo, odd, playCateName, playName)

    fun onClickQuickCateTab(matchId: String?) = clickListenerQuickCateTab(matchId)

    fun onClickQuickCateClose() = clickListenerQuickCateClose()
}