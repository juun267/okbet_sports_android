//package org.cxct.sportlottery.ui.sport
//
//import android.annotation.SuppressLint
//import android.content.Context
//import android.os.Handler
//import android.os.Looper
//import android.text.Html
//import android.text.Spanned
//import android.text.style.ForegroundColorSpan
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.core.view.isVisible
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import androidx.recyclerview.widget.SimpleItemAnimator
//import kotlinx.android.synthetic.main.content_baseball_status.view.*
//import kotlinx.android.synthetic.main.item_sport_odd.view.*
//import org.cxct.sportlottery.MultiLanguagesApplication
//import org.cxct.sportlottery.R
//import org.cxct.sportlottery.enum.MatchSource
//import org.cxct.sportlottery.enum.PayLoadEnum
//import org.cxct.sportlottery.network.common.GameStatus
//import org.cxct.sportlottery.network.common.GameType
//import org.cxct.sportlottery.network.common.MatchType
//import org.cxct.sportlottery.network.common.PlayCate
//import org.cxct.sportlottery.network.odds.MatchInfo
//import org.cxct.sportlottery.network.odds.Odd
//import org.cxct.sportlottery.network.odds.list.LeagueOdd
//import org.cxct.sportlottery.network.odds.list.MatchOdd
//import org.cxct.sportlottery.network.odds.list.TimeCounting
//import org.cxct.sportlottery.ui.common.CustomLinearLayoutManager
//import org.cxct.sportlottery.ui.component.overScrollView.OverScrollDecoratorHelper
//import org.cxct.sportlottery.ui.game.common.*
//import org.cxct.sportlottery.ui.game.common.view.OddBtnList
//import org.cxct.sportlottery.ui.menu.OddsType
//import org.cxct.sportlottery.util.*
//import java.util.*
//
//class SportOddAdapter2(private val matchType: MatchType, private val oddBtnCachePool: RecyclerView.RecycledViewPool) :
//    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
//
//    var data = listOf<MatchOdd>()
//    set(value) {
//        field = value
//        matchInfos = null
//    }
//    private var matchInfos: List<MatchInfo>? = null
//
//    var oddsType: OddsType = OddsType.EU
//    fun setData(data: List<MatchOdd> = listOf(), oddsType: OddsType = OddsType.EU) {
//        this.data = data
//        this.oddsType = oddsType
//        //notifyDataSetChanged()
//    }
//
//    var isTimerEnable = false
//        set(value) {
//            if (value != field) {
//                field = value
//                //notifyDataSetChanged()
//            }
//        }
//
//    var leagueOddListener: LeagueOddListener? = null
//    var leagueOdd: LeagueOdd? = null
//    private val oddStateRefreshListener by lazy {
//        object : OddStateViewHolder.OddStateChangeListener {
//            override fun refreshOddButton(odd: Odd) {
//                notifyItemChanged(data.indexOf(data.find { matchOdd ->
//                    matchOdd.oddsMap?.toList()
//                        ?.find { map -> map.second?.find { it == odd } != null } != null
//                }))
//            }
//        }
//    }
//
//    override fun getItemViewType(position: Int): Int {
//        return SportLeagueAdapter.ItemType.ITEM.ordinal
//    }
//
//    // region Update functions
//    fun update() {
//        data.forEachIndexed { index, matchOdd -> notifyItemChanged(index, matchOdd) }
//    }
//
//    fun updateIndex(index: Int, matchOdd: MatchOdd) {
//        notifyItemChanged(index, matchOdd)
//    }
//
//    fun updateByBetInfo(clickOdd: Odd?) {
//        data.forEachIndexed { index, matchOdd ->
//            matchOdd.oddsMap?.values?.forEach { oddList ->
//                if (oddList?.any { it?.id == clickOdd?.id } == true) {
//                    notifyItemChanged(index, Pair(PayLoadEnum.PAYLOAD_BET_INFO, matchOdd))
//                    leagueOddListener?.clickOdd = null
//                }
//            }
//        }
//    }
//
//    fun updateByPlayCate() {
//        data.forEachIndexed { index, matchOdd ->
//            notifyItemChanged(index, Pair(PayLoadEnum.PAYLOAD_PLAYCATE, matchOdd))
//        }
//    }
//
//    fun updateByMatchIdForOdds(matchOdd: MatchOdd) {
//        val index = data.indexOf(data.find { it == matchOdd })
//        notifyItemChanged(index, matchOdd)
//    }
//
//    fun updateBySelectCsTab(matchOdd: MatchOdd) {
//        val index = data.indexOf(data.find { it == matchOdd })
//        notifyItemChanged(index, matchOdd)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        val start = System.currentTimeMillis()
//        val holder = from(parent, oddStateRefreshListener, oddBtnCachePool)
//        forTest("SportOddAdapter onCreateViewHolder 111 耗费：${System.currentTimeMillis() - start}")
//        return holder
//    }
//
//    private fun from(parent: ViewGroup, refreshListener: OddStateViewHolder.OddStateChangeListener, cachePool: RecyclerView.RecycledViewPool): ViewHolderHdpOu {
//        val layoutInflater = LayoutInflater.from(parent.context)
//        val view = layoutInflater.inflate(R.layout.item_sport_odd, parent, false)
//        val hodler = ViewHolderHdpOu(view, refreshListener)
//        hodler.itemView.rv_league_odd_btn_pager_main.setRecycledViewPool(cachePool)
//        return hodler
//    }
//
//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//
//        val start = System.currentTimeMillis()
//        val item = data[position]
//        if (holder is ViewHolderHdpOu) {
//            holder.stopTimer()
//            holder.bind(matchType, item, leagueOddListener, isTimerEnable, oddsType)
//        }
//
//        forTest("SportOddAdapter onBindViewHolder 222 耗费：${System.currentTimeMillis() - start}")
//    }
//
//    private fun getMatchInfoList(): List<MatchInfo> {
//        if (matchInfos == null) {
//            matchInfos = data.mapNotNull { it.matchInfo }
//        }
//        return matchInfos!!
//    }
//
//    // region update by payload functions
//    override fun onBindViewHolder(
//        holder: RecyclerView.ViewHolder,
//        position: Int,
//        payloads: MutableList<Any>,
//    ) {
//        if (payloads.isNullOrEmpty()) {
//            onBindViewHolder(holder, position)
//        } else {
//            Log.d("Hewie", "更新：賽事($position)")
//            val start = System.currentTimeMillis()
//
//
//            when (payloads.first()) {
//                is MatchOdd -> {
//                    val matchOdd = payloads.first() as MatchOdd
//                    (holder as ViewHolderHdpOu).update(
//                        matchType,
//                        matchOdd,
//                        leagueOddListener,
//                        isTimerEnable,
//                        oddsType
//                    )
//                }
//
//                is Pair<*, *> -> {
//                    (payloads.first() as Pair<*, *>).apply {
//                        when (first) {
//                            PayLoadEnum.PAYLOAD_BET_INFO -> {
//                                (holder as ViewHolderHdpOu).updateByBetInfo(
//                                    item = second as MatchOdd,
//                                    leagueOddListener = leagueOddListener,
//                                    oddsType = oddsType,
//                                )
//                            }
//
//                            PayLoadEnum.PAYLOAD_PLAYCATE -> {
//                                (holder as ViewHolderHdpOu).updateByPlayCate(
//                                    item = second as MatchOdd,
//                                    oddsType = oddsType,
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//
//            forTest("SportOddAdapter onBindViewHolder 333 耗费：${System.currentTimeMillis() - start}")
//        }
//    }
//
//    override fun getItemCount(): Int = data.size
//
//    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
//        super.onViewRecycled(holder)
//
//        when (holder) {
//            is ViewHolderTimer -> holder.stopTimer()
//        }
//    }
//
//    inner class ViewHolderHdpOu constructor(
//        itemView: View,
//        private val refreshListener: OddStateChangeListener,
//    ) : ViewHolderTimer(itemView) {
//
//        val oddButtonPagerAdapter = OddButtonPagerAdapter()
//
//        fun bind(
//            matchType: MatchType,
//            item: MatchOdd,
//            leagueOddListener: LeagueOddListener?,
//            isTimerEnable: Boolean,
//            oddsType: OddsType,
//        ) {
//            setupMatchInfo(item, matchType, leagueOddListener)
//            val isTimerPause = item.matchInfo?.stopped == TimeCounting.STOP.value
//            item.matchInfo?.let {
//                setupMatchTimeAndStatus(it,
//                    matchType,
//                    isTimerEnable,
//                    isTimerPause,
//                    leagueOddListener)
//            }
//            setupOddsButton(matchType,
//                item,
//                oddsType,
//                leagueOddListener)
//
//            //setupQuickCategory(item, oddsType, leagueOddListener)
//        }
//
//        // region update functions
//        fun update(
//            matchType: MatchType,
//            item: MatchOdd,
//            leagueOddListener: LeagueOddListener?,
//            isTimerEnable: Boolean,
//            oddsType: OddsType,
//        ) {
//            updateMatchInfo(item, matchType)
//            val isTimerPause = item.matchInfo?.stopped == TimeCounting.STOP.value
//            item.matchInfo?.let {
//                setupMatchTimeAndStatus(it,
//                    matchType,
//                    isTimerEnable,
//                    isTimerPause,
//                    leagueOddListener)
//            }
//            updateOddsButton(item, oddsType)
//            setupCsTextLayout(matchType, item, leagueOddListener)
//        }
//
//
//        private fun setupCsTextLayout(
//            matchType: MatchType,
//            item: MatchOdd,
//            leagueOddListener: LeagueOddListener?,
//        ) {
//            itemView.apply {
//                if (matchType == MatchType.CS) {
//                    ll_cs_text_layout.isVisible = true
//
//                    //比照h5，直接使用local波膽翻譯文字
//                    tv_correct_1.text = context.getText(R.string.correct)
//
//                    var correct2 = item.playCateNameMap?.get(PlayCate.CS_1ST_SD.value)
//                        ?.getPlayCateName(context) ?: ""
//                    if (correct2.contains("||")) {
//                        val correct2Split = correct2.split("||")
//                        //將換行符後的文字移到前面顯示
//                        correct2 = correct2Split[1] + correct2Split[0]
//                    }
//                    tv_correct_2.text = correct2
//                    var correct3 = item.playCateNameMap?.get(PlayCate.LCS.value)
//                        ?.getPlayCateName(context) ?: ""
//                    tv_correct_3.text = correct3
//                    when (item.csTabSelected) {
//                        PlayCate.CS -> {
//                            tv_correct_1.isSelected = true
//                            tv_correct_2.isSelected = false
//                            tv_correct_3.isSelected = false
//                        }
//                        PlayCate.CS_1ST_SD -> {
//                            tv_correct_1.isSelected = false
//                            tv_correct_2.isSelected = true
//                            tv_correct_3.isSelected = false
//                        }
//                        else -> {
//                            tv_correct_1.isSelected = false
//                            tv_correct_2.isSelected = false
//                            tv_correct_3.isSelected = true
//                        }
//                    }
//
//                    tv_correct_1.setOnClickListener {
//                        tv_correct_1.isSelected = true
//                        tv_correct_2.isSelected = false
//                        tv_correct_3.isSelected = false
//                        leagueOddListener?.onClickCsTabListener(PlayCate.CS, item)
//                    }
//
//                    tv_correct_2.setOnClickListener {
//                        tv_correct_1.isSelected = false
//                        tv_correct_2.isSelected = true
//                        tv_correct_3.isSelected = false
//                        leagueOddListener?.onClickCsTabListener(PlayCate.CS_1ST_SD, item)
//                    }
//                    tv_correct_3.setOnClickListener {
//                        tv_correct_1.isSelected = false
//                        tv_correct_2.isSelected = false
//                        tv_correct_3.isSelected = true
//                        leagueOddListener?.onClickCsTabListener(PlayCate.LCS, item)
//                    }
//                } else {
//                    ll_cs_text_layout.isVisible = false
//                }
//            }
//        }
//
//        private fun <K, V> Map<K, V>?.getPlayCateName(context: Context): String {
//            var selectLanguage = LanguageManager.getSelectLanguage(context)
//            val playCateName = this?.get<Any?, V>(selectLanguage.key) ?: this?.get<Any?, V>(
//                LanguageManager.Language.EN.key)
//            return playCateName.toString()
//        }
//
//        fun updateByBetInfo(
//            item: MatchOdd,
//            oddsType: OddsType,
//            leagueOddListener: LeagueOddListener?,
//        ) {
//            updateOddsButtonByBetInfo(item)
//        }
//
//        fun updateByPlayCate(
//            item: MatchOdd,
//            oddsType: OddsType,
//        ) {
//            updateOddsButton(item, oddsType)
//        }
//
//        private fun updateMatchInfo(item: MatchOdd, matchType: MatchType) {
//            itemView.league_odd_match_name_home.text = item.matchInfo?.homeName
//            itemView.league_odd_match_name_away.text = item.matchInfo?.awayName
//            itemView.iv_home_team_logo.setTeamLogo(item.matchInfo?.homeIcon)
//            itemView.iv_away_team_logo.setTeamLogo(item.matchInfo?.awayIcon)
//            setupMatchScore(item, matchType)
//            itemView.league_odd_match_play_count.text =
//                item.matchInfo?.playCateNum.toString() + "+>"
//            itemView.league_odd_match_favorite.isSelected = item.matchInfo?.isFavorite ?: false
////            itemView.league_odd_match_price_boost.isVisible = item.matchInfo?.eps == 1
//            itemView.league_neutral.apply {
//                isSelected = item.matchInfo?.neutral == 1
//                isVisible = item.matchInfo?.neutral == 1
//            }
////            滚球动画 直播 显示控制
//            //itemView.space2.isVisible = (item.matchInfo?.eps == 1 || item.matchInfo?.liveVideo == 1)
//            itemView.iv_live.isVisible =
//                item.matchInfo?.isLive == 1
//            itemView.iv_play.isVisible =
//                item.matchInfo?.liveVideo == 1
//            itemView.iv_animation.isVisible =
//                !(item.matchInfo?.trackerId.isNullOrEmpty()) && MultiLanguagesApplication.getInstance()
//                    ?.getGameDetailAnimationNeedShow() == true
//        }
//
//        private fun setupMatchInfo(
//            item: MatchOdd,
//            matchType: MatchType,
//            leagueOddListener: LeagueOddListener?,
//        ) {
//
//            itemView.league_odd_match_name_home.text = item.matchInfo?.homeName
//
//            itemView.league_odd_match_name_away.text = item.matchInfo?.awayName
//            itemView.iv_home_team_logo.setTeamLogo(item.matchInfo?.homeIcon)
//            itemView.iv_away_team_logo.setTeamLogo(item.matchInfo?.awayIcon)
//
//            setupMatchScore(item, matchType)
//
//            itemView.league_odd_match_play_count.apply {
//                text = item.matchInfo?.playCateNum.toString() + "+>"
//
//                setOnClickListener {
//                    leagueOddListener?.onClickPlayType(
//                        item.matchInfo?.id,
//                        getMatchInfoList(),
//                        if (TimeUtil.isTimeInPlay(item.matchInfo?.startTime)) MatchType.IN_PLAY else matchType,
//                        item.matchInfo?.liveVideo ?: 0
//                    )
//                }
//            }
//            itemView.league_odd_match_favorite.isSelected = item.matchInfo?.isFavorite ?: false
//            itemView.league_odd_match_favorite.apply {
//                isSelected = item.matchInfo?.isFavorite ?: false
//                setOnClickListener {
//                    leagueOddListener?.onClickFavorite(item.matchInfo?.id)
//                }
//            }
//
//            itemView.league_neutral.apply {
//                isSelected = item.matchInfo?.neutral == 1
//                isVisible = item.matchInfo?.neutral == 1
//            }
//
//            itemView.league_odd_match_chart.apply {
//                visibility =
//                    if (item.matchInfo?.source == MatchSource.SHOW_STATISTICS.code) View.VISIBLE else View.GONE
//
//                setOnClickListener {
//                    leagueOddListener?.onClickStatistics(item.matchInfo?.id)
//                }
//            }
//
//            itemView.lin_match.setOnClickListener {
//                leagueOddListener?.onClickPlayType(
//                    item.matchInfo?.id,
//                    getMatchInfoList(),
//                    if (TimeUtil.isTimeInPlay(item.matchInfo?.startTime)) MatchType.IN_PLAY else matchType,
//                    item.matchInfo?.liveVideo ?: 0
//                )
//            }
//
////            itemView.league_odd_match_price_boost.isVisible = item.matchInfo?.eps == 1
//            //itemView.space2.isVisible = (item.matchInfo?.eps == 1 || item.matchInfo?.liveVideo == 1)
//            with(itemView.iv_play) {
//                isVisible =
//                    item.matchInfo?.liveVideo == 1 && (TimeUtil.isTimeInPlay(item.matchInfo?.startTime))
//
//                setOnClickListener {
//                    leagueOddListener?.onClickLiveIconListener(
//                        item.matchInfo?.id,
//                        getMatchInfoList(),
//                        if (TimeUtil.isTimeInPlay(item.matchInfo?.startTime)) MatchType.IN_PLAY else matchType,
//                        item.matchInfo?.liveVideo ?: 0
//                    )
//                }
//            }
//            with(itemView.iv_animation) {
//                isVisible =
//                    TimeUtil.isTimeInPlay(item.matchInfo?.startTime) && !(item.matchInfo?.trackerId.isNullOrEmpty()) && MultiLanguagesApplication.getInstance()
//                        ?.getGameDetailAnimationNeedShow() == true
//                setOnClickListener {
//                    leagueOddListener?.onClickAnimationIconListener(
//                        item.matchInfo?.id,
//                        getMatchInfoList(),
//                        if (TimeUtil.isTimeInPlay(item.matchInfo?.startTime)) MatchType.IN_PLAY else matchType,
//                        item.matchInfo?.liveVideo ?: 0
//                    )
//                }
//            }
//
//        }
//
//        private fun setupMatchScore(item: MatchOdd, matchType: MatchType) {
//            itemView.apply {
//                when {
//                    matchType != MatchType.IN_PLAY -> {
//                        linear_layout.isVisible = true
//                        content_baseball_status.isVisible = false
//                    }
//                    else -> {
//                        when (item.matchInfo?.gameType) {
//                            GameType.BB.key -> {
//                                linear_layout.isVisible = false
//                                content_baseball_status.isVisible = true
//                            }
//                            else -> {
//                                linear_layout.isVisible = true
//                                content_baseball_status.isVisible = false
//                            }
//                        }
//
//                    }
//                }
//            }
//
//            when (item.matchInfo?.socketMatchStatus) {
//                //20220507 status:999 邏輯變更 隱藏分數 -> 賽事狀態變為滾球
//                /*GameMatchStatus.HIDE_SCORE.value -> {
//                    hideMatchScoreText()
//                }*/
//                else -> {
//                    when (item.matchInfo?.gameType) {
//                        GameType.VB.key -> setVbScoreText(item.matchInfo)
//                        GameType.TN.key -> setTnScoreText(item.matchInfo)
//                        GameType.FT.key -> setFtScoreText(item.matchInfo)
//                        GameType.BK.key -> setBkScoreText(item.matchInfo)
//                        GameType.TT.key -> setVbScoreText(item.matchInfo)
//                        GameType.BM.key -> setBmScoreText(item.matchInfo)
//                        GameType.BB.key -> setBbScoreText(item.matchInfo)
//                        else -> item.matchInfo?.let { setBkScoreText(it) }
//                    }
//                }
//            }
//        }
//
//        private fun setFtScoreText(matchInfo: MatchInfo) {
//            setScoreTextAtFront(matchInfo)
//            setCardText(matchInfo)
//            setFbKicks(matchInfo)
//        }
//
//        private fun setBkScoreText(matchInfo: MatchInfo) {
//            setScoreTextAtFront(matchInfo)
//        }
//
//        private fun setVbScoreText(matchInfo: MatchInfo) {
//            setAllScoreTextAtBottom(matchInfo)
//            setScoreTextAtFront(matchInfo)
//            setSptText(matchInfo)
//            setCurrentPeroid(matchInfo)
//            setAttack(matchInfo)
//        }
//
//        private fun setTnScoreText(matchInfo: MatchInfo) {
//            setAllScoreTextAtBottom(matchInfo)
//            setSptText(matchInfo)
//            setTennisRoundScore(matchInfo)
//            setCurrentPeroid(matchInfo)
//            setAttack(matchInfo)
//        }
//
//        private fun setBmScoreText(matchInfo: MatchInfo) {
//            setAllScoreTextAtBottom(matchInfo)
//            setScoreTextAtFront(matchInfo)
//            setSptText(matchInfo)
//            setCurrentPeroid(matchInfo)
//            setAttack(matchInfo)
//        }
//
//        private fun setBbScoreText(matchInfo: MatchInfo) {
//            if (TimeUtil.isTimeInPlay(matchInfo.startTime)) {
//                setScoreTextAtFront(matchInfo)
//                setAttack(matchInfo)
//                setBBStatus(matchInfo)
//            } else
//                setBkScoreText(matchInfo)
//        }
//
//        /**
//         * 賽制(5盤3勝)
//         * 只有网球，排球，乒乓球，羽毛球
//         */
//        @SuppressLint("SetTextI18n")
//        private fun setSptText(matchInfo: MatchInfo) {
//            matchInfo.spt?.let {
//                if (it == 3 || it == 5 || it == 7) {
//                    itemView.league_spt.visibility = View.VISIBLE
//                    itemView.league_spt.text = when (it) {
//                        3 -> {
//                            when (matchInfo.gameType) {
//                                ////排球，乒乓球显示3局2胜
//                                GameType.BM.key -> itemView.context.getString(R.string.spt_number_3_2_bm)
//                                else -> itemView.context.getString(R.string.spt_number_3_2)
//                            }
//                        }
//                        5 -> {
//                            when (matchInfo.gameType) {
//                                //排球，乒乓球显示5局3胜
//                                GameType.VB.key, GameType.TT.key -> itemView.context.getString(R.string.spt_number_5_3_vb)
//                                else -> itemView.context.getString(R.string.spt_number_5_3)
//                            }
//                        }
//                        7 -> {
//                            //部分乒乓球会采用七局四胜制
//                            when (matchInfo.gameType) {
//                                GameType.TT.key -> itemView.context.getString(R.string.spt_number_7_4_tt)
//                                else -> ""
//                            }
//                        }
//                        else -> ""
//                    }
//                } else {
//                    itemView.league_spt.visibility = View.GONE
//                }
//            }
//        }
//
//        /**
//         * 设置当前盘数/局数/回合
//         * 网球显示 第x盘
//         * 其他球类显示 第x局
//         */
//        @SuppressLint("SetTextI18n")
//        private fun setCurrentPeroid(matchInfo: MatchInfo) {
//            matchInfo.matchStatusList?.let { it ->
//                if (it.isEmpty()) return
//                itemView.tv_peroid.visibility = View.VISIBLE
//                it.last()?.let {
//                    itemView.tv_peroid.text = it.statusNameI18n?.get(
//                        LanguageManager.getSelectLanguage(context = itemView.context).key
//                    ) ?: it.statusName
//                }
//            }
//        }
//
//        /**
//         * 设置足球黄牌，红牌数量
//         */
//        private fun setCardText(matchInfo: MatchInfo) {
//            itemView.apply {
//                league_odd_match_cards_home.apply {
//                    visibility = when {
//                        TimeUtil.isTimeInPlay(matchInfo.startTime)
//                                && (matchInfo.homeCards ?: 0 > 0) -> View.VISIBLE
//                        else -> View.GONE
//                    }
//                    text = (matchInfo.homeCards ?: 0).toString()
//                }
//                league_odd_match_cards_away.apply {
//                    visibility = when {
//                        TimeUtil.isTimeInPlay(matchInfo.startTime)
//                                && (matchInfo.awayCards ?: 0 > 0) -> View.VISIBLE
//                        else -> View.GONE
//                    }
//                    text = (matchInfo.awayCards ?: 0).toString()
//                }
//                league_odd_yellow_cards_home.apply {
//                    visibility = when {
//                        TimeUtil.isTimeInPlay(matchInfo.startTime)
//                                && (matchInfo.homeYellowCards ?: 0 > 0) -> View.VISIBLE
//                        else -> View.GONE
//                    }
//                    text = (matchInfo.homeYellowCards ?: 0).toString()
//                }
//                league_odd_yellow_cards_away.apply {
//                    visibility = when {
//                        TimeUtil.isTimeInPlay(matchInfo.startTime)
//                                && (matchInfo.awayYellowCards ?: 0 > 0) -> View.VISIBLE
//                        else -> View.GONE
//                    }
//                    text = (matchInfo.awayYellowCards ?: 0).toString()
//                }
//            }
//
//        }
//
//        /**
//         * 设置球权标识，
//         *  目前支持 棒球，网球，排球，乒乓球，羽毛球
//         *  其中网球标识是另外一个位置
//         */
//        private fun setAttack(matchInfo: MatchInfo) {
//            if (TimeUtil.isTimeInPlay(matchInfo.startTime)) {
//                itemView.apply {
//                    when (matchInfo.gameType) {
//                        GameType.BB.key,
//                        GameType.VB.key,
//                        GameType.TT.key,
//                        GameType.BM.key,
//                        -> {
//                            if (matchInfo.attack.equals("H")) {
//                                ic_attack_h.visibility = View.VISIBLE
//                                ic_attack_c.visibility = View.INVISIBLE
//                            } else {
//                                ic_attack_h.visibility = View.INVISIBLE
//                                ic_attack_c.visibility = View.VISIBLE
//                            }
//                        }
//                        GameType.TN.key -> {
//                            if (matchInfo.attack.equals("H")) {
//                                ic_attack_tn_h.visibility = View.VISIBLE
//                                ic_attack_tn_c.visibility = View.INVISIBLE
//                            } else {
//                                ic_attack_tn_h.visibility = View.INVISIBLE
//                                ic_attack_tn_c.visibility = View.VISIBLE
//                            }
//                        }
//                        else -> {
//                            ic_attack_h.visibility = View.GONE
//                            ic_attack_c.visibility = View.GONE
//                        }
//                    }
//                }
//            } else {
//                itemView.apply {
//                    ic_attack_h.visibility = View.GONE
//                    ic_attack_c.visibility = View.GONE
//                    ic_attack_tn_h.visibility = View.INVISIBLE
//                    ic_attack_tn_c.visibility = View.INVISIBLE
//                }
//            }
//
//        }
//
//        private fun setFbKicks(matchInfo: MatchInfo) {
//            itemView.apply {
//                league_corner_kicks.apply {
//                    visibility = when {
//                        TimeUtil.isTimeInPlay(matchInfo.startTime)
//                                && (matchInfo.homeCornerKicks ?: 0 > 0 || matchInfo.awayCornerKicks ?: 0 > 0) -> View.VISIBLE
//                        else -> View.GONE
//                    }
//                    text = (matchInfo.homeCornerKicks
//                        ?: 0).toString() + "-" + (matchInfo.awayCornerKicks ?: 0)
//                }
//            }
//        }
//
//        private fun setScoreTextAtFront(matchInfo: MatchInfo) {
//            itemView.apply {
//                league_odd_match_score_home.apply {
//                    visibility = when (TimeUtil.isTimeInPlay(matchInfo.startTime)) {
//                        true -> View.VISIBLE
//                        else -> View.GONE
//                    }
//                    text = when (matchInfo.gameType) {
//                        GameType.VB.key, GameType.TT.key, GameType.BM.key -> (matchInfo.homeTotalScore
//                            ?: 0).toString()
//                        else -> (matchInfo.homeScore ?: 0).toString()
//                    }
//                }
//
//                league_odd_match_score_away.apply {
//                    visibility = when (TimeUtil.isTimeInPlay(matchInfo.startTime)) {
//                        true -> View.VISIBLE
//                        else -> View.GONE
//                    }
//                    text = when (matchInfo.gameType) {
//                        GameType.VB.key, GameType.TT.key, GameType.BM.key -> (matchInfo.awayTotalScore
//                            ?: 0).toString()
//                        else -> (matchInfo.awayScore ?: 0).toString()
//                    }
//                }
//            }
//        }
//
//        private val isScoreTextVisible = { matchInfo: MatchInfo ->
//            when (TimeUtil.isTimeInPlay(matchInfo.startTime)) {
//                true -> View.VISIBLE
//                else -> View.GONE
//            }
//        }
//
//        /**
//         * 网球和羽毛球  排球，乒乓球 显示局比分
//         */
//        private fun setAllScoreTextAtBottom(matchInfo: MatchInfo) {
//            matchInfo.matchStatusList?.let { matchStatusList ->
//                var spanny = Spanny()
//                matchStatusList.forEachIndexed { index, it ->
//                    val spanScore = "${it.homeScore ?: 0}-${it.awayScore ?: 0}"
//                    if (index < matchStatusList.lastIndex) {
//                        spanny.append(spanScore)
//                        spanny.append("  ")
//                    } else {
//                        spanny.append(spanScore,
//                            ForegroundColorSpan(itemView.context.getColor(R.color.color_F0A536)))
//                    }
//                }
//                itemView.tv_peroids_score.isVisible = true
//                itemView.tv_peroids_score.text = spanny
//            }
//        }
//
//        /**
//         * 设置网球的中间分数布局
//         */
//        private fun setTennisRoundScore(matchInfo: MatchInfo) {
//            //隐藏其他球类的比分
//            itemView.apply {
//                league_odd_match_score_home.visibility = View.GONE
//                league_odd_match_score_away.visibility = View.GONE
//                ic_attack_h.visibility = View.GONE
//                ic_attack_c.visibility = View.GONE
//                lin_home_round_score.visibility = View.VISIBLE
//                lin_away_round_score.visibility = View.VISIBLE
//                //设置盘比，局比，分数
//                league_odd_match_total_score_home_bottom.apply {
//                    visibility = isScoreTextVisible(matchInfo)
//                    text = (matchInfo.homeTotalScore ?: 0).toString()
//                }
//
//                league_odd_match_total_score_away_bottom.apply {
//                    visibility = isScoreTextVisible(matchInfo)
//                    text = (matchInfo.awayTotalScore ?: 0).toString()
//                }
//                league_odd_match_score_home_bottom.apply {
//                    visibility = isScoreTextVisible(matchInfo)
//                    text = (matchInfo.homeScore ?: 0).toString()
//                }
//
//                league_odd_match_score_away_bottom.apply {
//                    visibility = isScoreTextVisible(matchInfo)
//                    text = (matchInfo.awayScore ?: 0).toString()
//                }
//                league_odd_match_point_home_bottom.apply {
//                    visibility = isScoreTextVisible(matchInfo)
//                    text = (matchInfo.homePoints ?: 0).toString()
//                }
//
//                league_odd_match_point_away_bottom.apply {
//                    visibility = isScoreTextVisible(matchInfo)
//                    text = (matchInfo.awayPoints ?: 0).toString()
//                }
//            }
//
//
//        }
//
//        private fun setupMatchTimeAndStatus(
//            matchInfo: MatchInfo,
//            matchType: MatchType,
//            isTimerEnable: Boolean,
//            isTimerPause: Boolean,
//            leagueOddListener: LeagueOddListener?,
//        ) {
//
//            /* TODO 依目前開發方式優化，將狀態和時間保存回 viewModel 於下次刷新頁面前 api 取得資料時先行代入相關 data 內，
//                此處倒數計時前須先設置時間及狀態，可解決控件短暫空白。(賽事狀態已於 BaseFavoriteViewModel #1 處調整過)*/
//
//            when {
//                TimeUtil.isTimeInPlay(matchInfo.startTime) -> {
//                    if (matchInfo.gameType == GameType.TN.key) {
//                        itemView.league_odd_match_time.visibility = View.GONE
//                        return
//                    }
//                    val socketValue = matchInfo.socketMatchStatus
//
//                    if (needCountStatus(socketValue)) {
//                        itemView.league_odd_match_time.visibility = View.VISIBLE
//                        listener = object : TimerListener {
//                            override fun onTimerUpdate(timeMillis: Long) {
//                                if (timeMillis > 1000) {
//                                    itemView.league_odd_match_time.text =
//                                        TimeUtil.longToMmSs(timeMillis)
//                                } else {
//                                    itemView.league_odd_match_time.text =
//                                        itemView.context.getString(R.string.time_up)
//                                }
//                                matchInfo.leagueTime = (timeMillis / 1000).toInt()
//                            }
//                        }
//
//                        updateTimer(
//                            isTimerEnable,
//                            isTimerPause,
//                            matchInfo.leagueTime ?: 0,
//                            (matchInfo.gameType == GameType.BK.key ||
//                                    matchInfo.gameType == GameType.RB.key ||
//                                    matchInfo.gameType == GameType.AFT.key)
//                        )
//
//                    } else {
//                        itemView.league_odd_match_time.visibility = View.GONE
//                    }
////                    itemView.league_odd_match_remain_time_icon.visibility = View.GONE
//                }
//
//                TimeUtil.isTimeAtStart(matchInfo.startTime) -> {
//                    listener = object : TimerListener {
//                        override fun onTimerUpdate(timeMillis: Long) {
//                            if (timeMillis > 1000) {
//                                val min = TimeUtil.longToMinute(timeMillis)
//                                itemView.league_odd_match_time.text = String.format(
//                                    itemView.context.resources.getString(R.string.at_start_remain_minute),
//                                    min
//                                )
//                            } else {
//                                //等待Socket更新
//                                itemView.league_odd_match_time.text = String.format(
//                                    itemView.context.resources.getString(R.string.at_start_remain_minute),
//                                    0
//                                )
//                            }
//                            matchInfo.remainTime = timeMillis
////                            itemView.league_odd_match_remain_time_icon.visibility = View.VISIBLE
//                        }
//                    }
//
//                    matchInfo.remainTime?.let { remainTime ->
//                        updateTimer(
//                            true,
//                            isTimerPause,
//                            (remainTime / 1000).toInt(),
//                            true
//                        )
//                    }
//                }
//                else -> {
//                    itemView.league_odd_match_time.visibility = View.VISIBLE
//                    itemView.league_odd_match_time.text =
//                        TimeUtil.timeFormat(matchInfo?.startTime,
//                            if (TimeUtil.isTimeToday(matchInfo?.startTime)) TimeUtil.HM_FORMAT else TimeUtil.DM_HM_FORMAT)
//                }
//            }
//
//            setStatusText(matchInfo)
//            setTextViewStatus(matchInfo)
//        }
//
//        private fun setStatusText(matchInfo: MatchInfo) {
//            itemView.league_odd_match_status.text = when {
//                (TimeUtil.isTimeInPlay(matchInfo.startTime)
//                        && matchInfo.status == GameStatus.POSTPONED.code
//                        && (matchInfo.gameType == GameType.FT.name || matchInfo.gameType == GameType.BK.name || matchInfo.gameType == GameType.TN.name)) -> {
//                    itemView.context.getString(R.string.game_postponed)
//                }
//                TimeUtil.isTimeInPlay(matchInfo.startTime) -> {
//                    if (matchInfo.statusName18n != null) {
//                        //网球，排球，乒乓，羽毛球，就不显示
//                        if (matchInfo.gameType == GameType.TN.name
//                            || matchInfo.gameType == GameType.VB.name
//                            || matchInfo.gameType == GameType.TT.name
//                            || matchInfo.gameType == GameType.BM.name
//                        ) {
//                            ""
//                        } else {
//                            matchInfo.statusName18n
//                        }
//
//                    } else {
//                        ""
//                    }
//                }
//                TimeUtil.isTimeToday(matchInfo.startTime) -> {
//                    itemView.context.getString((R.string.home_tab_today))
//                }
//                else -> {
//                    itemView.league_odd_match_status.isVisible = false
//                    ""
//                }
//            }
//        }
//
//        private fun setTextViewStatus(matchInfo: MatchInfo) {
//            when {
//                (TimeUtil.isTimeInPlay(matchInfo.startTime) && matchInfo.status == GameStatus.POSTPONED.code && (matchInfo.gameType == GameType.FT.name || matchInfo.gameType == GameType.BK.name || matchInfo.gameType == GameType.TN.name)) -> {
//                    itemView.league_odd_match_time.visibility = View.GONE
//                }
//
//                TimeUtil.isTimeInPlay(matchInfo.startTime) -> {
//                    if (matchInfo.statusName18n != null) {
//                        itemView.league_odd_match_status.visibility = View.VISIBLE
//                    }
//                }
//                TimeUtil.isTimeAtStart(matchInfo.startTime) -> {
//                    itemView.league_odd_match_status.visibility = View.GONE
//                }
//
//            }
//        }
//
//        /**
//         * 棒球的特殊布局处理
//         */
//        private fun setBBStatus(matchInfo: MatchInfo) {
//            itemView.apply {
//                linear_layout.isVisible = false
//                content_baseball_status.isVisible = true
//                league_odd_match_bb_status.apply {
//                    text = matchInfo.statusName18n
//                    isVisible = true
//                }
//
//                txvOut.apply {
//                    text = this.context.getString(R.string.game_out,
//                        matchInfo.outNumber ?: "")
//                    isVisible = true
//                }
//
//                league_odd_match_halfStatus.apply {
//                    setImageResource(if (matchInfo.halfStatus == 0) R.drawable.ic_bb_first_half else R.drawable.ic_bb_second_half)
//                    isVisible = true
//                }
//
//                league_odd_match_basebag.apply {
//                    setImageResource(
//                        when {
//                            matchInfo.firstBaseBag == 0 && matchInfo.secBaseBag == 0 && matchInfo.thirdBaseBag == 0 -> R.drawable.ic_bb_base_bag_0_0_0
//                            matchInfo.firstBaseBag == 1 && matchInfo.secBaseBag == 0 && matchInfo.thirdBaseBag == 0 -> R.drawable.ic_bb_base_bag_1_0_0
//                            matchInfo.firstBaseBag == 0 && matchInfo.secBaseBag == 1 && matchInfo.thirdBaseBag == 0 -> R.drawable.ic_bb_base_bag_0_1_0
//                            matchInfo.firstBaseBag == 0 && matchInfo.secBaseBag == 0 && matchInfo.thirdBaseBag == 1 -> R.drawable.ic_bb_base_bag_0_0_1
//                            matchInfo.firstBaseBag == 1 && matchInfo.secBaseBag == 1 && matchInfo.thirdBaseBag == 0 -> R.drawable.ic_bb_base_bag_1_1_0
//                            matchInfo.firstBaseBag == 1 && matchInfo.secBaseBag == 0 && matchInfo.thirdBaseBag == 1 -> R.drawable.ic_bb_base_bag_1_0_1
//                            matchInfo.firstBaseBag == 0 && matchInfo.secBaseBag == 1 && matchInfo.thirdBaseBag == 1 -> R.drawable.ic_bb_base_bag_0_1_1
//                            matchInfo.firstBaseBag == 1 && matchInfo.secBaseBag == 1 && matchInfo.thirdBaseBag == 1 -> R.drawable.ic_bb_base_bag_1_1_1
//                            else -> R.drawable.ic_bb_base_bag_0_0_0
//                        }
//                    )
//                    isVisible = true
//                }
//            }
//
//        }
//
//        val linearLayoutManager by lazy {
//            CustomLinearLayoutManager(
//                itemView.context,
//                LinearLayoutManager.HORIZONTAL,
//                false
//            )
//        }
//
//        private fun setupOddsButton(
//            matchType: MatchType,
//            item: MatchOdd,
//            oddsType: OddsType,
//            leagueOddListener: LeagueOddListener?,
//        ) {
//            itemView.rv_league_odd_btn_pager_main.apply {
//                linearLayoutManager.isAutoMeasureEnabled = false
//                layoutManager = linearLayoutManager
//                setHasFixedSize(true)
//                (rv_league_odd_btn_pager_main.itemAnimator as SimpleItemAnimator).supportsChangeAnimations =
//                    false
//
//                this.adapter = oddButtonPagerAdapter.apply {
//                    stateRestorationPolicy = StateRestorationPolicy.PREVENT
//                    //this.odds = item.oddsMap ?: mutableMapOf()
//                    //this.oddsType = oddsType
//                    this.matchType = matchType
//                    this.listener =
//                        OddButtonListener { matchInfo, odd, playCateCode, playCateName, betPlayCateName ->
//                            leagueOddListener?.onClickBet(
//                                matchInfo,
//                                odd,
//                                playCateCode,
//                                betPlayCateName,
//                                item.betPlayCateNameMap
//                            )
//                        }
//                }
//                updateOddsButton(item, oddsType)
//
//                OverScrollDecoratorHelper.setUpOverScroll(this,
//                    OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL)
//                itemView.hIndicator.bindRecyclerView(this)
//            }
//
//        }
//
//        private fun updateOddsButton(
//            item: MatchOdd,
//            oddsType: OddsType,
//        ) {
//            itemView.rv_league_odd_btn_pager_main.apply {
//                oddButtonPagerAdapter.setData(
//                    item.matchInfo,
//                    item.oddsSort,
//                    item.playCateNameMap,
//                    item.betPlayCateNameMap,
//                    null,
//                    item
//                )
//                oddButtonPagerAdapter.apply {
//                    stateRestorationPolicy = StateRestorationPolicy.PREVENT
//                    this.oddsType = oddsType
//                    this.odds = item.oddsMap ?: mutableMapOf()
////                    update()
//                    //notifyDataSetChanged() // TODO
//                }
//            }
//        }
//
//        private fun updateOddsButtonByBetInfo(item: MatchOdd) {
//            oddButtonPagerAdapter.odds = item.oddsMap ?: mutableMapOf()
//        }
//
//
//
//
//        override val oddStateChangeListener: OddStateChangeListener
//            get() = refreshListener
//    }
//
//    abstract class ViewHolderTimer(itemView: View) : OddStateViewHolder(itemView) {
//        interface TimerListener {
//            fun onTimerUpdate(timeMillis: Long)
//        }
//
//        protected var listener: TimerListener? = null
//
//        private var timer: Timer? = null
//
//        fun updateTimer(
//            isTimerEnable: Boolean,
//            isTimerPause: Boolean,
//            startTime: Int,
//            isDecrease: Boolean,
//        ) {
//            when (isTimerEnable) {
//                false -> {
//                    stopTimer()
//                }
//
//                true -> {
//                    startTimer(isTimerPause, startTime, isDecrease)
//                }
//
//            }
//        }
//
//        private fun startTimer(isTimerPause: Boolean, startTime: Int, isDecrease: Boolean) {
//            var timeMillis = startTime * 1000L
//            stopTimer()
//            Handler(Looper.getMainLooper()).post {
//                listener?.onTimerUpdate(timeMillis)
//            }
//
//            timer = Timer()
//            timer?.schedule(object : TimerTask() {
//                override fun run() {
//                    when (isDecrease) {
//                        true -> {
//                            if (!isTimerPause) timeMillis -= 1000
//                        }
//                        false -> {
//                            if (!isTimerPause) timeMillis += 1000
//                        }
//                    }
//
//                    if (timeMillis > 0) {
//                        Handler(Looper.getMainLooper()).post {
//                            listener?.onTimerUpdate(timeMillis)
//                        }
//                    }
//                }
//            }, 1000L, 1000L)
//        }
//
//        fun stopTimer() {
//            timer?.cancel()
//            timer = null
//        }
//    }
//
//    private fun forTest(msg: String) {
//        Log.e("For Test", "======>>> $msg")
//    }
//
//}