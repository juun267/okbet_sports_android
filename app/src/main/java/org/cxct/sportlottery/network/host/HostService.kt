package org.cxct.sportlottery.network.host

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface HostService {
    @GET
    suspend fun getHost(@Url url: String): Response<HostResult>
}