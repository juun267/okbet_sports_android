package org.cxct.sportlottery.repository

import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.odds.detail.OddsDetailRequest
import org.cxct.sportlottery.network.odds.detail.OddsDetailResult

class OddsRepository {

    suspend fun getOddsDetail(matchId: String, oddsType: String): OddsDetailResult? {

        val oddsDetailResponse = OneBoSportApi.oddsService.getOddsDetail(
            OddsDetailRequest(matchId, oddsType)
        )

        if (oddsDetailResponse.isSuccessful) {
            return oddsDetailResponse.body()
        }

        return null
    }

}