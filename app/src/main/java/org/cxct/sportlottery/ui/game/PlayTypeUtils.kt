package org.cxct.sportlottery.ui.game

import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.PlayType
import org.cxct.sportlottery.network.common.SportType
import org.cxct.sportlottery.network.odds.Odd

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
