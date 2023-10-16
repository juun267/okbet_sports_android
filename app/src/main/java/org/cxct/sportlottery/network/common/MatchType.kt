package org.cxct.sportlottery.network.common

import androidx.annotation.StringRes
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.ui.maintab.worldcup.FIBAItem
import org.cxct.sportlottery.ui.maintab.worldcup.FIBAUtil

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
    IN12HR("IN12HR",R.string.home_tab_in12hr),
    IN24HR("IN12HR",R.string.home_tab_in24hr),
    FIBA("FIBA", R.string.fiba_2023);

    companion object {
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
                else -> R.string.unknown_name
            }
        }
    }
}
