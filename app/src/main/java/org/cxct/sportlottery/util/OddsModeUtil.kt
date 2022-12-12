package org.cxct.sportlottery.util

import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R

object OddsModeUtil {


    var currentSelectModeIndex: (Int) -> Int = {
        when (it) {
            0 -> 1 //接受任何赔率变化
            1 -> 2 //接受更好赔率变化
            2 -> 0 //永不接受赔率变化
            else -> 1  //接受任何赔率变化
        }
    }

    var currentSelectModeText: (Int) -> String = {
        val context = MultiLanguagesApplication.appContext
        when (it) {
            0 -> context.getString(R.string.accept_any_change_in_odds)
            1 -> context.getString(R.string.accept_better_change_in_odds)
            2 -> context.getString(R.string.accept_never_change_in_odds)
            else -> context.getString(R.string.accept_any_change_in_odds)
        }
    }

    var currentSelectModeIndexWithText: (String)->Int = {
        val context = MultiLanguagesApplication.appContext
        when(it){
            context.getString(R.string.accept_any_change_in_odds) -> 1
            context.getString(R.string.accept_better_change_in_odds) -> 2
            context.getString(R.string.accept_never_change_in_odds) -> 0
            else ->1
        }
    }
}