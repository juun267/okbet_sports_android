package org.cxct.sportlottery.common.extentions

import android.annotation.SuppressLint
import android.content.Context
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.util.*
import timber.log.Timber


/**
 * 賽制(5盤3勝)
 * 只有网球，排球，乒乓球，羽毛球
 */
@SuppressLint("SetTextI18n")
fun TextView.setMatchSptText(matchInfo: MatchInfo) {
    if (matchInfo.gameType == GameType.CK.key) {
        visibility = View.VISIBLE
        text = when (matchInfo.attack){
            "H"->" ${matchInfo.homeOver.toFloatS()}"
            "C"->" ${matchInfo.awayOver.toFloatS()}"
            else -> ""
        }
        return
    }
    matchInfo.spt?.let {
        if (it == 3 || it == 5 || it == 7) {
            visibility = View.VISIBLE
            text = when (it) {
                3 -> {
                    when (matchInfo.gameType) {
                        ////排球，乒乓球显示3局2胜
                        GameType.BM.key -> context.getString(R.string.spt_number_3_2_bm)
                        else -> context.getString(R.string.spt_number_3_2)
                    }
                }
                5 -> {
                    when (matchInfo.gameType) {
                        //排球，乒乓球显示5局3胜
                        GameType.VB.key, GameType.TT.key -> context.getString(R.string.spt_number_5_3_vb)
                        else -> context.getString(R.string.spt_number_5_3)
                    }
                }
                7 -> {
                    //部分乒乓球会采用七局四胜制
                    when (matchInfo.gameType) {
                        GameType.TT.key -> context.getString(R.string.spt_number_7_4_tt)
                        else -> ""
                    }
                }
                else -> ""
            }
        } else {
            visibility = View.GONE
        }
    }
}

/**
 * 设置当前盘数/局数/回合
 * 网球显示 第x盘
 * 其他球类显示 第x局
 */
@SuppressLint("SetTextI18n")
fun TextView.setMatchCurrentPeroid(matchInfo: MatchInfo) {
    visibility = View.VISIBLE
    if (matchInfo.socketMatchStatus == GameMatchStatus.HIDE_SCORE.value || matchInfo.matchStatusList.isNullOrEmpty()) {
        text = matchInfo.statusName18n
    } else {
        matchInfo.matchStatusList?.let { it ->
            it.last()?.let {
                text = it.statusNameI18n?.get(
                    LanguageManager.getSelectLanguage(context = context).key
                ) ?: it.statusName
            }
        }
    }
}

/**
 * 设置比赛比分
 * includeTennis 是否包含网球，兼容赛事详情头部比分显示
 */
fun setMatchScore(matchInfo: MatchInfo, tvHomeScore: TextView, tvAwayScore: TextView,includeTennis: Boolean = false) {
    val showTotalScoreType = mutableListOf(GameType.VB.key, GameType.TT.key, GameType.BM.key, GameType.BB.key).apply { if(includeTennis)  add(GameType.TN.key) }
    tvHomeScore.apply {
        visibility = when (TimeUtil.isTimeInPlay(matchInfo.startTime)) {
            true -> View.VISIBLE
            else -> View.GONE
        }
        text = when {
            showTotalScoreType.contains(matchInfo.gameType) -> (matchInfo.homeTotalScore
                ?: 0).toString()
            else -> (matchInfo.homeScore ?: 0).toString()
        }
    }
    tvAwayScore.apply {
        visibility = when (TimeUtil.isTimeInPlay(matchInfo.startTime)) {
            true -> View.VISIBLE
            else -> View.GONE
        }
        text = when {
            showTotalScoreType.contains(matchInfo.gameType) -> (matchInfo.awayTotalScore
                ?: 0).toString()
            else -> (matchInfo.awayScore ?: 0).toString()
        }
    }
}

/**
 * 网球和羽毛球  排球，乒乓球 显示局比分
 */
