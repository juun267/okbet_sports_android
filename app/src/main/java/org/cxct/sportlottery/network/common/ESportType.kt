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
    LOLWR("SB:LOLWR"),
    VLR("SB:VLR"),
    ML("SB:ML"),
    COD("SB:COD"),
    PUBG("SB:PUBG"),
    APL("SB:APL"),
    OTHERS("others");
    companion object {
        fun getESportIcon(code: String): Int {
            return when (code) {
                ALL.key -> R.drawable.ic_esport_all
                DOTA.key -> R.drawable.ic_esport_dota
                LOL.key -> R.drawable.ic_esport_lol
                CS.key -> R.drawable.ic_esport_cs
                KOG.key -> R.drawable.ic_esport_kog
                LOLWR.key -> R.drawable.ic_esport_lolwr
                VLR.key -> R.drawable.ic_esport_vlr
                ML.key -> R.drawable.ic_esport_ml
                COD.key -> R.drawable.ic_esport_cod
                PUBG.key -> R.drawable.ic_esport_pubg
                APL.key -> R.drawable.ic_esport_apl
                else -> R.drawable.ic_esport_other
            }
        }
        fun getESportImg(code: String): Int {
            return when (code) {
                ALL.key -> R.drawable.img_egame_mobile02
                DOTA.key -> R.drawable.img_esport_dota
                LOL.key -> R.drawable.img_esport_lol
                CS.key -> R.drawable.img_esport_cs
                KOG.key -> R.drawable.img_esport_kog
                LOLWR.key -> R.drawable.img_esport_lolwr
                VLR.key -> R.drawable.img_esport_vlr
                ML.key -> R.drawable.img_esport_ml
                COD.key -> R.drawable.img_esport_cod
                PUBG.key -> R.drawable.img_esport_pubg
                APL.key -> R.drawable.img_esport_apl
                else -> R.drawable.img_egame_mobile02
            }
        }
        fun getHomeESportIcon(code: String): Int {
            return when (code) {
                ALL.key -> R.drawable.ic_home_esport_other
                DOTA.key -> R.drawable.ic_home_esport_dota
                LOL.key -> R.drawable.ic_home_esport_lol
                CS.key -> R.drawable.ic_home_esport_cs
                KOG.key -> R.drawable.ic_home_esport_kog
                LOLWR.key -> R.drawable.ic_home_esport_lolwr
                VLR.key -> R.drawable.ic_home_esport_vlr
                ML.key -> R.drawable.ic_home_esport_ml
                COD.key -> R.drawable.ic_home_esport_cod
                PUBG.key -> R.drawable.ic_home_esport_pubg
                APL.key -> R.drawable.ic_home_esport_apl
                else -> R.drawable.ic_home_esport_other
            }
        }
        fun getHomeESportBg(code: String): Int {
            return when (code) {
                ALL.key -> R.drawable.bg_home_esport_other
                DOTA.key -> R.drawable.bg_home_esport_dota
                LOL.key -> R.drawable.bg_home_esport_lol
                CS.key -> R.drawable.bg_home_esport_cs
                KOG.key -> R.drawable.bg_home_esport_kog
                LOLWR.key -> R.drawable.bg_home_esport_lolwr
                VLR.key -> R.drawable.bg_home_esport_vlr
                ML.key -> R.drawable.bg_home_esport_ml
                COD.key -> R.drawable.bg_home_esport_cod
                PUBG.key -> R.drawable.bg_home_esport_pubg
                APL.key -> R.drawable.bg_home_esport_apl
                else -> R.drawable.bg_home_esport_other
            }
        }
    }

}
