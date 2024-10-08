package org.cxct.sportlottery.ui.sport.detail.fragment

import android.view.View
import androidx.core.view.isVisible
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.ViewDetailHeadToolbar1Binding
import org.cxct.sportlottery.network.common.ESportType
import org.cxct.sportlottery.network.common.GameStatus
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.sport.SportViewModel
import org.cxct.sportlottery.ui.sport.detail.SportDetailActivity
import org.cxct.sportlottery.util.*

class SportToolBarTopFragment :
    BaseSocketFragment<SportViewModel, ViewDetailHeadToolbar1Binding>() {

    private var isInited = false
    private val matchInfo by lazy {
        arguments?.get("matchInfo").toString().fromJson<MatchInfo>()
    }

    private var newMatchInfo: MatchInfo? = null

    private var timeText: String? = null
    private var timeEnable = true

    fun updateMatchTime(time: String) {
        timeText = time
        if (isInited) {
            binding.tvMatchTime.text = timeText
        }
    }

    fun setMatchTimeEnable(enable: Boolean) {
        timeEnable = enable
        if (isInited) {
            binding.tvMatchTime.isVisible = enable
        }
    }

    override fun onInitView(view: View) {
        isInited = true
        binding.ivDetailBg.setImageResource(
            if (matchInfo?.gameType==GameType.ES.key){
                ESportType.getESportImg(matchInfo?.categoryCode?:ESportType.ALL.key)
            }else{
                GameType.getGameTypeDetailBg(
                    GameType.getGameType(matchInfo?.gameType) ?: GameType.FT
                )
            }

        )
    }

    override fun onInitData() {
        super.onInitData()
        if (newMatchInfo != null) {
            setupMatchInfo(newMatchInfo!!)
        } else {
            matchInfo?.let {
                setupMatchInfo(it)
            }
        }

        timeText?.let {
            binding.tvMatchTime.text = it
            binding.tvMatchTime.isVisible = timeEnable
        }
    }

    fun updateMatchInfo(matchInfo: MatchInfo, fromApi: Boolean = false) {
        if (isInited) {
            setupMatchInfo(matchInfo, fromApi)
        } else {
            newMatchInfo = matchInfo
        }
    }


    /**
     * 配置賽事資訊(隊伍名稱、是否延期、賽制)
     * fromApi api的状态不携带红黄牌等信息
     */
    private fun setupMatchInfo(matchInfo: MatchInfo, fromApi: Boolean = false)=binding.run {
        //region 隊伍名稱
        tvHomeName.text = matchInfo.homeName ?: ""
        tvAwayName.text = matchInfo.awayName ?: ""
        val activity: SportDetailActivity = activity as SportDetailActivity
        activity.apply {
            tvToolBarHomeName?.text = matchInfo.homeName ?: ""
            tvToolBarAwayName?.text = matchInfo.awayName ?: ""
            ivToolbarHomeLogo.setTeamLogo(matchInfo.homeIcon)
            ivToolbarAwayLogo.setTeamLogo(matchInfo.awayIcon)
        }
        imgHomeLogo.setTeamLogo(matchInfo.homeIcon)
        imgAwayLogo.setTeamLogo(matchInfo.awayIcon)
        //endregion
        //region 比賽延期判斷
        if (matchInfo.status == GameStatus.POSTPONED.code && (matchInfo.gameType == GameType.FT.name || matchInfo.gameType == GameType.BK.name || matchInfo.gameType == GameType.TN.name)) {
//            toolBar.tv_score.text = getString(R.string.game_postponed)
            activity.tvToolbarHomeScore.text = "-"
            activity.tvToolbarAwayScore.text = "-"
            linBottom.isVisible = false
        }

        //赛事进行中，就显示比分状态，否则就不显示左下角，并且显示开赛时间
        var isInPlay = TimeUtil.isTimeInPlay(matchInfo.startTime)
        activity.tvToolbarNoStart.isVisible = !isInPlay
        activity.tvToolbarHomeScore.isVisible = isInPlay
        activity.tvToolbarAwayScore.isVisible = isInPlay
        if (isInPlay) {
            if (!fromApi) {
                setStatusText(matchInfo)
                setupMatchScore(matchInfo)
            }
        } else {
            var startDate = TimeUtil.timeFormat(matchInfo.startTime, TimeUtil.DM_HM_FORMAT)
            startDate.split(" ").let {
                if (it.size == 2) {
                    binding.tvMatchTime.text = it[0]
                    tvScore.text = it[1]
                    tvMatchStatus.isVisible = false
                    tvScore.isVisible = true
                    binding.tvMatchTime.isVisible = true
                } else {
                    tvMatchStatus.isVisible = false
                    tvScore.isVisible = false
                    binding.tvMatchTime.isVisible = false
                }
            }
            linBottom.isVisible = false

        }
    }


    private fun setStatusText(matchInfo: MatchInfo) {
        binding.tvMatchStatus.text = when {
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
                binding.linTips.isVisible = false
                binding.contentBaseballStatus.root.isVisible = true
            }

            else -> {
                binding.linTips.isVisible = true
                binding.contentBaseballStatus.root.isVisible = false
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
            GameType.IH.key -> setIHScoreText(matchInfo)
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

    private fun setIHScoreText(matchInfo: MatchInfo) {
        setScoreTextAtFront(matchInfo)
        setAllScoreTextAtBottom(matchInfo)
        setAttack(matchInfo)
    }

    private fun setFbKicks(matchInfo: MatchInfo) {
        binding.leagueCornerKicks.setFbKicks(matchInfo)
    }

    private fun setScoreTextAtFront(matchInfo: MatchInfo) {
        binding.tvScore.apply {
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
            binding.tvTotalScore.isVisible=true
            binding.tvTotalScore.text =  "(${matchInfo.homeTotalScore.toStringS("0")}-${matchInfo.awayTotalScore.toStringS("0")})"
        }else{
            binding.tvTotalScore.isVisible=false
        }
        setMatchScore(
            matchInfo,
            (activity as SportDetailActivity).tvToolbarHomeScore,
            (activity as SportDetailActivity).tvToolbarAwayScore,
            includeTennis = true
        )
    }


    /**
     * 网球和羽毛球  排球，乒乓球 显示局比分
     */
    private fun setAllScoreTextAtBottom(matchInfo: MatchInfo) {
        binding.tvPeroidsScore.setMatchRoundScore(matchInfo)
    }

    /**
     * 排球，兵乓球，羽球 小节分累加
     * 网球设置局比分显示
     */
    private fun setPointScore(matchInfo: MatchInfo) {
        when(matchInfo.gameType){
            GameType.VB.key,GameType.TT.key,GameType.BM.key->{
                binding.tvPointScore.isVisible = TimeUtil.isTimeInPlay(matchInfo.startTime)
                if(matchInfo.matchStatusList.isNullOrEmpty()){
                    binding.tvPointScore.text=""
                }else{
                    val homePoints= matchInfo.matchStatusList?.sumOf { it.homeScore?:0 }
                    val awayPoints= matchInfo.matchStatusList?.sumOf { it.awayScore?:0 }
                    binding.tvPointScore.text = "(${homePoints}-${awayPoints})"
                }
            }
            GameType.TN.key->{
                binding.tvPointScore.isVisible = TimeUtil.isTimeInPlay(matchInfo.startTime)
                binding.tvPointScore.text = "(${matchInfo.homePoints.toStringS("0")}-${matchInfo.awayPoints.toStringS("0")})"
            }
        }
    }


    /**
     * 棒球的特殊布局处理
     */
    private fun setBBStatus(matchInfo: MatchInfo)=binding.run {
        linTips.isVisible = false
        contentBaseballStatus.root.isVisible = true
        contentBaseballStatus.leagueOddMatchBbStatus.isVisible = false
        contentBaseballStatus.leagueOddMatchHalfStatus.isVisible = false

        contentBaseballStatus.txvOut.apply {
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

        contentBaseballStatus.leagueOddMatchBasebag.apply {
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
        binding.tvFtHalf.apply {
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
        binding.tvRedCard.apply {
            visibility = when {
                (matchInfo.homeCards ?: 0 > 0) || (matchInfo.awayCards ?: 0 > 0) -> View.VISIBLE
                else -> View.GONE
            }
            text =
                (matchInfo.homeCards ?: 0).toString() + "-" + (matchInfo.awayCards ?: 0).toString()
        }

        binding.tvYellowCard.apply {
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
    private fun setAttack(matchInfo: MatchInfo)=binding.run {
        setMatchAttack(matchInfo, icAttackH, icAttackC, icAttackH, icAttackC)
    }
}