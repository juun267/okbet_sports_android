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
    ES("ES", R.string.esports),
    OTHER("OTHER",R.string.other),
    BB_COMING_SOON("BB_COMING_SOON", R.string.baseball),
    ES_COMING_SOON("ES_COMING_SOON", R.string.esports);


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
                ES.key -> ES
                BB_COMING_SOON.key -> BB_COMING_SOON
                ES_COMING_SOON.key -> ES_COMING_SOON
                else -> null
            }
        }

        private fun getGameTypeStringRes(gameType: String?): Int {
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
                ES.key -> ES.string
                BB_COMING_SOON.key -> BB_COMING_SOON.string
                ES_COMING_SOON.key -> ES_COMING_SOON.string
                else -> R.string.unknown_name
            }
        }

        fun getGameTypeString(context: Context, gameType: String?): String {
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
            return when (gameType) {
                FT -> R.drawable.selector_sport_football
                BK -> R.drawable.selector_sport_basketball
                TN -> R.drawable.selector_sport_tennis
                VB -> R.drawable.selector_sport_volleyball
                BM -> R.drawable.selector_sport_badminton
                TT -> R.drawable.selector_sport_pingpong
                IH -> R.drawable.selector_sport_icehockey
                BX -> R.drawable.selector_sport_boxing
                CB -> R.drawable.selector_sport_snooker
                CK -> R.drawable.selector_sport_electronic
                BB -> R.drawable.selector_sport_baseball
                RB -> R.drawable.selector_sport_rugby
                AFT -> R.drawable.selector_sport_amfootball
                MR -> R.drawable.selector_sport_racing
                GF -> R.drawable.selector_sport_golf
                FB -> R.drawable.ic_home_finance_piechart
                ES -> R.drawable.selector_sport_gaming
                OTHER -> R.drawable.ic_home_champ
                BB_COMING_SOON -> R.drawable.selector_sport_baseball
                ES_COMING_SOON -> R.drawable.selector_sport_gaming
            }
        }


        fun getGameTypeMenuIcon(gameType: String): Int{
            return when (gameType) {
                FT.key -> R.drawable.selector_sport_football
                BK.key -> R.drawable.selector_sport_basketball
                TN.key -> R.drawable.selector_sport_tennis
                VB.key -> R.drawable.selector_sport_volleyball
                BM.key -> R.drawable.selector_sport_badminton
                TT.key -> R.drawable.selector_sport_pingpong
                IH.key -> R.drawable.selector_sport_icehockey
                BX.key -> R.drawable.selector_sport_boxing
                CB.key -> R.drawable.selector_sport_snooker
                CK.key -> R.drawable.selector_sport_electronic
                BB.key -> R.drawable.selector_sport_baseball
                RB.key -> R.drawable.selector_sport_rugby
                AFT.key -> R.drawable.selector_sport_amfootball
                MR.key -> R.drawable.selector_sport_racing
                GF.key -> R.drawable.selector_sport_golf
                FB.key -> R.drawable.ic_home_finance_piechart
                ES.key -> R.drawable.selector_sport_gaming
                OTHER.key -> R.drawable.ic_home_champ
                BB_COMING_SOON.key -> R.drawable.selector_sport_baseball
                ES_COMING_SOON.key -> R.drawable.selector_sport_gaming
                else -> R.drawable.ic_game_champ
            }
        }


        fun getGameTypeWhiteIcon(gameType: String): Int {
            return when (gameType) {
                FT.key -> R.drawable.img_soccer_white
                BK.key -> R.drawable.img_basketball_white
                TN.key -> R.drawable.img_tennis_white
                VB.key -> R.drawable.img_volleyball_white
                BM.key -> R.drawable.img_badminton_white
                TT.key -> R.drawable.img_pingpong_white
                IH.key -> R.drawable.img_ice_hockey_white
                BX.key -> R.drawable.img_boxing_white
                CB.key -> R.drawable.img_snooker_white
                CK.key -> R.drawable.img_cricket_white
                BB.key -> R.drawable.img_baseball_white
                RB.key -> R.drawable.img_rugby_white
                AFT.key -> R.drawable.img_amfootball_white
                MR.key -> R.drawable.img_racing_white
                GF.key -> R.drawable.img_golf_white
                FB.key -> R.drawable.img_finance_white
                ES.key -> R.drawable.img_esports_white
                BB_COMING_SOON.key -> R.drawable.img_baseball_white
                ES_COMING_SOON.key -> R.drawable.img_esports_white
                else -> R.drawable.ic_game_champ
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
                ES -> R.drawable.selector_sport_type_item_img_es_v5
                OTHER -> R.drawable.ic_game_champ
                BB_COMING_SOON -> R.drawable.selector_sport_type_item_img_bb_v5
                ES_COMING_SOON -> R.drawable.selector_sport_type_item_img_es_v5
            }
        }

        fun getBetListGameTypeIcon(gameType: GameType): Int {
            return when (gameType) {
                FT -> R.drawable.img_soccer
                BK -> R.drawable.img_basketball
                TN -> R.drawable.img_tennis
                VB -> R.drawable.img_volleyball
                BM -> R.drawable.img_badminton
                TT -> R.drawable.img_pingpong
                IH -> R.drawable.img_ice_hockey
                BX -> R.drawable.img_boxing
                CB -> R.drawable.img_snooker
                CK -> R.drawable.img_cricket
                BB -> R.drawable.img_baseball
                RB -> R.drawable.img_rugby
                AFT -> R.drawable.img_amfootball
                MR -> R.drawable.img_racing
                GF -> R.drawable.img_golf
                FB -> R.drawable.img_finance
                OTHER -> R.drawable.ic_bet_champ
                BB_COMING_SOON -> R.drawable.img_baseball
                ES_COMING_SOON -> R.drawable.img_esports
                else -> 0
            }
        }

        fun getGameTypePublicityItemBackground(gameType: GameType): Int {
            return when (gameType) {
                FT -> R.drawable.bg_publicity_sport_item_football
                BK -> R.drawable.bg_publicity_sport_item_basketball
                TN -> R.drawable.bg_publicity_sport_item_tennis
                VB -> R.drawable.bg_publicity_sport_item_volleyball
                BM -> R.drawable.bg_publicity_sport_item_badminton
                TT -> R.drawable.bg_publicity_sport_item_pingpong
                IH -> R.drawable.bg_publicity_sport_item_icehockey
                BX -> R.drawable.bg_publicity_sport_item_boxing
                CB -> R.drawable.bg_publicity_sport_item_snooker
                CK -> R.drawable.bg_publicity_sport_item_cricket
                BB -> R.drawable.bg_publicity_sport_item_baseball
                RB -> R.drawable.bg_publicity_sport_item_rugby
                AFT -> R.drawable.bg_publicity_sport_item_amfootball
                MR -> R.drawable.bg_publicity_sport_item_racing
                GF -> R.drawable.bg_publicity_sport_item_golf
                FB -> R.drawable.bg_publicity_sport_item_piechart
                BB_COMING_SOON -> R.drawable.bg_publicity_sport_item_baseball
                ES -> R.drawable.bg_publicity_sport_item_e_sport
                else -> R.drawable.bg_publicity_sport_item_football
            }
        }

        fun getGameTypeBannerBg(gameType: GameType): Int {
            return when (gameType) {
                FT -> R.drawable.card_sport_football
                BK -> R.drawable.card_sport_basketball
                TN -> R.drawable.card_sport_tennis
                VB -> R.drawable.card_sport_volleyball
                BM -> R.drawable.card_sport_badminton
                TT -> R.drawable.card_sport_pingpong
                IH -> R.drawable.card_sport_icehockey
                BX -> R.drawable.card_sport_boxing
                CB -> R.drawable.card_sport_snooker
                CK -> R.drawable.card_sport_cricket
                BB -> R.drawable.card_sport_baseball
                RB -> R.drawable.card_sport_rugby
                AFT -> R.drawable.card_sport_amfootball
                MR -> R.drawable.card_sport_racing
                GF -> R.drawable.card_sport_golf
                ES -> R.drawable.card_sport_esports
                else ->
                    R.drawable.card_sport_football
            }
        }

        fun getGameTypeDetailBg(gameType: GameType): Int {
            return when (gameType) {
                FT -> R.drawable.img_soccer_mobile01
                BK -> R.drawable.img_basketball_mobile01
                TN -> R.drawable.img_tennis_mobile01
                VB -> R.drawable.img_volleyball_mobile01
                BM -> R.drawable.img_badminton_mobile01
                TT -> R.drawable.img_tabletennis_mobile01
                IH -> R.drawable.img_icehockey_mobile01
                BX -> R.drawable.img_boxing_mobile01
                CB -> R.drawable.img_snooker_mobile01
                CK -> R.drawable.img_cricket_mobile01
                BB -> R.drawable.img_baseball_mobile01
                RB -> R.drawable.img_rugby_mobile01
                AFT -> R.drawable.img_americafootball_mobile01
                MR -> R.drawable.img_rancing_mobile01
                GF -> R.drawable.img_golf_mobile01
                ES -> R.drawable.img_egame_mobile01
                else ->
                    R.drawable.img_soccer_mobile01
            }
        }
    }

}