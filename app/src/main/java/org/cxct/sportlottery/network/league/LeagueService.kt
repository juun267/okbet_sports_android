package org.cxct.sportlottery.network.league

import org.cxct.sportlottery.network.Constants.LEAGUE_LIST
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST


interface LeagueService {

    @Headers("x-session-platform-code:plat1")
    @POST(LEAGUE_LIST)
    suspend fun getLeagueList(
        @Body leagueListRequest: LeagueListRequest
    ): Response<LeagueListResponse>
}