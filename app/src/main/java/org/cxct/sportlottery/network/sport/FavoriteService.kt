package org.cxct.sportlottery.network.sport

import org.cxct.sportlottery.network.Constants
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface FavoriteService {

    @POST(Constants.MYFAVORITE_QUERY)
    suspend fun getMyFavorite(): Response<SportMenuFavoriteResult>

    @POST(Constants.MYFAVORITE_MATCH_QUERY)
    suspend fun getMyFavoriteMatch(
        @Body myFavoriteMatchRequest: MyFavoriteMatchRequest
    ): Response<MyFavoriteMatchResult>

    @POST(Constants.MYFAVORITE_SAVE)
    suspend fun saveMyFavorite(
        @Body saveMyFavoriteRequest: SaveMyFavoriteRequest
    ): Response<MyFavoriteBaseResult>

}