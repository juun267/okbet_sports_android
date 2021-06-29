package org.cxct.sportlottery.network.sport

import org.cxct.sportlottery.network.Constants.MYFAVORITE_MATCH_QUERY
import org.cxct.sportlottery.network.Constants.MYFAVORITE_QUERY
import org.cxct.sportlottery.network.Constants.MYFAVORITE_SAVE
import org.cxct.sportlottery.network.Constants.SPORT_MENU
import org.cxct.sportlottery.network.common.BaseResult
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST


interface SportService {

    @POST(SPORT_MENU)
    suspend fun getMenu(
        @Body sportMenuRequest: SportMenuRequest
    ): Response<SportMenuResult>

    @POST(MYFAVORITE_QUERY)
    suspend fun getMyFavorite(): Response<SportMenuFavoriteResult>

    @POST(MYFAVORITE_MATCH_QUERY)
    suspend fun getMyFavoriteMatch(
        @Body myFavoriteMatchRequest: MyFavoriteMatchRequest
    ): Response<MyFavoriteMatchResult>

    @POST(MYFAVORITE_SAVE)
    suspend fun saveMyFavorite(
        @Body saveMyFavoriteRequest: SaveMyFavoriteRequest
    ):Response<BaseResult>
}