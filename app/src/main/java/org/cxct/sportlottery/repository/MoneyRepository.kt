package org.cxct.sportlottery.repository

import androidx.lifecycle.LiveData
import com.google.gson.JsonObject
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.games.OKGamesRepository
import org.cxct.sportlottery.net.games.data.OKGamesHall
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.money.MoneyAddRequest
import org.cxct.sportlottery.network.money.MoneyAddResult
import org.cxct.sportlottery.network.money.config.MoneyRechCfgResult
import org.cxct.sportlottery.network.user.UserInfo
import org.cxct.sportlottery.util.JsonUtil
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

object MoneyRepository {

    val userInfo: LiveData<UserInfo?>
        get() = MultiLanguagesApplication.mInstance.userInfo

    suspend fun getRechCfg(): Response<MoneyRechCfgResult> {
        return OneBoSportApi.moneyService.getRechCfg()
    }

    suspend fun rechargeAdd(moneyAddRequest: MoneyAddRequest): Response<MoneyAddResult> {
        return OneBoSportApi.moneyService.rechargeAdd(moneyAddRequest)
    }

    suspend fun rechCheckStauts(map: HashMap<String,String>): ApiResult<String> {
        return OneBoSportApi.moneyService.rechCheckStauts(map.toMap())
    }
}