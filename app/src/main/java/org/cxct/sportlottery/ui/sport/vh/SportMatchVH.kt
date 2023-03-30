//package org.cxct.sportlottery.ui.sport.vh
//
//import android.annotation.SuppressLint
//import android.content.Context
//import android.os.Handler
//import android.os.Looper
//import android.text.style.ForegroundColorSpan
//import android.view.LayoutInflater
//import android.view.View
//import androidx.core.view.isVisible
//import androidx.lifecycle.LifecycleOwner
//import androidx.lifecycle.lifecycleScope
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import androidx.recyclerview.widget.SimpleItemAnimator
//import kotlinx.android.synthetic.main.content_baseball_status.view.*
//import kotlinx.android.synthetic.main.item_sport_odd.view.*
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import org.cxct.sportlottery.application.MultiLanguagesApplication
//import org.cxct.sportlottery.R
//import org.cxct.sportlottery.databinding.ItemSportOddBinding
//import org.cxct.sportlottery.enum.MatchSource
//import org.cxct.sportlottery.network.common.GameStatus
//import org.cxct.sportlottery.network.common.GameType
//import org.cxct.sportlottery.network.common.MatchType
//import org.cxct.sportlottery.network.odds.MatchInfo
//import org.cxct.sportlottery.network.odds.list.LeagueOdd
//import org.cxct.sportlottery.network.odds.list.MatchOdd
//import org.cxct.sportlottery.network.odds.list.TimeCounting
//import org.cxct.sportlottery.ui.common.CustomLinearLayoutManager
//import org.cxct.sportlottery.ui.component.overScrollView.OverScrollDecoratorHelper
//import org.cxct.sportlottery.ui.game.common.LeagueOddListener
//import org.cxct.sportlottery.ui.game.common.OddButtonListener
//import org.cxct.sportlottery.ui.game.common.OddButtonPagerAdapter
//import org.cxct.sportlottery.ui.game.common.view.OddsButton2
//import org.cxct.sportlottery.enum.OddsType
//import org.cxct.sportlottery.util.*
//import org.cxct.sportlottery.widget.expandablerecyclerview.ExpandableAdapter
//import java.util.*
//
//class SportMatchVH(val context: Context,
//                   val matchType: MatchType,
//                   val lifecycle: LifecycleOwner,
//                   val onChildVieClick:(View, MatchOdd)-> Unit,
//                   val onOddsClick:(OddsButton2, MatchOdd)-> Unit,
//                   val leagueOddListener: LeagueOddListener,
//                   val binding: ItemSportOddBinding = ItemSportOddBinding.inflate(LayoutInflater.from(context))):
//    ExpandableAdapter.ViewHolder(binding.root)  {
//
//    private var timer: Timer? = null
//    protected var listener: ((Long) -> Unit)? = null
//    var isTimerEnable = false
//
//    val oddButtonPagerAdapter = OddButtonPagerAdapter {
//
//    }
//
//    init {
//        itemView.rv_league_odd_btn_pager_main.run {
//            linearLayoutManager.isAutoMeasureEnabled = false
//            layoutManager = linearLayoutManager
//            setHasFixedSize(true)
//            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
//
//            adapter = oddButtonPagerAdapter.apply {
//                stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT
//                matchType = matchType
//                listener = OddButtonListener { matchInfo, odd, playCateCode, playCateName, betPlayCateName ->
//                    leagueOddListener?.onClickBet(
//                        matchInfo,
//                        odd,
//                        playCateCode,
//                        betPlayCateName,
//                        null
//                    )
//                }
//            }
//
//            OverScrollDecoratorHelper.setUpOverScroll(this,  OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL)
//            itemView.hIndicator.bindRecyclerView(this)
//        }
//    }
//
//    fun bind(item: MatchOdd, leagueOdd: LeagueOdd, oddsType: OddsType, payloads: List<Any>) = binding.run {
//        stopTimer()
//        setupMatchInfo(item)
//        val isTimerPause = item.matchInfo?.stopped == TimeCounting.STOP.value
//        item.matchInfo?.let { setupMatchTimeAndStatus(it, isTimerEnable, isTimerPause) }
//        updateOddsButton(item, oddsType)
//    }
//
//    val linearLayoutManager by lazy { CustomLinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false) }
//
//    private fun updateOddsButton(item: MatchOdd, oddsType: OddsType) {
//        itemView.rv_league_odd_btn_pager_main.apply {
//            oddButtonPagerAdapter.setData(
//                item.matchInfo,
//                item.oddsSort,
//                item.playCateNameMap,
//                item.betPlayCateNameMap,
//                null,
//                item
//            )
//            oddButtonPagerAdapter.apply {
//                stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT
//                this.oddsType = oddsType
//                this.odds = item.oddsMap ?: mutableMapOf()
//            }
//        }
//    }
//
//    private fun updateOddsButtonByBetInfo(item: MatchOdd) {
//        oddButtonPagerAdapter.odds = item.oddsMap ?: mutableMapOf()
//    }
//
//
//    private fun setupMatchTimeAndStatus(
//        matchInfo: MatchInfo,
//        isTimerEnable: Boolean,
//        isTimerPause: Boolean) {
//
//        /* TODO 依目前開發方式優化，將狀態和時間保存回 viewModel 於下次刷新頁面前 api 取得資料時先行代入相關 data 內，
//            此處倒數計時前須先設置時間及狀態，可解決控件短暫空白。(賽事狀態已於 BaseFavoriteViewModel #1 處調整過)*/
//
//        when {
//            TimeUtil.isTimeInPlay(matchInfo.startTime) -> {
//                if (matchInfo.gameType == GameType.TN.key) {
//                    itemView.league_odd_match_time.visibility = View.GONE
//                    return
//                }
//                val socketValue = matchInfo.socketMatchStatus
//
//                if (needCountStatus(socketValue)) {
//                    itemView.league_odd_match_time.visibility = View.VISIBLE
//                    listener = { timeMillis->
//                        if (timeMillis > 1000) {
//                            itemView.league_odd_match_time.text = TimeUtil.longToMmSs(timeMillis)
//                        } else {
//                            itemView.league_odd_match_time.text = itemView.context.getString(R.string.time_up)
//                        }
//                        matchInfo.leagueTime = (timeMillis / 1000).toInt()
//                    }
//
//                    updateTimer(
//                        isTimerEnable,
//                        isTimerPause,
//                        matchInfo.leagueTime ?: 0,
//                        (matchInfo.gameType == GameType.BK.key ||
//                                matchInfo.gameType == GameType.RB.key ||
//                                matchInfo.gameType == GameType.AFT.key)
//                    )
//
//                } else {
//                    itemView.league_odd_match_time.visibility = View.GONE
//                }
//            }
//
//            TimeUtil.isTimeAtStart(matchInfo.startTime) -> {
//                listener = { timeMillis->
//                    val min = if (timeMillis > 1000) {
//                        TimeUtil.longToMinute(timeMillis)
//                    } else {
//                        //等待Socket更新
//                        0
//                    }
//
//                    itemView.league_odd_match_time.text =
//                        String.format(itemView.context.resources.getString(R.string.at_start_remain_minute), min)
//                    matchInfo.remainTime = timeMillis
////                  itemView.league_odd_match_remain_time_icon.visibility = View.VISIBLE
//                }
//
//                matchInfo.remainTime?.let { remainTime ->
//                    updateTimer(
//                        true,
//                        isTimerPause,
//                        (remainTime / 1000).toInt(),
//                        true
//                    )
//                }
//            }
//            else -> {
//                itemView.league_odd_match_time.visibility = View.VISIBLE
//                itemView.league_odd_match_time.text =
//                    TimeUtil.timeFormat(matchInfo?.startTime,
//                        if (TimeUtil.isTimeToday(matchInfo?.startTime)) TimeUtil.HM_FORMAT else TimeUtil.DM_HM_FORMAT)
//            }
//        }
//
//        setStatusText(matchInfo)
//        setTextViewStatus(matchInfo)
//    }
//
//    private fun setStatusText(matchInfo: MatchInfo) {
//
//        itemView.league_odd_match_status.text = when {
//
//            (TimeUtil.isTimeInPlay(matchInfo.startTime)
//                    && matchInfo.status == GameStatus.POSTPONED.code
//                    && (matchInfo.gameType == GameType.FT.name || matchInfo.gameType == GameType.BK.name || matchInfo.gameType == GameType.TN.name)) -> {
//                itemView.context.getString(R.string.game_postponed)
//            }
//
//            TimeUtil.isTimeInPlay(matchInfo.startTime) -> {
//                if (matchInfo.statusName18n != null) {
//                    //网球，排球，乒乓，羽毛球，就不显示
//                    if (matchInfo.gameType == GameType.TN.name
//                        || matchInfo.gameType == GameType.VB.name
//                        || matchInfo.gameType == GameType.TT.name
//                        || matchInfo.gameType == GameType.BM.name
//                    ) {
//                        ""
//                    } else {
//                        matchInfo.statusName18n
//                    }
//
//                } else {
//                    ""
//                }
//            }
//
//            TimeUtil.isTimeToday(matchInfo.startTime) -> {
//                itemView.context.getString((R.string.home_tab_today))
//            }
//
//            else -> {
//                itemView.league_odd_match_status.isVisible = false
//                ""
//            }
//        }
//    }
//
//    private fun setTextViewStatus(matchInfo: MatchInfo) {
//        when {
//            (TimeUtil.isTimeInPlay(matchInfo.startTime)
//                    && matchInfo.status == GameStatus.POSTPONED.code
//                    && (matchInfo.gameType == GameType.FT.name || matchInfo.gameType == GameType.BK.name || matchInfo.gameType == GameType.TN.name)) -> {
//                itemView.league_odd_match_time.visibility = View.GONE
//            }
//
//            TimeUtil.isTimeInPlay(matchInfo.startTime) -> {
//                if (matchInfo.statusName18n != null) {
//                    itemView.league_odd_match_status.visibility = View.VISIBLE
//                }
//            }
//
//            TimeUtil.isTimeAtStart(matchInfo.startTime) -> {
//                itemView.league_odd_match_status.visibility = View.GONE
//            }
//        }
//    }
//
//    private fun setupMatchInfo(item: MatchOdd) = itemView.apply {
//
//        league_odd_match_name_home.text = item.matchInfo?.homeName
//        league_odd_match_name_away.text = item.matchInfo?.awayName
//        iv_home_team_logo.setTeamLogo(item.matchInfo?.homeIcon)
//        iv_away_team_logo.setTeamLogo(item.matchInfo?.awayIcon)
//
//        setupMatchScore(item, matchType)
//
//        league_odd_match_play_count.run {
//            text = item.matchInfo?.playCateNum.toString() + "+>"
//            setOnClickListener { onChildVieClick.invoke(it, item) }
//        }
//
//        league_odd_match_favorite.run {
//            isSelected = item.matchInfo?.isFavorite ?: false
//            isSelected = item.matchInfo?.isFavorite ?: false
//            setOnClickListener { onChildVieClick.invoke(it, item) }
//        }
//
//        val neutralUsable = item.matchInfo?.neutral == 1
//        league_neutral.isSelected = neutralUsable
//        league_neutral.isVisible = neutralUsable
//
//        league_odd_match_chart.run {
//            isVisible = item.matchInfo?.source == MatchSource.SHOW_STATISTICS.code
//            setOnClickListener { onChildVieClick.invoke(it, item) }
//        }
//
//        lin_match.setOnClickListener { onChildVieClick.invoke(it, item) }
//
//        with(iv_play) {
//            isVisible = item.matchInfo?.liveVideo == 1 && (TimeUtil.isTimeInPlay(item.matchInfo?.startTime))
//            setOnClickListener { onChildVieClick.invoke(it, item) }
//        }
//
//        with(iv_animation) {
//            isVisible = TimeUtil.isTimeInPlay(item.matchInfo?.startTime)
//                    && !(item.matchInfo?.trackerId.isNullOrEmpty())
//                    && MultiLanguagesApplication.getInstance()?.getGameDetailAnimationNeedShow() == true
//            setOnClickListener { onChildVieClick.invoke(it, item) }
//        }
//    }
//
//    private fun startTimer(isTimerPause: Boolean, startTime: Int, isDecrease: Boolean) {
//        var timeMillis = startTime * 1000L
//        stopTimer()
//
//        post({ listener?.invoke(timeMillis) })
//        timer = Timer()
//        timer?.schedule(object : TimerTask() {
//            override fun run() {
//                when (isDecrease) {
//                    true -> {
//                        if (!isTimerPause) timeMillis -= 1000
//                    }
//                    false -> {
//                        if (!isTimerPause) timeMillis += 1000
//                    }
//                }
//
//                if (timeMillis > 0) {
//                    listener?.let { post({ it.invoke(timeMillis) }) }
//                }
//            }
//        }, 1000L, 1000L)
//    }
//
//    private fun post(runnable: Runnable) {
//        lifecycle.lifecycleScope.launch(Dispatchers.Main) { runnable.run() }
//    }
//
//    fun stopTimer() {
//        timer?.cancel()
//        timer = null
//    }
//
//    fun updateTimer(
//        isTimerEnable: Boolean,
//        isTimerPause: Boolean,
//        startTime: Int,
//        isDecrease: Boolean) {
//
//        when (isTimerEnable) {
//            false -> {
//                stopTimer()
//            }
//
//            true -> {
//                startTimer(isTimerPause, startTime, isDecrease)
//            }
//        }
//    }
//
//    private fun setupMatchScore(item: MatchOdd, matchType: MatchType) {
//        itemView.apply {
//            when {
//                matchType != MatchType.IN_PLAY -> {
//                    linear_layout.isVisible = true
//                    content_baseball_status.isVisible = false
//                }
//                else -> {
//                    when (item.matchInfo?.gameType) {
//                        GameType.BB.key -> {
//                            linear_layout.isVisible = false
//                            content_baseball_status.isVisible = true
//                        }
//                        else -> {
//                            linear_layout.isVisible = true
//                            content_baseball_status.isVisible = false
//                        }
//                    }
//
//                }
//            }
//        }
//
//        when (item.matchInfo?.socketMatchStatus) {
//            //20220507 status:999 邏輯變更 隱藏分數 -> 賽事狀態變為滾球
//            /*GameMatchStatus.HIDE_SCORE.value -> {
//                hideMatchScoreText()
//            }*/
//            else -> {
//                when (item.matchInfo?.gameType) {
//                    GameType.VB.key -> setVbScoreText(item.matchInfo)
//                    GameType.TN.key -> setTnScoreText(item.matchInfo)
//                    GameType.FT.key -> setFtScoreText(item.matchInfo)
//                    GameType.BK.key -> setBkScoreText(item.matchInfo)
//                    GameType.TT.key -> setVbScoreText(item.matchInfo)
//                    GameType.BM.key -> setBmScoreText(item.matchInfo)
//                    GameType.BB.key -> setBbScoreText(item.matchInfo)
//                    else -> item.matchInfo?.let { setBkScoreText(it) }
//                }
//            }
//        }
//    }
//
//    private fun setFtScoreText(matchInfo: MatchInfo) {
//        setScoreTextAtFront(matchInfo)
//        setCardText(matchInfo)
//        setFbKicks(matchInfo)
//    }
//
//    private fun setBkScoreText(matchInfo: MatchInfo) {
//        setScoreTextAtFront(matchInfo)
//    }
//
//    private fun setVbScoreText(matchInfo: MatchInfo) {
//        setAllScoreTextAtBottom(matchInfo)
//        setScoreTextAtFront(matchInfo)
//        setSptText(matchInfo)
//        setCurrentPeroid(matchInfo)
//        setAttack(matchInfo)
//    }
//
//    private fun setTnScoreText(matchInfo: MatchInfo) {
//        setAllScoreTextAtBottom(matchInfo)
//        setSptText(matchInfo)
//        setTennisRoundScore(matchInfo)
//        setCurrentPeroid(matchInfo)
//        setAttack(matchInfo)
//    }
//
//    private fun setBmScoreText(matchInfo: MatchInfo) {
//        setAllScoreTextAtBottom(matchInfo)
//        setScoreTextAtFront(matchInfo)
//        setSptText(matchInfo)
//        setCurrentPeroid(matchInfo)
//        setAttack(matchInfo)
//    }
//
//    private fun setBbScoreText(matchInfo: MatchInfo) {
//        if (TimeUtil.isTimeInPlay(matchInfo.startTime)) {
//            setScoreTextAtFront(matchInfo)
//            setAttack(matchInfo)
//            setBBStatus(matchInfo)
//        } else
//            setBkScoreText(matchInfo)
//    }
//
//    /**
//     * 賽制(5盤3勝)
//     * 只有网球，排球，乒乓球，羽毛球
//     */
//    @SuppressLint("SetTextI18n")
//    private fun setSptText(matchInfo: MatchInfo) {
//        matchInfo.spt?.let {
//            if (it == 3 || it == 5 || it == 7) {
//                itemView.league_spt.visibility = View.VISIBLE
//                itemView.league_spt.text = when (it) {
//                    3 -> {
//                        when (matchInfo.gameType) {
//                            ////排球，乒乓球显示3局2胜
//                            GameType.BM.key -> itemView.context.getString(R.string.spt_number_3_2_bm)
//                            else -> itemView.context.getString(R.string.spt_number_3_2)
//                        }
//                    }
//                    5 -> {
//                        when (matchInfo.gameType) {
//                            //排球，乒乓球显示5局3胜
//                            GameType.VB.key, GameType.TT.key -> itemView.context.getString(R.string.spt_number_5_3_vb)
//                            else -> itemView.context.getString(R.string.spt_number_5_3)
//                        }
//                    }
//                    7 -> {
//                        //部分乒乓球会采用七局四胜制
//                        when (matchInfo.gameType) {
//                            GameType.TT.key -> itemView.context.getString(R.string.spt_number_7_4_tt)
//                            else -> ""
//                        }
//                    }
//                    else -> ""
//                }
//            } else {
//                itemView.league_spt.visibility = View.GONE
//            }
//        }
//    }
//
//    /**
//     * 设置当前盘数/局数/回合
//     * 网球显示 第x盘
//     * 其他球类显示 第x局
//     */
//    @SuppressLint("SetTextI18n")
//    private fun setCurrentPeroid(matchInfo: MatchInfo) {
//        matchInfo.matchStatusList?.let { it ->
//            if (it.isEmpty()) return
//            itemView.tv_peroid.visibility = View.VISIBLE
//            it.last()?.let {
//                itemView.tv_peroid.text = it.statusNameI18n?.get(
//                    LanguageManager.getSelectLanguage(context = itemView.context).key
//                ) ?: it.statusName
//            }
//        }
//    }
//
//    /**
//     * 设置足球黄牌，红牌数量
//     */
//    private fun setCardText(matchInfo: MatchInfo) {
//        itemView.apply {
//            league_odd_match_cards_home.apply {
//                visibility = when {
//                    TimeUtil.isTimeInPlay(matchInfo.startTime)
//                            && (matchInfo.homeCards ?: 0 > 0) -> View.VISIBLE
//                    else -> View.GONE
//                }
//                text = (matchInfo.homeCards ?: 0).toString()
//            }
//            league_odd_match_cards_away.apply {
//                visibility = when {
//                    TimeUtil.isTimeInPlay(matchInfo.startTime)
//                            && (matchInfo.awayCards ?: 0 > 0) -> View.VISIBLE
//                    else -> View.GONE
//                }
//                text = (matchInfo.awayCards ?: 0).toString()
//            }
//            league_odd_yellow_cards_home.apply {
//                visibility = when {
//                    TimeUtil.isTimeInPlay(matchInfo.startTime)
//                            && (matchInfo.homeYellowCards ?: 0 > 0) -> View.VISIBLE
//                    else -> View.GONE
//                }
//                text = (matchInfo.homeYellowCards ?: 0).toString()
//            }
//            league_odd_yellow_cards_away.apply {
//                visibility = when {
//                    TimeUtil.isTimeInPlay(matchInfo.startTime)
//                            && (matchInfo.awayYellowCards ?: 0 > 0) -> View.VISIBLE
//                    else -> View.GONE
//                }
//                text = (matchInfo.awayYellowCards ?: 0).toString()
//            }
//        }
//
//    }
//
//    /**
//     * 设置球权标识，
//     *  目前支持 棒球，网球，排球，乒乓球，羽毛球
//     *  其中网球标识是另外一个位置
//     */
//    private fun setAttack(matchInfo: MatchInfo) {
//        if (TimeUtil.isTimeInPlay(matchInfo.startTime)) {
//            itemView.apply {
//                when (matchInfo.gameType) {
//                    GameType.BB.key,
//                    GameType.VB.key,
//                    GameType.TT.key,
//                    GameType.BM.key,
//                    -> {
//                        if (matchInfo.attack.equals("H")) {
//                            ic_attack_h.visibility = View.VISIBLE
//                            ic_attack_c.visibility = View.INVISIBLE
//                        } else {
//                            ic_attack_h.visibility = View.INVISIBLE
//                            ic_attack_c.visibility = View.VISIBLE
//                        }
//                    }
//                    GameType.TN.key -> {
//                        if (matchInfo.attack.equals("H")) {
//                            ic_attack_tn_h.visibility = View.VISIBLE
//                            ic_attack_tn_c.visibility = View.INVISIBLE
//                        } else {
//                            ic_attack_tn_h.visibility = View.INVISIBLE
//                            ic_attack_tn_c.visibility = View.VISIBLE
//                        }
//                    }
//                    else -> {
//                        ic_attack_h.visibility = View.GONE
//                        ic_attack_c.visibility = View.GONE
//                    }
//                }
//            }
//        } else {
//            itemView.apply {
//                ic_attack_h.visibility = View.GONE
//                ic_attack_c.visibility = View.GONE
//                ic_attack_tn_h.visibility = View.INVISIBLE
//                ic_attack_tn_c.visibility = View.INVISIBLE
//            }
//        }
//
//    }
//
//    private fun setFbKicks(matchInfo: MatchInfo) {
//        itemView.apply {
//            league_corner_kicks.apply {
//                visibility = when {
//                    TimeUtil.isTimeInPlay(matchInfo.startTime)
//                            && (matchInfo.homeCornerKicks ?: 0 > 0 || matchInfo.awayCornerKicks ?: 0 > 0) -> View.VISIBLE
//                    else -> View.GONE
//                }
//                text = (matchInfo.homeCornerKicks
//                    ?: 0).toString() + "-" + (matchInfo.awayCornerKicks ?: 0)
//            }
//        }
//    }
//
//    private fun setScoreTextAtFront(matchInfo: MatchInfo) {
//        itemView.apply {
//            league_odd_match_score_home.apply {
//                visibility = when (TimeUtil.isTimeInPlay(matchInfo.startTime)) {
//                    true -> View.VISIBLE
//                    else -> View.GONE
//                }
//                text = when (matchInfo.gameType) {
//                    GameType.VB.key, GameType.TT.key, GameType.BM.key -> (matchInfo.homeTotalScore
//                        ?: 0).toString()
//                    else -> (matchInfo.homeScore ?: 0).toString()
//                }
//            }
//
//            league_odd_match_score_away.apply {
//                visibility = when (TimeUtil.isTimeInPlay(matchInfo.startTime)) {
//                    true -> View.VISIBLE
//                    else -> View.GONE
//                }
//                text = when (matchInfo.gameType) {
//                    GameType.VB.key, GameType.TT.key, GameType.BM.key -> (matchInfo.awayTotalScore
//                        ?: 0).toString()
//                    else -> (matchInfo.awayScore ?: 0).toString()
//                }
//            }
//        }
//    }
//
//    /**
//     * 网球和羽毛球  排球，乒乓球 显示局比分
//     */
//    private fun setAllScoreTextAtBottom(matchInfo: MatchInfo) {
//        matchInfo.matchStatusList?.let { matchStatusList ->
//            var spanny = Spanny()
//            matchStatusList.forEachIndexed { index, it ->
//                val spanScore = "${it.homeScore ?: 0}-${it.awayScore ?: 0}"
//                if (index < matchStatusList.lastIndex) {
//                    spanny.append(spanScore)
//                    spanny.append("  ")
//                } else {
//                    spanny.append(spanScore,
//                        ForegroundColorSpan(itemView.context.getColor(R.color.color_F0A536))
//                    )
//                }
//            }
//            itemView.tv_peroids_score.isVisible = true
//            itemView.tv_peroids_score.text = spanny
//        }
//    }
//
//    /**
//     * 设置网球的中间分数布局
//     */
//    private fun setTennisRoundScore(matchInfo: MatchInfo) = itemView.run {
//        //隐藏其他球类的比分
//        league_odd_match_score_home.visibility = View.GONE
//        league_odd_match_score_away.visibility = View.GONE
//        ic_attack_h.visibility = View.GONE
//        ic_attack_c.visibility = View.GONE
//        lin_home_round_score.visibility = View.VISIBLE
//        lin_away_round_score.visibility = View.VISIBLE
//        val visable = TimeUtil.isTimeInPlay(matchInfo.startTime)
//        //设置盘比，局比，分数
//        league_odd_match_total_score_home_bottom.apply {
//            isVisible = visable
//            text = (matchInfo.homeTotalScore ?: 0).toString()
//        }
//
//        league_odd_match_total_score_away_bottom.apply {
//            isVisible = visable
//            text = (matchInfo.awayTotalScore ?: 0).toString()
//        }
//        league_odd_match_score_home_bottom.apply {
//            isVisible = visable
//            text = (matchInfo.homeScore ?: 0).toString()
//        }
//
//        league_odd_match_score_away_bottom.apply {
//            isVisible = visable
//            text = (matchInfo.awayScore ?: 0).toString()
//        }
//        league_odd_match_point_home_bottom.apply {
//            isVisible = visable
//            text = (matchInfo.homePoints ?: 0).toString()
//        }
//
//        league_odd_match_point_away_bottom.apply {
//            isVisible = visable
//            text = (matchInfo.awayPoints ?: 0).toString()
//        }
//    }
//
//    /**
//     * 棒球的特殊布局处理
//     */
//    private fun setBBStatus(matchInfo: MatchInfo) {
//        itemView.apply {
//            linear_layout.isVisible = false
//            content_baseball_status.isVisible = true
//            league_odd_match_bb_status.apply {
//                text = matchInfo.statusName18n
//                isVisible = true
//            }
//
//            txvOut.apply {
//                text = this.context.getString(R.string.game_out,
//                    matchInfo.outNumber ?: "")
//                isVisible = true
//            }
//
//            league_odd_match_halfStatus.apply {
//                setImageResource(if (matchInfo.halfStatus == 0) R.drawable.ic_bb_first_half else R.drawable.ic_bb_second_half)
//                isVisible = true
//            }
//
//            league_odd_match_basebag.apply {
//                setImageResource(
//                    when {
//                        matchInfo.firstBaseBag == 0 && matchInfo.secBaseBag == 0 && matchInfo.thirdBaseBag == 0 -> R.drawable.ic_bb_base_bag_0_0_0
//                        matchInfo.firstBaseBag == 1 && matchInfo.secBaseBag == 0 && matchInfo.thirdBaseBag == 0 -> R.drawable.ic_bb_base_bag_1_0_0
//                        matchInfo.firstBaseBag == 0 && matchInfo.secBaseBag == 1 && matchInfo.thirdBaseBag == 0 -> R.drawable.ic_bb_base_bag_0_1_0
//                        matchInfo.firstBaseBag == 0 && matchInfo.secBaseBag == 0 && matchInfo.thirdBaseBag == 1 -> R.drawable.ic_bb_base_bag_0_0_1
//                        matchInfo.firstBaseBag == 1 && matchInfo.secBaseBag == 1 && matchInfo.thirdBaseBag == 0 -> R.drawable.ic_bb_base_bag_1_1_0
//                        matchInfo.firstBaseBag == 1 && matchInfo.secBaseBag == 0 && matchInfo.thirdBaseBag == 1 -> R.drawable.ic_bb_base_bag_1_0_1
//                        matchInfo.firstBaseBag == 0 && matchInfo.secBaseBag == 1 && matchInfo.thirdBaseBag == 1 -> R.drawable.ic_bb_base_bag_0_1_1
//                        matchInfo.firstBaseBag == 1 && matchInfo.secBaseBag == 1 && matchInfo.thirdBaseBag == 1 -> R.drawable.ic_bb_base_bag_1_1_1
//                        else -> R.drawable.ic_bb_base_bag_0_0_0
//                    }
//                )
//                isVisible = true
//            }
//        }
//
//    }
//
//}