package org.cxct.sportlottery.network.sport

import org.cxct.sportlottery.network.Constants.SPORT_LIST
import org.cxct.sportlottery.network.Constants.SPORT_MENU
import org.cxct.sportlottery.network.Constants.SPORT_PUBLICITY_RECOMMEND
import org.cxct.sportlottery.network.Constants.SPORT_MENU_FILTER
import org.cxct.sportlottery.network.Constants.SPORT_SEARCH_ENGINE
import org.cxct.sportlottery.network.Constants.INDEX_RESOURCE_JSON
import org.cxct.sportlottery.network.sport.list.SportListResponse
import org.cxct.sportlottery.network.sport.publicityRecommend.PublicityRecommendRequest
import org.cxct.sportlottery.network.sport.publicityRecommend.PublicityRecommendResult
import org.cxct.sportlottery.network.sport.query.SearchRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


interface SportService {

    /**
     * @param type 1 仅过滤平台球种开关 若不带此参数或不为1 则过滤风控球种开关与平台球种开关
     */
    @GET(SPORT_LIST)
    suspend fun getSportList(@Query("type") type: Int? = null): Response<SportListResponse>

    @POST(SPORT_MENU)
    suspend fun getMenu(
        @Body sportMenuRequest: SportMenuRequest
    ): Response<SportMenuResult>



    @POST(SPORT_SEARCH_ENGINE)
    suspend fun getSearchResult(
        @Body searchRequest: SearchRequest
    ): Response<SearchResponse>

    @POST(SPORT_PUBLICITY_RECOMMEND)
    suspend fun getPublicityRecommend(
        @Body publicityRecommendRequest: PublicityRecommendRequest
    ): Response<PublicityRecommendResult>

    @GET(SPORT_MENU_FILTER)
    suspend fun getSportListFilter(): Response<SportMenuFilterResult>

    @GET(INDEX_RESOURCE_JSON)
    suspend fun getIndexResourceJson(): Response<IndexResourceJsonResult>
}
