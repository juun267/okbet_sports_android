package org.cxct.sportlottery.net.sport

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.cxct.sportlottery.common.enums.ChannelType
import org.cxct.sportlottery.common.extentions.callApiWithNoCancel
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.RetrofitHolder
import org.cxct.sportlottery.net.sport.api.SportService
import org.cxct.sportlottery.net.sport.data.RecommendLeague
import org.cxct.sportlottery.network.bet.add.betReceipt.Receipt
import org.cxct.sportlottery.network.sport.CategoryItem
import org.cxct.sportlottery.network.sport.SportMenuData
import org.cxct.sportlottery.repository.LOGIN_SRC
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.toJson

object SportRepository {

    private val sportApi by lazy { RetrofitHolder.createApiService(SportService::class.java) }
    val sportMenuResultEvent = MutableLiveData<ApiResult<SportMenuData>>()
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
                sportMenuResultEvent.postValue(it)     // 更新大廳上方球種數量、各MatchType下球種和數量
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
    /**
     * "matchId": "sm76509800", 赛事id
    "scoreList": ["1-1","5-5"],比分列表
    "nickName": "abc",昵称
    "stake": 10,投注金额
    "loginSrc": 0,设备号
    "channelType": 0 渠道
     */
    suspend fun addBetLGPCOFL(matchId: String,scoreList: List<String>,nickName: String,stake: Int):ApiResult<Receipt>{
    val params = JsonObject()
    params.addProperty("matchId",  matchId)
    params.add("scoreList", Gson().toJsonTree(scoreList))
    params.addProperty("nickName", nickName)
    params.addProperty("stake", stake)
    params.addProperty("loginSrc", LOGIN_SRC)
    params.addProperty("channelType", ChannelType.HALL.ordinal)
    return sportApi.addLGPCOFL(params)
    }

    suspend fun getLGPCOFLDetail(matchId: String) = sportApi.getLGPCOFLDetail(matchId)

    suspend fun getWinningList() = sportApi.getWinningList()

}