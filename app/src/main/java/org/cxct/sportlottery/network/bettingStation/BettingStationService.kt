package org.cxct.sportlottery.network.bettingStation

import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.NetResult
import org.cxct.sportlottery.network.index.chechBetting.CheckBettingResult
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.profileCenter.profile.Uide
import org.cxct.sportlottery.ui.profileCenter.profile.UserInfoDetailsEntity
import org.cxct.sportlottery.ui.profileCenter.profile.WorkNameEntity
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.POST

interface BettingStationService {


    @GET(Constants.BETTING_STATION_QUERY_INVITE)
    suspend fun queryPlatform(@Query("inviteCode") inviteCode: String): Response<CheckBettingResult>

    @GET(Constants.BETTING_STATION_QUERY_UWSTATION)
    suspend fun bettingStationsQueryUwStation(
        @Query("platformId") platformId: Long,
        @Query("countryId") countryId: Int,
        @Query("provinceId") provinceId: Int,
        @Query("cityId") cityId: Int
    ): Response<BettingStationResult>

    @POST(Constants.AREA_ALL)
    suspend fun areaAll(): Response<AreaAllResult>

    @POST(Constants.AREA_UNIVERSAL)
    suspend fun getAreaUniversal(): Response<AreaAllResult>

    @GET(Constants.WORKS_QUERYALL)
    suspend fun getWorksQueryAll(): Response<WorkNameEntity>

    @GET(Constants.USER_QUERYUSERINFODETAILS)
    suspend fun userQueryUserInfoDetails(): Response<UserInfoDetailsEntity>

    @POST(Constants.USER_COMPLETEUSERDETAILS)
    suspend fun userCompleteUserDetails(
        @Body uide: Uide
    ): Response<NetResult>

    @GET(Constants.BETTING_STATION_QUERY_BY_BETTING_STATION_ID)
    suspend fun queryByBettingStationId(
        @Query("platformId") platformId: Int? = sConfigData?.platformId?.toInt(),
        @Query("bettingStationId") bettingStationId: Int?
    ): Response<QueryByBettingStationIdResult>
}