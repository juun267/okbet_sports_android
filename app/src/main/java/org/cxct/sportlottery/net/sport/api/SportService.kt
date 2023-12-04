package org.cxct.sportlottery.net.sport.api

import com.google.gson.JsonObject
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.sport.data.SportCouponItem
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.myfavorite.match.MyFavoriteAllMatchItem
import org.cxct.sportlottery.network.sport.CategoryItem
import org.cxct.sportlottery.network.sport.SportMenuData
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface SportService {

    @POST(Constants.SPORT_MENU)
    suspend fun getMenu(@Body sportMenuRequest: Map<String, String>): ApiResult<SportMenuData>

    @POST(Constants.MYFAVORITE_QUERY_ALL)
    suspend fun getFavoriteSportList(@Body params: JsonObject): ApiResult<List<MyFavoriteAllMatchItem>>

    @GET(Constants.SPORT_COUPON_MENU)
    suspend fun getCouponMenu(): ApiResult<List<SportCouponItem>>

    @POST(Constants.SPORT_MENU_CATEGORYLIST)
    suspend fun getMenuCategoryList(@Body params: JsonObject): ApiResult<List<CategoryItem>>



}