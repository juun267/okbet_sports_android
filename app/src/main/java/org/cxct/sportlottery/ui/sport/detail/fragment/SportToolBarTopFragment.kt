package org.cxct.sportlottery.ui.sport.detail.fragment

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.activity_detail_sport.app_bar_layout
import kotlinx.android.synthetic.main.activity_detail_sport.collaps_toolbar
import kotlinx.android.synthetic.main.activity_detail_sport.live_view_tool_bar
import kotlinx.android.synthetic.main.activity_detail_sport.view.vpContainer
import kotlinx.android.synthetic.main.content_baseball_status.league_odd_match_basebag
import kotlinx.android.synthetic.main.content_baseball_status.league_odd_match_bb_status
import kotlinx.android.synthetic.main.content_baseball_status.league_odd_match_halfStatus
import kotlinx.android.synthetic.main.content_baseball_status.txvOut
import kotlinx.android.synthetic.main.item_endscore_battle.ivAwayLogo
import kotlinx.android.synthetic.main.item_endscore_battle.ivHomeLogo
import kotlinx.android.synthetic.main.view_detail_head_toolbar1.*
import kotlinx.android.synthetic.main.view_toolbar_detail_collaps1.tv_toolbar_away_name
import kotlinx.android.synthetic.main.view_toolbar_detail_collaps1.tv_toolbar_home_name
import kotlinx.android.synthetic.main.view_toolbar_detail_live.iv_fullscreen
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.setFbKicks
import org.cxct.sportlottery.common.extentions.setMatchAttack
import org.cxct.sportlottery.common.extentions.setMatchRoundScore
import org.cxct.sportlottery.common.extentions.setMatchScore
import org.cxct.sportlottery.databinding.FragmentToolBarTopBinding
import org.cxct.sportlottery.network.common.GameMatchStatus
import org.cxct.sportlottery.network.common.GameStatus
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.detail.MatchOdd
import org.cxct.sportlottery.ui.base.BindingSocketFragment
import org.cxct.sportlottery.ui.sport.SportViewModel
import org.cxct.sportlottery.ui.sport.detail.SportDetailActivity
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.TimerManager
import org.cxct.sportlottery.util.fromJson
import org.cxct.sportlottery.util.needCountStatus
import org.cxct.sportlottery.util.setTeamLogo
import java.util.Timer

