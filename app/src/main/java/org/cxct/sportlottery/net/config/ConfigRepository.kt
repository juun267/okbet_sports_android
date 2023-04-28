package org.cxct.sportlottery.net.config

import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.RetrofitHolder
import org.cxct.sportlottery.net.config.api.ConfigApi

object ConfigRepository {

    val configApi by lazy { RetrofitHolder.createApiService(ConfigApi::class.java) }

    suspend fun getConfigByName(platformId: String, name: String): ApiResult<String> {
        return configApi.getConfigByName(platformId, name)
    }

}