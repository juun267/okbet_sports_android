package org.cxct.sportlottery.network.common

import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
enum class ESportType(val key: String) {
    ALL("ALL"),
    DOTA("SB:DOTA"),
    LOL("SB:LOL"),
    CS("SB:CSGO"),
    KOG("SB:KOG"),
    OTHERS("others");
    companion object {
        fun getESportIcon(code: String): Int {
            return when (code) {
                ALL.key -> R.drawable.ic_esport_all
                DOTA.key -> R.drawable.ic_esport_dota
                LOL.key -> R.drawable.ic_esport_lol
                CS.key -> R.drawable.ic_esport_cs
                KOG.key -> R.drawable.ic_esport_honor
                else -> R.drawable.ic_esport_other
            }
        }
        fun getESportImg(code: String): Int {
            return when (code) {
                ALL.key -> R.drawable.img_egame_mobile02
                DOTA.key -> R.drawable.img_egame_dota
                LOL.key -> R.drawable.img_egame_lol
                CS.key -> R.drawable.img_egame_cs
                KOG.key -> R.drawable.img_egame_honr
                else -> R.drawable.img_egame_mobile02
            }
        }
    }
}
