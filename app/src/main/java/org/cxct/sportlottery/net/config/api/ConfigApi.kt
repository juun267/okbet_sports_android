package org.cxct.sportlottery.net.config.api

import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.network.Constants
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface ConfigApi {

    @GET(Constants.GET_CONFIG_BY_NAME)
    suspend fun getConfigByName(
        @Header("platformId") platformId: String,
        @Path("name") name: String,
    ): ApiResult<String>
}