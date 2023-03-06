package org.cxct.sportlottery.ui.maintab.live

import android.os.Build
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.listener.GSYVideoProgressListener
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ItemHomeLiveBinding
import org.cxct.sportlottery.extentions.isEmptyStr
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.MatchLiveData
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.odds.list.TimeCounting
import org.cxct.sportlottery.ui.game.widget.OddsButtonHome
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.widget.OKVideoPlayer

class ItemHomeLiveHolder(
    lifecycleOwner: LifecycleOwner,
    val binding: ItemHomeLiveBinding,
    private val homeLiveListener: HomeLiveListener,
) : ViewHolderUtils.TimerViewHolderTimer(lifecycleOwner, binding.root),
    GSYVideoProgressListener {
    lateinit var data: MatchLiveData
    var lastExpandLive = false;

    override val oddStateChangeListener: OddStateChangeListener
        get() = object : OddStateChangeListener {
            override fun refreshOddButton(odd: Odd) {}
        }

    private val mRequestOptions = RequestOptions()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .dontTransform()

    fun bind(data: MatchLiveData, oddsType: OddsType) {
        //設置賽事資訊是否顯示
        this.data = data
        update(oddsType)
        val matchId = (bindingAdapter as HomeLiveAdapter).expandMatchId
        val isExpendLive =
            (!matchId.isNullOrEmpty()) && matchId == data.matchInfo.id && data.matchInfo.isLive == 1
        binding.tvExpandLive.isVisible = !isExpendLive && data.matchInfo.isLive == 1
        binding.tvExpandLive.setOnClickListener {
            data.matchInfo.roundNo?.let {
                if (data.matchInfo.pullRtmpUrl.isNullOrEmpty()) {
                    homeLiveListener.onClickLiveListener(data.matchInfo, it)
                } else {
                    (bindingAdapter as HomeLiveAdapter).expandMatchId = data.matchInfo?.id
                    homeLiveListener.onClickLiveListener(data.matchInfo, it)
                }
            }
        }
        if (lastExpandLive != isExpendLive) {
            lastExpandLive = isExpendLive
            updateLive(isExpendLive)
        }
//        binding.blockNormalGame.measure(0, 0)
//        val width = binding.blockNormalGame.measuredWidth
//        val margin = width + 4.dp
//        (binding.tvLeagueName.layoutParams as MarginLayoutParams).let {
//            it.leftMargin = margin
//            it.rightMargin = margin
//        }
    }

    fun updateLive(isExpandLive: Boolean) {
        initPlayView()
        if (isExpandLive) {
            if (!data.matchInfo.pullRtmpUrl.isNullOrEmpty()) {
                GSYVideoManager.instance().setNeedMute(true)
                binding.videoView.startPlayLogic()
                (bindingAdapter as HomeLiveAdapter).playerView = binding.videoView
            }
            binding.rippleView.showWaveAnimation()
        } else {
            binding.rippleView.cancelWaveAnimation()
            binding.tvLiveTime.text = null
            GSYVideoManager.instance().setNeedMute(true)
            binding.videoView.release()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                binding.videoView.releasePointerCapture()
            }
        }
        binding.flLive.isVisible = isExpandLive
        GSYVideoManager.instance().isNeedMute = !binding.ivLiveSound.isSelected
        binding.ivLiveSound.setOnClickListener {
            it.isSelected = !it.isSelected
            GSYVideoManager.instance().isNeedMute = !binding.ivLiveSound.isSelected
        }
        binding.tvCollse.setOnClickListener {
            (bindingAdapter as HomeLiveAdapter).expandMatchId = null
        }
    }

    fun initPlayView() {
        data.matchInfo.let {
            binding.videoView.setGSYVideoProgressListener(this)
            binding.ivCover.isVisible = true
            binding.videoView.setOnOkListener(object : OKVideoPlayer.OnOkListener {
                override fun onStartPrepared() {

                }

                override fun onPrepared() {
                    binding.ivCover.isVisible = false
                    binding.videoView.layoutParams.apply {
                        height =
                            binding.videoView.width * binding.videoView.currentVideoHeight / binding.videoView.currentVideoWidth
                        binding.videoView.layoutParams = this
                    }
                }

                override fun onError() {
                    binding.ivCover.isVisible = true
                }
            })
            Glide.with(binding.root.context)
                .load(data.matchInfo.frontCoverUrl)
                .apply(mRequestOptions)
                .into(binding.ivCover)
            if (!it.pullRtmpUrl.isNullOrEmpty()) {
                binding.videoView.setUp(it.pullRtmpUrl, true, "");
            } else if (!it.pullFlvUrl.isNullOrEmpty()) {
                binding.videoView.setUp(it.pullFlvUrl, true, "");
            } else {

            }
        }
    }
    fun update(oddsType: OddsType) {
        //設置賽事資訊是否顯示
        setupGameInfoVisibility(data)

        //設置背景、隊伍名稱、點擊事件
        setupGameInfo()

        //玩法Code
        var oddPlayCateCode = PlayCate.SINGLE.value

        var oddList = listOf<Odd?>()

        val oddsMap = mutableMapOf<String, List<Odd?>?>()
        data.oddsMap?.forEach {
            oddsMap[it.key] = it.value
        }
        val sortOddsMap = oddsMap.filterValues { it?.size ?: 0 > 0 }.sortOdds(data.oddsSort)
            .filterPlayCateSpanned(data.matchInfo.gameType!!)
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
        binding.tvGamePlayCateCodeName.text = LocalUtils.getString(R.string.win_line)
        with(binding) {
            //配置賽事比分及機制
            setupMatchScore()
            //region 第1個按鈕
            if (oddList.isNotEmpty()) {
                setOdds(oddBtn1, null, null, oddList[0], oddPlayCateCode, playCateName, oddList, oddsType)
            } else {
                oddBtn1.visibility = View.GONE
            }
            //endregion

            //region 第2個按鈕
            if (oddList.size > 1) {
                setOdds(oddBtn2, oddBtn1, null, oddList[1], oddPlayCateCode, playCateName, oddList, oddsType)
            } else {
                oddBtn2.visibility = View.GONE
            }

            //region 第3個按鈕
            if (oddList.size > 2) {
                setOdds(oddBtn3, oddBtn1, oddBtn2, oddList[2], oddPlayCateCode, playCateName, oddList, oddsType)
            } else {
                oddBtn3.visibility = View.GONE
            }
            //endregion

            //region 比賽狀態(狀態、時間)
            val gameType = data.matchInfo?.gameType
            setupMatchTimeAndStatus(
                item = data,
                isTimerEnable = true,
                isTimerPause = data.matchInfo?.stopped == TimeCounting.STOP.value
            )
            //endregion
        }
    }

    private fun setOdds(currentOddBtn: OddsButtonHome,
                        otherBtn1: OddsButtonHome?,
                        otherBtn2: OddsButtonHome?,
                        odd: Odd?,
                        oddPlayCateCode: String,
                        playCateName: String,
                        oddList: List<Odd?>,
                        oddsType: OddsType) {

        currentOddBtn.visibility = View.VISIBLE
        if (otherBtn1?.isLocked() == true || otherBtn2?.isLocked()  == true) {
            currentOddBtn.lockOdds()
            return
        }

        if (otherBtn1?.isDeactivated() == true || otherBtn2?.isDeactivated()  == true) {
            currentOddBtn.deactivatedOdds()
            return
        }

        setupOddsButton(currentOddBtn, odd)
        currentOddBtn.setupOdd4hall(oddPlayCateCode, odd, oddList, oddsType)
        currentOddBtn.setButtonBetClick(
            data = data,
            odd = odd,
            playCateCode = oddPlayCateCode,
            playCateName = playCateName,
            homeLiveListener = homeLiveListener
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
            //聯賽名稱
            if (data.matchInfo.streamerName.toString().isNotBlank()) {
                tvAnchorName.text = data.matchInfo.streamerName
            } else {
                tvAnchorName.text = binding.tvAnchorName.context.getString(R.string.okbet_live_name)
            }

            //region 隊伍名稱
            tvHomeName.text = data.matchInfo.homeName
            tvAwayName.text = data.matchInfo.awayName
            //endregion

//            if (!Objects.equals(ivAnchorAvatar.tag, data.matchInfo.streamerIcon)) {
//                ivAnchorAvatar.tag = data.matchInfo.streamerIcon
//                Glide.with(binding.root.context)
//                    .load(data.matchInfo.streamerIcon)
//                    .apply(mRequestOptions)
//                    .fallback(R.drawable.icon_avatar)
//                    .error(R.drawable.icon_avatar)
//                    .into(ivAnchorAvatar)
//            }
            if (rippleView.getmBtnImg() != null) {
                Glide.with(binding.root.context)
                    .asBitmap()
                    .load(data.matchInfo.streamerIcon)
                    .apply(mRequestOptions)
                    .dontAnimate()
                    .placeholder(R.drawable.icon_avatar)
                    .fallback(R.drawable.icon_avatar)
                    .error(R.drawable.icon_avatar)
                    .into(rippleView.getmBtnImg())
            }

            //region 隊伍圖示
            ivHomeIcon.setTeamLogo(data.matchInfo?.homeIcon)
            ivAwayIcon.setTeamLogo(data.matchInfo?.awayIcon)
            //endregion
            tvLeagueName.text = data.league.name
            if (data.matchInfo != null && data.matchInfo.leagueName.isEmptyStr()) {
                data.matchInfo.leagueName = data.league.name
            }

            //region 點擊進入賽事詳情
            val matchOddList = transferMatchOddList(data)
            val matchInfoList = matchOddList.mapNotNull {
                it.matchInfo
            }
            View.OnClickListener {
                homeLiveListener.onItemClickListener(data)
            }.let {
                root.setOnClickListener(it)
                binding.linEnterLive.setOnClickListener(it)
                binding.vEmpty.setOnClickListener(it)
                binding.ivCover.setOnClickListener(it)
            }
        }
    }

    /**
     * 配置投注按鈕Callback
     */
    private fun OddsButtonHome.setButtonBetClick(
        data: MatchLiveData,
        odd: Odd?,
        playCateCode: String,
        playCateName: String,
        homeLiveListener: HomeLiveListener,
    ) {
        setOnClickListener {
            odd?.let { odd ->
                homeLiveListener.onClickBetListener(
                    gameType = data.matchInfo.gameType!!,
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
    private val isScoreTextVisible = { item: MatchLiveData ->
        when (TimeUtil.isTimeInPlay(item.matchInfo?.startTime)) {
            true -> View.VISIBLE
            else -> View.GONE
        }
    }

    /**
     * 設置賽事顯示VS或比分
     */
    private fun setupGameInfoVisibility(item: MatchLiveData) {
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
    private fun setupMatchScore() {
        when (data.matchInfo.gameType) {
            GameType.VB.key -> setVbScoreText()
            GameType.TN.key -> setTnScoreText()
            GameType.FT.key -> setFtScoreText()
            GameType.BK.key -> setBkScoreText()
            GameType.TT.key -> setVbScoreText()
            GameType.BM.key -> setBmScoreText()
            GameType.BB.key -> setBbScoreText()
            else -> setBkScoreText()
        }
    }

    /**
     * 設置排球類型比分及比賽制度
     */
    private fun setVbScoreText() {
        binding.apply {
            setAllScoreTextAtBottom(data)
        }
    }

    /**
     * 設置網球類型比分及比賽制度
     */
    private fun setTnScoreText() {
        binding.apply {
            setAllScoreTextAtBottom(data)

        }
    }

    /**
     * 設置足球類型比分及比賽制度
     */
    private fun setFtScoreText() {
        binding.setScoreText(data)
    }

    /**
     * 設置籃球類型比分及比賽制度
     */
    private fun setBkScoreText() {
        binding.setScoreText(data)
    }

    /**
     * 設置羽球類型比分及比賽制度
     */
    private fun setBmScoreText() {
        binding.apply {
            setAllScoreTextAtBottom(data)
        }
    }

    /**
     * 設置羽球類型比分及比賽制度
     */
    private fun setBbScoreText() {
        with(binding) {
            setScoreText(data)
        }
    }

    /**
     * 設置盤類型比分
     */
    private fun ItemHomeLiveBinding.setAllScoreTextAtBottom(item: MatchLiveData) {
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
    private fun ItemHomeLiveBinding.setScoreText(item: MatchLiveData) {
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
        item: MatchLiveData,
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
        item: MatchLiveData,
        isTimerEnable: Boolean,
        isTimerPause: Boolean,
    ) {
        when {
            TimeUtil.isTimeInPlay(item.matchInfo?.startTime) -> {
                val socketValue = item.matchInfo?.socketMatchStatus

                if (needCountStatus(socketValue) && item.matchInfo?.leagueTime != 0) {
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

    private fun setStatusText(item: MatchLiveData) {
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
                if (TimeUtil.isTimeToday(item.matchInfo?.startTime)) {
                    ""
                    //                    itemView.context.getString((R.string.home_tab_today))
                } else {
                    item.matchInfo?.startDateDisplay
                }
            }
        }
    }

    private fun setTextViewStatus(item: MatchLiveData) {
        when {
            (TimeUtil.isTimeInPlay(item.matchInfo?.startTime)
                    && item.matchInfo?.status == GameStatus.POSTPONED.code
                    && (item.matchInfo?.gameType == GameType.FT.name || item.matchInfo?.gameType == GameType.BK.name || item.matchInfo?.gameType == GameType.TN.name)) -> {
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
    private fun setupGameStatusBlockVisibility(item: MatchLiveData) {
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

    private fun transferMatchOddList(item: MatchLiveData): MutableList<MatchOdd> {
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

    override fun onLifeDestroy() {
        super.onLifeDestroy()
        binding.videoView.release()
    }

    override fun onProgress(
        progress: Long,
        secProgress: Long,
        currentPosition: Long,
        duration: Long,
    ) {
        if (currentPosition > 60 * 60 * 1000) {
            binding.tvLiveTime.text =
                TimeUtil.timeFormat(currentPosition, TimeUtil.HM_FORMAT_SS_12)
        } else {
            binding.tvLiveTime.text =
                TimeUtil.timeFormat(currentPosition, TimeUtil.HM_FORMAT_MS)
        }
    }

}