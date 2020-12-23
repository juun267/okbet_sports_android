package org.cxct.sportlottery.network.league

import org.cxct.sportlottery.network.Constants.LEAGUE_LIST
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST


interface LeagueService {

    @POST(LEAGUE_LIST)
    suspend fun getLeagueList(
        @Body leagueListRequest: LeagueListRequest
    ): Response<LeagueListResult>
}