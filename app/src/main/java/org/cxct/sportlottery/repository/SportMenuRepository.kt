package org.cxct.sportlottery.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.sport.*
import org.cxct.sportlottery.network.sport.coupon.SportCouponMenuResult
import retrofit2.Response

object SportMenuRepository {

    private val _sportSortList = MutableLiveData<List<SportMenu>>()
    val sportSortList: LiveData<List<SportMenu>>
        get() = _sportSortList

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

    fun postSportSortList(list: List<SportMenu>){
        _sportSortList.postValue(list)
    }

}