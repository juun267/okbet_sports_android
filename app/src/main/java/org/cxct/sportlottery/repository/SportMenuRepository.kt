package org.cxct.sportlottery.repository

import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.sport.SportMenuRequest
import org.cxct.sportlottery.network.sport.SportMenuResult
import org.cxct.sportlottery.network.sport.coupon.SportCouponMenuResult
import retrofit2.Response

object SportMenuRepository {


    suspend fun getSportMenu(now: String, todayStart: String): Response<SportMenuResult> {
        return OneBoSportApi.sportService.getMenu(
            SportMenuRequest(
                now, todayStart
            )
        )
    }

    suspend fun getSportCouponMenu(): Response<SportCouponMenuResult> {
        return OneBoSportApi.sportService.getSportCouponMenu()
    }

}