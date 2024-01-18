
package org.cxct.sportlottery.ui.maintab.games.adapter

import android.view.View
import androidx.lifecycle.LifecycleOwner
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.extentions.clickDelay
import org.cxct.sportlottery.common.extentions.setSptText
import org.cxct.sportlottery.databinding.ItemHotGameViewBinding
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.odds.list.TimeCounting
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
import org.cxct.sportlottery.ui.maintab.home.HomeRecommendListener
import org.cxct.sportlottery.ui.sport.oddsbtn.OddsHotButtonHome
import org.cxct.sportlottery.util.*

class ItemHotMatchHolder(
    lifecycleOwner: LifecycleOwner,
    val binding: ItemHotGameViewBinding,
    private val homeRecommendListener: HomeRecommendListener,
) : ViewHolderUtils.TimerViewHolderTimer(lifecycleOwner, binding.root) {
    lateinit var data: Recommend

    override val oddStateChangeListener: OddStateChangeListener
        get() = object : OddStateChangeListener {
            override fun refreshOddButton(odd: Odd) {}
        }


    fun bind(data: Recommend, oddsType: OddsType) {
        //設置賽事資訊是否顯示
        this.data = data
        update(oddsType)
        setupMatchScore(data)
        val matchId = data.id

    }

    fun update(oddsType: OddsType) {

        //設置背景、隊伍名稱、點擊事件
        setupGameInfo()
        //根据sofia 说法，只获取有效玩法中的第一个玩法，而不是直接获取独赢玩法，因为赛事列表有可能不存在独赢玩法
        //玩法Code
        var oddPlayCateCode = ""

        data.oddsSort?.let {
            oddPlayCateCode = it
        }

        var oddList = listOf<Odd?>()

        val oddsMap = mutableMapOf<String, List<Odd?>?>()
        data.oddsMap?.forEach {
            oddsMap[it.key] = it.value
        }
        val sortOddsMap = oddsMap.filterValues { it?.size ?: 0 > 0 }.sortOdds(data.oddsSort)
            .filterPlayCateSpanned(data.gameType!!)
        if (sortOddsMap.isNotEmpty()) {
            if (oddPlayCateCode.isEmpty()) {
                sortOddsMap.iterator().next().key.let {
                    oddPlayCateCode = it
                }
            }
            //是否包含选择的玩法
            if (sortOddsMap.keys.contains(oddPlayCateCode)) {
                oddList = sortOddsMap[oddPlayCateCode]!!
            } else {
                sortOddsMap.iterator().next().value?.let { it ->
                    oddList = it
                }
            }
        } else
            return
        //玩法名稱
        val playCateName = data.playCateNameMap?.get(oddPlayCateCode)
            ?.get(LanguageManager.getSelectLanguage(binding.root.context).key) ?: ""
//        binding.tvGamePlayCateCodeName.text = LocalUtils.getString(R.string.win_line)
        with(binding) {
            //配置賽事比分及機制
            //region 第1個按鈕
            if (oddList.isNotEmpty()) {
                setOdds(oddBtn1,
                    null,
                    null,
                    oddList[0],
                    oddPlayCateCode,
                    playCateName,
                    oddList,
                    oddsType)
            } else {
                oddBtn1.visibility = View.GONE
            }
            //endregion

            //region 第2個按鈕
            if (oddList.size > 1) {
                setOdds(oddBtn2,
                    oddBtn1,
                    null,
                    oddList[1],
                    oddPlayCateCode,
                    playCateName,
                    oddList,
                    oddsType)
            } else {
                oddBtn2.visibility = View.GONE
            }

            //region 第3個按鈕
            if (oddList.size > 2) {
                setOdds(oddBtn3,
                    oddBtn1,
                    oddBtn2,
                    oddList[2],
                    oddPlayCateCode,
                    playCateName,
                    oddList,
                    oddsType)
            } else {
                oddBtn3.visibility = View.GONE
            }
            //endregion

            //region 比賽狀態(狀態、時間)
            val gameType = data.matchInfo?.gameType
            setupMatchTimeAndStatus(
                item = data,
                isTimerEnable = true,
                isTimerPause = data.matchInfo?.stopped == TimeCounting.STOP
            )
            //endregion
        }
    }

    private fun setOdds(
        currentOddBtn: OddsHotButtonHome,
        otherBtn1: OddsHotButtonHome?,
        otherBtn2: OddsHotButtonHome?,
        odd: Odd?,
        oddPlayCateCode: String,
        playCateName: String,
        oddList: List<Odd?>,
        oddsType: OddsType,
    ) {

        currentOddBtn.visibility = View.VISIBLE
        if (otherBtn1?.isLocked() == true || otherBtn2?.isLocked() == true) {
            currentOddBtn.lockOdds()
            return
        }

        if (otherBtn1?.isDeactivated() == true || otherBtn2?.isDeactivated() == true) {
            currentOddBtn.deactivatedOdds()
            return
        }

        setupOddsButton(currentOddBtn, odd)
        val hideName =odd?.nameMap?.containsValue(data?.homeName)==true
           || odd?.nameMap?.containsValue(data?.awayName)==true
           || odd?.nameMap?.containsValue(binding.root.context.getString(R.string.draw)) ==true
        currentOddBtn.setupOdd4hall(oddPlayCateCode, odd, oddList, oddsType,hideName = hideName)
        currentOddBtn.setButtonBetClick(
            data = data,
            odd = odd,
            playCateCode = oddPlayCateCode,
            playCateName = playCateName,
            homeRecommendListener = homeRecommendListener
        )

        if (currentOddBtn.isLocked()) {
            otherBtn1?.lockOdds()
            otherBtn2?.lockOdds()
        }

        if (currentOddBtn.isDeactivated()) {
            otherBtn1?.deactivatedOdds()
            otherBtn2?.deactivatedOdds()
        }
    }

    /**
     * 設置背景、隊伍名稱、點擊事件、玩法數量、聯賽名稱
     */
    private fun setupGameInfo() {
        with(binding) {
            //region 隊伍名稱
            tvHomeName.text = data.matchInfo?.homeName
            tvAwayName.text = data.matchInfo?.awayName


            //region 隊伍圖示
            ivHomeIcon.setTeamLogo(data.matchInfo?.homeIcon)
            ivAwayIcon.setTeamLogo(data.matchInfo?.awayIcon)
            //endregion
            tvLeagueName.text = data.leagueName

            //region 點擊進入賽事詳情
            val matchOddList = transferMatchOddList(data)
            val matchInfoList = matchOddList.mapNotNull {
                it.matchInfo
            }
            View.OnClickListener {
                homeRecommendListener.onItemClickListener(data.matchInfo)
            }.let {
                root.setOnClickListener(it)
            }
        }
    }

    /**
     * 配置投注按鈕Callback
     */
    private fun OddsHotButtonHome.setButtonBetClick(
        data: Recommend,
        odd: Odd?,
        playCateCode: String,
        playCateName: String,
        homeRecommendListener: HomeRecommendListener,
    ) {
        clickDelay {
            odd?.let { odd ->
                homeRecommendListener.onClickBetListener(
                    gameType = data.gameType,
                    matchType = MatchType.IN_PLAY,
                    matchInfo = data.matchInfo,
                    odd = odd,
                    playCateCode = playCateCode,
                    playCateName = playCateName,
                    betPlayCateNameMap = data.betPlayCateNameMap,
                    playCateMenuCode = MenuCode.MAIN.code,
                )
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
     * 配置比分及比賽制度
     */
    private fun setupMatchScore(item: Recommend) {
        binding.setScoreText(item)
    }

    /**
     * 設置局類型比分
     */
    private fun ItemHotGameViewBinding.setScoreText(item: Recommend) {
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
            TimeUtil.isTimeInPlay(item.startTime) -> {
                val socketValue = item.matchInfo?.socketMatchStatus

                if (needCountStatus(socketValue, item.matchInfo?.leagueTime)) {
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
                    binding.tvGamePlayTime.text=""
                }
            }
            else -> {
                stopTimer()
                binding.tvGamePlayTime.text =
                    TimeUtil.timeFormat(item.startTime, TimeUtil.DM_HM_FORMAT)
            }
        }
    }

    private fun setStatusText(item: Recommend) {
        binding.tvGameStatus.text = when {
            (TimeUtil.isTimeInPlay(item.startTime)
                    && item.matchInfo?.status == GameStatus.POSTPONED.code
                    && (item.matchInfo?.gameType == GameType.FT.name || item.matchInfo?.gameType == GameType.BK.name || item.matchInfo?.gameType == GameType.TN.name)) -> {
                itemView.context.getString(R.string.game_postponed)
            }

            TimeUtil.isTimeInPlay(item.startTime) -> {
                if (item.matchInfo?.statusName18n != null) {
                    item.matchInfo?.statusName18n
                } else {
                    ""
                }
            }

            else -> {
                ""
            }
        }
    }

    private fun setTextViewStatus(item: Recommend) {
        when {
            (TimeUtil.isTimeInPlay(item.startTime)
                    && item.matchInfo?.status == GameStatus.POSTPONED.code
                    && (item.matchInfo?.gameType == GameType.FT.name || item.matchInfo?.gameType == GameType.BK.name || item.matchInfo?.gameType == GameType.TN.name)) -> {
                binding.tvGamePlayTime.text=""
            }

            TimeUtil.isTimeInPlay(item.startTime) -> {
                if (item.matchInfo?.statusName18n != null) {
                    binding.tvGameStatus.visibility = View.VISIBLE
                }
            }

            TimeUtil.isTimeAtStart(item.startTime) -> {
                binding.tvGameStatus.text=""
            }
        }
    }

    //endregion

    private fun setupOddsButton(oddsButton: OddsHotButtonHome, odd: Odd?) {

        oddsButton.apply {
            setupOddState(oddsButton, odd)
            odd?.let {
                this.isSelected = it.isSelected ?: false
            }
        }
    }

    private fun transferMatchOddList(item: Recommend): MutableList<MatchOdd> {
        with(item) {
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
                when {
                    //只有足球才有主客和的独赢盘才有三个赔率
                    gameType == GameType.FT.key && map.key.contains(PlayCate.SINGLE.value) -> 3
                    else -> 2
                }
            map.value?.filterIndexed { index, _ ->
                index < playCateNum
            }
        }
    }

}