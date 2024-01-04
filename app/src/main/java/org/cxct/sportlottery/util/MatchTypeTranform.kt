package org.cxct.sportlottery.util


import android.content.Context
import android.widget.TextView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.OddSpreadForSCOCompare
import org.cxct.sportlottery.network.common.PlayCate


/**
 * @author kevin
 * @create 2022/3/8
 * @description
 */
fun TextView.tranByPlayCode(playCode: String?, playCateCode: String?,default: String?, rtScore:String?) {
    text = tranByPlayCode(this.context, playCode, playCateCode, default, rtScore)
}


fun tranByPlayCode(
    context: Context,
    playCode: String?,
    playCateCode: String?,
    default: String?,
    rtScore: String?
): String {
    return when {
        playCode?.contains(OddSpreadForSCOCompare.SCORE_1ST) == true -> {
            context.getString(R.string.odds_button_name_first)
        }
        playCode?.contains(OddSpreadForSCOCompare.SCORE_ANT) == true -> {
            context.getString(R.string.odds_button_name_ant)
        }
        playCode?.contains(OddSpreadForSCOCompare.SCORE_LAST) == true -> {
            context.getString(R.string.odds_button_name_last)
        }
        playCode?.contains(OddSpreadForSCOCompare.SCORE_N) == true -> {
            context.getString(R.string.odds_button_name_n)
        }
        //角球非區間型玩法需顯示當前角球數
        !rtScore.isNullOrEmpty() && PlayCate.needShowCurrentCorner(playCateCode) -> {
            if (default != null) {
                "$default ($rtScore)"
            } else {
                "($rtScore)"
            }
        }
        else -> {
            default ?: ""
        }
    }
}
