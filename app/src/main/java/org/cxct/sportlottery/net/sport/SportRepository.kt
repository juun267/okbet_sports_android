package org.cxct.sportlottery.net.sport

import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.RetrofitHolder
import org.cxct.sportlottery.net.sport.api.SportService
import org.cxct.sportlottery.network.sport.SportMenuData

object SportRepository {

    private val sportApi by lazy { RetrofitHolder.createApiService(SportService::class.java) }

    suspend fun getSportMenu(now: String, todayStart: String): ApiResult<SportMenuData> {
        return sportApi.getMenu(mapOf("now" to now, "todayStart" to todayStart))
    }

    suspend fun getCouponMenu() = sportApi.getCouponMenu()
}