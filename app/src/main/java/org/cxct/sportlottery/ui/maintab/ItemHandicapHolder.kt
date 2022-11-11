package org.cxct.sportlottery.ui.maintab

import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.button_odd_home.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ItemHomeHandicapBinding
import org.cxct.sportlottery.network.common.GameStatus
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.odds.list.TimeCounting
import org.cxct.sportlottery.network.third_game.third_games.hot.HotMatchInfo
import org.cxct.sportlottery.ui.game.widget.OddsButtonHome
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.*

//TODO 棒球比分狀態顯示
class ItemHandicapHolder(
    lifecycleOwner: LifecycleOwner,
    val binding: ItemHomeHandicapBinding,
    private val homeRecommendListener: HomeRecommendListener,
) : ViewHolderUtils.TimerViewHolderTimer(lifecycleOwner, binding.root) {
    override val oddStateChangeListener: OddStateChangeListener
        get() = object : OddStateChangeListener {
            override fun refreshOddButton(odd: Odd) {}
        }

    private val mRequestOptions = RequestOptions()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .dontTransform()

    fun bind(data: HotMatchInfo, oddsType: OddsType) {
        //設置賽事資訊是否顯示
        update(data, oddsType)
    }

    fun update(data: HotMatchInfo, oddsType: OddsType) {
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
        //  binding.tvGamePlayCateCodeName.text = playCateName
        with(binding) {
            //配置賽事比分及機制
            setupMatchScore(data)
//            org.cxct.sportlottery.util.LogUtil.d("oddList="+oddList.size)
            //region 第1個按鈕
            if (oddList.isNotEmpty()) {
                val odd1 = oddList[0]
                with(oddBtn1) {
                    visibility = View.VISIBLE
                    setupOddsButton(this, odd1)
                    setupOdd4hall(oddPlayCateCode, odd1, oddList, oddsType, hideName = true)
                    if (data?.oddsSort?.contains(PlayCate.SINGLE.value) == true) {
                        lin_name.isVisible = false
                    } else {
                        lin_name.isVisible = isVisible
                    }
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
                    setupOdd4hall(oddPlayCateCode, odd2, oddList, oddsType, hideName = true)
                    if (data?.oddsSort?.contains(PlayCate.SINGLE.value) == true) {
                        lin_name.isVisible = false
                    } else {
                        lin_name.isVisible = isVisible
                    }
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
            //region 第3个按钮
            if (oddList.size > 2) {
                val odd3 = oddList[2]
                with(oddBtn3) {
                    visibility = View.VISIBLE
                    setupOddsButton(this, odd3)
                    setupOdd4hall(oddPlayCateCode, odd3, oddList, oddsType, hideName = true)
                    if (data?.oddsSort?.contains(PlayCate.SINGLE.value) == true) {
                        lin_name.isVisible = false
                    } else {
                        lin_name.isVisible = isVisible
                    }
                    setButtonBetClick(
                        data = data,
                        odd = odd3,
                        playCateCode = oddPlayCateCode,
                        playCateName = playCateName,
                        homeRecommendListener = homeRecommendListener
                    )
                }
            } else {
                oddBtn3.visibility = View.GONE
            }
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
    private fun setupGameInfo(data: HotMatchInfo) {
        with(binding) {
            //聯賽名稱
           // tvLeagueName.text = data.leagueName
            //region 隊伍名稱
            tvHomeName.text = data.homeName
            tvAwayName.text = data.awayName
            //endregion

            //region 隊伍圖示
            ivHomeIcon.setTeamLogo(data.homeIcon)
            ivAwayIcon.setTeamLogo(data.awayIcon)
            //endregion

            //玩法數量
            tvPlayCateCount.text = data.playCateNum.toString()+"+ >"

            //region 點擊進入賽事詳情
            val matchOddList = transferMatchOddList(data)
            val matchInfoList = matchOddList.mapNotNull {
                it.matchInfo
            }

            root.setOnClickListener {

                homeRecommendListener.onItemClickListener(
                    matchInfo = data.getBuildMatchInfo()
                )
            }

            //endregion
        }
    }

    /**
     * 配置投注按鈕Callback
     */
    private fun OddsButtonHome.setButtonBetClick(
        data: HotMatchInfo,
        odd: Odd?,
        playCateCode: String,
        playCateName: String,
        homeRecommendListener: HomeRecommendListener,
    ) {
        setOnClickListener {
                odd?.let { odd ->
                    homeRecommendListener.onClickBetListener(
                        gameType = data.gameType,
                        matchType = if (TimeUtil.isTimeInPlay(data.startTime)) MatchType.IN_PLAY else MatchType.EARLY,
                        matchInfo = data.getBuildMatchInfo(),
                        odd = odd,
                        playCateCode = playCateCode,
                        playCateName = playCateName,
                        betPlayCateNameMap = data.betPlayCateNameMap,
                        playCateMenuCode = null
                    )
                }
        }
    }

    //region 賽事比分Method
    private val isScoreTextVisible = { item: HotMatchInfo ->
        when (TimeUtil.isTimeInPlay(item.matchInfo?.startTime)) {
            true -> View.VISIBLE
            else -> View.GONE
        }
    }

    /**
     * 設置賽事顯示VS或比分
     */
    private fun setupGameInfoVisibility(item: HotMatchInfo) {
        if (TimeUtil.isTimeInPlay(item.matchInfo?.startTime)) {
            with(binding) {
              //  blockScore.visibility = View.VISIBLE
             //   tvVs.visibility = View.GONE
            }
        } else {
            with(binding) {
               // blockScore.visibility = View.GONE
               // tvVs.visibility = View.VISIBLE
            }
        }
    }

    /**
     * 配置比分及比賽制度
     */
    private fun setupMatchScore(item: HotMatchInfo) {
        //TODO review 棒球賽事狀態版型
        when (item.gameType) {
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
    private fun setVbScoreText(item: HotMatchInfo) {
        binding.apply {
            setAllScoreTextAtBottom(item)

        }
    }

    /**
     * 設置網球類型比分及比賽制度
     */
    private fun setTnScoreText(item: HotMatchInfo) {
        binding.apply {
            setAllScoreTextAtBottom(item)


        }
    }

    /**
     * 設置足球類型比分及比賽制度
     */
    private fun setFtScoreText(item: HotMatchInfo) {
        binding.setScoreText(item)
    }

    /**
     * 設置籃球類型比分及比賽制度
     */
    private fun setBkScoreText(item: HotMatchInfo) {
        binding.setScoreText(item)
    }

    /**
     * 設置羽球類型比分及比賽制度
     */
    private fun setBmScoreText(item: HotMatchInfo) {
        binding.apply {
            setAllScoreTextAtBottom(item)

        }
    }

    /**
     * 設置羽球類型比分及比賽制度
     */
    private fun setBbScoreText(item: HotMatchInfo) {
        with(binding) {
            setScoreText(item)

        }
    }

    /**
     * 設置盤類型比分
     */
    private fun ItemHomeHandicapBinding.setAllScoreTextAtBottom(item: HotMatchInfo) {
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
    private fun ItemHomeHandicapBinding.setScoreText(item: HotMatchInfo) {
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




    //endregion

    //region 賽事時間狀態Method
    private fun setupMatchTimeAndStatus(
        item: HotMatchInfo,
        isTimerEnable: Boolean,
        isTimerPause: Boolean
    ) {
        setupMatchTime(item, isTimerEnable, isTimerPause)
        setStatusText(item)
        setTextViewStatus(item)
    }

    /**
     * 賽事時間
     */
    private fun setupMatchTime(
        item: HotMatchInfo,
        isTimerEnable: Boolean,
        isTimerPause: Boolean
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
                    stopTimer()
                    binding.tvGamePlayTime.visibility = View.GONE
                }
            }
            else -> {
                stopTimer()
                binding.tvGamePlayTime.text =
                    TimeUtil.timeFormat(item.matchInfo?.startTime, TimeUtil.DM_HM_FORMAT)

            }
        }
    }

    private fun setStatusText(item: HotMatchInfo) {
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
//                if (TimeUtil.isTimeToday(item.matchInfo?.startTime))
//                    itemView.context.getString((R.string.home_tab_today))
//                else
                    item.matchInfo?.startDateDisplay
            }
        }
    }

    private fun setTextViewStatus(item: HotMatchInfo) {
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



    private fun setupOddsButton(oddsButton: OddsButtonHome, odd: Odd?) {

        oddsButton.apply {
            setupOddState(oddsButton, odd)
            odd?.let {
                this.isSelected = it.isSelected ?: false
            }
        }
    }

    private fun transferMatchOddList(recommend: HotMatchInfo): MutableList<MatchOdd> {
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
                    gameType == GameType.FT.key
                            && map.key.contains(PlayCate.SINGLE.value)->3 //足球并且独赢玩法显示三个

                    else -> 2
                }
            map.value?.filterIndexed { index, _ ->
                index < playCateNum
            }
        }
    }
}