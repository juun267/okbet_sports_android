package org.cxct.sportlottery.net

import org.cxct.sportlottery.net.user.api.UserApiService

object ApiHolder {

    val userApi by lazy { RetrofitHolder.createApiService(UserApiService::class.java) }

}