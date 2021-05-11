package org.cxct.sportlottery.util


import android.content.Context
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.add.BetAddResult
import org.cxct.sportlottery.network.error.BetAddError


fun messageByResultCode(context: Context, result: BetAddResult): String {
    return if (result.success) {
        context.getString(R.string.bet_info_add_bet_success)
    } else {
        when (result.code) {
            BetAddError.MATCH_NOT_EXIST.code,
            BetAddError.ODDS_NOT_EXIST.code,
            BetAddError.MATCH_INVALID_STATUS.code,
            BetAddError.ODDS_LOCKED.code,
            BetAddError.PARLAY_UN_SUPPORT.code,
            BetAddError.ODDS_ID_NOT_ALLOW_BET.code,
            BetAddError.ODDS_HAVE_CHANGED.code -> {
                context.getString(R.string.bet_info_content_has_been_changed)
            }
            else -> {
                result.msg
            }
        }
    }
}


fun getBetAddError(resultCode: Int): BetAddError? {
    return when (resultCode) {
        BetAddError.MATCH_NOT_EXIST.code -> BetAddError.MATCH_NOT_EXIST
        BetAddError.ODDS_NOT_EXIST.code -> BetAddError.ODDS_NOT_EXIST
        BetAddError.MATCH_INVALID_STATUS.code -> BetAddError.MATCH_INVALID_STATUS
        BetAddError.ODDS_LOCKED.code -> BetAddError.ODDS_LOCKED
        BetAddError.PARLAY_UN_SUPPORT.code -> BetAddError.PARLAY_UN_SUPPORT
        BetAddError.ODDS_ID_NOT_ALLOW_BET.code -> BetAddError.ODDS_ID_NOT_ALLOW_BET
        BetAddError.ODDS_HAVE_CHANGED.code -> BetAddError.ODDS_HAVE_CHANGED
        else -> null
    }
}

