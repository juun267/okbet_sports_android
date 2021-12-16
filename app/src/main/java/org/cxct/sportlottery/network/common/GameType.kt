package org.cxct.sportlottery.network.common

import androidx.annotation.StringRes
import org.cxct.sportlottery.R

enum class GameType(val key: String, @StringRes val string: Int) {
    FT("FT", R.string.soccer),
    BK("BK", R.string.basketball),
    TN("TN", R.string.tennis),
    BM("BM", R.string.badminton),
    TT("TT", R.string.ping_pong),
    IH("IH", R.string.ice_hockey),
    BX("BX", R.string.boxing),
    CB("CB", R.string.cue_ball),
    CK("CK", R.string.cricket),
    BB("BB", R.string.baseball),
    RB("RB", R.string.rugby_football),
    MR("MR", R.string.motor_racing),
    GF("GF", R.string.golf),
    VB("VB", R.string.volleyball),
    AFT("AFT",R.string.AFT);

    companion object {
        fun getGameType(code: String?): GameType? {
            return when (code) {
                FT.key -> FT
                BK.key -> BK
                TN.key -> TN
                BM.key -> BM
                TT.key -> TT
                IH.key -> IH
                BX.key -> BX
                CB.key -> CB
                CK.key -> CK
                BB.key -> BB
                RB.key -> RB
                MR.key -> MR
                GF.key -> GF
                VB.key -> VB
                AFT.key -> AFT
                else -> null
            }
        }

        fun getGameTypeEnName(gameType: GameType): String{
            return when(gameType){
                FT -> "FOOTBALL"
                BK -> "BASKETBALL"
                TN -> "TENNIS"
                VB -> "VOLLEYBALL"
                BM -> "BADMINTON"
                TT -> "PING PONG"
                IH -> "ICE HOCKEY"
                BX -> "BOXING"
                CB -> "CUE_BALL"
                CK -> "CRICKET"
                BB -> "BASEBALL"
                RB -> "RUGBY FOOTBALL"
                MR -> "MOTOR RACING"
                GF -> "GOLF"
                AFT -> "AMERICA FOOT BALL"
            }
        }

        fun getGameTypeMenuIcon(gameType: GameType): Int{
            return when(gameType){
                FT -> R.drawable.ic_game_football
                BK -> R.drawable.ic_game_basketball
                TN -> R.drawable.ic_game_tennis
                VB -> R.drawable.ic_game_volleyball
                BM -> R.drawable.img_badminton
                TT -> R.drawable.img_pingpong
                IH -> R.drawable.img_icehockey
                BX -> R.drawable.img_boxing
                CB -> R.drawable.img_snooker
                CK -> R.drawable.img_cricket
                BB -> R.drawable.img_baseball
                RB -> R.drawable.img_rugby
                AFT -> R.drawable.img_amfootball
                MR -> R.drawable.img_racing
                GF -> R.drawable.img_golf
            }
        }
    }
}