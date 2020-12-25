package org.cxct.sportlottery.repository

import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.playcate.PlayCateListResult

class PlayCateListRepository {

    suspend fun getPlayCateList(gameType: String): PlayCateListResult? {

        val playCateListRepository = OneBoSportApi.playCateListService.getPlayCateList(gameType)

        if (playCateListRepository.isSuccessful) {
            return playCateListRepository.body()
        }

        return null
    }

}