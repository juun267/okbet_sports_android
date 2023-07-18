package org.cxct.sportlottery.ui.sport.list.adapter

import android.view.View
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd

interface OnOddClickListener {

    fun oddClick(matchInfo: MatchInfo,
                 odd: Odd,
                 playCateCode: String,
                 betPlayCateName: String,
                 betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
                 view: View)
}