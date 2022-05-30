package org.cxct.sportlottery.ui.game.home.gameTable4

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_login.view.*
import kotlin.collections.MutableList
import kotlinx.android.synthetic.main.button_odd_detail.view.*
import kotlinx.android.synthetic.main.home_game_table_item_4.view.*
import kotlinx.android.synthetic.main.home_game_table_item_4.view.iv_play
import org.cxct.sportlottery.R
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.enum.MatchSource
import org.cxct.sportlottery.interfaces.OnSelectItemListener
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.MenuCode
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.TimeCounting
import org.cxct.sportlottery.network.service.match_clock.MatchClockCO
import org.cxct.sportlottery.network.service.match_status_change.MatchStatusCO
import org.cxct.sportlottery.ui.game.common.OddStateViewHolder
import org.cxct.sportlottery.ui.game.home.OnClickFavoriteListener
import org.cxct.sportlottery.ui.game.home.OnClickOddListener
import org.cxct.sportlottery.ui.game.home.OnClickStatisticsListener
import org.cxct.sportlottery.ui.game.home.OnSubscribeChannelHallListener
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.MatchOddUtil.updateDiscount
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.setTextTypeFace
import java.util.*

@SuppressLint("SetTextI18n")
class Vp2GameTable4Adapter(
    val matchType: MatchType,
) :
    RecyclerView.Adapter<Vp2GameTable4Adapter.ViewHolderHdpOu>() {

    enum class GameTablePayload {
        PAYLOAD_MATCH_STATUS
    }

    var onClickOddListener: OnClickOddListener? = null

    var onClickMatchListener: OnSelectItemListener<MatchInfo>? = null //賽事畫面跳轉

    var onClickFavoriteListener: OnClickFavoriteListener? = null

    var onClickStatisticsListener: OnClickStatisticsListener? = null

    private var isLogin: Boolean = false
    private var oddsType: OddsType = OddsType.EU
    private var gameType: String = GameType.FT.key
    private var selectedOdds = mutableListOf<String>()
    private var dataList: List<MatchOdd> = listOf()

    //主頁的翻譯要取外層的playCateNameMap，odds為{}時內層的playCateNameMap會是空的
    private var playCateNameMap: MutableMap<String?, Map<String?, String?>?>? = mutableMapOf()
    private var timeMap = mutableMapOf<String, Long>()
    private var discount: Float = 1.0F

    private val mOddStateRefreshListener by lazy {
        object : OddStateViewHolder.OddStateChangeListener {
            override fun refreshOddButton(odd: Odd) {}
        }
    }

    fun setData(
        gameType: String, dataList: List<MatchOdd>, isLogin: Boolean, oddsType: OddsType,
        playCateNameMap: MutableMap<String?, Map<String?, String?>?>?, selectedOdds: MutableList<String>
    ) {
        this.dataList = dataList
        this.gameType = gameType
        when (matchType) {
            MatchType.AT_START -> {
                dataList?.forEach { it ->
                    it.matchInfo?.id?.let { id ->
                        timeMap[id] = (it.matchInfo?.remainTime ?: -1)
                    }
                }
            }
        }

        this.isLogin = isLogin
        this.oddsType = oddsType
        this.playCateNameMap = playCateNameMap
        this.selectedOdds = selectedOdds
        this.dataList.forEachIndexed { index, matchOdd ->
            notifyItemChanged(index)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderHdpOu {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.home_game_table_item_4, parent, false)
        return ViewHolderHdpOu(view)
    }

    override fun onBindViewHolder(holder: ViewHolderHdpOu, position: Int) {
        try {
            val data = dataList[position]
            val time = timeMap[data.matchInfo?.id] ?: -1
            holder.bind(data, time)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBindViewHolder(holder: ViewHolderHdpOu, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            when (payloads.first()) {
                GameTablePayload.PAYLOAD_MATCH_STATUS -> {
                    holder.updateMatchStatus(dataList[position].matchInfo)
                }
            }
        } else {
            onBindViewHolder(holder, position)
        }
    }

    override fun getItemCount(): Int = dataList.size

    fun notifySelectedOddsChanged(selectedOdds: MutableList<String>) {
        this.selectedOdds = selectedOdds
        Handler(Looper.getMainLooper()).post {
            notifyDataSetChanged()
        }
    }

    fun notifyOddsTypeChanged(oddsType: OddsType) {
        this.oddsType = oddsType
        Handler(Looper.getMainLooper()).post {
            notifyDataSetChanged()
        }
    }

    fun notifyOddsDiscountChanged(discount: Float) {
        dataList.forEachIndexed { index, matchOdd ->
            matchOdd.oddsMap?.forEach { (key, value) ->
                value?.forEach { odd ->
                    odd?.updateDiscount(this.discount, discount)
                }
            }
        }
        Handler(Looper.getMainLooper()).post {
            notifyDataSetChanged()
        }
        this.discount = discount
    }

    fun notifyMatchStatusChanged(matchStatusCO: MatchStatusCO, statusValue: String?) {
        if (matchStatusCO.status == 100) {

        } else {
            dataList.forEachIndexed { index, matchOdd ->
                if (matchOdd.matchInfo?.id == matchStatusCO.matchId) {
                    matchOdd.matchInfo?.homeTotalScore = matchStatusCO.homeTotalScore
                    matchOdd.matchInfo?.awayTotalScore = matchStatusCO.awayTotalScore
                    matchOdd.matchInfo?.homeScore = "${matchStatusCO.homeScore}"
                    matchOdd.matchInfo?.awayScore = "${matchStatusCO.awayScore}"
                    matchOdd.matchInfo?.homePoints = matchStatusCO.homePoints
                    matchOdd.matchInfo?.awayPoints = matchStatusCO.awayPoints
                    matchOdd.matchInfo?.statusName18n = statusValue
                    matchOdd.matchInfo?.homeCards = matchStatusCO.homeCards
                    matchOdd.matchInfo?.awayCards = matchStatusCO.awayCards
                    matchOdd.matchInfo?.scoreStatus = matchStatusCO.status
                    Handler(Looper.getMainLooper()).post {
                        notifyItemChanged(index, GameTablePayload.PAYLOAD_MATCH_STATUS)
                    }
                }
            }
        }
    }

    fun notifyUpdateTime(matchClockCO: MatchClockCO) {
        dataList.forEach { matchOdd ->
            matchOdd.matchInfo?.stopped = matchClockCO.stopped
            matchOdd.matchInfo?.id?.let { id ->
                if (id == matchClockCO.matchId) {
                    when (matchClockCO.gameType) {
                        GameType.FT.key -> {
                            if (matchClockCO.matchTime == null) return
                            matchClockCO.matchTime?.let {
                                if (it == 0L) return
                                timeMap[id] = it
                            }
                        }
                        GameType.BK.key, GameType.RB.key, GameType.AFT.key -> {
                            matchClockCO.remainingTimeInPeriod?.let {
                                timeMap[id] = it
                            }
                        }
                    }
                }
            }
        }
    }

    fun notifyTimeChanged(diff: Int) {
        when (matchType) {
            MatchType.IN_PLAY -> {
                var needUpdate = false
                dataList.forEach { matchOdd ->
                    if (matchOdd.matchInfo?.stopped == TimeCounting.STOP.value) {
                        needUpdate = true
                        return
                    }
                    matchOdd.matchInfo?.id?.let { id ->
                        timeMap[id]?.let { time ->
                            var newTime = time
                            when (gameType) {
                                GameType.FT.key -> { //足球
                                    newTime = time + diff
                                }
                                GameType.BK.key -> { //籃球
                                    newTime = time - diff
                                }
                            }
                            if (newTime != time) {
                                needUpdate = true
                                timeMap[id] = newTime
                            }
                        }
                    }
                }
                if (needUpdate) {
                    Handler(Looper.getMainLooper()).post {
                        notifyDataSetChanged()
                    }
                }
            }
            MatchType.AT_START -> {
                var needUpdate = false
                dataList.forEachIndexed { index, matchOdd ->
                    if (matchOdd.matchInfo?.stopped == TimeCounting.STOP.value) {
                        needUpdate = true
                        return
                    }
                    matchOdd.matchInfo?.id?.let { id ->
                        timeMap[id]?.let { time ->
                            var newTime = time - diff
                            if (newTime != time) {
                                needUpdate = true
                                timeMap[id] = newTime
                            }
                        }
                    }
                }
                if (needUpdate) {
                    Handler(Looper.getMainLooper()).post {
                        notifyDataSetChanged()
                    }
                }
            }
        }
    }

    inner class ViewHolderHdpOu(itemView: View) : OddStateViewHolder(itemView) {

        private var oddList: MutableList<Odd?>? = null

        fun bind(data: MatchOdd, time: Long) {
            setupOddList(data)
            setupMatchInfo(data.matchInfo)
            setupOddButton(data)
            setupTime(data.matchInfo, time)
            setupCardText(data.matchInfo)

            //TODO simon test review 賠率 icon 顯示邏輯
            itemView.iv_match_in_play.visibility = if (matchType == MatchType.AT_START) {
                View.VISIBLE
            } else {
                View.GONE
            }
            itemView.iv_match_price.visibility =
                if (data.matchInfo?.eps == 1) View.VISIBLE else View.GONE

            itemView.iv_play.isVisible = (data.matchInfo?.liveVideo == 1) && (matchType == MatchType.IN_PLAY)
            itemView.iv_animation.isVisible = !(data.matchInfo?.trackerId.isNullOrEmpty())

            itemView.table_match_info_border.setOnClickListener {
                data.matchInfo?.let {
                    var matchInfo = it
                    matchInfo.gameType = gameType
                    onClickMatchListener?.onClick(matchInfo)
                }
            }

            itemView.tv_match_play_type_count.setOnClickListener {
                data.matchInfo?.let {
                    var matchInfo = it
                    matchInfo.gameType = gameType
                    onClickMatchListener?.onClick(matchInfo)
                }
            }

            with(itemView.btn_chart) {
                visibility = if (data.matchInfo?.source == MatchSource.SHOW_STATISTICS.code) View.VISIBLE else View.GONE

                setOnClickListener {
                    onClickStatisticsListener?.onClickStatistics(data.matchInfo?.id)
                }
            }
        }

        fun updateMatchStatus(data: MatchInfo?) {
            itemView.apply {
                when (matchType) {
                    MatchType.IN_PLAY -> {
                        tv_match_status.setTextColor(ContextCompat.getColor(context, R.color.color_FFFFFF_000000))
                        when (gameType) {
                            GameType.TN.key -> {
                                tv_match_status.visibility = View.GONE

                                //盤比分
                                tv_game_score_home.apply {
                                    text = "${data?.homeTotalScore ?: 0}"
                                    visibility = View.VISIBLE
                                }
                                tv_game_score_away.apply {
                                    text = "${data?.awayTotalScore ?: 0}"
                                    visibility = View.VISIBLE
                                }

                                //局比分
                                tv_score.apply {
                                    text = "${data?.homeScore ?: 0}–${data?.awayScore ?: 0}"
                                    visibility = View.VISIBLE
                                }

                                //點比分
                                tv_point.apply {
                                    text = "${data?.homePoints ?: 0}–${data?.awayPoints ?: 0}"
                                    visibility = View.VISIBLE
                                }
                            }
                            GameType.VB.key, GameType.TT.key, GameType.BM.key -> {
                                tv_game_score_home.visibility = View.GONE
                                tv_game_score_away.visibility = View.GONE
                                tv_score.visibility = View.GONE
                                tv_point.visibility = View.GONE

                                tv_match_status.apply {
                                    text = "${data?.statusName18n ?: ""} / ${data?.spt ?: ""}"
                                    visibility = View.VISIBLE
                                }

                                //盤比分
                                tv_game_total_score_home_center.apply {
                                    text = "${data?.homeTotalScore ?: 0}"
                                    visibility = View.VISIBLE
                                }
                                tv_game_total_score_away_center.apply {
                                    text = "${data?.awayTotalScore ?: 0}"
                                    visibility = View.VISIBLE
                                }

                                //點比分
                                tv_game_score_home_center.apply {
                                    text = "${data?.homeScore ?: 0}"
                                    visibility = View.VISIBLE
                                }
                                tv_game_score_away_center.apply {
                                    text = "${data?.awayScore ?: 0}"
                                    visibility = View.VISIBLE
                                }

                            }
                            GameType.CK.key -> {
                                tv_match_status.apply {
                                    text = data?.statusName18n ?: ""
                                    visibility = View.VISIBLE
                                }

                                tv_game_score_home.apply {
                                    text = "${data?.homeTotalScore ?: 0}"
                                    visibility = View.VISIBLE
                                }
                                tv_game_score_away.apply {
                                    text = "${data?.awayTotalScore ?: 0}"
                                    visibility = View.VISIBLE
                                }
                            }
                            else -> {
                                tv_match_status.apply {
                                    text = data?.statusName18n ?: ""
                                    visibility = View.VISIBLE
                                }

                                tv_game_score_home.apply {
                                    text = "${data?.homeScore ?: 0}"
                                    visibility = View.VISIBLE
                                }
                                tv_game_score_away.apply {
                                    text = "${data?.awayScore ?: 0}"
                                    visibility = View.VISIBLE
                                }
                            }
                        }
                    }
                    MatchType.AT_START -> {
                        tv_match_status.setTextColor(ContextCompat.getColor(context, R.color.color_BCBCBC_666666))
                        tv_game_type.text = context.getString(R.string.home_tab_today)
                        data?.startTime?.let {
                            val date = Date(it)
                            tv_game_type.text = context.getString(R.string.home_tab_today) + " " + TimeUtil.dateToDateFormat(date, TimeUtil.HM_FORMAT)
                        }

                        tv_game_score_home.visibility = View.GONE
                        tv_game_score_away.visibility = View.GONE

                        tv_score.visibility = View.GONE
                        tv_point.visibility = View.GONE
                    }else->  {tv_match_time.setTextColor(ContextCompat.getColor(context, R.color.color_BCBCBC_666666))
                    }
                }
            }
        }

        /* 目前不會用，先留著，以防之後說要改回來。
                private fun ImageView.setLiveImg() {
                    when (gameType) {
                        GameType.FT.key -> setImageResource(R.drawable.ic_live_football_small)
                        GameType.BK.key -> setImageResource(R.drawable.ic_live_basketball_small)
                        GameType.TN.key -> setImageResource(R.drawable.ic_live_tennis_small)
                        GameType.VB.key -> setImageResource(R.drawable.ic_live_volleyball_small)
                        GameType.BM.key -> setImageResource(R.drawable.ic_live_badminton_small)
                        GameType.TT.key -> setImageResource(R.drawable.ic_live_pingpong_small)
                        GameType.IH.key -> setImageResource(R.drawable.ic_live_icehockey_small)
                        GameType.BX.key -> setImageResource(R.drawable.ic_live_boxing_small)
                        GameType.CB.key -> setImageResource(R.drawable.ic_live_billiards_small)
                        GameType.CK.key -> setImageResource(R.drawable.ic_live_cricket_small)
                        GameType.BB.key -> setImageResource(R.drawable.ic_live_baseball_small)
                        GameType.RB.key -> setImageResource(R.drawable.ic_live_rugby_small)
                        GameType.AFT.key -> setImageResource(R.drawable.ic_live_soccer_small)
                        GameType.MR.key -> setImageResource(R.drawable.ic_live_racing_small)
                        GameType.GF.key -> setImageResource(R.drawable.ic_live_golf_small)
                    }
                }
                */
        private fun setupOddList(data: MatchOdd) {
            itemView.apply {
                //要取 datas 的matchOdds 下面的 oddsSort 去抓排序裡第一個的翻譯顯示 2022/01/11 與後端Ｍax確認 by Bill
                val playCateName =
                    if (data.oddsSort?.split(",")?.size ?: 0 > 0) data.oddsSort?.split(",")
                        ?.getOrNull(0) else data.oddsSort
                data.oddsMap?.let {
                    val odds = it[playCateName]
                    oddList = if (odds?.isNotEmpty() == true) {
                        odds.toMutableList()
                    } else {
                        mutableListOf()
                    }
                }
            }
        }

        private fun setupMatchInfo(data: MatchInfo?) {
            itemView.apply {
                tv_game_name_home.text = data?.homeName
                tv_game_name_away.text = data?.awayName
                showStrongTeam()
                tv_match_play_type_count.text = data?.playCateNum?.toString() ?: ""

                btn_star.apply {
                    isSelected = data?.isFavorite ?: false

                    setOnClickListener {
                        onClickFavoriteListener?.onClickFavorite(data?.id)
                        if (isLogin) btn_star.isSelected = !isSelected
                    }
                }

                when (matchType) {
                    MatchType.IN_PLAY -> {
                        tv_game_type.text = context.getString(R.string.home_tab_in_play)
                    }
                    MatchType.AT_START -> {
                        tv_game_type.text = context.getString(R.string.home_tab_today)
                        data?.startTime?.let {
                            val date = Date(it)
                            tv_game_type.text = context.getString(R.string.home_tab_today) + " " + TimeUtil.dateToDateFormat(date, TimeUtil.HM_FORMAT)
                        }
                    }
                    else -> {}
                }
                updateMatchStatus(data)
            }
        }

        private fun showStrongTeam() {
            itemView.apply {

                val homeStrongType = if (oddList?.getOrNull(0)?.spread?.contains("-") == true)
                    Typeface.BOLD
                else
                    Typeface.NORMAL

                val awayStrongType = if (oddList?.getOrNull(1)?.spread?.contains("-") == true)
                    Typeface.BOLD
                else
                    Typeface.NORMAL

//                tv_game_score_home.setTextTypeFace(homeStrongType)
                tv_game_name_home.setTextTypeFace(homeStrongType)

//                tv_game_score_away.setTextTypeFace(awayStrongType)
                tv_game_name_away.setTextTypeFace(awayStrongType)
            }
        }

        @SuppressLint("SetTextI18n")
        private fun setupTime(data: MatchInfo?, time: Long) {
            itemView.apply {
                when (matchType) {
                    MatchType.IN_PLAY -> {
                        tv_match_time.setTextColor(ContextCompat.getColor(context, R.color.color_FFFFFF_000000))
                        if (time == -1L) tv_match_time.text = dataList[bindingAdapterPosition].runningTime
                        else {
                            when (gameType) {
                                GameType.FT.key, GameType.BK.key -> { //足球
                                    val timeMillisAbs = if (time > 0) time else 0
                                    val timeStr = TimeUtil.longToMmSs(timeMillisAbs * 1000)
                                    tv_match_time.text = timeStr
                                    dataList[bindingAdapterPosition].runningTime = timeStr
                                }
                                else -> {
                                    tv_match_time.text = null
                                }
                            }
                        }
                    }
                    MatchType.AT_START -> {
                        tv_match_time.setTextColor(ContextCompat.getColor(context, R.color.color_BCBCBC_666666))
                        if (time == -1L) {
                            tv_match_time.text = dataList[bindingAdapterPosition].runningTime
                            tv_match_time.setTextColor(ContextCompat.getColor(context, R.color.color_BCBCBC_666666))
                            tv_match_time.setTextColor(ContextCompat.getColor(context, R.color.color_BCBCBC_666666))
                        } else {
                            val statusName =
                                if (data?.startDateDisplay.isNullOrEmpty()) "" else data?.startDateDisplay + " "
                            val timeMillisAbs = if (time > 0) time else 0
                            val timeStr = statusName + String.format(
                                itemView.context.resources.getString(R.string.at_start_remain_minute),
                                TimeUtil.longToMinute(timeMillisAbs)
                            )
                            tv_match_time.text = timeStr
                            dataList[bindingAdapterPosition].runningTime = timeStr
                            tv_match_time.setTextColor(ContextCompat.getColor(context, R.color.color_BCBCBC_666666))
                        }
                    }
                    else -> {
                        tv_match_time.setTextColor(ContextCompat.getColor(context, R.color.color_BCBCBC_666666))
                        tv_match_time.text = "${data?.startDateDisplay ?: ""} ${data?.startTimeDisplay ?: ""}"
                    }
                }
            }
        }

        private fun setupOddButton(data: MatchOdd) {
            itemView.apply {
                this
                //要取 datas 的matchOdds 下面的 oddsSort 去抓排序裡第一個的翻譯顯示 2022/01/11 與後端Ｍax確認 by Bill
                val playCateName =
                    if (data.oddsSort?.split(",")?.size ?: 0 > 0) data.oddsSort?.split(",")
                        ?.getOrNull(0) else data.oddsSort

                tv_play_type.text = playCateNameMap?.get(playCateName)
                    ?.get(LanguageManager.getSelectLanguage(context).key)

                btn_match_odd1.apply {

                    if (oddList?.isNullOrEmpty() == true || (oddList?.size ?: 0) < 2) {
                        // 沒有第一個按鈕
                        //isSelected = false
                        betStatus = BetStatus.DEACTIVATED.code
                    } else {

                        // 有第一個按鈕
                        val oddFirst = oddList?.getOrNull(0)
                        //isSelected = oddFirst?.isSelected ?: false
                        betStatus = oddFirst?.status ?: BetStatus.LOCKED.code

                        this@ViewHolderHdpOu.setupOddState(this, oddFirst)

                        oddFirst?.isSelected = selectedOdds.contains(oddFirst?.id ?: "")
                        this.isSelected = selectedOdds.contains(oddFirst?.id ?: "")

                        setupOdd(oddFirst, oddsType)

                        setupOddName4Home("1", playCateName)

                        setOnClickListener {
                            oddFirst?.let { odd ->
                                it.isSelected = !it.isSelected
                                data.matchInfo?.gameType = gameType
                                onClickOddListener?.onClickBet(
                                    data,
                                    odd,
                                    playCateName.toString(),
                                    itemView.tv_play_type.text.toString(),
                                    data.betPlayCateNameMap
                                )
                            }
                        }
                    }
                }

                btn_match_odd2.apply {
                    if (oddList?.isNullOrEmpty() == true || (oddList?.size ?: 0) < 2) {
                        // 沒有第二個按鈕
                        isSelected = false
                        betStatus = BetStatus.DEACTIVATED.code
                    } else {
                        // 有第二個按鈕
                        val oddSecond = oddList?.getOrNull(1)
                        isSelected = oddSecond?.isSelected ?: false
                        betStatus = oddSecond?.status ?: BetStatus.LOCKED.code

                        this@ViewHolderHdpOu.setupOddState(this, oddSecond)

                        oddSecond?.isSelected = selectedOdds.contains(oddSecond?.id ?: "")
                        this.isSelected = selectedOdds.contains(oddSecond?.id ?: "")

                        setupOdd(oddSecond, oddsType)

                        if (gameType == GameType.CK.key)
                            setupOddName4Home("X", playCateName)
                        else
                            setupOddName4Home("2", playCateName)

                        setOnClickListener {
                            oddSecond?.let { odd ->
                                it.isSelected = !it.isSelected
                                data.matchInfo?.gameType = gameType
                                onClickOddListener?.onClickBet(
                                    data,
                                    odd,
                                    playCateName.toString(),
                                    itemView.tv_play_type.text.toString(),
                                    data.betPlayCateNameMap
                                )
                            }
                        }
                    }
                }

                btn_match_odd3.apply {
                    if (oddList?.isNullOrEmpty() == true || (oddList?.size ?: 0) < 3) {
                        // 沒有第三個按鈕
                        isSelected = false
                        betStatus = BetStatus.DEACTIVATED.code
                    } else {
                        // 有第三個按鈕
                        val oddThird = oddList?.getOrNull(2)
                        isSelected = oddThird?.isSelected ?: false
                        betStatus = oddThird?.status ?: BetStatus.LOCKED.code

                        oddThird?.isSelected = selectedOdds.contains(oddThird?.id ?: "")
                        this@ViewHolderHdpOu.setupOddState(this, oddThird)

                        this.isSelected = selectedOdds.contains(oddThird?.id ?: "")

                        setupOdd(oddThird, oddsType)

                        setupOddName4Home("2", playCateName)

                        setOnClickListener {
                            oddThird?.let { odd ->
                                it.isSelected = !it.isSelected
                                data.matchInfo?.gameType = gameType
                                onClickOddListener?.onClickBet(
                                    data,
                                    odd,
                                    playCateName.toString(),
                                    itemView.tv_play_type.text.toString(),
                                    data.betPlayCateNameMap
                                )
                            }
                        }
                    }
                }
            }
        }

        private fun setupCardText(data: MatchInfo?) {
            itemView.apply {
                tv_game_odd_match_cards_home.apply {
                    visibility = when {
                        TimeUtil.isTimeInPlay(data?.startTime) && (data?.homeCards ?: 0 > 0) -> View.VISIBLE
                        else -> View.GONE
                    }
                    text = (data?.homeCards ?: 0).toString()
                }

                tv_game_odd_match_cards_away.apply {
                    visibility = when {
                        TimeUtil.isTimeInPlay(data?.startTime) && (data?.awayCards ?: 0 > 0) -> View.VISIBLE
                        else -> View.GONE
                    }
                    text = (data?.awayCards ?: 0).toString()
                }
            }
        }

        override val oddStateChangeListener: OddStateChangeListener
            get() = mOddStateRefreshListener
    }

}
