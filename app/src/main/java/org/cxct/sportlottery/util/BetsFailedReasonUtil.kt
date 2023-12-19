package org.cxct.sportlottery.util

import org.cxct.sportlottery.R

object BetsFailedReasonUtil {

    private const val ODDS_CHANGED = "ODDS_CHANGED" //赔率变化
    private const val COST_TOO_LONG_TIME_TO_CONFIRM = "COST_TOO_LONG_TIME_TO_CONFIRM"//注单长时间未确认
    private const val BET_MONEY_EXCEED_FOR_SINGLE_BET =
        "BET_MONEY_EXCEED_FOR_SINGLE_BET" // 超出单注投注上限
    private const val BET_MONEY_EXCEED_FOR_MATCH_BET =
        "BET_MONEY_EXCEED_FOR_MATCH_BET" // 超出赛事累计投注上限
    private const val BET_MONEY_EXCEED_FOR_MATCH_PLAY =
        "BET_MONEY_EXCEED_FOR_MATCH_PLAY" // 超出某场赛事单个玩法类别playCate的上限
    private const val BET_MONEY_EXCEED_FOR_USER = "BET_MONEY_EXCEED_FOR_USER" // 用户超出单日投注上限
    private const val BET_IN_RISK_TIME = "BET_IN_RISK_TIME" // 危险球期间下注
    private const val BET_MATCH_STATUS_CHANGED = "BET_MATCH_STATUS_CHANGED" // 发生危险球事件(赛事比分或红牌变化)
    private const val MATCH_STATUS_CHANGED = "MATCH_STATUS_CHANGED" //赛事状态异动
    private const val MATCH_STATUS_CLOSE = "MATCH_STATUS_CLOSE"//赛事不存在
    private const val ODDS_SUSPENDED = "ODDS_SUSPENDED"//赛事暂停
    private const val ODDS_TYPE_ERROR = "ODDS_TYPE_ERROR"//盘口错误
    private const val MONEY_ERROR = "MONEY_ERROR"//金额不正确
    private const val STAKE_GT_MAX_BET_LIMIT = "STAKE_GT_MAX_BET_LIMIT"//下注金额超过最大投注额
    private const val STAKE_LT_MIN_BET_LIMIT = "STAKE_LT_MIN_BET_LIMIT"//下注金额低于最小投注额
    private const val WINNABLE_GT_MAX_WINNABLE_LIMIT =
        "WINNABLE_GT_MAX_WINNABLE_LIMIT"//可赢金额超过最大投注额
    private const val WINNABLE_LT_MIN_WINNABLE_LIMIT =
        "WINNABLE_LT_MIN_WINNABLE_LIMIT"//可赢金额低于最小投注额
    private const val PARLAY_HAVE_OTHER_SPORT = "PARLAY_HAVE_OTHER_SPORT"//串关只能同一种球类
    private const val PARLAY_NUM_IS_OVER = "PARLAY_NUM_IS_OVER"//串关数量超出限制
    private const val STAKE_GT_SELF_LIMIT = "STAKE_GT_SELF_LIMIT"//自我禁制
    private const val SOURCE_ERROR = "SOURCE_ERROR"//赛事数据源错误
    private const val PARLAY_ODD_NUM_ERROR = "PARLAY_ODD_NUM_ERROR"// 串关可以下注的赔率数量与玩家下注的赔率数量不一致
    private const val OUTRIGHT_PARLAY_ERROR = "OUTRIGHT_PARLAY_ERROR"// 冠军赛事禁止串关
    private const val PARLAY_TYPE_ERROR = "PARLAY_TYPE_ERROR"// 串关类型错误
    private const val ODDS_CLOSE = "ODDS_CLOSE"
    private const val MATCH_CANNOT_PARLAY = "MATCH_CANNOT_PARLAY"
    private const val PLAY_CATE_CANNOT_PARLAY = "PLAY_CATE_CANNOT_PARLAY"

