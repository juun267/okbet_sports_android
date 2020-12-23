package org.cxct.sportlottery.repository

import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.error.ErrorUtils
import org.cxct.sportlottery.network.playcate.PlayCateListResult
import java.lang.Error

class PlayCateListRepository {

    suspend fun getPlayCateList(gameType: String): PlayCateListResult? {

        val playCateListRepository = OneBoSportApi.playCateListService.getPlayCateList(gameType)

        return if (playCateListRepository.isSuccessful) {
            playCateListRepository.body()
        } else {
            ErrorUtils.parseError(playCateListRepository)
        }
    }

}