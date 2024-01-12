package org.cxct.sportlottery.network.myfavorite

import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.myfavorite.match.MyFavoriteAllMatchResult
import org.cxct.sportlottery.network.myfavorite.match.MyFavoriteMatchRequest
import org.cxct.sportlottery.network.myfavorite.query.SportMenuFavoriteResult
import org.cxct.sportlottery.network.myfavorite.save.MyFavoriteBaseResult
import org.cxct.sportlottery.network.myfavorite.save.SaveMyFavoriteRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface FavoriteService {

    @POST(Constants.MYFAVORITE_QUERY)
    suspend fun getMyFavorite(): Response<SportMenuFavoriteResult>

    @POST(Constants.MYFAVORITE_SAVE)
    suspend fun saveMyFavorite(
        @Body saveMyFavoriteRequest: SaveMyFavoriteRequest,
    ): Response<MyFavoriteBaseResult>

    @POST(Constants.MYFAVORITE_QUERY_ALL)
    suspend fun getMyFavoriteQueryAll(
        @Body myFavoriteMatchRequest: MyFavoriteMatchRequest,
    ): Response<MyFavoriteAllMatchResult>

}