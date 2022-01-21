package org.cxct.sportlottery.network.common

import androidx.annotation.StringRes
import org.cxct.sportlottery.R

enum class MatchType(val postValue: String, @StringRes val resId: Int) {
    MAIN("MAIN", R.string.home_tan_main),
    IN_PLAY("INPLAY", R.string.home_tab_in_play),
    TODAY("TODAY", R.string.home_tab_today),
    EARLY("EARLY", R.string.home_tab_early),
    PARLAY("PARLAY", R.string.home_tab_parlay),
    OUTRIGHT("OUTRIGHT", R.string.home_tab_outright),
    AT_START("ATSTART", R.string.home_tab_at_start),
    EPS("EPS", R.string.home_tab_eps),
    MY_EVENT("MYEVENT", R.string.my_favorite),
    OTHER("TODAY", R.string.my_favorite),
    OTHER_OUTRIGHT("TODAY", R.string.my_favorite),
    OTHER_EPS("TODAY", R.string.my_favorite)
}