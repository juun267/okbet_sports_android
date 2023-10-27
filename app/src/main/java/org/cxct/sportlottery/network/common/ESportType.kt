package org.cxct.sportlottery.network.common

import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
enum class ESportType(val key: String) {
    ALL(""),
    DOTA("SB:DOTA"),
    LOL("SB:LOL"),
    CS("SB:CS"),
    HK("SB:HK"),
    OTHERS("others");
    companion object {
        fun getESportIcon(code: String): Int {
            return when (code) {
                ALL.key -> R.drawable.ic_esport_all
                DOTA.key -> R.drawable.ic_esport_dota
                LOL.key -> R.drawable.ic_esport_lol
                CS.key -> R.drawable.ic_esport_cs
                HK.key -> R.drawable.ic_esport_honor
                else -> R.drawable.ic_esport_other
            }
        }
    }
}
