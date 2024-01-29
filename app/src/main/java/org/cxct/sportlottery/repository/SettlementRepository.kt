package org.cxct.sportlottery.repository

import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.PagingParams
import org.cxct.sportlottery.network.common.TimeRangeParams
import org.cxct.sportlottery.network.error.ErrorUtils
import org.cxct.sportlottery.network.matchresult.list.MatchResultListRequest
import org.cxct.sportlottery.network.matchresult.list.MatchResultListResult
import org.cxct.sportlottery.network.matchresult.playlist.MatchResultPlayListResult
import org.cxct.sportlottery.network.outright.OutrightResultListRequest
import org.cxct.sportlottery.network.outright.OutrightResultListResult
import retrofit2.Response

object SettlementRepository {

    suspend fun resultList(
        pagingParams: PagingParams?,
        timeRangeParams: TimeRangeParams,
        gameType: String,
        matchId:String? = null ,
        leagueId:String? = null ,
    ): Response<MatchResultListResult> {

        return OneBoSportApi.matchResultService.getMatchResultList(
            MatchResultListRequest(
                gameType = gameType,
                matchId = matchId,
                leagueId = leagueId,
                page = pagingParams?.page,
                pageSize = pagingParams?.pageSize,
                startTime = timeRangeParams.startTime,
                endTime = timeRangeParams.endTime
            )
        )
    }

    suspend fun resultPlayList(matchId: String): Response<MatchResultPlayListResult> {

        return OneBoSportApi.matchResultService.getMatchResultPlayList(matchId = matchId)
    }

    suspend fun resultOutRightList(gameType: String): Response<OutrightResultListResult>{
        return OneBoSportApi.outrightService.getOutrightResultList(OutrightResultListRequest(gameType = gameType))
    }
}