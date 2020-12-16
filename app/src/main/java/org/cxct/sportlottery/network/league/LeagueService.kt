package org.cxct.sportlottery.network.league

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST


const val LEAGUE_LIST = "/api/front/match/league/list"

interface LeagueService {

    @Headers("x-session-platform-code:plat1")
    @POST(LEAGUE_LIST)
    suspend fun getLeagueList(
        @Header("x-session-token") token: String,
        @Body leagueListRequest: LeagueListRequest
    ): Response<LeagueListResponse>
}