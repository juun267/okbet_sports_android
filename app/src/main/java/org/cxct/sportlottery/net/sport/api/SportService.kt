package org.cxct.sportlottery.net.sport.api

import com.google.gson.JsonObject
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.sport.data.*
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.bet.add.betReceipt.BetAddResult
import org.cxct.sportlottery.network.bet.add.betReceipt.Receipt
import org.cxct.sportlottery.network.myfavorite.match.MyFavoriteAllMatchItem
import org.cxct.sportlottery.network.sport.CategoryItem
import org.cxct.sportlottery.network.sport.SportMenuData
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface SportService {

    @POST(Constants.SPORT_MENU)
    suspend fun getMenu(@Body params: JsonObject): ApiResult<SportMenuData>

    @POST(Constants.MYFAVORITE_QUERY_ALL)
    suspend fun getFavoriteSportList(@Body params: JsonObject): ApiResult<List<MyFavoriteAllMatchItem>>

    @GET(Constants.SPORT_COUPON_MENU)
    suspend fun getCouponMenu(): ApiResult<List<SportCouponItem>>

    @POST(Constants.SPORT_MENU_CATEGORYLIST)
    suspend fun getMenuCategoryList(@Body params: JsonObject): ApiResult<List<CategoryItem>>


    @POST(Constants.SPORT_RECOMMEND_LEAGUE)
    suspend fun getRecommendLeague(): ApiResult<List<RecommendLeague>>

    @POST(Constants.MATCH_BET_ADD_LGPCOFL)
    suspend fun addLGPCOFL(@Body params: JsonObject): ApiResult<Receipt>

    @POST(Constants.MATCH_LGPCOFL_DETAIL + "/{matchId}")
    suspend fun getLGPCOFLDetail(@Path("matchId") matchId: String): ApiResult<Array<EndCardBet>>

    @POST(Constants.WINNINGNEWS_LIST)
    suspend fun getWinningList(): ApiResult<List<String>?>

    @POST(Constants.BET_CASHOUT)
    suspend fun betCashOut(@Body params: JsonObject): ApiResult<CashOutResult>

    @POST(Constants.BET_CHECK_CASHOUT_STATUS)
    suspend fun betCheckCashOutStatus(@Body params: JsonObject): ApiResult<List<CheckCashOutResult>>
}