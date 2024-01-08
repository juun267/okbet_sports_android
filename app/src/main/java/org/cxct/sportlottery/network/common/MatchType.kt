package org.cxct.sportlottery.network.common

import androidx.annotation.StringRes
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
enum class MatchType private constructor(val postValue: String, @StringRes val resId: Int) {
    MAIN("MAIN", R.string.home_tan_main),
    IN_PLAY("INPLAY", R.string.home_tab_in_play),
    TODAY("TODAY", R.string.home_tab_today),
    EARLY("EARLY", R.string.home_tab_early),
    CS("CS", R.string.home_tab_cs),
    PARLAY("PARLAY", R.string.home_tab_parlay),
    OUTRIGHT("OUTRIGHT", R.string.home_tab_outright),
    END_SCORE("LGPC", R.string.home_tab_end_score),
    AT_START("ATSTART", R.string.home_tab_at_start),
    EPS("EPS", R.string.home_tab_eps),
    MY_EVENT("MYEVENT", R.string.my_favorite),
    OTHER("TODAY", R.string.my_favorite),
    OTHER_OUTRIGHT("OTHER_OUTRIGHT", R.string.my_favorite),
    OTHER_EPS("OTHER_EPS", R.string.my_favorite),
    DETAIL("DETAIL", R.string.my_favorite),
    SINGLE("SINGLE",R.string.ou_hdp_1x2_title),
    IN12HR("IN12HR",R.string.P228),
    IN24HR("IN24HR",R.string.P229);

    companion object {
        fun getMatchType(matchType: String?): MatchType? {
            return when (matchType) {
                MAIN.postValue -> MAIN
                IN_PLAY.postValue -> IN_PLAY
                TODAY.postValue -> TODAY
                EARLY.postValue -> EARLY
                CS.postValue -> CS
                PARLAY.postValue -> PARLAY
                OUTRIGHT.postValue -> OUTRIGHT
                AT_START.postValue -> AT_START
                EPS.postValue -> EPS
                MY_EVENT.postValue -> MY_EVENT
                OTHER.postValue -> OTHER
                OTHER_OUTRIGHT.postValue -> OTHER_OUTRIGHT
                OTHER_EPS.postValue -> OTHER_EPS
                DETAIL.postValue -> DETAIL
                SINGLE.postValue -> SINGLE
                IN12HR.postValue -> IN12HR
                IN24HR.postValue -> IN24HR
                else -> null
            }
        }
        fun getMatchTypeStringRes(matchType: String?): Int {
            return when (matchType) {
                MAIN.postValue -> MAIN.resId
                IN_PLAY.postValue -> IN_PLAY.resId
                TODAY.postValue -> TODAY.resId
                EARLY.postValue -> EARLY.resId
                CS.postValue -> CS.resId
                PARLAY.postValue -> PARLAY.resId
                OUTRIGHT.postValue -> OUTRIGHT.resId
                AT_START.postValue -> AT_START.resId
                EPS.postValue -> EPS.resId
                MY_EVENT.postValue -> MY_EVENT.resId
                OTHER.postValue -> OTHER.resId
                OTHER_OUTRIGHT.postValue -> OTHER_OUTRIGHT.resId
                OTHER_EPS.postValue -> OTHER_EPS.resId
                DETAIL.postValue -> DETAIL.resId
                SINGLE.postValue -> SINGLE.resId
                IN12HR.postValue -> IN12HR.resId
                IN24HR.postValue -> IN24HR.resId
                else -> R.string.unknown_name
            }
        }
        fun getMatchTypeByStringId(@StringRes stringId: Int): MatchType {
            return when (stringId) {
                MAIN.resId -> MAIN
                IN_PLAY.resId -> IN_PLAY
                TODAY.resId -> TODAY
                EARLY.resId -> EARLY
                CS.resId -> CS
                PARLAY.resId -> PARLAY
                OUTRIGHT.resId -> OUTRIGHT
                AT_START.resId -> AT_START
                EPS.resId -> EPS
                MY_EVENT.resId -> MY_EVENT
                OTHER.resId -> OTHER
                OTHER_OUTRIGHT.resId -> OTHER_OUTRIGHT
                OTHER_EPS.resId -> OTHER_EPS
                OTHER_EPS.resId -> OTHER_EPS
                DETAIL.resId -> DETAIL
                SINGLE.resId -> SINGLE
                IN12HR.resId -> IN12HR
                IN24HR.resId -> IN24HR
                else -> IN_PLAY
            }
        }
    }
}
