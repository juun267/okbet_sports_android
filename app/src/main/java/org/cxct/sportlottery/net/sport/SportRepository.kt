package org.cxct.sportlottery.net.sport

import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonObject
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.RetrofitHolder
import org.cxct.sportlottery.net.sport.api.SportService
import org.cxct.sportlottery.net.sport.data.RecommendLeague
import org.cxct.sportlottery.network.sport.CategoryItem
import org.cxct.sportlottery.network.sport.SportMenuData
import org.cxct.sportlottery.util.TimeUtil

object SportRepository {

    private val sportApi by lazy { RetrofitHolder.createApiService(SportService::class.java) }
    val _sportMenuResultEvent = MutableLiveData<ApiResult<SportMenuData>>()
    /**
     * isNew 则不返回categoryList参数
     */
    suspend fun getSportMenu(isNew: Boolean? = null): ApiResult<SportMenuData> {
        val params = JsonObject()
        params.addProperty("now",  TimeUtil.getNowTimeStamp().toString())
        params.addProperty("todayStart", TimeUtil.getTodayStartTimeStamp().toString())
        params.addProperty("isNew", isNew)
        return sportApi.getMenu(params)
    }

    suspend fun getCouponMenu() = sportApi.getCouponMenu()

    suspend fun getMenuCatecoryList(gameType: String,matchType: String):ApiResult<List<CategoryItem>> {
        val params = JsonObject()
        params.addProperty("gameType", gameType)
        params.addProperty("matchType", matchType)
        params.addProperty("now", TimeUtil.getNowTimeStamp().toString())
        params.addProperty("todayStart", TimeUtil.getTodayStartTimeStamp().toString())
        params.addProperty("isPc", false)
        return sportApi.getMenuCategoryList(params)
    }
    suspend fun getRecommendLeague():ApiResult<List<RecommendLeague>> {
        return sportApi.getRecommendLeague()
    }
}