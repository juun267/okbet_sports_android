package org.cxct.sportlottery.network.common

import android.content.Context
import androidx.annotation.StringRes
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
enum class GameType(val key: String, @StringRes val string: Int) {
    ALL("ALL", R.string.label_all),
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
                ALL.key -> ALL
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
                ALL.key -> ALL.string
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


        fun getGameTypeMenuIcon(gameType: String): Int {
            return when (gameType) {
                ALL.key -> R.drawable.selector_sport_all_new
                FT.key -> R.drawable.icon_football_selected
                BK.key -> R.drawable.icon_basketball_selected
                TN.key -> R.drawable.icon_tennis_selected
                VB.key -> R.drawable.icon_volleyball_selected
                BM.key -> R.drawable.icon_badminton_selected
                TT.key -> R.drawable.icon_tabletennis_selected
                IH.key -> R.drawable.icon_icehockey_selected
                BX.key -> R.drawable.icon_boxing_selected
                CB.key -> R.drawable.icon_billiards_selected
                CK.key -> R.drawable.icon_electronic_selected
                BB.key -> R.drawable.icon_baseball_selected
                RB.key -> R.drawable.icon_rugby_selected
                AFT.key -> R.drawable.icon_american_football_selected
                MR.key -> R.drawable.icon_motorracing_selected
                GF.key -> R.drawable.icon_golf_selected
                FB.key -> R.drawable.ic_home_finance_piechart
                ES.key -> R.drawable.icon_esport_selected
                OTHER.key -> R.drawable.ic_home_champ
                BB_COMING_SOON.key -> R.drawable.icon_baseball_selected
                ES_COMING_SOON.key -> R.drawable.icon_esport_selected
                else -> R.drawable.ic_game_champ
            }
        }




        fun getLeftGameTypeMenuIcon(gameType: String): Int {
            return when (gameType) {
                ALL.key -> R.drawable.selector_sport_all_new
                FT.key -> R.drawable.ic_inplay_football
                BK.key -> R.drawable.ic_inplay_basketball
                TN.key -> R.drawable.ic_inplay_tennis
                VB.key -> R.drawable.ic_inplay_volleyball
                BM.key -> R.drawable.ic_inplay_badminton
                TT.key -> R.drawable.ic_inplay_tabletennis
                IH.key -> R.drawable.ic_inplay_ice_hockey
                BX.key -> R.drawable.ic_inplay_ice_boxing
                CB.key -> R.drawable.ic_inplay_billiards
                CK.key -> R.drawable.ic_inplay_criket
                BB.key -> R.drawable.ic_inplay_baseball
                RB.key -> R.drawable.ic_inplay_rugby
                AFT.key -> R.drawable.ic_inplay_usfootball
                MR.key -> R.drawable.ic_inplay_motor_racing
                GF.key -> R.drawable.ic_inplay_golf
                FB.key -> R.drawable.ic_home_finance_piechart
                ES.key -> R.drawable.ic_inplay_esports
                OTHER.key -> R.drawable.ic_home_champ
                BB_COMING_SOON.key -> R.drawable.ic_inplay_baseball
                ES_COMING_SOON.key -> R.drawable.ic_inplay_esports
                else -> R.drawable.ic_game_champ
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

        fun getInplayIcon(gameType: String): Int {
            return when (gameType) {
                FT.key -> R.drawable.bg_menu_inplay_football
                BK.key -> R.drawable.bg_menu_inplay_basketball
                TN.key -> R.drawable.bg_menu_inplay_tennis
                VB.key -> R.drawable.bg_menu_inplay_volleyball
                BM.key -> R.drawable.bg_menu_inplay_badminton
                TT.key -> R.drawable.bg_menu_inplay_tabletennis
                IH.key -> R.drawable.bg_menu_inplay_icehockey
                BX.key -> R.drawable.bg_menu_inplay_boxing
                CB.key -> R.drawable.bg_menu_inplay_billiards
                CK.key -> R.drawable.bg_menu_inplay_cricket
                BB.key -> R.drawable.bg_menu_inplay_baseball
                RB.key -> R.drawable.bg_menu_inplay_rugby
                AFT.key -> R.drawable.bg_menu_inplay_usfootball
                MR.key -> R.drawable.bg_menu_inplay_car
                GF.key -> R.drawable.bg_menu_inplay_golf
                FB.key -> R.drawable.ic_home_finance_piechart
                ES.key -> R.drawable.bg_menu_inplay_electronic
                OTHER.key -> R.drawable.ic_home_champ
                else -> R.drawable.ic_game_champ
            }
        }
    }
}