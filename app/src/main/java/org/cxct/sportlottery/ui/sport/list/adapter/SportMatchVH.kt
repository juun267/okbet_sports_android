package org.cxct.sportlottery.ui.sport.list.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.enums.MatchSource
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.ItemSportOdd2Binding
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.ui.sport.common.OddButtonPagerAdapter
import org.cxct.sportlottery.ui.sport.detail.SportDetailActivity
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.needCountStatus
import org.cxct.sportlottery.util.setTeamLogo
import org.cxct.sportlottery.view.layoutmanager.CustomLinearLayoutManager
import org.cxct.sportlottery.view.overScrollView.OverScrollDecoratorHelper
import java.util.*

class SportMatchVH(private val binding: ItemSportOdd2Binding): BaseViewHolder(binding.root) {

    companion object {

        fun of(parent: ViewGroup, pool: RecyclerView.RecycledViewPool,
               onItemClick:(Int, View, BaseNode) -> Unit,
               lifecycleOwner: LifecycleOwner): SportMatchVH {
            val context = parent.context
            val biding = ItemSportOdd2Binding.inflate(LayoutInflater.from(context), parent, false)
            val rcv = biding.rvLeagueOddBtnPagerMain
            rcv.setRecycledViewPool(pool)
            rcv.layoutManager = CustomLinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false).apply {
                isAutoMeasureEnabled = false
            }
            biding.hIndicator.run {
                setIndicatorColor(context.getColor(R.color.color_BEC7DC), context.getColor(R.color.color_025BE8))
                val height = 4.dp
                itemWidth = 10.dp
                itemHeight = height
                mRadius = itemWidth
                setSpacing(height)
                biding.hIndicator.itemClickListener = { rcv.smoothScrollToPosition(it) }
            }
            val oddButtonPagerAdapter = OddButtonPagerAdapter(context)
            oddButtonPagerAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT
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

            rcv.adapter = oddButtonPagerAdapter
            rcv.setHasFixedSize(true)
            (rcv.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            OverScrollDecoratorHelper.setUpOverScroll(rcv, OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL)
            PagerSnapHelper().attachToRecyclerView(rcv)
            return SportMatchVH(biding).apply { lifecycleOwner.doOnDestory { onStop() } }
        }
    }

    private val sportMatchTimer by lazy { SportMatchTimer() }

    fun onStop() {
        sportMatchTimer.stop()
    }

    fun resetStatusView() = binding.run {
        setViewGone(leagueNeutral,
            leagueCornerKicks,
            leagueSpt,
            tvPeroid,
            binding.contentBaseballStatus.root,
            icAttackH,
            icAttackC,
            icAttackTnH,
            icAttackTnC,
            linHomeRoundScore,
            linAwayRoundScore,
            leagueOddYellowCardsHome,
            leagueOddYellowCardsAway,
            leagueOddMatchCardsHome,
            leagueOddMatchCardsAway,
            tvPeroidsScore,
            ivLive,
            ivPlay,
            ivAnimation
        )
    }

    fun setupMatchInfo(matchInfo: MatchInfo?, matchType: MatchType) = binding.run {
        leagueOddMatchNameHome.text = matchInfo?.homeName
        leagueOddMatchNameAway.text = matchInfo?.awayName
        ivHomeTeamLogo.setTeamLogo(matchInfo?.homeIcon)
        ivAwayTeamLogo.setTeamLogo(matchInfo?.awayIcon)

        val navDetailClick = View.OnClickListener { matchInfo?.let { navMatchDetailPage(ivAwayTeamLogo.context, it, matchType) } }

        setupMatchScore(matchInfo, matchType)
        leagueOddMatchPlayCount.text = matchInfo?.playCateNum.toString() + "+>"
        leagueOddMatchPlayCount.setOnClickListener(navDetailClick)

        leagueOddMatchFavorite.isSelected = matchInfo?.isFavorite ?: false
        leagueOddMatchFavorite.setOnClickListener(navDetailClick)

        leagueNeutral.isSelected = matchInfo?.neutral == 1
        leagueNeutral.isVisible = matchInfo?.neutral == 1
        leagueOddMatchChart.isVisible = matchInfo?.source == MatchSource.SHOW_STATISTICS.code
        leagueOddMatchChart.setOnClickListener(navDetailClick)

        ivPlay.isVisible = matchInfo?.liveVideo == 1 && (TimeUtil.isTimeInPlay(matchInfo?.startTime))
        ivPlay.setOnClickListener(navDetailClick)

        ivAnimation.isVisible = TimeUtil.isTimeInPlay(matchInfo?.startTime)
                && !(matchInfo?.trackerId.isNullOrEmpty())
                && MultiLanguagesApplication.getInstance()?.getGameDetailAnimationNeedShow() == true
        ivAnimation.setOnClickListener(navDetailClick)
    }

