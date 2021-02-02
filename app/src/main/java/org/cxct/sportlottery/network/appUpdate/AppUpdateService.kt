package org.cxct.sportlottery.network.appUpdate

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface AppUpdateService {
    @GET
    suspend fun checkAppVersion(@Url url: String): Response<CheckAppVersionResult>
}