fun TextView.setMatchRoundScore(matchInfo: MatchInfo) {
    isVisible = true
    if (matchInfo.socketMatchStatus == GameMatchStatus.HIDE_SCORE.value) {
        val spanny = Spanny()
        matchInfo.matchStatusList?.forEachIndexed { index, it ->
            val spanScore = "${it.homeScore ?: 0}-${it.awayScore ?: 0}"
            if (index == 0) {
                spanny.append(spanScore)
            } else {
                spanny.append(" ")
                spanny.append(spanScore)
            }
        }
        text = spanny
    }else{
        val matchStatusList = matchInfo.matchStatusList
        if (matchStatusList.isNullOrEmpty()) {
            text = ""
        }else{
            val spanny = Spanny()
            val quarter: Int = if (matchInfo.gameType == GameType.BK.key) { //球種為籃球有特殊處理
                //篮球类 1:第一节 2:第二节 6:上半场 7:下半场 13:第一节 14:第二节 15:第三节 16:第四节
                // 31:半场 32:等待加时赛 40:加时 80:中断 90:弃赛 100:完场 110:加时赛后 301:第1次休息 302:第2次休息 303:第3次休息 999:滚球
                when (matchInfo.socketMatchStatus) {
                    1, 13 -> 0     //第一节
                    2, 14, 31 -> 1 //第二节
                    15 -> 2        //第三节
                    16 -> 3        //第四节
                    else -> 4      //其他情况全部显示
                }
            } else {
                matchStatusList.lastIndex
            }
            matchStatusList.forEachIndexed { index, it ->
                if (index > quarter) return@forEachIndexed
                val spanScore = "${it.homeScore ?: 0}-${it.awayScore ?: 0}"
                if (index < quarter) {
                    spanny.append(spanScore)
                    spanny.append("  ")
                } else {
                    spanny.append(
                        spanScore,
                        ForegroundColorSpan(this.context.getColor(R.color.color_FF8A00))
                    )
                }
            }
            text = spanny
        }
    }

}

/**
 * 足球角球
 */
fun TextView.setFbKicks(matchInfo: MatchInfo) {
    visibility = when {
        TimeUtil.isTimeInPlay(matchInfo.startTime)
                && (matchInfo.homeCornerKicks ?: 0 > 0 || matchInfo.awayCornerKicks ?: 0 > 0) -> View.VISIBLE
        else -> View.GONE
    }
    text = (matchInfo.homeCornerKicks
        ?: 0).toString() + "-" + (matchInfo.awayCornerKicks ?: 0)
}

/**
 * 设置球权标识，
 *  目前支持 棒球，网球，排球，乒乓球，羽毛球
 *  其中网球标识是另外一个位置
 */
fun setMatchAttack(
    matchInfo: MatchInfo,
    ivHomeAttack: View,
    ivAwayAttack: View,
    ivTNHomeAttack: View,
    ivTNAwayAttack: View,
) {
    if (TimeUtil.isTimeInPlay(matchInfo.startTime)) {
        when (matchInfo.gameType) {
            GameType.BB.key,
            GameType.VB.key,
            GameType.TT.key,
            GameType.IH.key,
            GameType.BM.key,
            GameType.CK.key,
            -> {
                when(matchInfo.attack){
                    "H"->{
                        ivHomeAttack.visibility = View.VISIBLE
                        ivAwayAttack.visibility = View.INVISIBLE
                    }
                    "C"->{
                        ivHomeAttack.visibility = View.INVISIBLE
                        ivAwayAttack.visibility = View.VISIBLE
                    }
                    else->{
                        ivHomeAttack.visibility = View.INVISIBLE
                        ivAwayAttack.visibility = View.INVISIBLE
                    }
                }
            }
            GameType.TN.key -> {
                when(matchInfo.attack){
                    "H"->{
                        ivTNHomeAttack.visibility = View.VISIBLE
                        ivTNAwayAttack.visibility = View.INVISIBLE
                    }
                    "C"->{
                        ivTNHomeAttack.visibility = View.INVISIBLE
                        ivTNAwayAttack.visibility = View.VISIBLE
                    }
                    else->{
                        ivTNHomeAttack.visibility = View.INVISIBLE
                        ivTNAwayAttack.visibility = View.INVISIBLE
                    }
                }
            }
            else -> {
                ivHomeAttack.visibility = View.GONE
                ivAwayAttack.visibility = View.GONE
            }
        }
    } else {
        ivHomeAttack.visibility = View.GONE
        ivAwayAttack.visibility = View.GONE
        ivTNHomeAttack.visibility = View.INVISIBLE
        ivTNAwayAttack.visibility = View.INVISIBLE
    }
}