    private fun navMatchDetailPage(context: Context, matchInfo: MatchInfo, matchType: MatchType) {
        SportDetailActivity.startActivity(context, matchInfo, matchType)
    }

    private fun setupMatchScore(matchInfo: MatchInfo?, matchType: MatchType) = binding.run {
        if (matchType == MatchType.IN_PLAY && matchInfo?.gameType == GameType.BB.key) {
            linearLayout.gone()
            contentBaseballStatus.root.visible()
        } else {
            linearLayout.visible()
            contentBaseballStatus.root.gone()
        }

        when (matchInfo?.gameType) {
            GameType.VB.key -> setVbScoreText(matchInfo)
            GameType.TN.key -> setTnScoreText(matchInfo)
            GameType.FT.key -> setFtScoreText(matchInfo)
            GameType.BK.key -> setBkScoreText(matchInfo)
            GameType.TT.key -> setVbScoreText(matchInfo)
            GameType.BM.key -> setBmScoreText(matchInfo)
            GameType.BB.key -> setBbScoreText(matchInfo)
            GameType.CK.key -> setCkScoreText(matchInfo)
            else -> matchInfo?.let { setBkScoreText(it) }
        }
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
        setMatchCardText(matchInfo,
            binding.leagueOddMatchCardsHome,
            binding.leagueOddMatchCardsAway,
            binding.leagueOddYellowCardsHome,
            binding.leagueOddYellowCardsAway)
    }

    private fun setFtScoreText(matchInfo: MatchInfo) {
        setScoreTextAtFront(matchInfo)
        setCardText(matchInfo)
        setFbKicks(matchInfo)
    }

    private fun setBkScoreText(matchInfo: MatchInfo) {
        setScoreTextAtFront(matchInfo)
    }

    private fun setVbScoreText(matchInfo: MatchInfo) {
        setAllScoreTextAtBottom(matchInfo)
        setScoreTextAtFront(matchInfo)
        setSptText(matchInfo)
        setCurrentPeroid(matchInfo)
        setAttack(matchInfo)
    }

    private fun setTnScoreText(matchInfo: MatchInfo) {
        setAllScoreTextAtBottom(matchInfo)
        setSptText(matchInfo)
        setTennisRoundScore(matchInfo)
        setCurrentPeroid(matchInfo)
        setAttack(matchInfo)
    }

    private fun setBmScoreText(matchInfo: MatchInfo) {
        setAllScoreTextAtBottom(matchInfo)
        setScoreTextAtFront(matchInfo)
        setSptText(matchInfo)
        setCurrentPeroid(matchInfo)
        setAttack(matchInfo)
    }

