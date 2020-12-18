package org.cxct.sportlottery.repository

import android.content.Context
import android.content.SharedPreferences
import liveData
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.PagingParams
import org.cxct.sportlottery.network.common.TimeRangeParams
import org.cxct.sportlottery.network.error.ErrorUtils
import org.cxct.sportlottery.network.matchresult.list.MatchResultListRequest
import org.cxct.sportlottery.network.matchresult.list.MatchResultListResult
import org.cxct.sportlottery.ui.menu.results.GameType

class SettlementRepository(private val androidContext: Context) {
    private val sharedPref: SharedPreferences by lazy {
        androidContext.getSharedPreferences(NAME_LOGIN, Context.MODE_PRIVATE)
    }

    val token = sharedPref.liveData(KEY_TOKEN, "")

    suspend fun resultList(pagingParams: PagingParams, timeRangeParams: TimeRangeParams, gameType: String): MatchResultListResult? {
        val resultResponse = OneBoSportApi.matchResultService.getMatchResultList(
            token.value ?: "", MatchResultListRequest(gameType = gameType, pagingParams = pagingParams, timeRangeParams = timeRangeParams)
        )
        if (resultResponse.isSuccessful){
            return resultResponse.body()
        }else{
            val apiError = ErrorUtils.parseError(resultResponse)
            apiError?.let {
                if (it.success != null && it.code != null && it.msg != null){
                    return MatchResultListResult(it.code, it.msg, null, it.success, 0)
                }
            }
        }
        return null
    }
}