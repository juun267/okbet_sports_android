package org.cxct.sportlottery.network.error

import androidx.annotation.StringRes
import org.cxct.sportlottery.R

enum class BetAddError(val code: Int, @StringRes val string: Int) {
    MATCH_NOT_EXIST(5001, R.string.match_not_exist),
    ODDS_NOT_EXIST(5003, R.string.odds_not_exist),
    MATCH_INVALID_STATUS(5004, R.string.match_invalid_status),
    ODDS_LOCKED(5005, R.string.odds_locked),
    PARLAY_UN_SUPPORT(5011, R.string.parlay_un_support),
    ODDS_ID_NOT_ALLOW_BET(5014, R.string.odds_id_not_allow_bet),
    ODDS_HAVE_CHANGED(5023, R.string.odds_have_changed)
}