    private fun setBbScoreText(matchInfo: MatchInfo) {
        if (TimeUtil.isTimeInPlay(matchInfo.startTime)) {
            setScoreTextAtFront(matchInfo)
            setAttack(matchInfo)
            setBBStatus(matchInfo)
        } else {
            setBkScoreText(matchInfo)
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
    private inline fun setAllScoreTextAtBottom(matchInfo: MatchInfo) {
        binding.tvPeroidsScore.setMatchRoundScore(matchInfo)
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
        setMatchAttack(matchInfo, icAttackH, icAttackC, icAttackTnH, icAttackTnC)
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

    }

    /**
     * 设置网球的中间分数布局
     */
    private fun setTennisRoundScore(matchInfo: MatchInfo) = binding.run {
        //隐藏其他球类的比分
        setViewGone(leagueOddMatchScoreHome,
            leagueOddMatchScoreAway,
            icAttackTnH,
            icAttackTnC
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
            binding.root.context.getString(R.string.time_up)
        }
        matchInfo.leagueTime = (timeMillis / 1000).toInt()
    }

    private fun onTimerUpdate2(timeMillis: Long, matchInfo: MatchInfo) {
        binding.leagueOddMatchTime.text = String.format(
            binding.root.context.getString(R.string.at_start_remain_minute),
            if (timeMillis > 1000) TimeUtil.longToMinute(timeMillis) else 0)
        matchInfo.remainTime = timeMillis
    }

    fun setupMatchTimeAndStatus(matchInfo: MatchInfo,
                                matchType: MatchType) {

        if (TimeUtil.isTimeInPlay(matchInfo.startTime) ) {
            if (matchInfo.gameType == GameType.TN.key
                || !isTimerEnable(matchInfo?.gameType, matchType)
                || !needCountStatus(matchInfo.socketMatchStatus, matchInfo.leagueTime)) {
                binding.leagueOddMatchTime.gone()
                return
            }

            binding.leagueOddMatchTime.visible()
            var timeMillis = matchInfo.leagueTime?.toLong() ?: 0
            onTimerUpdate(timeMillis * 1000, matchInfo)

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
            onTimerUpdate2(remainTime, matchInfo)
            sportMatchTimer.start(1000, 1000, object : TimerTask() {
                override fun run() {
                    binding.root.post { onTimerUpdate2(--remainTime, matchInfo) }
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
        val adapter = (binding.rvLeagueOddBtnPagerMain.adapter as OddButtonPagerAdapter)
        adapter.matchType = matchType
        updateOddsButton(item, oddsType)
        binding.hIndicator.resetItemCount(adapter.itemCount)
    }

    fun updateOddsButton(item: MatchOdd, oddsType: OddsType)
    = (binding.rvLeagueOddBtnPagerMain.adapter as OddButtonPagerAdapter).run {
        setData(item.matchInfo,
            item.oddsSort,
            item.playCateNameMap,
            item.betPlayCateNameMap,
            item
        )

        stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT
        this.oddsType = oddsType
        this.odds = item.oddsMap ?: mutableMapOf()
        binding.hIndicator.resetItemCount(itemCount)
    }

    fun updateMatchInfo(matchInfo: MatchInfo?, matchType: MatchType) = binding.run {
        leagueOddMatchNameHome.text = matchInfo?.homeName
        leagueOddMatchNameAway.text = matchInfo?.awayName
        ivHomeTeamLogo.setTeamLogo(matchInfo?.homeIcon)
        ivAwayTeamLogo.setTeamLogo(matchInfo?.awayIcon)
        setupMatchScore(matchInfo, matchType)
        leagueOddMatchPlayCount.text = matchInfo?.playCateNum.toString() + "+>"
        leagueOddMatchFavorite.isSelected = matchInfo?.isFavorite ?: false
        leagueNeutral.isSelected = matchInfo?.neutral == 1
        leagueNeutral.isVisible = matchInfo?.neutral == 1

//            滚球动画 直播 显示控制
        //itemView.space2.isVisible = (item.matchInfo?.eps == 1 || item.matchInfo?.liveVideo == 1)
        ivLive.isVisible = matchInfo?.liveVideo == 1 && matchInfo?.isLive == 1
        ivPlay.isVisible = matchInfo?.liveVideo == 1 && matchInfo?.isLive != 1
        ivAnimation.isVisible = !matchInfo?.trackerId.isNullOrEmpty()
    }

    fun setupCsTextLayout(matchType: MatchType, item: MatchOdd) = binding.run {
        if (matchType == MatchType.CS) {
            llCsTextLayout.visible()
            setMatchCsLayout(item, tvCorrect1, tvCorrect2, tvCorrect3, null)
        } else {
            llCsTextLayout.gone()
        }
    }


    
}