class SportToolBarTopFragment : BindingSocketFragment<SportViewModel, FragmentToolBarTopBinding>(),
    TimerManager {


    val matchInfo by lazy {
        arguments?.get("matchInfo").toString().fromJson<MatchInfo>()
    }
    private var isGamePause = false
    private var matchOdd: MatchOdd? = null
    private var intoLive = false

    override fun onInitView(view: View) {
        binding.toolBar.ivDetailBg.setImageResource(
            GameType.getGameTypeDetailBg(
                GameType.getGameType(matchInfo?.gameType) ?: GameType.FT
            )
        )
    }

    override var startTime: Long = 0
    override var timer: Timer = Timer()
    val handler = Handler(Looper.myLooper()!!)


    override var timerHandler: Handler = Handler(Looper.myLooper()!!) {
        var timeMillis = startTime * 1000L
        if (TimeUtil.isTimeInPlay(matchOdd?.matchInfo?.startTime)) {
            if (!isGamePause) {
                when (matchInfo?.gameType) {
                    GameType.FT.key -> {
                        timeMillis += 1000
                    }

                    GameType.BK.key, GameType.RB.key, GameType.AFT.key -> {
                        timeMillis -= 1000
                    }

                    else -> {
                    }
                }
            }
            //过滤部分球类
            if (when (matchInfo?.gameType) {
                    GameType.BB.key, GameType.TN.key, GameType.VB.key, GameType.TT.key, GameType.BM.key -> true
                    else -> {
                        false
                    }
                }
            ) {
                tv_match_time.isVisible = false
                cancelTimer()
                return@Handler false
            }
            tv_match_time?.apply {
                if (needCountStatus(
                        matchOdd?.matchInfo?.socketMatchStatus, matchOdd?.matchInfo?.leagueTime
                    )
                ) {
                    if (timeMillis >= 1000) {
                        text = TimeUtil.longToMmSs(timeMillis)
                        startTime = timeMillis / 1000L
                        isVisible = true
                    } else {
                        text = this.context.getString(R.string.time_null)
                        isVisible = false
                    }
                } else {
                    text = this.context.getString(R.string.time_null)
                    isVisible = false
                }
//                collaps_toolbar.tv_toolbar_match_time.text = text
//                collaps_toolbar.tv_toolbar_match_time.isVisible = isVisible
            }
        }
        return@Handler false
    }

    override fun onInitData() {
        super.onInitData()

        matchInfo?.let {
            setupMatchInfo(it)
        }
    }




    /**
     * 配置賽事資訊(隊伍名稱、是否延期、賽制)
     * fromApi api的状态不携带红黄牌等信息
     */
    fun setupMatchInfo(matchInfo: MatchInfo, fromApi: Boolean = false) {
        //region 隊伍名稱
        tv_home_name.text = matchInfo.homeName ?: ""
        tv_away_name.text = matchInfo.awayName ?: ""
        val activity: SportDetailActivity = activity as SportDetailActivity
        activity.apply {
            tvToolBarHomeName?.text = matchInfo.homeName ?: ""
            tvToolBarAwayName?.text = matchInfo.awayName ?: ""
            ivToolbarHomeLogo.setTeamLogo(matchInfo.homeIcon)
            ivToolbarAwayLogo.setTeamLogo(matchInfo.awayIcon)
        }
        img_home_logo.setTeamLogo(matchInfo.homeIcon)
        img_away_logo.setTeamLogo(matchInfo.awayIcon)

        //endregion
        //region 比賽延期判斷
        if (matchInfo.status == GameStatus.POSTPONED.code && (matchInfo.gameType == GameType.FT.name || matchInfo.gameType == GameType.BK.name || matchInfo.gameType == GameType.TN.name)) {
//            toolBar.tv_score.text = getString(R.string.game_postponed)
            activity.tvToolbarHomeScore.text = "-"
            activity.tvToolbarAwayScore.text = "-"
            lin_bottom.isVisible = false
            return
        }

        //赛事进行中，就显示比分状态，否则就不显示左下角，并且显示开赛时间
        var isInPlay = TimeUtil.isTimeInPlay(matchInfo.startTime)
        if (isInPlay) {
            lin_bottom.isVisible = true
            if (!fromApi) {
                setStatusText(matchInfo)
                setupMatchScore(matchInfo)
            }
        } else {
            var startDate = TimeUtil.timeFormat(matchInfo.startTime, TimeUtil.DM_HM_FORMAT)
            startDate.split(" ").let {
                if (it.size == 2) {
                    tv_match_time.text = it[0]
                    tv_score.text = it[1]
                    tv_match_status.isVisible = false
                    tv_score.isVisible = true
                    tv_match_time.isVisible = true
                } else {
                    tv_match_status.isVisible = false
                    tv_score.isVisible = false
                    tv_match_time.isVisible = false
                }
            }
            lin_bottom.isVisible = false

        }
    }




    private fun setStatusText(matchInfo: MatchInfo) {
        tv_match_status.text = when {
            (TimeUtil.isTimeInPlay(matchInfo.startTime) && matchInfo.status == GameStatus.POSTPONED.code && (matchInfo.gameType == GameType.FT.name || matchInfo.gameType == GameType.BK.name || matchInfo.gameType == GameType.TN.name)) -> {
                getString(R.string.game_postponed) + setSptText(matchInfo)
            }

            TimeUtil.isTimeInPlay(matchInfo.startTime) -> {
                if (matchInfo.statusName18n != null) {
                    //网球，排球，乒乓，羽毛球，就不显示
                    if (matchInfo.gameType == GameType.TN.name || matchInfo.gameType == GameType.VB.name || matchInfo.gameType == GameType.TT.name || matchInfo.gameType == GameType.BM.name) {
                        "" + setSptText(matchInfo)
                    } else {
                        matchInfo.statusName18n + (setSptText(matchInfo))
                    }

                } else {
                    ""
                }
            }

            else -> {
                if (TimeUtil.isTimeToday(matchInfo.startTime)) getString((R.string.home_tab_today))
                else matchInfo.startDateDisplay
            }
        }
//        tv_toolbar_match_status.text = tv_match_status.text.trim()
    }

    /**
     * 棒球同时处理
     */
    private fun setupMatchScore(matchInfo: MatchInfo) {
        when (matchInfo?.gameType) {
            GameType.BB.key -> {
                lin_tips.isVisible = false
                content_baseball_status.isVisible = true
            }

            else -> {
                lin_tips.isVisible = true
                content_baseball_status.isVisible = false
            }
        }
        when (matchInfo.gameType) {
            GameType.VB.key -> setVbScoreText(matchInfo)
            GameType.TN.key -> setTnScoreText(matchInfo)
            GameType.FT.key -> setFtScoreText(matchInfo)
            GameType.BK.key -> setBkScoreText(matchInfo)
            GameType.TT.key -> setVbScoreText(matchInfo)
            GameType.BM.key -> setBmScoreText(matchInfo)
            GameType.BB.key -> setBbScoreText(matchInfo)
            GameType.CK.key -> setCkScoreText(matchInfo)
            else -> setBkScoreText(matchInfo)
        }
    }


    private fun setFtScoreText(matchInfo: MatchInfo) {
        setScoreTextAtFront(matchInfo)
        setCardText(matchInfo)
        setFbKicks(matchInfo)
        setFtHalfScore(matchInfo)
    }

    private fun setBkScoreText(matchInfo: MatchInfo) {
        setScoreTextAtFront(matchInfo)
        setAllScoreTextAtBottom(matchInfo)
    }

    private fun setVbScoreText(matchInfo: MatchInfo) {
        setScoreTextAtFront(matchInfo)
        setAllScoreTextAtBottom(matchInfo)
        setSptText(matchInfo)
        setCurrentPeroid(matchInfo)
        setAttack(matchInfo)
    }

    private fun setTnScoreText(matchInfo: MatchInfo) {
        setScoreTextAtFront(matchInfo)
        setAllScoreTextAtBottom(matchInfo)
        setPointScore(matchInfo)
        setSptText(matchInfo)
        setCurrentPeroid(matchInfo)
        setAttack(matchInfo)
    }

    private fun setBmScoreText(matchInfo: MatchInfo) {
        setScoreTextAtFront(matchInfo)
        setAllScoreTextAtBottom(matchInfo)
        setSptText(matchInfo)
        setCurrentPeroid(matchInfo)
        setAttack(matchInfo)
    }

    private fun setBbScoreText(matchInfo: MatchInfo) {
        if (TimeUtil.isTimeInPlay(matchInfo.startTime)) {
            setScoreTextAtFront(matchInfo)
            setAttack(matchInfo)
            setBBStatus(matchInfo)
            setCurrentPeroid(matchInfo)
        } else setBkScoreText(matchInfo)
    }

    private fun setCkScoreText(matchInfo: MatchInfo) {
        setScoreTextAtFront(matchInfo)
        setAttack(matchInfo)
        setSptText(matchInfo)
    }

    private fun setFbKicks(matchInfo: MatchInfo) {
        league_corner_kicks.setFbKicks(matchInfo)
    }

    private fun setScoreTextAtFront(matchInfo: MatchInfo) {
        tv_score.apply {
            visibility = when (TimeUtil.isTimeInPlay(matchInfo.startTime)) {
                true -> View.VISIBLE
                else -> View.GONE
            }
            text = when (matchInfo.gameType) {
                GameType.VB.key, GameType.TT.key, GameType.BM.key, GameType.TN.key -> (matchInfo.homeTotalScore
                    ?: 0).toString() + " - " + (matchInfo.awayTotalScore ?: 0).toString()

                else -> (matchInfo.homeScore ?: 0).toString() + " - " + (matchInfo.awayScore
                    ?: 0).toString()
            }
        }
        setMatchScore(
            matchInfo,
            (activity as SportDetailActivity).tvToolbarHomeScore,
            (activity as SportDetailActivity).tvToolbarAwayScore,
        )
    }


    /**
     * 网球和羽毛球  排球，乒乓球 显示局比分
     */
    private fun setAllScoreTextAtBottom(matchInfo: MatchInfo) {
        tv_peroids_score.setMatchRoundScore(matchInfo)
    }

    /**
     * 网球设置局比分显示
     */
    private fun setPointScore(matchInfo: MatchInfo) {
        tv_point_score.isVisible = TimeUtil.isTimeInPlay(matchInfo.startTime)
        tv_point_score.text = "(${matchInfo.homePoints ?: "0"}-${matchInfo.awayPoints ?: "0"})"
    }


    /**
     * 棒球的特殊布局处理
     */
    private fun setBBStatus(matchInfo: MatchInfo) {
        lin_tips.isVisible = false
        content_baseball_status.isVisible = true
        league_odd_match_bb_status.isVisible = false
        league_odd_match_halfStatus.isVisible = false

        txvOut.apply {
            text = getString(
                R.string.game_out, matchInfo.outNumber ?: ""
            )
            isVisible = true
        }
        tv_match_time.apply {
            text =
                if (matchInfo.halfStatus == 0) getString(R.string.half_first_short) else getString(
                    R.string.half_second_short
                )
            isVisible = true
        }

        league_odd_match_basebag.apply {
            setImageResource(
                when {
                    matchInfo.firstBaseBag == 0 && matchInfo.secBaseBag == 0 && matchInfo.thirdBaseBag == 0 -> R.drawable.ic_bb_base_bag_0_0_0
                    matchInfo.firstBaseBag == 1 && matchInfo.secBaseBag == 0 && matchInfo.thirdBaseBag == 0 -> R.drawable.ic_bb_base_bag_1_0_0
                    matchInfo.firstBaseBag == 0 && matchInfo.secBaseBag == 1 && matchInfo.thirdBaseBag == 0 -> R.drawable.ic_bb_base_bag_0_1_0
                    matchInfo.firstBaseBag == 0 && matchInfo.secBaseBag == 0 && matchInfo.thirdBaseBag == 1 -> R.drawable.ic_bb_base_bag_0_0_1
                    matchInfo.firstBaseBag == 1 && matchInfo.secBaseBag == 1 && matchInfo.thirdBaseBag == 0 -> R.drawable.ic_bb_base_bag_1_1_0
                    matchInfo.firstBaseBag == 1 && matchInfo.secBaseBag == 0 && matchInfo.thirdBaseBag == 1 -> R.drawable.ic_bb_base_bag_1_0_1
                    matchInfo.firstBaseBag == 0 && matchInfo.secBaseBag == 1 && matchInfo.thirdBaseBag == 1 -> R.drawable.ic_bb_base_bag_0_1_1
                    matchInfo.firstBaseBag == 1 && matchInfo.secBaseBag == 1 && matchInfo.thirdBaseBag == 1 -> R.drawable.ic_bb_base_bag_1_1_1
                    else -> R.drawable.ic_bb_base_bag_0_0_0
                }
            )
            isVisible = true
        }
    }

    /**
     * 賽制(5盤3勝)
     * 只有网球，排球，乒乓球，羽毛球
     */
    @SuppressLint("SetTextI18n")
    private fun setSptText(matchInfo: MatchInfo): String {
        if (matchInfo.gameType == GameType.CK.key) {
            val homeOver = (matchInfo.homeOver ?: "0").toFloat()
            val awayOver = (matchInfo.awayOver ?: "0").toFloat()
            return when {
                homeOver > 0 -> " $homeOver"
                awayOver > 0 -> " $awayOver"
                else -> ""
            }
        }
        return ""
    }

    /**
     * 设置当前盘数/局数/回合
     * 网球显示 第x盘
     * 其他球类显示 第x局
     */
    @SuppressLint("SetTextI18n")
    private fun setCurrentPeroid(matchInfo: MatchInfo) {
        if (matchInfo.socketMatchStatus == GameMatchStatus.HIDE_SCORE.value || matchInfo.matchStatusList.isNullOrEmpty()) {
            with(tv_match_status) {
                visibility = android.view.View.VISIBLE
                text = matchInfo.statusName18n
            }
        } else {
            matchInfo.matchStatusList?.let {
                tv_match_status.visibility = View.VISIBLE
                it.last().let {
                    tv_match_status.text = (it.statusNameI18n?.get(
                        LanguageManager.getSelectLanguage(context = activity).key
                    ) ?: it.statusName) + setSptText(matchInfo)
                }
            }
        }
//        tv_toolbar_match_status.isVisible = tv_match_status.isVisible
//        tv_toolbar_match_status.text = tv_match_status.text.trim()
    }

    /**
     * 设置足球半场比分
     */
    private fun setFtHalfScore(matchInfo: MatchInfo) {
        tv_ft_half.apply {
            visibility = when {
                (!matchInfo.homeHalfScore.isNullOrEmpty()) || (!matchInfo.awayHalfScore.isNullOrEmpty()) -> View.VISIBLE
                else -> View.GONE
            }
            text =
                getString(R.string.half) + ": " + matchInfo.homeHalfScore + "-" + matchInfo.awayHalfScore
        }

    }

    /**
     * 设置足球黄牌，红牌数量
     */
    private fun setCardText(matchInfo: MatchInfo) {
        tv_red_card.apply {
            visibility = when {
                (matchInfo.homeCards ?: 0 > 0) || (matchInfo.awayCards ?: 0 > 0) -> View.VISIBLE
                else -> View.GONE
            }
            text =
                (matchInfo.homeCards ?: 0).toString() + "-" + (matchInfo.awayCards ?: 0).toString()
        }

        tv_yellow_card.apply {
            visibility = when {
                (matchInfo.homeYellowCards ?: 0 > 0) || (matchInfo.awayYellowCards ?: 0 > 0) -> View.VISIBLE
                else -> View.GONE
            }
            text = (matchInfo.homeYellowCards ?: 0).toString() + "-" + (matchInfo.awayYellowCards
                ?: 0).toString()
        }

    }

    /**
     * 设置球权标识，
     *  目前支持 棒球，网球，排球，乒乓球，羽毛球
     *  其中网球标识是另外一个位置
     */
    private fun setAttack(matchInfo: MatchInfo) {
        setMatchAttack(matchInfo, ic_attack_h, ic_attack_c, ic_attack_h, ic_attack_c)
        if (TimeUtil.isTimeInPlay(matchInfo.startTime)) {
            when (matchInfo.gameType) {
                GameType.BB.key,
                GameType.VB.key,
                GameType.TT.key,
                GameType.BM.key,
                GameType.CK.key,
                GameType.TN.key,
                -> {
                    if (matchInfo.attack.equals("H")) {
                        ic_attack_h.visibility = View.VISIBLE
                        ic_attack_c.visibility = View.INVISIBLE
                    } else {
                        ic_attack_h.visibility = View.INVISIBLE
                        ic_attack_c.visibility = View.VISIBLE
                    }
                }

                else -> {
                    ic_attack_h.visibility = View.GONE
                    ic_attack_c.visibility = View.GONE
                }
            }
        } else {
            ic_attack_h.visibility = View.GONE
            ic_attack_c.visibility = View.GONE
        }
    }


    override fun onResume() {
        super.onResume()
        startTimer()
//        isLogin = viewModel.loginRepository.isLogin.value == true
//        live_view_tool_bar.initLoginStatus(isLogin)
//        live_view_tool_bar.startPlayer()
    }


    override fun onPause() {
        super.onPause()
        cancelTimer()
    }



}