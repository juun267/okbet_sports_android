package org.cxct.sportlottery.net.sport

import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonObject
import org.cxct.sportlottery.common.extentions.callApiWithNoCancel
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

    fun loadSportMenu() {
        callApiWithNoCancel({ getSportMenu() }) {
            if (it.succeeded()) {
                it.getData()?.sortSport()
//                it.getData()?.makeEsportCategoryItem()
                _sportMenuResultEvent.postValue(it)     // 更新大廳上方球種數量、各MatchType下球種和數量
            }
        }
    }

    private fun SportMenuData.sortSport(): SportMenuData {
        this.menu.inPlay.items.sortedBy { sport ->
            sport.sortNum
        }
        this.menu.today.items.sortedBy { sport ->
            sport.sortNum
        }
        this.menu.early.items.sortedBy { sport ->
            sport.sortNum
        }
        this.menu.cs.items.sortedBy { sport ->
            sport.sortNum
        }
        this.menu.parlay.items.sortedBy { sport ->
            sport.sortNum
        }
        this.menu.outright.items.sortedBy { sport ->
            sport.sortNum
        }
        this.atStart.items.sortedBy { sport ->
            sport.sortNum
        }
        this.menu.eps?.items?.sortedBy { sport ->
            sport.sortNum
        }

        return this
    }
}