/**
 * 设置足球黄牌，红牌数量
 */
fun setMatchCardText(
    matchInfo: MatchInfo,
    cardsHome: TextView,
    cardsAway: TextView,
    yellowCardsHome: TextView,
    yellowCardsAway: TextView,
) {
    cardsHome.apply {
        visibility = when {
            TimeUtil.isTimeInPlay(matchInfo.startTime)
                    && (matchInfo.homeCards ?: 0 > 0) -> View.VISIBLE
            else -> View.GONE
        }
        text = (matchInfo.homeCards ?: 0).toString()
    }
    cardsAway.apply {
        visibility = when {
            TimeUtil.isTimeInPlay(matchInfo.startTime)
                    && (matchInfo.awayCards ?: 0 > 0) -> View.VISIBLE
            else -> View.GONE
        }
        text = (matchInfo.awayCards ?: 0).toString()
    }
    yellowCardsHome.apply {
        visibility = when {
            TimeUtil.isTimeInPlay(matchInfo.startTime)
                    && (matchInfo.homeYellowCards ?: 0 > 0) -> View.VISIBLE
            else -> View.GONE
        }
        text = (matchInfo.homeYellowCards ?: 0).toString()
    }
    yellowCardsAway.apply {
        visibility = when {
            TimeUtil.isTimeInPlay(matchInfo.startTime)
                    && (matchInfo.awayYellowCards ?: 0 > 0) -> View.VISIBLE
            else -> View.GONE
        }
        text = (matchInfo.awayYellowCards ?: 0).toString()
    }
}

/**
 * 设置网球的中间分数布局
 */
fun setTNRoundScore(
    matchInfo: MatchInfo,
    tvHomeTotalScore: TextView,
    tvAwayTotalScore: TextView,
    tvHomeScore: TextView,
    tvAwayScore: TextView,
    tvHomePoints: TextView,
    tvAwayPoints: TextView,
) {

    val isScoreTextVisible = TimeUtil.isTimeInPlay(matchInfo.startTime)
    //设置盘比，局比，分数
    tvHomeTotalScore.apply {
        isVisible = isScoreTextVisible
        text = (matchInfo.homeTotalScore ?: 0).toString()
    }

    tvAwayTotalScore.apply {
        isVisible = isScoreTextVisible
        text = (matchInfo.awayTotalScore ?: 0).toString()
    }
    tvHomeScore.gone()
    tvAwayScore.gone()
    tvHomePoints.gone()
    tvAwayPoints.gone()
//    tvHomeScore.apply {
//        isVisible = isScoreTextVisible
//        text = (matchInfo.homeScore ?: 0).toString()
//    }
//
//    tvAwayScore.apply {
//        isVisible = isScoreTextVisible
//        text = (matchInfo.awayScore ?: 0).toString()
//    }
//    tvHomePoints.apply {
//        isVisible = isScoreTextVisible
//        text = (matchInfo.homePoints ?: 0).toString()
//    }
//
//    tvAwayPoints.apply {
//        isVisible = isScoreTextVisible
//        text = (matchInfo.awayPoints ?: 0).toString()
//    }
}

/**
 * 棒球的特殊布局处理
 */
