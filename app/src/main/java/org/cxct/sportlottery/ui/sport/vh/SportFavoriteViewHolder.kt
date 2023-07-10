package org.cxct.sportlottery.ui.sport.vh

import android.annotation.SuppressLint
import android.content.Context
import android.text.Html
import android.text.Spanned
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import kotlinx.android.synthetic.main.content_baseball_status.view.*
import kotlinx.android.synthetic.main.item_sport_favorite.view.*
import kotlinx.android.synthetic.main.item_sport_favorite.view.content_baseball_status
import kotlinx.android.synthetic.main.item_sport_favorite.view.hIndicator
import kotlinx.android.synthetic.main.item_sport_favorite.view.ic_attack_c
import kotlinx.android.synthetic.main.item_sport_favorite.view.ic_attack_h
import kotlinx.android.synthetic.main.item_sport_favorite.view.ic_attack_tn_c
import kotlinx.android.synthetic.main.item_sport_favorite.view.ic_attack_tn_h
import kotlinx.android.synthetic.main.item_sport_favorite.view.iv_animation
import kotlinx.android.synthetic.main.item_sport_favorite.view.iv_away_team_logo
import kotlinx.android.synthetic.main.item_sport_favorite.view.iv_home_team_logo
import kotlinx.android.synthetic.main.item_sport_favorite.view.iv_live
import kotlinx.android.synthetic.main.item_sport_favorite.view.iv_play
import kotlinx.android.synthetic.main.item_sport_favorite.view.league_corner_kicks
import kotlinx.android.synthetic.main.item_sport_favorite.view.league_neutral
import kotlinx.android.synthetic.main.item_sport_favorite.view.league_odd_match_cards_away
import kotlinx.android.synthetic.main.item_sport_favorite.view.league_odd_match_cards_home
import kotlinx.android.synthetic.main.item_sport_favorite.view.league_odd_match_chart
import kotlinx.android.synthetic.main.item_sport_favorite.view.league_odd_match_favorite
import kotlinx.android.synthetic.main.item_sport_favorite.view.league_odd_match_name_away
import kotlinx.android.synthetic.main.item_sport_favorite.view.league_odd_match_name_home
import kotlinx.android.synthetic.main.item_sport_favorite.view.league_odd_match_play_count
import kotlinx.android.synthetic.main.item_sport_favorite.view.league_odd_match_point_away_bottom
import kotlinx.android.synthetic.main.item_sport_favorite.view.league_odd_match_point_home_bottom
import kotlinx.android.synthetic.main.item_sport_favorite.view.league_odd_match_score_away
import kotlinx.android.synthetic.main.item_sport_favorite.view.league_odd_match_score_away_bottom
import kotlinx.android.synthetic.main.item_sport_favorite.view.league_odd_match_score_home
import kotlinx.android.synthetic.main.item_sport_favorite.view.league_odd_match_score_home_bottom
import kotlinx.android.synthetic.main.item_sport_favorite.view.league_odd_match_status
import kotlinx.android.synthetic.main.item_sport_favorite.view.league_odd_match_time
import kotlinx.android.synthetic.main.item_sport_favorite.view.league_odd_match_total_score_away_bottom
import kotlinx.android.synthetic.main.item_sport_favorite.view.league_odd_match_total_score_home_bottom
import kotlinx.android.synthetic.main.item_sport_favorite.view.league_odd_yellow_cards_away
import kotlinx.android.synthetic.main.item_sport_favorite.view.league_odd_yellow_cards_home
import kotlinx.android.synthetic.main.item_sport_favorite.view.league_spt
import kotlinx.android.synthetic.main.item_sport_favorite.view.lin_away_round_score
import kotlinx.android.synthetic.main.item_sport_favorite.view.lin_home_round_score
import kotlinx.android.synthetic.main.item_sport_favorite.view.lin_match
import kotlinx.android.synthetic.main.item_sport_favorite.view.linear_layout
import kotlinx.android.synthetic.main.item_sport_favorite.view.ll_cs_text_layout
import kotlinx.android.synthetic.main.item_sport_favorite.view.rv_league_odd_btn_pager_main
import kotlinx.android.synthetic.main.item_sport_favorite.view.tv_correct_1
import kotlinx.android.synthetic.main.item_sport_favorite.view.tv_correct_2
import kotlinx.android.synthetic.main.item_sport_favorite.view.tv_correct_3
import kotlinx.android.synthetic.main.item_sport_favorite.view.tv_peroid
import kotlinx.android.synthetic.main.item_sport_favorite.view.tv_peroids_score
import kotlinx.android.synthetic.main.item_sport_odd.view.*
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.enums.MatchSource
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.odds.list.TimeCounting
import org.cxct.sportlottery.ui.sport.common.LeagueOddListener
import org.cxct.sportlottery.ui.sport.common.OddButtonListener
import org.cxct.sportlottery.ui.sport.common.OddButtonPagerAdapter
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.setLeagueLogo
import org.cxct.sportlottery.util.setTeamLogo
import org.cxct.sportlottery.view.layoutmanager.CustomLinearLayoutManager
import org.cxct.sportlottery.view.overScrollView.OverScrollDecoratorHelper

