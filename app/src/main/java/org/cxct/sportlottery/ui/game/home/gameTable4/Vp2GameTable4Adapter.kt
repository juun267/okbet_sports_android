package org.cxct.sportlottery.ui.game.home.gameTable4

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_login.view.*
import kotlin.collections.MutableList
import kotlinx.android.synthetic.main.button_odd_detail.view.*
import kotlinx.android.synthetic.main.home_game_table_item_4.view.*
import kotlinx.android.synthetic.main.home_game_table_item_4.view.iv_play
import kotlinx.android.synthetic.main.itemview_league_odd_v4.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.interfaces.OnSelectItemListener
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.service.match_clock.MatchClockCO
import org.cxct.sportlottery.ui.game.common.OddStateViewHolder
import org.cxct.sportlottery.ui.game.home.OnClickFavoriteListener
import org.cxct.sportlottery.ui.game.home.OnClickOddListener
import org.cxct.sportlottery.ui.game.home.OnClickStatisticsListener
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.MatchOddUtil.updateDiscount
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.setTextTypeFace
import timber.log.Timber
import java.util.*

class Vp2GameTable4Adapter (
    val matchType: MatchType,
) :
    RecyclerView.Adapter<Vp2GameTable4Adapter.ViewHolderHdpOu>() {

    var onClickOddListener: OnClickOddListener? = null

    var onClickMatchListener: OnSelectItemListener<MatchOdd>? = null //賽事畫面跳轉

    var onClickFavoriteListener: OnClickFavoriteListener? = null

    var onClickStatisticsListener: OnClickStatisticsListener? = null

    private var isLogin: Boolean = false
    private var oddsType: OddsType = OddsType.EU
    private var selectedOdds = mutableListOf<String>()
    private var dataList: List<MatchOdd> = listOf()
    //主頁的翻譯要取外層的playCateNameMap，odds為{}時內層的playCateNameMap會是空的
    private var playCateNameMap: Map<String?, Map<String?, String?>?>? = mapOf()
    private var discount: Float = 1.0F

    private val mOddStateRefreshListener by lazy {
        object : OddStateViewHolder.OddStateChangeListener {
            override fun refreshOddButton(odd: Odd) {
                notifyItemChanged(dataList.indexOf(dataList.find { matchOdd ->
                    matchOdd.oddsMap.toList()
                        .find { map -> map.second?.find { it == odd } != null } != null
                }))
            }
        }
    }

    fun setData(dataList: List<MatchOdd>, isLogin: Boolean, oddsType: OddsType,
                playCateNameMap: Map<String?, Map<String?, String?>?>, selectedOdds: MutableList<String>) {
        this.dataList = dataList

        when (matchType) {
            MatchType.AT_START -> {
                dataList?.forEach { it ->
                    it.matchInfo?.id?.let { id ->
                        it.leagueTime = (it.matchInfo?.remainTime?.toInt() ?: -1)
                    }
                }
            }
        }

        this.isLogin = isLogin
        this.oddsType = oddsType
        this.playCateNameMap = playCateNameMap
        this.selectedOdds = selectedOdds
        this.notifyDataSetChanged()
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

    fun notifySelectedOddsChanged(selectedOdds: MutableList<String>) {
        this.selectedOdds = selectedOdds
        this.notifyDataSetChanged()
    }

    fun notifyOddsTypeChanged(oddsType: OddsType) {
        this.oddsType = oddsType
        this.notifyDataSetChanged()
    }

    fun notifyOddsDiscountChanged(discount: Float) {
        dataList.forEach { matchOdd ->
            matchOdd.oddsMap.forEach { (key, value) ->
                value?.forEach { odd ->
                    odd?.updateDiscount(this.discount, discount)
                }
            }
        }

        this.notifyDataSetChanged()
        this.discount = discount
    }

    fun notifyUpdateTime(matchClockCO: MatchClockCO) {
        var needUpdate = false
        dataList.forEach { matchOdd ->
            matchOdd.matchInfo?.id?.let { id ->
                if (id == matchClockCO.matchId) {
                    when (matchClockCO.gameType) {
                        GameType.FT.key -> {
                            matchClockCO.matchTime?.let {
                                matchOdd.leagueTime = it
                                needUpdate = true
                            }
                        }
                        GameType.BK.key -> {
                            matchClockCO.remainingTimeInPeriod?.let {
                                matchOdd.leagueTime = it
                                needUpdate = true
                            }
                        }
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

    fun notifyTimeChanged(diff: Int) {
        when (matchType) {
            MatchType.IN_PLAY -> {
                var needUpdate = false
                dataList.forEach { matchOdd ->
                    var time = matchOdd.leagueTime
                    when (matchOdd.matchInfo?.gameType) {
                        GameType.FT.key -> { //足球
                            matchOdd.matchInfo.id?.let { id ->
                                time?.apply {
                                    time += diff
                                }
                            }
                        }
                        GameType.BK.key -> { //籃球
                            matchOdd.matchInfo.id?.let { id ->
                                time?.apply {
                                    time -= diff
                                }
                            }
                        }
                    }
                    if (time != null) {
                        matchOdd.leagueTime = time
                        needUpdate = true
                    }
                }
                if (needUpdate) {
                    Handler(Looper.getMainLooper()).post {
                        notifyDataSetChanged()
                    }
                }
            }
            MatchType.AT_START -> {
                dataList.forEach { matchOdd ->
                    matchOdd.matchInfo?.id?.let { id ->
                        var time = matchOdd.leagueTime
                        time?.apply {
                            time -= diff
                        }
                        if (time != null) matchOdd.leagueTime = time
                    }
                }
                Handler(Looper.getMainLooper()).post {
                    notifyDataSetChanged()
                }
            }
        }

    }

    inner class ViewHolderHdpOu(itemView: View) : OddStateViewHolder(itemView) {

        private var gameType: String? = null
        private var oddList: MutableList<Odd?>? = null

        fun bind(data: MatchOdd) {
            setupOddList(data)
            setupMatchInfo(data)
            setupOddButton(data)
            setupTime(data.matchInfo, data.leagueTime ?: -1)
            setupCardText(data.matchInfo)

            //TODO simon test review 賠率 icon 顯示邏輯
            itemView.iv_match_in_play.visibility = if (matchType == MatchType.AT_START) {
                View.VISIBLE
            } else {
                View.GONE
            }
            itemView.iv_match_price.visibility =
                if (data.matchInfo?.eps == 1) View.VISIBLE else View.GONE

            itemView.iv_play.isVisible = (data.matchInfo?.liveVideo == 1)

            itemView.table_match_info_border.setOnClickListener {
                onClickMatchListener?.onClick(data)
            }

            itemView.tv_match_play_type_count.setOnClickListener {
                onClickMatchListener?.onClick(data)
            }

            itemView.btn_chart.setOnClickListener {
                onClickStatisticsListener?.onClickStatistics(data.matchInfo?.id)
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
                gameType = data.matchInfo?.gameType

                oddList = if (data.oddsMap.isNotEmpty()) {
                    data.oddsMap.iterator().next().value
                } else {
                    mutableListOf()
                }
            }
        }

        private fun setupMatchInfo(data: MatchOdd) {
            itemView.apply {
                tv_game_name_home.text = data.matchInfo?.homeName
                tv_game_name_away.text = data.matchInfo?.awayName
                showStrongTeam()
                tv_match_play_type_count.text = data.matchInfo?.playCateNum?.toString()

                btn_star.apply {
                    isSelected = data.matchInfo?.isFavorite ?: false

                    setOnClickListener {
                        onClickFavoriteListener?.onClickFavorite(data.matchInfo?.id)
                        if (isLogin) btn_star.isSelected = !isSelected
                    }
                }

                when (matchType) {
                    MatchType.IN_PLAY -> {
                        tv_game_type.text = context.getString(R.string.home_tab_in_play)

                        when (data.matchInfo?.gameType) {
                            GameType.TN.key -> {
                                tv_match_status.visibility = View.GONE

                                //盤比分
                                tv_game_score_home.visibility = View.VISIBLE
                                tv_game_score_away.visibility = View.VISIBLE
                                val homeTotalScore = data.matchInfo.homeTotalScore ?: 0
                                val awayTotalScore = data.matchInfo.awayTotalScore ?: 0
                                tv_game_score_home.text = "$homeTotalScore"
                                tv_game_score_away.text = "$awayTotalScore"

                                //局比分
                                tv_score.visibility = View.VISIBLE
                                val homeScore = data.matchInfo.homeScore ?: 0
                                val awayScore = data.matchInfo.awayScore ?: 0
                                tv_score.text = "$homeScore–$awayScore"

                                //點比分
                                tv_point.visibility = View.VISIBLE
                                val homePoint = data.matchInfo.homePoints ?: 0
                                val awayPoint = data.matchInfo.awayPoints ?: 0
                                tv_point.text = "$homePoint–$awayPoint"
                            }
                            GameType.VB.key, GameType.TT.key -> {
                                tv_game_total_score_home_center.visibility = View.VISIBLE
                                tv_game_score_home_center.visibility = View.VISIBLE
                                tv_game_total_score_away_center.visibility = View.VISIBLE
                                tv_game_score_away_center.visibility = View.VISIBLE

                                tv_match_status.visibility = View.VISIBLE

                                tv_match_status.text =
                                    "${data.matchInfo.statusName18n} / ${data.matchInfo.spt}" ?: ""

                                tv_game_score_home.visibility = View.GONE
                                tv_game_score_away.visibility = View.GONE

                                //盤比分
                                val homeTotalScore = data.matchInfo.homeTotalScore ?: 0
                                val awayTotalScore = data.matchInfo.awayTotalScore ?: 0
                                tv_game_total_score_home_center.text = "$homeTotalScore"
                                tv_game_total_score_away_center.text = "$awayTotalScore"

                                //點比分
                                val homeScore = data.matchInfo.homeScore ?: 0
                                val awayScore = data.matchInfo.awayScore ?: 0
                                tv_game_score_home_center.text = "$homeScore"
                                tv_game_score_away_center.text = "$awayScore"

                                tv_score.visibility = View.GONE
                                tv_point.visibility = View.GONE
                            }
                            else -> {
                                tv_match_status.visibility = View.VISIBLE
                                tv_match_status.text = data.matchInfo?.statusName18n ?: ""

                                tv_game_score_home.visibility = View.VISIBLE
                                tv_game_score_away.visibility = View.VISIBLE
                                val homeScore = data.matchInfo?.homeScore ?: 0
                                val awayScore = data.matchInfo?.awayScore ?: 0
                                tv_game_score_home.text = "$homeScore"
                                tv_game_score_away.text = "$awayScore"
                            }
                        }
                    }
                    MatchType.AT_START -> {
                        tv_game_type.text = context.getString(R.string.home_tab_today)
                        data.matchInfo?.startTime?.let {
                            val date = Date(it)
                            tv_game_type.text = context.getString(R.string.home_tab_today) + " " + TimeUtil.dateToDateFormat(date,TimeUtil.HM_FORMAT)
                        }

                        tv_game_score_home.visibility = View.GONE
                        tv_game_score_away.visibility = View.GONE

                        tv_game_name_home.setTextTypeFace(Typeface.NORMAL)
                        tv_game_name_away.setTextTypeFace(Typeface.NORMAL)

                        tv_score.visibility = View.GONE
                        tv_point.visibility = View.GONE
                    }
                    else -> {
                    }
                }
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

                tv_game_score_home.setTextTypeFace(homeStrongType)
                tv_game_name_home.setTextTypeFace(homeStrongType)

                tv_game_score_away.setTextTypeFace(awayStrongType)
                tv_game_name_away.setTextTypeFace(awayStrongType)
            }
        }

        @SuppressLint("SetTextI18n")
        private fun setupTime(data: MatchInfo?, time: Int = -1) {
            itemView.apply {
                when (matchType) {
                    MatchType.IN_PLAY -> {
                        if (time == -1) tv_match_time.text = null
                        else {
                            when (data?.gameType) {
                                GameType.FT.key, GameType.BK.key -> { //足球, //籃球
                                    val timeMillisAbs = if (time > 0) time else 0
                                    val timeStr = TimeUtil.longToMmSs(timeMillisAbs.toLong()*1000)
                                    tv_match_time.text = timeStr
                                }
                                else -> tv_match_time.text = null
                            }
                        }
                    }
                    MatchType.AT_START -> {
                        if (time == -1) tv_match_time.text = null
                        else {
                            val statusName =
                                if (data?.startDateDisplay.isNullOrEmpty()) "" else data?.startDateDisplay + " "
                            val timeMillisAbs = if (time > 0) time else 0
                            val timeStr = statusName + String.format(itemView.context.resources.getString(R.string.at_start_remain_minute), TimeUtil.longToMinute(timeMillisAbs.toLong()))
                            tv_match_time.text = timeStr
                        }
                    }
                    else -> {
                        tv_match_time.text = "${data?.startDateDisplay ?: ""} ${data?.startTimeDisplay ?: ""}"
                    }
                }
            }
        }

        private fun setupOddButton(data: MatchOdd) {
            itemView.apply { this
                gameType = data.matchInfo?.gameType

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
                    }
                    else {

                        // 有第一個按鈕
                        val oddFirst = oddList?.getOrNull(0)
                        //isSelected = oddFirst?.isSelected ?: false
                        betStatus = oddFirst?.status ?: BetStatus.LOCKED.code

                        this@ViewHolderHdpOu.setupOddState(this, oddFirst)

                        oddFirst?.isSelected = selectedOdds.contains(oddFirst?.id ?: "")
                        this.isSelected = selectedOdds.contains(oddFirst?.id ?: "")

                        setupOdd(oddFirst, oddsType)

                        setupOddName4Home("1" , playCateName)

                        setOnClickListener {
                            oddFirst?.let { odd ->
                                onClickOddListener?.onClickBet( data, odd, playCateName.toString(), itemView.tv_play_type.text.toString(), data.betPlayCateNameMap )
                            }
                        }
                    }
                }

                btn_match_odd2.apply {
                    if (oddList?.isNullOrEmpty() == true || (oddList?.size ?: 0) < 2) {
                        // 沒有第二個按鈕
                        isSelected = false
                        betStatus = BetStatus.DEACTIVATED.code
                    }
                    else {
                        // 有第二個按鈕
                        val oddSecond = oddList?.getOrNull(1)
                        isSelected = oddSecond?.isSelected ?: false
                        betStatus = oddSecond?.status ?: BetStatus.LOCKED.code

                        this@ViewHolderHdpOu.setupOddState(this, oddSecond)

                        oddSecond?.isSelected = selectedOdds.contains(oddSecond?.id ?: "")
                        this.isSelected = selectedOdds.contains(oddSecond?.id ?: "")

                        setupOdd(oddSecond, oddsType)

                        if(data.matchInfo?.gameType == GameType.CK.key)
                            setupOddName4Home("X" , playCateName)
                        else
                            setupOddName4Home("2" , playCateName)

                        setOnClickListener {
                            oddSecond?.let { odd ->
                                onClickOddListener?.onClickBet( data, odd, playCateName.toString(), itemView.tv_play_type.text.toString(), data.betPlayCateNameMap )
                            }
                        }
                    }
                }

                btn_match_odd3.apply {
                    if (oddList?.isNullOrEmpty() == true || (oddList?.size ?: 0) < 3) {
                        // 沒有第三個按鈕
                        isSelected = false
                        betStatus = BetStatus.DEACTIVATED.code
                    }
                    else {
                        // 有第三個按鈕
                        val oddThird = oddList?.getOrNull(2)
                        isSelected = oddThird?.isSelected ?: false
                        betStatus = oddThird?.status ?: BetStatus.LOCKED.code

                        oddThird?.isSelected = selectedOdds.contains(oddThird?.id ?: "")
                        this@ViewHolderHdpOu.setupOddState(this, oddThird)

                        this.isSelected = selectedOdds.contains(oddThird?.id ?: "")

                        setupOdd(oddThird, oddsType)

                        setupOddName4Home("2" , playCateName)

                        setOnClickListener {
                            oddThird?.let { odd ->
                                onClickOddListener?.onClickBet( data, odd, playCateName.toString(), itemView.tv_play_type.text.toString(), data.betPlayCateNameMap )
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
                        (matchType == MatchType.IN_PLAY || (matchType == MatchType.MY_EVENT && data?.isInPlay ?: false)) && (data?.homeCards ?: 0 > 0) -> View.VISIBLE
                        else -> View.GONE
                    }
                    text = (data?.homeCards ?: 0).toString()
                }

                tv_game_odd_match_cards_away.apply {
                    visibility = when {
                        (matchType == MatchType.IN_PLAY || (matchType == MatchType.MY_EVENT && data?.isInPlay ?: false)) && (data?.awayCards ?: 0 > 0) -> View.VISIBLE
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
