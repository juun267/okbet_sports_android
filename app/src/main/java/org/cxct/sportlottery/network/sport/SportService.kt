package org.cxct.sportlottery.network.sport
import org.cxct.sportlottery.network.Constants.SPORT_COUPON_MENU
import org.cxct.sportlottery.network.Constants.SPORT_LIST
import org.cxct.sportlottery.network.Constants.SPORT_MENU
import org.cxct.sportlottery.network.Constants.SPORT_QUERY
import org.cxct.sportlottery.network.Constants.SPORT_SEARCH_ENGINE
import org.cxct.sportlottery.network.sport.coupon.SportCouponMenuResult
import org.cxct.sportlottery.network.sport.query.SearchRequest
import org.cxct.sportlottery.network.sport.query.SportQueryRequest
import org.cxct.sportlottery.network.sport.query.SportQueryResult
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


interface SportService {
    @GET(SPORT_LIST)
    suspend fun getSportList(): Response<SportListResponse>

    @POST(SPORT_MENU)
    suspend fun getMenu(
        @Body sportMenuRequest: SportMenuRequest
    ): Response<SportMenuResult>

    @POST(SPORT_QUERY)
    suspend fun getQuery(
        @Body sportQueryRequest: SportQueryRequest
    ): Response<SportQueryResult>

    @GET(SPORT_COUPON_MENU)
    suspend fun getSportCouponMenu(): Response<SportCouponMenuResult>

    @POST(SPORT_SEARCH_ENGINE)
    suspend fun getSearchResult(
        @Body searchRequest: SearchRequest
    ): Response<SearchResponse>
}