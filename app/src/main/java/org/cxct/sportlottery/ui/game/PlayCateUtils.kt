package org.cxct.sportlottery.ui.game

import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.common.SportType
import org.cxct.sportlottery.network.odds.Odd

object PlayCateUtils {

    fun filterOdds(
        odds: Map<String, List<Odd?>>,
        sportCode: String
    ): MutableMap<String, MutableList<Odd?>> {
        return odds.mapValues {
            it.value.filterIndexed { index, _ ->
                index < getPlayCateSpanCount(it.key, sportCode)
            }.toMutableList()

        }.toMutableMap()
    }

    fun getPlayCateSpanCount(playCate: PlayCate, sportType: SportType) {
        getPlayCateSpanCount(playCate.value, sportType.code)
    }

    fun getPlayCateSpanCount(playCateCode: String, sportTypeCode: String) =
        when (playCateCode) {
            PlayCate.SINGLE.value, PlayCate.SINGLE_SEG1.value, PlayCate.SINGLE_1ST.value, PlayCate.SINGLE_2ST.value -> {
                when (sportTypeCode) {
                    SportType.FOOTBALL.code, SportType.BASKETBALL.code -> 3
                    SportType.TENNIS.code, SportType.VOLLEYBALL.code -> 2
                    else -> 0
                }
            }

            PlayCate.HDP.value, PlayCate.SET_HDP.value, PlayCate.HDP_SEG1.value, PlayCate.HDP_1ST.value, PlayCate.HDP_INCL_OT.value,
            PlayCate.OU.value, PlayCate.OU_1ST.value, PlayCate.OU_2ST.value, PlayCate.OU_I_OT.value,
            PlayCate.BTS.value,
            PlayCate.OE.value,
            PlayCate.TG_OU.value,
            PlayCate.TG_OU_H_INCL_OT.value, PlayCate.TG_OU_C_INCL_OT.value,
            PlayCate.TG_OU_H_1ST.value, PlayCate.TG_OU_C_1ST.value,
            PlayCate.TWTN.value,
            PlayCate.CLSH.value,
            PlayCate.C_OU.value,
            PlayCate.C_OE.value,
            PlayCate.OU_I_OT.value,
            PlayCate.SINGLE_OT.value, PlayCate.SINGLE_OT_2.value,
            PlayCate.WBH.value,
            PlayCate.WEH.value,
            PlayCate.SBH.value
            -> 2

            PlayCate.HWMG_SINGLE.value, PlayCate.W3.value, PlayCate.DC.value -> 3

            else -> 0
        }

    fun getPlayCateTitleResId(playCate: PlayCate, sportType: SportType) {
        getPlayCateTitleResId(playCate.value, sportType.code)
    }

    fun getPlayCateTitleResId(playCateCode: String, sportTypeCode: String?) = when (playCateCode) {
        PlayCate.SINGLE.value -> {
            R.string.game_play_type_1x2
        }
        PlayCate.SINGLE_1ST.value -> {
            R.string.game_play_type_1x2_1st
        }
        PlayCate.SINGLE_SEG1.value -> {
            R.string.game_play_type_1x2_seg1
        }
        PlayCate.HDP.value -> {
            when (sportTypeCode) {
                SportType.FOOTBALL.code, SportType.BASKETBALL.code -> {
                    R.string.game_play_type_hdp_ft
                }
                SportType.TENNIS.code -> {
                    R.string.game_play_type_hdp_tn
                }
                SportType.VOLLEYBALL.code -> {
                    R.string.game_play_type_hdp_vb
                }
                else -> null
            }
        }
        PlayCate.HDP_1ST.value -> {
            R.string.game_play_type_hdp_1st
        }
        PlayCate.SET_HDP.value -> {
            R.string.game_play_type_set_hdp
        }
        PlayCate.HDP_SEG1.value -> {
            R.string.game_play_type_hdp_seg1
        }
        PlayCate.HDP_INCL_OT.value -> {
            R.string.game_play_type_hdp_incl_ot
        }
        PlayCate.OU.value -> {
            when (sportTypeCode) {
                SportType.FOOTBALL.code, SportType.BASKETBALL.code -> {
                    R.string.game_play_type_ou_ft
                }
                SportType.TENNIS.code -> {
                    R.string.game_play_type_ou_tn
                }
                else -> null
            }
        }
        PlayCate.OU_1ST.value -> {
            R.string.game_play_type_ou_1st
        }
        PlayCate.OU_I_OT.value -> {
            R.string.game_play_type_ou_incl_ot
        }
        PlayCate.BTS.value -> {
            R.string.game_play_type_bts
        }
        PlayCate.OE.value -> {
            R.string.game_play_type_oe
        }
        PlayCate.TG_OU_H_INCL_OT.value -> {
            R.string.game_play_type_tg_ou_h_ot
        }
        PlayCate.TG_OU_C_INCL_OT.value -> {
            R.string.game_play_type_tg_ou_c_ot
        }
        PlayCate.TG_OU_H_1ST.value -> {
            R.string.game_play_type_tg_ou_h_1st
        }
        PlayCate.TG_OU_C_1ST.value -> {
            R.string.game_play_type_tg_ou_c_1st
        }
        else -> null
    }

    fun getOUSeries() = listOf(
        PlayCate.OU,
        PlayCate.OU_1ST,
        PlayCate.OU_I_OT,
        PlayCate.TG_OU_C_1ST,
        PlayCate.TG_OU_H_1ST,
        PlayCate.TG_OU_C_INCL_OT,
        PlayCate.TG_OU_H_INCL_OT
    )
}