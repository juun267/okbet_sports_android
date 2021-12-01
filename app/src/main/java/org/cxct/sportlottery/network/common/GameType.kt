package org.cxct.sportlottery.network.common

import androidx.annotation.StringRes
import org.cxct.sportlottery.R

enum class GameType(val key: String, @StringRes val string: Int) {
    FT("FT", R.string.soccer),
    BK("BK", R.string.basketball),
    TN("TN", R.string.tennis),
    VB("VB", R.string.volleyball),
    BM("BM",R.string.badminton),
    TT("TT",R.string.TT),
    IH("IH",R.string.IH),
    BX("BX",R.string.BX),
    CB("CB",R.string.CB),
    CK("CK",R.string.CK),
    BB("BB",R.string.BB),
    RB("RB",R.string.RB),
    AFT("AFT",R.string.AFT),
    MR("MR",R.string.MR),
    GF("GF",R.string.GF);

    companion object {
        fun getGameType(code: String?): GameType? {
            return when (code) {
                FT.key -> FT
                BK.key -> BK
                TN.key -> TN
                VB.key -> VB
                else -> null
            }
        }

        fun getGameTypeEnName(gameType: GameType): String?{
            return when(gameType){
                FT -> "FOOTBALL"
                BK -> "BASKETBALL"
                TN -> "TENNIS"
                VB -> "VOLLEYBALL"
                else -> null
            }
        }

        fun getGameTypeMenuIcon(gameType: GameType): Int?{
            return when(gameType){
                FT -> R.drawable.ic_game_football
                BK -> R.drawable.ic_game_basketball
                TN -> R.drawable.ic_game_tennis
                VB -> R.drawable.ic_game_volleyball
                else -> null
            }
        }
    }
}