package org.cxct.sportlottery.ui.sport.detail.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.content_baseball_status.league_odd_match_basebag
import kotlinx.android.synthetic.main.content_baseball_status.league_odd_match_bb_status
import kotlinx.android.synthetic.main.content_baseball_status.league_odd_match_halfStatus
import kotlinx.android.synthetic.main.content_baseball_status.txvOut
import kotlinx.android.synthetic.main.view_detail_head_toolbar1.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.ViewDetailHeadToolbar1Binding
import org.cxct.sportlottery.network.common.GameMatchStatus
import org.cxct.sportlottery.network.common.GameStatus
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.detail.MatchOdd
import org.cxct.sportlottery.ui.base.BindingSocketFragment
import org.cxct.sportlottery.ui.sport.SportViewModel
import org.cxct.sportlottery.ui.sport.detail.SportDetailActivity
import org.cxct.sportlottery.util.*

class SportToolBarTopFragment :
    BindingSocketFragment<SportViewModel, ViewDetailHeadToolbar1Binding>() {


    val matchInfo by lazy {
        arguments?.get("matchInfo").toString().fromJson<MatchInfo>()
    }

    override fun createRootView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val frameLayout = FrameLayout(inflater.context)
        frameLayout.layoutParams = ViewGroup.LayoutParams(-1, -1)
        frameLayout.addView(super.createRootView(inflater, container, savedInstanceState))
        return frameLayout
    }

//    val tv_match_time by lazy { binding.tvMatchTime }

    fun getTvMatchTime(): TextView {
        return binding.tvMatchTime
    }

    override fun onInitView(view: View) {
        binding.ivDetailBg.setImageResource(
            GameType.getGameTypeDetailBg(
                GameType.getGameType(matchInfo?.gameType) ?: GameType.FT
            )
        )
    }

    val handler = Handler(Looper.myLooper()!!)

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
                    binding.tvMatchTime.text = it[0]
                    tv_score.text = it[1]
                    tv_match_status.isVisible = false
                    tv_score.isVisible = true
                    binding.tvMatchTime.isVisible = true
                } else {
                    tv_match_status.isVisible = false
                    tv_score.isVisible = false
                    binding.tvMatchTime.isVisible = false
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
                    matchInfo.statusName18n + (setSptText(matchInfo))
//                    //网球，排球，乒乓，羽毛球，就不显示
//                    when(matchInfo.gameType){
//                        GameType.TN.name,GameType.VB.name,GameType.TT.name,GameType.BM.name->{
//                            "" + setSptText(matchInfo)
//                        }
//                        else->{
//                            matchInfo.statusName18n + (setSptText(matchInfo))
//                        }
//                    }
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
        setPointScore(matchInfo)
        setSptText(matchInfo)
//        setCurrentPeroid(matchInfo)
        setAttack(matchInfo)
    }

    private fun setTnScoreText(matchInfo: MatchInfo) {
        setScoreTextAtFront(matchInfo)
        setAllScoreTextAtBottom(matchInfo)
        setPointScore(matchInfo)
        setSptText(matchInfo)
//        setCurrentPeroid(matchInfo)
        setAttack(matchInfo)
    }

    private fun setBmScoreText(matchInfo: MatchInfo) {
        setScoreTextAtFront(matchInfo)
        setAllScoreTextAtBottom(matchInfo)
        setPointScore(matchInfo)
        setSptText(matchInfo)
//        setCurrentPeroid(matchInfo)
        setAttack(matchInfo)
    }

    private fun setBbScoreText(matchInfo: MatchInfo) {
        if (TimeUtil.isTimeInPlay(matchInfo.startTime)) {
            setScoreTextAtFront(matchInfo)
            setAttack(matchInfo)
            setBBStatus(matchInfo)
//            setCurrentPeroid(matchInfo)
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
                GameType.VB.key, GameType.TT.key, GameType.BM.key, GameType.TN.key ->
                    matchInfo.homeTotalScore.toStringS("0")+ "-" + matchInfo.awayTotalScore.toStringS("0")
                else ->
                    matchInfo.homeScore.toStringS("0") + "-" + matchInfo.awayScore.toStringS("0")
            }
        }
        //棒球，沙巴数据源才显示总比分
        if (matchInfo.gameType==GameType.BB.key&&matchInfo.source==2){
            tv_total_score.isVisible=true
            tv_total_score.text =  "(${matchInfo.homeTotalScore.toStringS("0")}-${matchInfo.awayTotalScore.toStringS("0")})"
        }else{
            tv_total_score.isVisible=false
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
     * 排球，兵乓球，羽球 小节分累加
     * 网球设置局比分显示
     */
    private fun setPointScore(matchInfo: MatchInfo) {
        when(matchInfo.gameType){
            GameType.VB.key,GameType.TT.key,GameType.BM.key->{
                tv_point_score.isVisible = TimeUtil.isTimeInPlay(matchInfo.startTime)
                if(matchInfo.matchStatusList.isNullOrEmpty()){
                    tv_point_score.text=""
                }else{
                    val homePoints= matchInfo.matchStatusList?.sumOf { it.homeScore?:0 }
                    val awayPoints= matchInfo.matchStatusList?.sumOf { it.awayScore?:0 }
                    tv_point_score.text = "(${homePoints}-${awayPoints})"
                }
            }
            GameType.TN.key->{
                tv_point_score.isVisible = TimeUtil.isTimeInPlay(matchInfo.startTime)
                tv_point_score.text = "(${matchInfo.homePoints.toStringS("0")}-${matchInfo.awayPoints.toStringS("0")})"
            }
        }
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
        binding.tvMatchTime.apply {
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


//    /**
//     * 设置当前盘数/局数/回合
//     * 网球显示 第x盘
//     * 其他球类显示 第x局
//     */
//    @SuppressLint("SetTextI18n")
//    private fun setCurrentPeroid(matchInfo: MatchInfo) {
//        if (matchInfo.socketMatchStatus == GameMatchStatus.HIDE_SCORE.value || matchInfo.matchStatusList.isNullOrEmpty()) {
//            with(tv_match_status) {
//                visibility = android.view.View.VISIBLE
//                text = matchInfo.statusName18n
//            }
//        } else {
//            matchInfo.matchStatusList?.let {
//                tv_match_status.visibility = View.VISIBLE
//                it.last().let {
//                    tv_match_status.text = (it.statusNameI18n?.get(
//                        LanguageManager.getSelectLanguage(context = activity).key
//                    ) ?: it.statusName) + setSptText(matchInfo)
//                }
//            }
//        }
////        tv_toolbar_match_status.isVisible = tv_match_status.isVisible
////        tv_toolbar_match_status.text = tv_match_status.text.trim()
//    }

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
    }
}