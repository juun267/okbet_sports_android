package org.cxct.sportlottery.util


import android.content.Context
import android.widget.TextView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.enum.OddSpreadForSCOCompare


/**
 * @author kevin
 * @create 2022/3/8
 * @description
 */
fun TextView.tranByPlayCode(playCode: String?, default: String?) {
    text = tranByPlayCode(this.context, playCode, default)
}


fun tranByPlayCode(context: Context, playCode: String?, default: String?): String {
    return when {
        playCode?.contains(OddSpreadForSCOCompare.SCORE_1ST.playCode) == true -> {
            context.getString(R.string.sco_name_first)
        }
        playCode?.contains(OddSpreadForSCOCompare.SCORE_ANT.playCode) == true -> {
            context.getString(R.string.sco_name_any)
        }
        playCode?.contains(OddSpreadForSCOCompare.SCORE_LAST.playCode) == true -> {
            context.getString(R.string.sco_name_last)
        }
        playCode?.contains(OddSpreadForSCOCompare.SCORE_N.playCode) == true -> {
            ""
        }
        else -> {
            default ?: ""
        }
    }
}
