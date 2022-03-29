package org.cxct.sportlottery.network.common

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.StringRes
import org.cxct.sportlottery.R
import org.cxct.sportlottery.util.LanguageManager
import java.util.*

enum class GameType(val key: String, @StringRes val string: Int) {
    FT("FT", R.string.soccer),
    BK("BK", R.string.basketball),
    TN("TN", R.string.tennis),
    VB("VB", R.string.volleyball),
    BM("BM", R.string.badminton),
    TT("TT", R.string.table_tennis),
    IH("IH", R.string.ice_hockey),
    BX("BX", R.string.boxing),
    CB("CB", R.string.snooker),
    CK("CK", R.string.cricket),
    BB("BB", R.string.baseball),
    RB("RB", R.string.rugby_football),
    AFT("AFT",R.string.america_football),
    MR("MR", R.string.motor_racing),
    GF("GF", R.string.golf),
    FB("FB",R.string.financial_bets),
    OTHER("OTHER",R.string.other);

    companion object {
        fun getGameType(code: String?): GameType? {
            return when (code) {
                FT.key -> FT
                BK.key -> BK
                TN.key -> TN
                VB.key -> VB
                BM.key -> BM
                TT.key -> TT
                IH.key -> IH
                BX.key -> BX
                CB.key -> CB
                CK.key -> CK
                BB.key -> BB
                RB.key -> RB
                AFT.key -> AFT
                MR.key -> MR
                GF.key -> GF
                FB.key -> FB
                else -> null
            }
        }

        private fun getGameTypeStringRes(gameType: String?): Int{
            return when (gameType) {
                FT.key -> FT.string
                BK.key -> BK.string
                TN.key -> TN.string
                VB.key -> VB.string
                BM.key -> BM.string
                TT.key -> TT.string
                BX.key -> BX.string
                CB.key -> CB.string
                CK.key -> CK.string
                BB.key -> BB.string
                RB.key -> RB.string
                AFT.key -> AFT.string
                IH.key -> IH.string
                MR.key -> MR.string
                GF.key -> GF.string
                FB.key -> FB.string
                else -> R.string.unknown_name
            }
        }

        fun getGameTypeString(context: Context, gameType: String?): String{
            return context.getString(getGameTypeStringRes(gameType))
        }

        fun getSpecificLanguageString(context: Context, gameType: String?, language: String?): String {
            return when (language) {
                LanguageManager.Language.EN.key ->
                    context.getStringByLocale(getGameTypeStringRes(gameType), Locale.ENGLISH)
                else ->
                    context.getString(getGameTypeStringRes(gameType))
            }
        }

        private fun Context.getStringByLocale(@StringRes stringRes: Int, locale: Locale): String {
            val configuration = Configuration(resources.configuration)
            configuration.setLocale(locale)
            return createConfigurationContext(configuration).resources.getString(stringRes)
        }

        fun getGameTypeMenuIcon(gameType: GameType): Int{
            return when(gameType){
                FT -> R.drawable.ic_game_football
                BK -> R.drawable.ic_game_basketball
                TN -> R.drawable.ic_game_tennis
                VB -> R.drawable.ic_game_volleyball
                BM -> R.drawable.ic_game_badminton
                TT -> R.drawable.ic_game_pingpong
                IH -> R.drawable.ic_game_icehockey
                BX -> R.drawable.ic_game_boxing
                CB -> R.drawable.ic_game_snooker
                CK -> R.drawable.ic_game_cricket
                BB -> R.drawable.ic_game_baseball
                RB -> R.drawable.ic_game_rugby
                AFT -> R.drawable.ic_game_amfootball
                MR -> R.drawable.ic_game_rancing
                GF -> R.drawable.ic_game_golf
                FB -> R.drawable.ic_game_finance
                OTHER -> R.drawable.ic_game_champ
            }
        }

        fun getGameTypeIcon(gameType: GameType): Int {
            return when (gameType) {
                FT -> R.drawable.selector_sport_type_item_img_ft_v5
                BK -> R.drawable.selector_sport_type_item_img_bk_v5
                TN -> R.drawable.selector_sport_type_item_img_tn_v5
                VB -> R.drawable.selector_sport_type_item_img_vb_v5
                BM -> R.drawable.selector_sport_type_item_img_bm_v5
                TT -> R.drawable.selector_sport_type_item_img_tt_v5
                IH -> R.drawable.selector_sport_type_item_img_ih_v5
                BX -> R.drawable.selector_sport_type_item_img_bx_v5
                CB -> R.drawable.selector_sport_type_item_img_cb_v5
                CK -> R.drawable.selector_sport_type_item_img_ck_v5
                BB -> R.drawable.selector_sport_type_item_img_bb_v5
                RB -> R.drawable.selector_sport_type_item_img_rb_v5
                AFT -> R.drawable.selector_sport_type_item_img_aft_v5
                MR -> R.drawable.selector_sport_type_item_img_mr_v5
                GF -> R.drawable.selector_sport_type_item_img_gf_v5
                FB -> R.drawable.selector_sport_type_item_img_fb_v5
                OTHER -> R.drawable.ic_game_champ
            }
        }
    }
}