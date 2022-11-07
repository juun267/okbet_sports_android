package org.cxct.sportlottery.ui.maintab.live

import android.view.View
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.pili.pldroid.player.AVOptions
import com.pili.pldroid.player.PLOnErrorListener
import com.pili.pldroid.player.PLOnInfoListener
import com.pili.pldroid.player.PLOnInfoListener.MEDIA_INFO_VIDEO_FRAME_RENDERING
import com.pili.pldroid.player.PLOnVideoSizeChangedListener
import com.pili.pldroid.player.widget.PLVideoView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ItemHomeLiveBinding
import org.cxct.sportlottery.extentions.gone
import org.cxct.sportlottery.extentions.visible
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.MatchLiveData
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.odds.list.TimeCounting
import org.cxct.sportlottery.ui.game.widget.OddsButtonHome
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.*
import java.util.*

class ItemHomeLiveHolder(
    val binding: ItemHomeLiveBinding,
    private val homeLiveListener: HomeLiveListener,
) : ViewHolderUtils.TimerViewHolderTimer(binding.root),
    PLOnInfoListener,
    PLOnVideoSizeChangedListener,
    PLOnErrorListener {
    lateinit var data: MatchLiveData

    override val oddStateChangeListener: OddStateChangeListener
        get() = object : OddStateChangeListener {
            override fun refreshOddButton(odd: Odd) {}
        }

    private val mRequestOptions = RequestOptions()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .dontTransform()

    fun bind(data: MatchLiveData, oddsType: OddsType) {
        //設置賽事資訊是否顯示
        update(data, oddsType)
        updateLive((bindingAdapter as HomeLiveAdapter).expandMatchId == data.matchInfo?.id)
    }

    fun updateLive(isExpandLive: Boolean) {
        initPlayView()
        if (isExpandLive) {
            if (!data.matchInfo.pullRtmpUrl.isNullOrEmpty()) {
                binding.videoView.start()
            }

            binding.rippleView.showWaveAnimation()
//            binding.rippleEnter.showWaveAnimation()
        } else {
            binding.rippleView.cancelWaveAnimation()
//            binding.rippleEnter.cancelWaveAnimation()
            binding.videoView.stopPlayback()
        }
        binding.flLive.isVisible = isExpandLive
        setVolumeState()
        binding.ivLiveSound.setOnClickListener {
            it.isSelected = !it.isSelected
            setVolumeState()
        }
        binding.tvCollse.setOnClickListener {
            (bindingAdapter as HomeLiveAdapter).expandMatchId = null
        }
        binding.tvExpandLive.isVisible = !isExpandLive
        binding.tvExpandLive.setOnClickListener {
            (bindingAdapter as HomeLiveAdapter).expandMatchId = data.matchInfo?.id
            data.matchInfo.roundNo?.let {
                homeLiveListener.onClickLiveListener(data.matchInfo.id, it)
            }
        }
    }

    fun initPlayView() {
        data.matchInfo?.let {
            val options = AVOptions()
            options.setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 10 * 1000)
            options.setInteger(AVOptions.KEY_SEEK_MODE, 1)
            options.setInteger(AVOptions.KEY_MEDIACODEC, AVOptions.MEDIA_CODEC_HW_DECODE)
            options.setInteger(AVOptions.KEY_LIVE_STREAMING, 1)
            options.setInteger(AVOptions.KEY_CACHE_BUFFER_DURATION, 200)
            options.setInteger(AVOptions.KEY_CACHE_BUFFER_DURATION_SPEED_ADJUST, 0)

            binding.videoView.setAVOptions(options)
            binding.videoView.setOnVideoSizeChangedListener(this)
            binding.videoView.setOnErrorListener(this)
            binding.videoView.setOnInfoListener(this)
            binding.videoView.setDisplayAspectRatio(PLVideoView.ASPECT_RATIO_FIT_PARENT)
            binding.videoView.setVolume(0f, 0f)
//            binding.videoView.setCoverView(binding.ivCover)
            Glide.with(binding.root.context)
                .load(data.matchInfo.frontCoverUrl)
                .apply(mRequestOptions)
                .into(binding.ivCover)
            if (!it.pullRtmpUrl.isNullOrEmpty()) {
                binding.videoView.setVideoPath(it.pullRtmpUrl)
            } else if (!it.pullFlvUrl.isNullOrEmpty()) {
                binding.videoView.setVideoPath(it.pullFlvUrl)
            }
        }
    }

    fun setVolumeState() {
        if (binding.ivLiveSound.isSelected) {
            binding.videoView.setVolume(1f, 1f)
        } else {
            binding.videoView.setVolume(0f, 0f)
        }
    }

    fun update(data: MatchLiveData, oddsType: OddsType) {
        this.data = data
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
                        homeLiveListener = homeLiveListener
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
                    setButtonBetClick(
                        data = data,
                        odd = odd2,
                        playCateCode = oddPlayCateCode,
                        playCateName = playCateName,
                        homeLiveListener = homeLiveListener
                    )
                }
            } else {
                oddBtn2.visibility = View.GONE
            }

            //region 第3個按鈕
            if (oddList.size > 2) {
                val odd3 = oddList[2]
                with(oddBtn3) {
                    visibility = View.VISIBLE
                    setupOddsButton(this, odd3)
                    setupOdd4hall(oddPlayCateCode, odd3, oddList, oddsType)
                    setButtonBetClick(
                        data = data,
                        odd = odd3,
                        playCateCode = oddPlayCateCode,
                        playCateName = playCateName,
                        homeLiveListener = homeLiveListener
                    )
                }
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

    /**
     * 設置背景、隊伍名稱、點擊事件、玩法數量、聯賽名稱
     */
    private fun setupGameInfo() {
        with(binding) {
            //聯賽名稱
            tvAnchorName.text = data.matchInfo.streamerName
                ?: binding.tvAnchorName.context.getString(R.string.okbet_live_name)
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


            //region 點擊進入賽事詳情
            val matchOddList = transferMatchOddList(data)
            val matchInfoList = matchOddList.mapNotNull {
                it.matchInfo
            }
            root.setOnClickListener {
                homeLiveListener.onItemClickListener(data)
            }
            //endregion
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
                if (TimeUtil.isTimeToday(item.matchInfo?.startTime))
                    itemView.context.getString((R.string.home_tab_today))
                else
                    item.matchInfo?.startDateDisplay
            }
        }
    }

    private fun setTextViewStatus(item: MatchLiveData) {
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

    override fun onVideoSizeChanged(p0: Int, p1: Int) {
        binding.videoView.layoutParams.apply {
            height = binding.videoView.width * p1 / p0
        }
    }

    override fun onError(p0: Int, p1: Any?): Boolean {
//        ToastUtil.showToast(context = binding.root.context, p0.toString() + "," + p1);
        LogUtil.e(p0.toString() + "," + p1)
        return false
    }

    override fun onInfo(p0: Int, p1: Int, p2: Any?) {
        when (p0) {
            MEDIA_INFO_VIDEO_FRAME_RENDERING -> {
                if (p1 > 60 * 60 * 1000) {
                    binding.tvLiveTime.text =
                        TimeUtil.timeFormat(p1.toLong(), TimeUtil.HM_FORMAT_SS_12)
                } else {
                    binding.tvLiveTime.text =
                        TimeUtil.timeFormat(p1.toLong(), TimeUtil.HM_FORMAT_MS)
                }
            }
        }
    }

}