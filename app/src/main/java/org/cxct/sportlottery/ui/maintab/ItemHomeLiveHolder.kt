package org.cxct.sportlottery.ui.maintab

import android.view.View
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ItemHomeLiveBinding
import org.cxct.sportlottery.network.common.GameStatus
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.odds.list.TimeCounting
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
import org.cxct.sportlottery.ui.game.widget.OddsButtonHome
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.*

//TODO 棒球比分狀態顯示
class ItemHomeLiveHolder(
    val binding: ItemHomeLiveBinding,
    private val homeRecommendListener: HomeRecommendListener,
) : ViewHolderUtils.TimerViewHolderTimer(binding.root) {
    override val oddStateChangeListener: OddStateChangeListener
        get() = object : OddStateChangeListener {
            override fun refreshOddButton(odd: Odd) {}
        }

    private val mRequestOptions = RequestOptions()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .dontTransform()

    fun bind(data: Recommend, oddsType: OddsType) {
        //設置賽事資訊是否顯示
        update(data, oddsType)
    }

    fun update(data: Recommend, oddsType: OddsType) {
        //設置賽事資訊是否顯示
        setupGameInfoVisibility(data)

        //設置背景、隊伍名稱、點擊事件
        setupGameInfo(data)

        //玩法Code
        var oddPlayCateCode = ""

        var oddList = listOf<Odd?>()

        val oddsMap = mutableMapOf<String, List<Odd?>?>()
        data.oddsMap?.forEach {
            oddsMap[it.key] = it.value
        }
        val sortOddsMap = oddsMap.filterValues { it?.size ?: 0 > 0 }.sortOdds(data.oddsSort)
            .filterPlayCateSpanned(data.gameType)
        if (sortOddsMap.isNotEmpty()) {
            sortOddsMap.iterator().next().key.let {
                oddPlayCateCode = it
            }
            sortOddsMap.iterator().next().value?.let { it ->
                oddList = it
            }
        } else
            return
        //玩法名稱
        val playCateName = data.playCateNameMap?.get(oddPlayCateCode)
            ?.get(LanguageManager.getSelectLanguage(binding.root.context).key) ?: ""
        binding.tvGamePlayCateCodeName.text = playCateName
        with(binding) {
            //配置賽事比分及機制
            data.matchType?.let { matchType ->
                setupMatchScore(data, matchType)
            }
            //region 第1個按鈕
            if (oddList.isNotEmpty()) {
                val odd1 = oddList[0]
                with(oddBtn1) {
                    visibility = View.VISIBLE
                    setupOddsButton(this, odd1)
                    setupOdd4hall(oddPlayCateCode, odd1, oddList, oddsType)
                    setButtonBetClick(
                        data = data,
                        odd = odd1,
                        playCateCode = oddPlayCateCode,
                        playCateName = playCateName,
                        homeRecommendListener = homeRecommendListener
                    )
                }
            } else {
                oddBtn1.visibility = View.GONE
            }
            //endregion

            //region 第2個按鈕
            if (oddList.size > 1) {
                val odd2 = oddList[1]
                with(oddBtn2) {
                    visibility = View.VISIBLE
                    setupOddsButton(this, odd2)
                    setupOdd4hall(oddPlayCateCode, odd2, oddList, oddsType)
                    if (oddList.size > 2) setupOdd4hall(
                        oddPlayCateCode,
                        odd2,
                        oddList,
                        oddsType,
                        true
                    )
                    setButtonBetClick(
                        data = data,
                        odd = odd2,
                        playCateCode = oddPlayCateCode,
                        playCateName = playCateName,
                        homeRecommendListener = homeRecommendListener
                    )
                }
            } else {
                oddBtn2.visibility = View.GONE
            }
            //endregion

            //region 比賽狀態(狀態、時間)
            val gameType = data.gameType
            val matchType = data.matchType
            setupMatchTimeAndStatus(
                item = data,
                isTimerEnable = (gameType == GameType.FT.key || gameType == GameType.BK.key || gameType == GameType.RB.key || gameType == GameType.AFT.key || matchType == MatchType.PARLAY || matchType == MatchType.AT_START || matchType == MatchType.MY_EVENT),
                isTimerPause = data.matchInfo?.stopped == TimeCounting.STOP.value
            )
            //endregion
        }
    }

    /**
     * 設置背景、隊伍名稱、點擊事件、玩法數量、聯賽名稱
     */
    private fun setupGameInfo(data: Recommend) {
        with(binding) {
            //聯賽名稱
            tvLeagueName.text = data.leagueName
            //region 隊伍名稱
            tvHomeName.text = data.homeName
            tvAwayName.text = data.awayName
            //endregion

            //region 隊伍圖示
            ivHomeIcon.setTeamLogo(data.matchInfo?.homeIcon)
            ivAwayIcon.setTeamLogo(data.matchInfo?.awayIcon)
            //endregion


            //region 點擊進入賽事詳情
            val matchOddList = transferMatchOddList(data)
            val matchInfoList = matchOddList.mapNotNull {
                it.matchInfo
            }
            root.setOnClickListener {
                homeRecommendListener.onClickPlayTypeListener(
                    gameType = data.gameType,
                    matchType = data.matchType,
                    matchId = data.matchInfo?.id,
                    matchInfoList = matchInfoList
                )
            }
            //endregion
        }
    }

    /**
     * 配置投注按鈕Callback
     */
    private fun OddsButtonHome.setButtonBetClick(
        data: Recommend,
        odd: Odd?,
        playCateCode: String,
        playCateName: String,
        homeRecommendListener: HomeRecommendListener,
    ) {
        setOnClickListener {
            data.matchType?.let { matchType ->
                odd?.let { odd ->
                    homeRecommendListener.onClickBetListener(
                        gameType = data.gameType,
                        matchType = matchType,
                        matchInfo = data.matchInfo,
                        odd = odd,
                        playCateCode = playCateCode,
                        playCateName = playCateName,
                        betPlayCateNameMap = data.betPlayCateNameMap,
                        playCateMenuCode = data.menuList.firstOrNull()?.code
                    )
                }
            }
        }
    }

    //region 賽事比分Method
    private val isScoreTextVisible = { item: Recommend ->
        when (TimeUtil.isTimeInPlay(item.matchInfo?.startTime)) {
            true -> View.VISIBLE
            else -> View.GONE
        }
    }

    /**
     * 設置賽事顯示VS或比分
     */
    private fun setupGameInfoVisibility(item: Recommend) {
        if (TimeUtil.isTimeInPlay(item.matchInfo?.startTime)) {
            with(binding) {
                blockScore.visibility = View.VISIBLE
                tvVs.visibility = View.GONE
            }
        } else {
            with(binding) {
                blockScore.visibility = View.GONE
                tvVs.visibility = View.VISIBLE
            }
        }
    }

    /**
     * 配置比分及比賽制度
     */
    private fun setupMatchScore(item: Recommend, matchType: MatchType) {
        //TODO review 棒球賽事狀態版型
        /*itemView.apply {
            when {
                matchType != MatchType.IN_PLAY -> {
                    linear_layout.isVisible = true
                    content_baseball_status.isVisible = false
                }
                else -> {
                    when (item.matchInfo?.gameType) {
                        GameType.BB.key -> {
                            linear_layout.isVisible = false
                            content_baseball_status.isVisible = true
                        }
                        else -> {
                            linear_layout.isVisible = true
                            content_baseball_status.isVisible = false
                        }
                    }

                }
            }
        }*/
        when (item.matchInfo?.gameType) {
            GameType.VB.key -> setVbScoreText(item)
            GameType.TN.key -> setTnScoreText(item)
            GameType.FT.key -> setFtScoreText(item)
            GameType.BK.key -> setBkScoreText(item)
            GameType.TT.key -> setVbScoreText(item)
            GameType.BM.key -> setBmScoreText(item)
            GameType.BB.key -> setBbScoreText(item)
            else -> setBkScoreText(item)
        }
    }

    /**
     * 設置排球類型比分及比賽制度
     */
    private fun setVbScoreText(item: Recommend) {
        binding.apply {
            setAllScoreTextAtBottom(item)
        }
    }

    /**
     * 設置網球類型比分及比賽制度
     */
    private fun setTnScoreText(item: Recommend) {
        binding.apply {
            setAllScoreTextAtBottom(item)

        }
    }

    /**
     * 設置足球類型比分及比賽制度
     */
    private fun setFtScoreText(item: Recommend) {
        binding.setScoreText(item)
    }

    /**
     * 設置籃球類型比分及比賽制度
     */
    private fun setBkScoreText(item: Recommend) {
        binding.setScoreText(item)
    }

    /**
     * 設置羽球類型比分及比賽制度
     */
    private fun setBmScoreText(item: Recommend) {
        binding.apply {
            setAllScoreTextAtBottom(item)
        }
    }

    /**
     * 設置羽球類型比分及比賽制度
     */
    private fun setBbScoreText(item: Recommend) {
        with(binding) {
            setScoreText(item)
        }
    }

    /**
     * 設置盤類型比分
     */
    private fun ItemHomeLiveBinding.setAllScoreTextAtBottom(item: Recommend) {
        val itemVisibility = isScoreTextVisible(item)
        with(tvHomeScore) {
            visibility = itemVisibility
            text = (item.matchInfo?.homeTotalScore ?: 0).toString()
        }

        with(tvAwayScore) {
            visibility = itemVisibility
            text = (item.matchInfo?.awayTotalScore ?: 0).toString()
        }
    }

    /**
     * 設置局類型比分
     */
    private fun ItemHomeLiveBinding.setScoreText(item: Recommend) {
        val itemVisibility = isScoreTextVisible(item)
        with(tvHomeScore) {
            visibility = itemVisibility
            text = (item.matchInfo?.homeScore ?: 0).toString()
        }
        with(tvAwayScore) {
            visibility = itemVisibility
            text = (item.matchInfo?.awayScore ?: 0).toString()
        }
    }


    //region 賽事時間狀態Method
    private fun setupMatchTimeAndStatus(
        item: Recommend,
        isTimerEnable: Boolean,
        isTimerPause: Boolean,
    ) {
        setupMatchTime(item, isTimerEnable, isTimerPause)
        setupGameStatusBlockVisibility(item)
        setStatusText(item)
        setTextViewStatus(item)
    }

    /**
     * 賽事時間
     */
    private fun setupMatchTime(
        item: Recommend,
        isTimerEnable: Boolean,
        isTimerPause: Boolean,
    ) {
        when {
            TimeUtil.isTimeInPlay(item.matchInfo?.startTime) -> {
                val socketValue = item.matchInfo?.socketMatchStatus

                if (needCountStatus(socketValue)) {
                    binding.tvGamePlayTime.text = item.runningTime
                    binding.tvGamePlayTime.visibility = View.VISIBLE
                    listener = object : TimerListener {
                        override fun onTimerUpdate(timeMillis: Long) {
                            if (timeMillis > 1000) {
                                binding.tvGamePlayTime.text =
                                    TimeUtil.longToMmSs(timeMillis)
                            } else {
                                binding.tvGamePlayTime.text =
                                    binding.root.context.getString(R.string.time_up)
                            }
                            item.matchInfo?.leagueTime = (timeMillis / 1000).toInt()
                            //TODO 記錄時間?
                            item.runningTime = binding.tvGamePlayTime.text.toString()
//                            getRecommendData()[0].runningTime = binding.tvGamePlayTime.text.toString()
                        }
                    }

                    updateTimer(
                        isTimerEnable,
                        isTimerPause,
                        item.matchInfo?.leagueTime ?: 0,
                        (item.matchInfo?.gameType == GameType.BK.key ||
                                item.matchInfo?.gameType == GameType.RB.key ||
                                item.matchInfo?.gameType == GameType.AFT.key)
                    )

                } else {
                    binding.tvGamePlayTime.visibility = View.GONE
                }
            }

            TimeUtil.isTimeAtStart(item.matchInfo?.startTime) -> {
                binding.tvGamePlayTime.text = item.runningTime
                listener = object : TimerListener {
                    override fun onTimerUpdate(timeMillis: Long) {
                        if (timeMillis > 1000) {
                            val min = TimeUtil.longToMinute(timeMillis)
                            binding.tvGamePlayTime.text = String.format(
                                itemView.context.resources.getString(R.string.at_start_remain_minute),
                                min
                            )
                        } else {
                            //等待Socket更新
                            binding.tvGamePlayTime.text = String.format(
                                itemView.context.resources.getString(R.string.at_start_remain_minute),
                                0
                            )
                        }
                        item.matchInfo?.remainTime = timeMillis
//                        TODO 記錄時間?
                        item.runningTime = binding.tvGamePlayTime.text.toString()
//                        getRecommendData()[0].runningTime = binding.tvGamePlayTime.text.toString()
                    }
                }

                item.matchInfo?.remainTime?.let { remainTime ->
                    updateTimer(
                        true,
                        isTimerPause,
                        (remainTime / 1000).toInt(),
                        true
                    )
                }
            }
            else -> {
                binding.tvGamePlayTime.text =
                    TimeUtil.timeFormat(item.matchInfo?.startTime, "HH:mm")
            }
        }
    }

    private fun setStatusText(item: Recommend) {
        binding.tvGameStatus.text = when {
            (TimeUtil.isTimeInPlay(item.matchInfo?.startTime)
                    && item.matchInfo?.status == GameStatus.POSTPONED.code
                    && (item.matchInfo?.gameType == GameType.FT.name || item.matchInfo?.gameType == GameType.BK.name || item.matchInfo?.gameType == GameType.TN.name)) -> {
                itemView.context.getString(R.string.game_postponed)
            }
            TimeUtil.isTimeInPlay(item.matchInfo?.startTime) -> {
                if (item.matchInfo?.statusName18n != null) {
                    item.matchInfo?.statusName18n
                } else {
                    ""
                }
            }
            else -> {
                if (TimeUtil.isTimeToday(item.matchInfo?.startTime))
                    itemView.context.getString((R.string.home_tab_today))
                else
                    item.matchInfo?.startDateDisplay
            }
        }
    }

    private fun setTextViewStatus(item: Recommend) {
        when {
            (TimeUtil.isTimeInPlay(item.matchInfo?.startTime) && item.matchInfo?.status == GameStatus.POSTPONED.code && (item.matchInfo?.gameType == GameType.FT.name || item.matchInfo?.gameType == GameType.BK.name || item.matchInfo?.gameType == GameType.TN.name)) -> {
                binding.tvGamePlayTime.visibility = View.GONE
            }

            TimeUtil.isTimeInPlay(item.matchInfo?.startTime) -> {
                if (item.matchInfo?.statusName18n != null) {
                    binding.tvGameStatus.visibility = View.VISIBLE
                }
            }
            TimeUtil.isTimeAtStart(item.matchInfo?.startTime) -> {
                binding.tvGameStatus.visibility = View.GONE
            }
        }
    }

    /**
     * 設置當前球種使用的賽事狀態區塊
     * 棒球獨立使用不同區塊
     */
    private fun setupGameStatusBlockVisibility(item: Recommend) {
        with(binding) {
            when {
                item.matchInfo?.gameType == GameType.BB.key && TimeUtil.isTimeInPlay(item.matchInfo?.startTime) -> {
                    blockNormalGame.visibility = View.GONE
                }
                else -> {
                    blockNormalGame.visibility = View.VISIBLE
                }
            }
        }
    }
    //endregion

    private fun setupOddsButton(oddsButton: OddsButtonHome, odd: Odd?) {

        oddsButton.apply {
            setupOddState(oddsButton, odd)
            odd?.let {
                this.isSelected = it.isSelected ?: false
            }
        }
    }

    private fun transferMatchOddList(recommend: Recommend): MutableList<MatchOdd> {
        with(recommend) {
            return mutableListOf(
                MatchOdd(
                    matchInfo = matchInfo,
                    oddsMap = oddsMap,
                    playCateNameMap = playCateNameMap,
                    betPlayCateNameMap = betPlayCateNameMap,
                    oddsSort = oddsSort
                )
            )
        }
    }

    private fun Map<String, List<Odd?>?>.sortOdds(oddsSort: String?): Map<String, List<Odd?>?> {
        val oddsMap: MutableMap<String, List<Odd?>?>
        val sortOrder = oddsSort?.split(",")
        val filterOdds = this.filter { sortOrder?.contains(it.key.split(":")[0]) == true }
        oddsMap = filterOdds.toSortedMap(compareBy<String> {
            val oddsIndex = sortOrder?.indexOf(it.split(":")[0])
            oddsIndex
        }.thenBy { it })
        return if (oddsSort.isNullOrEmpty()) this else oddsMap
    }

    private fun Map<String, List<Odd?>?>.filterPlayCateSpanned(gameType: String): Map<String, List<Odd?>?> {
        return this.mapValues { map ->
            val playCateNum =
                when { //根據IOS給的規則判斷顯示數量
                    map.value?.size ?: 0 < 3 -> 2

                    (gameType == GameType.TT.key || gameType == GameType.BM.key) && map.key.contains(
                        PlayCate.SINGLE.value
                    ) -> 2 //乒乓球獨贏特殊判斷 羽球獨贏特殊判斷

                    map.key.contains(PlayCate.HDP.value) || (map.key.contains(PlayCate.OU.value) && !map.key.contains(
                        PlayCate.SINGLE_OU.value
                    )) || map.key.contains(
                        PlayCate.CORNER_OU.value
                    ) -> 2

                    map.key.contains(PlayCate.SINGLE.value) || map.key.contains(PlayCate.NGOAL.value) || map.key.contains(
                        PlayCate.NGOAL_OT.value
                    ) -> 3

                    else -> 3
                }
            map.value?.filterIndexed { index, _ ->
                index < playCateNum
            }
        }
    }
}