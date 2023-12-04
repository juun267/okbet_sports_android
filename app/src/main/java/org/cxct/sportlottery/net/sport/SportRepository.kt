package org.cxct.sportlottery.net.sport

import com.google.gson.JsonObject
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.RetrofitHolder
import org.cxct.sportlottery.net.sport.api.SportService
import org.cxct.sportlottery.network.sport.CategoryItem
import org.cxct.sportlottery.network.sport.SportMenuData
import org.cxct.sportlottery.util.TimeUtil
import org.json.JSONObject

object SportRepository {

    private val sportApi by lazy { RetrofitHolder.createApiService(SportService::class.java) }

    suspend fun getSportMenu(now: String, todayStart: String): ApiResult<SportMenuData> {
        return sportApi.getMenu(mapOf("now" to now, "todayStart" to todayStart))
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
}