package org.cxct.sportlottery.ui.sport.list.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.doOnLayout
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.ItemSportOdd2Binding
import org.cxct.sportlottery.network.common.GameStatus
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.odds.list.TimeCounting
import org.cxct.sportlottery.ui.sport.common.OddButtonPagerAdapter2
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.LogUtil
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.needCountStatus
import org.cxct.sportlottery.view.expand
import org.cxct.sportlottery.view.isVisible
import org.cxct.sportlottery.view.layoutmanager.CustomLinearLayoutManager
import org.cxct.sportlottery.view.overScrollView.OverScrollDecoratorHelper
import java.util.*
class SportMatchVH(private val binding: ItemSportOdd2Binding,
                   private val onFavoriteClick: (String) -> Unit): BaseViewHolder(binding.root) {

    companion object {

        fun of(parent: ViewGroup, pool: RecyclerView.RecycledViewPool,
               onOddClick: OnOddClickListener,
               lifecycleOwner: LifecycleOwner,
               onFavoriteClick: (String) -> Unit,
               esportTheme: Boolean = false): SportMatchVH {

            val context = parent.context
            val biding = ItemSportOdd2Binding.inflate(LayoutInflater.from(context), parent, false)
            val rcv = biding.rvLeagueOddBtnPagerMain
            biding.contentBaseballStatus.root.setPadding(0, 4.dp, 0, 0)
            rcv.setRecycledViewPool(pool)
            rcv.layoutManager = CustomLinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false).apply {
                isAutoMeasureEnabled = false
            }
            biding.hIndicator.run {
                setIndicatorColor(context.getColor(R.color.color_80BEC7DC), context.getColor(R.color.color_025BE8))
                val height = 4.dp
                itemWidth = 10.dp
                itemHeight = height
                mRadius = itemWidth.toFloat()
                setSpacing(height)
                biding.hIndicator.itemClickListener = { rcv.smoothScrollToPosition(it) }
            }
            val oddPageAdapter = OddButtonPagerAdapter2(context, onOddClick,esportTheme)
            oddPageAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT
            rcv.doOnLayout {
                rcv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    val itemWidth = rcv.measuredWidth.toFloat() // 这个很重要，item的宽度要刚好等于recyclerview的宽度，不然PagerSnapHelper翻页会存在滑动偏差导致指示器位置计算的不准
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        val scrollX = recyclerView.computeHorizontalScrollOffset()
                        val positionFloat = scrollX / itemWidth
                        val position = positionFloat.toInt()
                        val progress = positionFloat - position
                        biding.hIndicator.onPageScrolled(position, progress, scrollX)
                    }
                })
            }

            rcv.adapter = oddPageAdapter
            rcv.setHasFixedSize(true)
            (rcv.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            OverScrollDecoratorHelper.setUpOverScroll(rcv, OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL)
            PagerSnapHelper().attachToRecyclerView(rcv)
            return SportMatchVH(biding, onFavoriteClick).apply { lifecycleOwner.doOnDestory { onStop() } }
        }
    }

    private val sportMatchTimer by lazy { SportMatchTimer() }

    fun onStop() {
        sportMatchTimer.stop()
    }

    fun resetStatusView() = binding.run {
        onStop()
        leagueOddMatchStatus.text = ""
        tvCollseStatus.text = ""
        tvCollseTime.text = ""
        setViewGone(leagueNeutral,
            leagueCornerKicks,
            leagueSpt,
            tvPeroid,
            binding.contentBaseballStatus.root,
            icAttackH,
            icAttackC,
            linHomeRoundScore,
            linAwayRoundScore,
            tvRedCards,
            tvYellowCards,
            tvPeroidsScore,
            ivLive,
            ivPlay,
            ivAnimation,
            leagueOddMatchTime
        )
    }


    // 是否显示加时标志
    private fun isOT(gameType: String, status: Int): Boolean {
        if (gameType == GameType.BK.key
            && (status == 32 || status == 40 || status == 110)) {
            return true
        }

        if (gameType == GameType.FT.key
            && (status == 32 || status == 33 || status == 41 || status == 42 || status == 106 || status == 107 || status == 110 || status == 131)) {
            return true
        }

        return false
    }

    fun setupMatchInfo(matchInfo: MatchInfo?, matchType: MatchType) = binding.run {
        leagueOddMatchNameHome.text = matchInfo?.homeName
        leagueOddMatchNameAway.text = matchInfo?.awayName
        leagueOddMatchNameHome.requestLayout()
        leagueOddMatchNameAway.requestLayout()
        tvCollseHomeName.text = leagueOddMatchNameHome.text
        tvCollseAwayName.text = leagueOddMatchNameAway.text

        setupMatchScore(matchInfo, matchType)
        leagueOddMatchPlayCount.text = matchInfo?.playCateNum.toString() + "+ "

        leagueOddMatchFavorite.isSelected = matchInfo?.isFavorite ?: false
        leagueOddMatchFavorite.setOnClickListener { matchInfo?.id?.let { onFavoriteClick.invoke(it) } }
        bindOTStatus(matchInfo)
        leagueNeutral.isVisible = matchInfo?.neutral == 1

//        leagueOddMatchChart.isVisible = matchInfo?.source == MatchSource.SHOW_STATISTICS.code
        ivCashOut.isVisible = matchInfo?.cashoutStatus==1
        matchInfo?.let { matchInfo->
            bindLiveStatus(matchInfo)
            setLinCollse(matchInfo)
            setOnClickListeners(linCollse,ivCollseArrow,llLeftPanel,viewVpTitle){
                matchInfo.expand = !matchInfo.expand
                setLinCollse(matchInfo)
            }
        }
    }

    fun bindOTStatus(matchInfo: MatchInfo?) = binding.run {
        if (matchInfo?.gameType == null || matchInfo?.socketMatchStatus == null) {
            ivOT.gone()
        } else {
            ivOT.isVisible = isOT(matchInfo?.gameType!!, matchInfo?.socketMatchStatus!!)
        }
    }

    private inline fun bindLiveStatus(matchInfo: MatchInfo) = binding.run {
        if (matchInfo.liveVideo == 1) {
            if (matchInfo.isLive == 1) {
                ivLive.visible()
            } else {
                ivPlay.visible()
            }
        } else if (!matchInfo.trackerId.isNullOrEmpty()) {
            ivAnimation.visible()
        }
    }

    fun setupMatchScore(matchInfo: MatchInfo?, matchType: MatchType) = binding.run {
        if (matchType == MatchType.IN_PLAY && matchInfo?.gameType == GameType.BB.key) {
            linearLayout.gone()
            contentBaseballStatus.root.visible()
        } else {
            linearLayout.visible()
            contentBaseballStatus.root.gone()
        }

        when (matchInfo?.gameType) {
            GameType.VB.key -> setVbScoreText(matchInfo, matchType)
            GameType.TN.key -> setTnScoreText(matchInfo, matchType)
            GameType.FT.key -> setFtScoreText(matchInfo, matchType)
            GameType.BK.key -> setBkScoreText(matchInfo, matchType)
            GameType.TT.key -> setVbScoreText(matchInfo, matchType)
            GameType.BM.key -> setBmScoreText(matchInfo, matchType)
            GameType.BB.key -> setBbScoreText(matchInfo, matchType)
            GameType.CK.key -> setCkScoreText(matchInfo)
            GameType.IH.key -> setIHScoreText(matchInfo, matchType)
            else -> matchInfo?.let { setBkScoreText(it, matchType) }
        }
        updateCollse(matchInfo)
    }

    private fun setFbKicks(matchInfo: MatchInfo) {
        binding.leagueCornerKicks.setFbKicks(matchInfo)
    }

    private fun setScoreTextAtFront(matchInfo: MatchInfo) {
        setMatchScore(matchInfo, binding.leagueOddMatchScoreHome, binding.leagueOddMatchScoreAway)
    }

    /**
     * 设置足球黄牌，红牌数量
     */
    private fun setCardText(matchInfo: MatchInfo) = binding.run {

        if (!TimeUtil.isTimeInPlay(matchInfo.startTime)) {
            tvYellowCards.gone()
            tvRedCards.gone()
            return
        }

        setCardsNum(tvRedCards, matchInfo.homeCards, matchInfo.awayCards)
        setCardsNum(tvYellowCards, matchInfo.homeYellowCards, matchInfo.awayYellowCards)
    }

    private inline fun setCardsNum(textView: TextView, homeCards: Int, awayCards: Int) {
        if (homeCards + awayCards == 0) {
            textView.gone()
        } else {
            textView.visible()
            textView.text = " ${(homeCards)}-${awayCards}"
        }
    }

    private fun setFtScoreText(matchInfo: MatchInfo, matchType: MatchType) {
        setScoreTextAtFront(matchInfo)
        setCardText(matchInfo)
        setFbKicks(matchInfo)
    }

    private fun setBkScoreText(matchInfo: MatchInfo, matchType: MatchType) {
        setScoreTextAtFront(matchInfo)
        setAllScoreTextAtBottom(matchInfo, matchType)
    }

    private fun setIHScoreText(matchInfo: MatchInfo, matchType: MatchType) {
        setScoreTextAtFront(matchInfo)
        setAllScoreTextAtBottom(matchInfo, matchType)
        setAttack(matchInfo)
    }

    private fun setVbScoreText(matchInfo: MatchInfo, matchType: MatchType) {
        setAllScoreTextAtBottom(matchInfo, matchType)
        setScoreTextAtFront(matchInfo)
        setSptText(matchInfo)
        setCurrentPeroid(matchInfo)
        setAttack(matchInfo)
    }

    private fun setTnScoreText(matchInfo: MatchInfo, matchType: MatchType) {
        setAllScoreTextAtBottom(matchInfo, matchType)
        setSptText(matchInfo)
        setTennisRoundScore(matchInfo)
        setCurrentPeroid(matchInfo)
        setAttack(matchInfo)
    }

    private fun setBmScoreText(matchInfo: MatchInfo, matchType: MatchType) {
        setAllScoreTextAtBottom(matchInfo, matchType)
        setScoreTextAtFront(matchInfo)
        setSptText(matchInfo)
        setCurrentPeroid(matchInfo)
        setAttack(matchInfo)
    }

    private fun setBbScoreText(matchInfo: MatchInfo, matchType: MatchType) {
        if (TimeUtil.isTimeInPlay(matchInfo.startTime)) {
            setScoreTextAtFront(matchInfo)
            setAttack(matchInfo)
            setBBStatus(matchInfo)
        } else {
            setBkScoreText(matchInfo, matchType)
        }
    }

    private fun setCkScoreText(matchInfo: MatchInfo) {
        setScoreTextAtFront(matchInfo)
        setAttack(matchInfo)
        setSptText(matchInfo)
    }

    /**
     * 网球和羽毛球  排球，乒乓球 显示局比分
     */
    private inline fun setAllScoreTextAtBottom(matchInfo: MatchInfo, matchType: MatchType) {
        if(matchType == MatchType.IN_PLAY) {
            binding.tvPeroidsScore.setMatchRoundScore(matchInfo)
        }
    }

    /**
     * 賽制(5盤3勝)
     * 只有网球，排球，乒乓球，羽毛球
     */
    private inline fun setSptText(matchInfo: MatchInfo) {
        binding.leagueSpt.setMatchSptText(matchInfo)
    }

    /**
     * 设置当前盘数/局数/回合
     * 网球显示 第x盘
     * 其他球类显示 第x局
     */
    private inline fun setCurrentPeroid(matchInfo: MatchInfo) {
        binding.tvPeroid.setMatchCurrentPeroid(matchInfo)
    }


    /**
     * 设置球权标识，
     *  目前支持 棒球，网球，排球，乒乓球，羽毛球
     *  其中网球标识是另外一个位置
     */
    private inline fun setAttack(matchInfo: MatchInfo) = binding.run {
        setMatchAttack(matchInfo, icAttackH, icAttackC, icAttackH, icAttackC)
    }


    /**
     * 棒球的特殊布局处理
     */
    private inline fun setBBStatus(matchInfo: MatchInfo) = binding.run {
        linearLayout.isVisible = false
        contentBaseballStatus.let {
            it.root.isVisible = true
            setBBStatusView(matchInfo,
                it.leagueOddMatchBbStatus,
                it.txvOut,
                it.leagueOddMatchHalfStatus,
                it.leagueOddMatchBasebag)
        }
        updateCollse(matchInfo)
    }

    /**
     * 设置网球的中间分数布局
     */
    private fun setTennisRoundScore(matchInfo: MatchInfo) = binding.run {
        //隐藏其他球类的比分
        setViewGone(leagueOddMatchScoreHome,
            leagueOddMatchScoreAway,
        )

        linHomeRoundScore.visible()
        linAwayRoundScore.visible()

        //设置盘比，局比，分数
        setTNRoundScore(matchInfo = matchInfo,
            tvHomeTotalScore = leagueOddMatchTotalScoreHomeBottom,
            tvAwayTotalScore = leagueOddMatchTotalScoreAwayBottom,
            tvHomeScore = leagueOddMatchScoreHomeBottom,
            tvAwayScore = leagueOddMatchScoreAwayBottom,
            tvHomePoints = leagueOddMatchPointHomeBottom,
            tvAwayPoints = leagueOddMatchPointAwayBottom)

    }

    private fun isTimerEnable(gameType: String?, matchType: MatchType): Boolean {
        return (gameType == GameType.FT.key
                || gameType == GameType.BK.key
                || gameType == GameType.RB.key
                || gameType == GameType.AFT.key
                || matchType == MatchType.PARLAY
                || matchType == MatchType.AT_START
                || matchType == MatchType.MY_EVENT)
    }

    private fun onTimerUpdate(timeMillis: Long, matchInfo: MatchInfo) {
        binding.leagueOddMatchTime.text = if (timeMillis > 1000) {
            TimeUtil.longToMmSs(timeMillis)
        } else {
            "00:00"
        }
        matchInfo.leagueTimeRecode = timeMillis
        updateCollse(matchInfo)
    }

    private fun onTimerUpdate2(timeMillis: Long, matchInfo: MatchInfo) {
        binding.leagueOddMatchTime.text = String.format(
            binding.root.context.getString(R.string.at_start_remain_minute),
            if (timeMillis > 1000) TimeUtil.longToMinute(timeMillis) else 0)
        matchInfo.leagueTimeRecode = timeMillis
        updateCollse(matchInfo)
    }

    fun setupMatchTimeAndStatus(matchInfo: MatchInfo,
                                matchType: MatchType) {

        sportMatchTimer.stop()
        val isTimeInPlay = TimeUtil.isTimeInPlay(matchInfo.startTime)


        binding.leagueOddMatchStatus.apply {
            text = when {
                (isTimeInPlay
                        && matchInfo.status == GameStatus.POSTPONED.code
                        && (matchInfo.gameType == GameType.FT.name || matchInfo.gameType == GameType.BK.name || matchInfo.gameType == GameType.TN.name)) -> {
                    context.getString(R.string.game_postponed)
                }
                isTimeInPlay -> {
                    if (matchInfo.statusName18n.isNullOrEmpty()
                        || (matchInfo.gameType == GameType.TN.name
                                || matchInfo.gameType == GameType.VB.name
                                || matchInfo.gameType == GameType.TT.name
                                || matchInfo.gameType == GameType.BM.name)) {
                        ""
                    } else {
                        matchInfo.statusName18n
                    }
                }
                TimeUtil.isTimeToday(matchInfo.startTime) -> {
                    context.getString((R.string.home_tab_today))
                }
                else -> {
                    ""
                }
            }
            isVisible = text.isNotEmpty()
        }

        if (isTimeInPlay) {
            if (matchInfo.gameType == GameType.TN.key
                || !isTimerEnable(matchInfo?.gameType, matchType)
                || !needCountStatus(matchInfo.socketMatchStatus, matchInfo.leagueTime)) {
                binding.leagueOddMatchTime.gone()
                return
            }
            binding.leagueOddMatchTime.visible()
            var timeMillis = (matchInfo.leagueTime?.toLong() ?: 0) * 1000
            matchInfo.leagueTimeRecode = timeMillis
            onTimerUpdate(timeMillis, matchInfo)

            if (matchInfo.stopped == TimeCounting.STOP) {
                return
            }

            val isDecrease = matchInfo.gameType == GameType.BK.key
                    || matchInfo.gameType == GameType.RB.key
                    || matchInfo.gameType == GameType.AFT.key
            sportMatchTimer.start(1000, 1000, object : TimerTask() {
                override fun run() {
                    if (isDecrease) {
                        timeMillis -= 1000
                    } else {
                        timeMillis += 1000
                    }

                    binding.root.post { onTimerUpdate(timeMillis, matchInfo) }
                }
            })
            return
        }


        if (TimeUtil.isTimeAtStart(matchInfo.startTime)) {
            var remainTime = matchInfo.remainTime ?: return
            matchInfo.leagueTimeRecode = remainTime
            onTimerUpdate2(remainTime, matchInfo)
            sportMatchTimer.start(1000, 1000, object : TimerTask() {
                override fun run() {
                    remainTime -= 1000
                    binding.root.post { onTimerUpdate2(remainTime, matchInfo) }
                }
            })
            return
        }

        binding.leagueOddMatchTime.visible()
        if(matchInfo?.startTime == null) {
            binding.leagueOddMatchTime.text = ""
        } else {
            binding.leagueOddMatchTime.text = TimeUtil.timeFormat(matchInfo.startTime,
                    if (TimeUtil.isTimeToday(matchInfo.startTime)) TimeUtil.HM_FORMAT else TimeUtil.DM_HM_FORMAT)
        }
    }

    fun setupOddsButton(matchType: MatchType,
                        item: MatchOdd,
                        oddsType: OddsType) {
        val adapter = (binding.rvLeagueOddBtnPagerMain.adapter as OddButtonPagerAdapter2)
        adapter.setupData(matchType, item, oddsType)
        binding.hIndicator.resetItemCount(adapter.itemCount)
    }

    fun updateMatchInfo(matchInfo: MatchInfo?, matchType: MatchType) = binding.run {
        leagueOddMatchNameAway.text = matchInfo?.awayName
        leagueOddMatchNameHome.text = matchInfo?.homeName
        leagueOddMatchNameHome.requestLayout()
        leagueOddMatchNameAway.requestLayout()
        tvCollseHomeName.text = leagueOddMatchNameHome.text
        tvCollseAwayName.text = leagueOddMatchNameAway.text
        setupMatchScore(matchInfo, matchType)
        leagueOddMatchPlayCount.text = matchInfo?.playCateNum.toString() + "+ "
        leagueOddMatchFavorite.isSelected = matchInfo?.isFavorite ?: false
        leagueNeutral.isVisible = matchInfo?.neutral == 1
        ivCashOut.isVisible = matchInfo?.cashoutStatus==1
        matchInfo?.let {
            bindLiveStatus(it)
        }
    }

    fun updateFavoriteStatus(matchInfo: MatchInfo?) {
        binding.leagueOddMatchFavorite.isSelected = matchInfo?.isFavorite ?: false
    }

    private fun setLinCollse(matchInfo: MatchInfo)=binding.run{
        linCollse.isVisible = !matchInfo.expand
        linMatch.isVisible = matchInfo.expand
        frBottom.isVisible = matchInfo.expand
        if (!matchInfo.expand){
            updateCollse(matchInfo)
        }
    }
    fun updateCollse(matchInfo: MatchInfo?){
        when (matchInfo?.gameType) {
            GameType.FT.key ->
                setCollseStatusAndTime(binding.leagueOddMatchStatus,binding.leagueOddMatchTime)
            GameType.VB.key, GameType.TT.key ->
                setCollseStatusAndTime(binding.leagueOddMatchStatus,if(TimeUtil.isTimeInPlay(matchInfo.startTime)) binding.tvPeroid else binding.leagueOddMatchTime)
            GameType.TN.key ->
                setCollseStatusAndTime(binding.leagueOddMatchStatus,if(TimeUtil.isTimeInPlay(matchInfo.startTime)) binding.tvPeroid else binding.leagueOddMatchTime)
            GameType.BK.key ->
                setCollseStatusAndTime(binding.leagueOddMatchStatus,binding.leagueOddMatchTime)
            GameType.BM.key ->
                setCollseStatusAndTime(binding.leagueOddMatchStatus,if(TimeUtil.isTimeInPlay(matchInfo.startTime)) binding.tvPeroid else binding.leagueOddMatchTime)
            GameType.BB.key ->
                setCollseStatusAndTime(if(TimeUtil.isTimeInPlay(matchInfo.startTime)) binding.contentBaseballStatus.leagueOddMatchBbStatus else binding.leagueOddMatchStatus, binding.leagueOddMatchTime)
            GameType.CK.key ->
                setCollseStatusAndTime(binding.leagueOddMatchStatus, binding.leagueOddMatchTime)
            else ->
                setCollseStatusAndTime(binding.leagueOddMatchStatus,binding.leagueOddMatchTime)
        }
    }
    private fun setCollseStatusAndTime(tvStatus:TextView,tvTime:TextView){
        post{
            binding.tvCollseStatus.run {
                isVisible = tvStatus.text.isNotEmpty()&&tvStatus.isVisible()
                text = tvStatus.text
            }
            binding.tvCollseTime.run {
                isVisible = tvTime.text.isNotEmpty()&&tvTime.isVisible()
                text = tvTime.text
            }
        }
    }
}