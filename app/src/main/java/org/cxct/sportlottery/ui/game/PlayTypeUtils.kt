package org.cxct.sportlottery.ui.game

import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.PlayType
import org.cxct.sportlottery.network.common.SportType
import org.cxct.sportlottery.network.odds.list.Odd

object PlayTypeUtils {

    fun filterOdds(
        odds: Map<String, List<Odd?>>,
        sportCode: String
    ): MutableMap<String, MutableList<Odd?>> {
        return odds.mapValues {
            it.value.filterIndexed { index, _ ->
                index < getPlayTypeSetCount(it.key, sportCode)
            }.toMutableList()

        }.toMutableMap()
    }

    fun getPlayTypeSetCount(playType: PlayType, sportType: SportType) {
        getPlayTypeSetCount(playType.code, sportType.code)
    }

    fun getPlayTypeSetCount(playTypeCode: String, sportTypeCode: String) =
        when (playTypeCode) {
            PlayType.X12.code, PlayType.X12_SEG1.code, PlayType.X12_1ST.code -> {
                when (sportTypeCode) {
                    SportType.FOOTBALL.code, SportType.BASKETBALL.code -> 3
                    SportType.TENNIS.code, SportType.VOLLEYBALL.code -> 2
                    else -> 0
                }
            }
            PlayType.HDP.code, PlayType.SET_HDP.code, PlayType.HDP_SEG1.code, PlayType.HDP_1ST.code, PlayType.HDP_INCL_OT.code,
            PlayType.OU.code, PlayType.OU_1ST.code, PlayType.OU_INCL_OT.code,
            PlayType.BTS.code,
            PlayType.OE.code,
            PlayType.TG_OU_H_INCL_OT.code, PlayType.TG_OU_C_INCL_OT.code,
            PlayType.TG_OU_H_1ST.code, PlayType.TG_OU_C_1ST.code -> 2
            else -> 0
        }

    fun getPlayTypeTitleResId(playType: PlayType, sportType: SportType) {
        getPlayTypeTitleResId(playType.code, sportType.code)
    }

    fun getPlayTypeTitleResId(playTypeCode: String, sportTypeCode: String?) = when (playTypeCode) {
        PlayType.X12.code -> {
            R.string.game_play_type_1x2
        }
        PlayType.X12_1ST.code -> {
            R.string.game_play_type_1x2_1st
        }
        PlayType.X12_SEG1.code -> {
            R.string.game_play_type_1x2_seg1
        }
        PlayType.HDP.code -> {
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
        PlayType.HDP_1ST.code -> {
            R.string.game_play_type_hdp_1st
        }
        PlayType.SET_HDP.code -> {
            R.string.game_play_type_set_hdp
        }
        PlayType.HDP_SEG1.code -> {
            R.string.game_play_type_hdp_seg1
        }
        PlayType.HDP_INCL_OT.code -> {
            R.string.game_play_type_hdp_incl_ot
        }
        PlayType.OU.code -> {
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
        PlayType.OU_1ST.code -> {
            R.string.game_play_type_ou_1st
        }
        PlayType.OU_INCL_OT.code -> {
            R.string.game_play_type_ou_incl_ot
        }
        PlayType.BTS.code -> {
            R.string.game_play_type_bts
        }
        PlayType.OE.code -> {
            R.string.game_play_type_oe
        }
        PlayType.TG_OU_H_INCL_OT.code -> {
            R.string.game_play_type_tg_ou_h_ot
        }
        PlayType.TG_OU_C_INCL_OT.code -> {
            R.string.game_play_type_tg_ou_c_ot
        }
        PlayType.TG_OU_H_1ST.code -> {
            R.string.game_play_type_tg_ou_h_1st
        }
        PlayType.TG_OU_C_1ST.code -> {
            R.string.game_play_type_tg_ou_c_1st
        }
        else -> null
    }

    fun getOUSeries() = listOf(
        PlayType.OU,
        PlayType.OU_1ST,
        PlayType.OU_INCL_OT,
        PlayType.TG_OU_C_1ST,
        PlayType.TG_OU_H_1ST,
        PlayType.TG_OU_C_INCL_OT,
        PlayType.TG_OU_H_INCL_OT
    )
}