    fun getFailedReasonByCode(code: String?): String {
        var reason = ""
        when (code) {
            ODDS_CHANGED -> {
                reason = LocalUtils.getString(R.string.str_odds_changed)
            } //赔率变化
            COST_TOO_LONG_TIME_TO_CONFIRM -> {
                reason = LocalUtils.getString(R.string.str_bet_slip_not_confirm_for_a_long_time)
            } //注单长时间未确认
            BET_MONEY_EXCEED_FOR_SINGLE_BET,
            BET_MONEY_EXCEED_FOR_MATCH_BET,
            BET_MONEY_EXCEED_FOR_MATCH_PLAY,
            BET_MONEY_EXCEED_FOR_USER -> {
                reason = LocalUtils.getString(R.string.str_exceeding_limit_for_the_event_or_market)
            } // 用户超出单日投注上限
            BET_IN_RISK_TIME ,
            BET_MATCH_STATUS_CHANGED ,
            MATCH_STATUS_CHANGED -> {
                reason = LocalUtils.getString(R.string.str_market_closed_please_try_later)
            } //赛事状态异动
            ODDS_CLOSE,
            MATCH_STATUS_CLOSE,
            MATCH_CANNOT_PARLAY -> {
                reason = LocalUtils.getString(R.string.str_match_closed)
            }//赛事不存在
            PLAY_CATE_CANNOT_PARLAY->{
                reason = LocalUtils.getString(R.string.your_bet_order_play_cate_cannot_parlay)
            }
            ODDS_SUSPENDED -> {
                reason = LocalUtils.getString(R.string.str_match_be_suspended)
            }//赛事暂停
            ODDS_TYPE_ERROR -> {
                reason = LocalUtils.getString(R.string.str_match_be_suspended)
            }//盘口错误
            MONEY_ERROR -> {
                reason = LocalUtils.getString(R.string.str_amount_wrong)
            }//金额不正确
            STAKE_GT_MAX_BET_LIMIT -> {
                reason = LocalUtils.getString(R.string.str_exceeding_limit_for_the_event_or_market)
            }//下注金额超过最大投注额
            STAKE_LT_MIN_BET_LIMIT -> {
                reason = LocalUtils.getString(R.string.str_less_than_min_bet)
            }//下注金额低于最小投注额
            WINNABLE_GT_MAX_WINNABLE_LIMIT -> {
                reason = LocalUtils.getString(R.string.str_exceeding_limit_for_the_event_or_market)
            }//可赢金额超过最大投注额
            WINNABLE_LT_MIN_WINNABLE_LIMIT -> {
                reason = LocalUtils.getString(R.string.str_less_than_min_bet)
            }//可赢金额低于最小投注额
            PARLAY_HAVE_OTHER_SPORT -> {
                reason = LocalUtils.getString(R.string.str_only_one_type_of_ball_can_be_used_parlay)
            }//串关只能同一种球类
            PARLAY_NUM_IS_OVER -> {
                reason = LocalUtils.getString(R.string.str_parlay_exceed_the_ties_limit)
            }//串关数量超出限制
            STAKE_GT_SELF_LIMIT -> {
                reason = LocalUtils.getString(R.string.str_self_exclusion)
            }//自我禁制
            SOURCE_ERROR -> {
                reason = LocalUtils.getString(R.string.str_match_be_suspended)
            }//赛事数据源错误
            PARLAY_ODD_NUM_ERROR -> {
                reason = LocalUtils.getString(R.string.str_match_be_suspended_or_closed_please_check)
            }// 串关可以下注的赔率数量与玩家下注的赔率数量不一致
            OUTRIGHT_PARLAY_ERROR -> {
                reason = LocalUtils.getString(R.string.str_parlay_is_not_allowed_in_championship_matches)
            }// 冠军赛事禁止串关
            PARLAY_TYPE_ERROR -> {
                reason = LocalUtils.getString(R.string.str_parlay_type_wrong)
            }// 串关类型错误
        }
        return reason
    }
}