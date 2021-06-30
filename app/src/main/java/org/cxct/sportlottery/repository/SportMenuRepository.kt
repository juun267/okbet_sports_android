package org.cxct.sportlottery.repository

import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.BaseResult
import org.cxct.sportlottery.network.sport.*
import retrofit2.Response

class SportMenuRepository {

    suspend fun getSportMenu(now: String, todayStart: String): Response<SportMenuResult> {
        return OneBoSportApi.sportService.getMenu(
            SportMenuRequest(
                now, todayStart
            )
        )
    }

    suspend fun getMyFavorite(): Response<SportMenuFavoriteResult> {
        return OneBoSportApi.sportService.getMyFavorite()
    }

    suspend fun getMyFavoriteMatch(myFavoriteMatchRequest: MyFavoriteMatchRequest): Response<MyFavoriteMatchResult> {
        return OneBoSportApi.sportService.getMyFavoriteMatch(myFavoriteMatchRequest)
    }

    suspend fun saveMyFavorite(saveMyFavoriteRequest: SaveMyFavoriteRequest): Response<MyFavoriteBaseResult> {
        return OneBoSportApi.sportService.saveMyFavorite(saveMyFavoriteRequest)
    }

}