fun setBBStatusView(
    matchInfo: MatchInfo,
    tvBBStatus: TextView,
    txvOut: TextView,
    ivHalfStatus: ImageView,
    ivBaseBag: ImageView,
) {
    tvBBStatus.apply {
        text = matchInfo.statusName18n
        setTextColor(ContextCompat.getColor(context,R.color.color_6C7BA8))
        isVisible = !matchInfo.statusName18n.isEmptyStr()
    }
    txvOut.apply {
        text = this.context.getString(R.string.game_out,
            matchInfo.outNumber ?: "0")
        setTextColor(ContextCompat.getColor(context,R.color.color_6C7BA8))
        isVisible = true
    }

    ivHalfStatus.apply {
        Timber.d("matchInfo.halfStatus: ${matchInfo.halfStatus}")
        setImageResource(if (matchInfo.halfStatus == 0) R.drawable.ic_bb_first_half else R.drawable.ic_bb_second_half)
        isVisible = matchInfo.halfStatus != null && matchInfo.halfStatus != 2
    }

    ivBaseBag.apply {
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
 * 设置波胆布局
 */
fun setMatchCsLayout(
    item: MatchOdd,
    tv_correct_1: TextView,
    tv_correct_2: TextView,
    tv_correct_3: TextView
) {
    //比照h5，直接使用local波膽翻譯文字
    tv_correct_1.text = tv_correct_1.context.getText(R.string.correct)
    var correct2 =
        item.playCateNameMap?.get(PlayCate.CS_1ST_SD.value)?.getPlayCateName(tv_correct_1.context)
            ?: ""
    if (correct2.contains("||")) {
        val correct2Split = correct2.split("||")
        //將換行符後的文字移到前面顯示
        correct2 = correct2Split[1] + correct2Split[0]
    }
    tv_correct_2.text = correct2
    var correct3 = item.playCateNameMap?.get(PlayCate.LCS.value)
        ?.getPlayCateName(tv_correct_1.context) ?: ""
    tv_correct_3.text = correct3
    when (item.csTabSelected) {
        PlayCate.CS -> {
            tv_correct_1.isSelected = true
            tv_correct_2.isSelected = false
            tv_correct_3.isSelected = false
        }
        PlayCate.CS_1ST_SD -> {
            tv_correct_1.isSelected = false
            tv_correct_2.isSelected = true
            tv_correct_3.isSelected = false
        }
        else -> {
            tv_correct_1.isSelected = false
            tv_correct_2.isSelected = false
            tv_correct_3.isSelected = true
        }
    }

    tv_correct_1.setOnClickListener {
        tv_correct_1.isSelected = true
        tv_correct_2.isSelected = false
        tv_correct_3.isSelected = false
    }

    tv_correct_2.setOnClickListener {
        tv_correct_1.isSelected = false
        tv_correct_2.isSelected = true
        tv_correct_3.isSelected = false
    }
    tv_correct_3.setOnClickListener {
        tv_correct_1.isSelected = false
        tv_correct_2.isSelected = false
        tv_correct_3.isSelected = true
    }
}

fun <K, V> Map<K, V>?.getPlayCateName(context: Context): String {
    var selectLanguage = LanguageManager.getSelectLanguage(context)
    val playCateName = this?.get<Any?, V>(selectLanguage.key) ?: this?.get<Any?, V>(
        LanguageManager.Language.EN.key)
    return playCateName.toString()
}

/**
 * 賽制(5盤3勝)
 * 只有网球，排球，乒乓球，羽毛球
 */
@SuppressLint("SetTextI18n")
fun setSptText(matchInfo: MatchInfo): String {
    if (matchInfo.gameType == GameType.CK.key) {
        return when (matchInfo.attack){
            "H"->" ${matchInfo.homeOver.toFloatS()}"
            "C"->" ${matchInfo.awayOver.toFloatS()}"
            else -> ""
        }
    }
    return ""
}