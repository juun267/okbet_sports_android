package org.cxct.sportlottery.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.money.MoneyAddRequest
import org.cxct.sportlottery.network.money.MoneyAddResult
import org.cxct.sportlottery.network.money.config.MoneyRechCfgResult
import org.cxct.sportlottery.network.user.UserInfo
import retrofit2.Response

object MoneyRepository {

    val userInfo: LiveData<UserInfo?>
        get() = MultiLanguagesApplication.mInstance.userInfo

    private var _moneyRechCfgResult = MutableLiveData<MoneyRechCfgResult>()
    val moneyRechCfgResult: LiveData<MoneyRechCfgResult>
        get() = _moneyRechCfgResult

    suspend fun getRechCfg(): Response<MoneyRechCfgResult> {
        return OneBoSportApi.moneyService.getRechCfg()
    }

    suspend fun rechargeAdd(moneyAddRequest: MoneyAddRequest): Response<MoneyAddResult> {
        return OneBoSportApi.moneyService.rechargeAdd(moneyAddRequest)
    }
    suspend fun checkRechargeSystem(): Response<MoneyRechCfgResult> {
        return OneBoSportApi.moneyService.getRechCfg().apply {
            if (isSuccessful) {
                _moneyRechCfgResult.value = body()
            }
        }
    }
}