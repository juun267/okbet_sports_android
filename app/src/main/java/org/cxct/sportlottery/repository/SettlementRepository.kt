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
import org.cxct.sportlottery.network.matchresult.playlist.MatchResultPlayListResult
import org.cxct.sportlottery.ui.menu.results.GameType

class SettlementRepository() {

    suspend fun resultList(
        pagingParams: PagingParams?,
        timeRangeParams: TimeRangeParams,
        gameType: String
    ): MatchResultListResult? {
        val resultResponse = OneBoSportApi.matchResultService.getMatchResultList(
            MatchResultListRequest(
                gameType = gameType,
                page = pagingParams?.page,
                pageSize = pagingParams?.pageSize,
                startTime = timeRangeParams.startTime,
                endTime = timeRangeParams.endTime
            )
        )
        if (resultResponse.isSuccessful) {
            return resultResponse.body()
        } else {
            val apiError = ErrorUtils.parseError(resultResponse)
            apiError?.let {
                if (it.success != null && it.code != null && it.msg != null) {
                    return MatchResultListResult(it.code, it.msg, null, it.success, 0)
                }
            }
        }
        return null
    }

    suspend fun resultPlayList(matchId: String): MatchResultPlayListResult? {
        val resultPlayListResponse = OneBoSportApi.matchResultService.getMatchResultPlayList(matchId = matchId)
        if (resultPlayListResponse.isSuccessful)
            return resultPlayListResponse.body()
        else{
            val apiError = ErrorUtils.parseError(resultPlayListResponse)
            apiError?.let {
                if (it.success != null && it.code != null && it.msg != null) {
                    return MatchResultPlayListResult(it.code, it.msg, null, it.success, 0)
                }
            }
        }
        return null
    }
}