class SportFavoriteViewHolder constructor(
    itemView: View,
) : ViewHolderTimer(itemView) {

    private val oddButtonPagerAdapter by lazy {
        OddButtonPagerAdapter(itemView.context)
    }

    init {
        itemView.rv_league_odd_btn_pager_main.run {
            layoutManager = CustomLinearLayoutManager(itemView.context,
                LinearLayoutManager.HORIZONTAL,
                false).apply {
                isAutoMeasureEnabled = false
            }

            oddButtonPagerAdapter.stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT
            adapter = oddButtonPagerAdapter
            setHasFixedSize(true)
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

            OverScrollDecoratorHelper.setUpOverScroll(this,
                OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL)
            itemView.hIndicator.bindRecyclerView(this)
        }
    }

    fun bind(
        matchType: MatchType,
        item: MatchOdd,
        leagueOddListener: LeagueOddListener?,
        isTimerEnable: Boolean,
        oddsType: OddsType,
        matchInfoList: List<MatchInfo>,
    ) {
        resetStatusView()
        setupMatchInfo(item, matchType, matchInfoList, leagueOddListener)
        val isTimerPause = item.matchInfo?.stopped == TimeCounting.STOP
        item.matchInfo?.let {
            setupMatchTimeAndStatus(
                it,
                isTimerEnable,
                isTimerPause,
            )
        }

        setupOddsButton(matchType,
            item,
            oddsType,
            leagueOddListener)

        //setupQuickCategory(item, oddsType, leagueOddListener)
    }

    // region update functions
    fun update(
        matchType: MatchType,
        item: MatchOdd,
        leagueOddListener: LeagueOddListener?,
        isTimerEnable: Boolean,
        oddsType: OddsType,
    ) {
        updateMatchInfo(item, matchType)
        val isTimerPause = item.matchInfo?.stopped == TimeCounting.STOP
        item.matchInfo?.let {
            setupMatchTimeAndStatus(
                it,
                isTimerEnable,
                isTimerPause,
            )
        }
        updateOddsButton(item, oddsType)
        setupCsTextLayout(matchType, item, leagueOddListener)
    }


    private fun setupCsTextLayout(
        matchType: MatchType,
        item: MatchOdd,
        leagueOddListener: LeagueOddListener?,
    ) {
        itemView.apply {
            if (matchType == MatchType.CS) {
                ll_cs_text_layout.isVisible = true
                setMatchCsLayout(item, tv_correct_1, tv_correct_2, tv_correct_3, leagueOddListener)
            } else {
                ll_cs_text_layout.isVisible = false
            }
        }
    }

    private fun <K, V> Map<K, V>?.getPlayCateName(context: Context): String {
        var selectLanguage = LanguageManager.getSelectLanguage(context)
        val playCateName = this?.get<Any?, V>(selectLanguage.key) ?: this?.get<Any?, V>(
            LanguageManager.Language.EN.key)
        return playCateName.toString()
    }

    private fun String.updatePlayCateColor(): Spanned {
        val color = if (MultiLanguagesApplication.isNightMode) "#a3a3a3"
        else "#6C7BA8"

        return Html.fromHtml(
            when {
                (this.contains("\n")) -> {
                    val strSplit = this.split("\n")
                    "<font color=$color>${strSplit.first()}</font><br><font color=#b73a20>${
                        strSplit.getOrNull(
                            1
                        )
                    }</font>"
                }
                else -> {
                    "<font color=$color>${this}</font>"
                }
            }
        )
    }

    fun updateByBetInfo(
        item: MatchOdd,
        oddsType: OddsType,
        leagueOddListener: LeagueOddListener?,
    ) {
        updateOddsButtonByBetInfo(item)
    }

    fun updateByPlayCate(
        item: MatchOdd,
        oddsType: OddsType,
    ) {
        updateOddsButton(item, oddsType)
    }

    private fun updateMatchInfo(item: MatchOdd, matchType: MatchType) {
        itemView.league_odd_match_name_home.text = item.matchInfo?.homeName
        itemView.league_odd_match_name_away.text = item.matchInfo?.awayName
        itemView.iv_home_team_logo.setTeamLogo(item.matchInfo?.homeIcon)
        itemView.iv_away_team_logo.setTeamLogo(item.matchInfo?.awayIcon)
        setupMatchScore(item, matchType)
        itemView.league_odd_match_play_count.text =
            item.matchInfo?.playCateNum.toString() + "+>"
        itemView.league_odd_match_favorite.isSelected = item.matchInfo?.isFavorite ?: false
//            itemView.league_odd_match_price_boost.isVisible = item.matchInfo?.eps == 1
        itemView.league_neutral.apply {
            isSelected = item.matchInfo?.neutral == 1
            isVisible = item.matchInfo?.neutral == 1
        }
//            滚球动画 直播 显示控制
        //itemView.space2.isVisible = (item.matchInfo?.eps == 1 || item.matchInfo?.liveVideo == 1)
        if (item.matchInfo?.liveVideo == 1) {
            if (item.matchInfo?.isLive == 1) {
                itemView.iv_live.isVisible = true
                itemView.iv_play.isVisible = false
            } else {
                itemView.iv_live.isVisible = false
                itemView.iv_play.isVisible = true
            }
        } else {
            itemView.iv_live.isVisible = false
            itemView.iv_play.isVisible = false
        }
        itemView.iv_animation.isVisible =
            !(item.matchInfo?.trackerId.isNullOrEmpty()) && MultiLanguagesApplication.getInstance()
                ?.getGameDetailAnimationNeedShow() == true
    }

    private fun setupMatchInfo(
        item: MatchOdd,
        matchType: MatchType,
        matchInfoList: List<MatchInfo>,
        leagueOddListener: LeagueOddListener?,
    ) {
        itemView.league_text.text = item.matchInfo?.leagueName
        itemView.iv_country.setLeagueLogo(item.matchInfo?.categoryIcon)
        itemView.league_odd_match_name_home.text = item.matchInfo?.homeName

        itemView.league_odd_match_name_away.text = item.matchInfo?.awayName
        itemView.iv_home_team_logo.setTeamLogo(item.matchInfo?.homeIcon)
        itemView.iv_away_team_logo.setTeamLogo(item.matchInfo?.awayIcon)

        setupMatchScore(item, matchType)

        itemView.league_odd_match_play_count.apply {
            text = item.matchInfo?.playCateNum.toString() + "+>"

            setOnClickListener {
                leagueOddListener?.onClickPlayType(
                    item.matchInfo?.id,
                    matchInfoList,
                    if (TimeUtil.isTimeInPlay(item.matchInfo?.startTime)) MatchType.IN_PLAY else matchType,
                    item.matchInfo?.liveVideo ?: 0
                )
            }
        }
        itemView.league_odd_match_favorite.isSelected = item.matchInfo?.isFavorite ?: false
        itemView.league_odd_match_favorite.apply {
            isSelected = item.matchInfo?.isFavorite ?: false
            setOnClickListener {
                leagueOddListener?.onClickFavorite(item.matchInfo?.id)
            }
        }

        itemView.league_neutral.apply {
            isSelected = item.matchInfo?.neutral == 1
            isVisible = item.matchInfo?.neutral == 1
        }

        itemView.league_odd_match_chart.apply {
            visibility =
                if (item.matchInfo?.source == MatchSource.SHOW_STATISTICS.code) View.VISIBLE else View.GONE

            setOnClickListener {
                leagueOddListener?.onClickStatistics(item.matchInfo?.id)
            }
        }

        itemView.lin_match.setOnClickListener {
            leagueOddListener?.onClickPlayType(
                item.matchInfo?.id,
                matchInfoList,
                if (TimeUtil.isTimeInPlay(item.matchInfo?.startTime)) MatchType.IN_PLAY else matchType,
                item.matchInfo?.liveVideo ?: 0
            )
        }

//            itemView.league_odd_match_price_boost.isVisible = item.matchInfo?.eps == 1
        //itemView.space2.isVisible = (item.matchInfo?.eps == 1 || item.matchInfo?.liveVideo == 1)
        with(itemView.iv_play) {
            isVisible =
                item.matchInfo?.liveVideo == 1 && (TimeUtil.isTimeInPlay(item.matchInfo?.startTime))

            setOnClickListener {
                leagueOddListener?.onClickLiveIconListener(
                    item.matchInfo?.id,
                    matchInfoList,
                    if (TimeUtil.isTimeInPlay(item.matchInfo?.startTime)) MatchType.IN_PLAY else matchType,
                    item.matchInfo?.liveVideo ?: 0
                )
            }
        }
        with(itemView.iv_animation) {
            isVisible =!(item.matchInfo?.trackerId.isNullOrEmpty()) && MultiLanguagesApplication.getInstance()
                    ?.getGameDetailAnimationNeedShow() == true
            setOnClickListener {
                leagueOddListener?.onClickAnimationIconListener(
                    item.matchInfo?.id,
                    matchInfoList,
                    if (TimeUtil.isTimeInPlay(item.matchInfo?.startTime)) MatchType.IN_PLAY else matchType,
                    item.matchInfo?.liveVideo ?: 0
                )
            }
        }

    }

    private fun setupMatchScore(item: MatchOdd, matchType: MatchType) {
        itemView.apply {
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
        }

        when (item.matchInfo?.socketMatchStatus) {
            //20220507 status:999 邏輯變更 隱藏分數 -> 賽事狀態變為滾球
            /*GameMatchStatus.HIDE_SCORE.value -> {
                hideMatchScoreText()
            }*/
            else -> {
                when (item.matchInfo?.gameType) {
                    GameType.VB.key -> setVbScoreText(item.matchInfo)
                    GameType.TN.key -> setTnScoreText(item.matchInfo)
                    GameType.FT.key -> setFtScoreText(item.matchInfo)
                    GameType.BK.key -> setBkScoreText(item.matchInfo)
                    GameType.TT.key -> setVbScoreText(item.matchInfo)
                    GameType.BM.key -> setBmScoreText(item.matchInfo)
                    GameType.BB.key -> setBbScoreText(item.matchInfo)
                    GameType.CK.key -> setCkScoreText(item.matchInfo)
                    else -> item.matchInfo?.let { setBkScoreText(it) }
                }
            }
        }
    }

    private fun resetStatusView() {
        itemView.apply {
            league_neutral.isVisible = false
            league_corner_kicks.isVisible = false
            league_spt.isVisible = false
            tv_peroid.isVisible = false
            content_baseball_status.isVisible = false
            ic_attack_h.isVisible = false
            ic_attack_c.isVisible = false
            ic_attack_tn_h.isInvisible = false
            ic_attack_tn_c.isInvisible = false
            lin_home_round_score.isInvisible = false
            lin_away_round_score.isInvisible = false
            league_odd_yellow_cards_home.isVisible = false
            league_odd_yellow_cards_away.isVisible = false
            league_odd_match_cards_home.isVisible = false
            league_odd_match_cards_away.isVisible = false
            tv_peroids_score.isVisible = false
            iv_live.isVisible = false
            iv_play.isVisible = false
            iv_animation.isVisible = false
        }
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
        } else
            setBkScoreText(matchInfo)
    }

    private fun setCkScoreText(matchInfo: MatchInfo) {
        setScoreTextAtFront(matchInfo)
        setAttack(matchInfo)
        setSptText(matchInfo)
    }

    /**
     * 賽制(5盤3勝)
     * 只有网球，排球，乒乓球，羽毛球
     */
    @SuppressLint("SetTextI18n")
    private fun setSptText(matchInfo: MatchInfo) {
        itemView.league_spt.setMatchSptText(matchInfo)
    }

    /**
     * 设置当前盘数/局数/回合
     * 网球显示 第x盘
     * 其他球类显示 第x局
     */
    @SuppressLint("SetTextI18n")
    private fun setCurrentPeroid(matchInfo: MatchInfo) {
        itemView.tv_peroid.setMatchCurrentPeroid(matchInfo)
    }

    /**
     * 设置足球黄牌，红牌数量
     */
    private fun setCardText(matchInfo: MatchInfo) {
        itemView.apply {
            setMatchCardText(matchInfo,
                league_odd_match_cards_home,
                league_odd_match_cards_away,
                league_odd_yellow_cards_home,
                league_odd_yellow_cards_away)
        }
    }

    /**
     * 设置球权标识，
     *  目前支持 棒球，网球，排球，乒乓球，羽毛球
     *  其中网球标识是另外一个位置
     */
    private fun setAttack(matchInfo: MatchInfo) {
        itemView.apply {
            setMatchAttack(matchInfo, ic_attack_h, ic_attack_c, ic_attack_tn_h, ic_attack_tn_c)
        }
    }

    private fun setFbKicks(matchInfo: MatchInfo) {
        itemView.league_corner_kicks.setFbKicks(matchInfo)
    }

    private fun setScoreTextAtFront(matchInfo: MatchInfo) {
        itemView.apply {
            setMatchScore(matchInfo = matchInfo,
                tvHomeScore = league_odd_match_score_home,
                tvAwayScore = league_odd_match_score_away)
        }
    }

    /**
     * 网球和羽毛球  排球，乒乓球 显示局比分
     */
    private fun setAllScoreTextAtBottom(matchInfo: MatchInfo) {
        itemView.tv_peroids_score.setMatchRoundScore(matchInfo)
    }

    /**
     * 设置网球的中间分数布局
     */
    private fun setTennisRoundScore(matchInfo: MatchInfo) {
        //隐藏其他球类的比分
        itemView.apply {
            league_odd_match_score_home.visibility = View.GONE
            league_odd_match_score_away.visibility = View.GONE
            ic_attack_h.visibility = View.GONE
            ic_attack_c.visibility = View.GONE
            lin_home_round_score.visibility = View.VISIBLE
            lin_away_round_score.visibility = View.VISIBLE
            //设置盘比，局比，分数
            setTNRoundScore(matchInfo = matchInfo,
                tvHomeTotalScore = league_odd_match_total_score_home_bottom,
                tvAwayTotalScore = league_odd_match_total_score_away_bottom,
                tvHomeScore = league_odd_match_score_home_bottom,
                tvAwayScore = league_odd_match_score_away_bottom,
                tvHomePoints = league_odd_match_point_home_bottom,
                tvAwayPoints = league_odd_match_point_away_bottom)
        }
    }

    private fun setupMatchTimeAndStatus(
        matchInfo: MatchInfo,
        isTimerEnable: Boolean,
        isTimerPause: Boolean,
    ) {
        itemView.apply {
            setMatchTimeAndStatus(matchInfo,
                league_odd_match_time,
                league_odd_match_status,
                this@SportFavoriteViewHolder,
                isTimerEnable,
                isTimerPause)
        }
    }

    /**
     * 棒球的特殊布局处理
     */
    private fun setBBStatus(matchInfo: MatchInfo) {
        itemView.apply {
            linear_layout.isVisible = false
            content_baseball_status.isVisible = true
            setBBStatusView(matchInfo = matchInfo,
                tvBBStatus = league_odd_match_bb_status,
                txvOut = txvOut,
                ivHalfStatus = league_odd_match_halfStatus,
                ivBaseBag = league_odd_match_basebag)
        }
    }

    private fun setupOddsButton(
        matchType: MatchType,
        item: MatchOdd,
        oddsType: OddsType,
        leagueOddListener: LeagueOddListener?,
    ) = oddButtonPagerAdapter.run {

        this.matchType = matchType
        this.listener =
            OddButtonListener { view, matchInfo, odd, playCateCode, betPlayCateName ->
                leagueOddListener?.onClickBet(
                    view,
                    matchInfo,
                    odd,
                    playCateCode,
                    betPlayCateName,
                    item.betPlayCateNameMap
                )
            }

        updateOddsButton(item, oddsType)
    }

    private fun updateOddsButton(
        item: MatchOdd,
        oddsType: OddsType,
    ) {
        itemView.rv_league_odd_btn_pager_main.apply {
            oddButtonPagerAdapter.setData(
                item.matchInfo,
                item.oddsSort,
                item.playCateNameMap,
                item.betPlayCateNameMap,
                item
            )
            oddButtonPagerAdapter.apply {
                stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT
                this.oddsType = oddsType
                this.odds = item.oddsMap ?: mutableMapOf()
//                    update()
                //notifyDataSetChanged() // TODO
            }
        }
    }

    private fun updateOddsButtonByBetInfo(item: MatchOdd) {
        oddButtonPagerAdapter.odds = item.oddsMap ?: mutableMapOf()
    }

}