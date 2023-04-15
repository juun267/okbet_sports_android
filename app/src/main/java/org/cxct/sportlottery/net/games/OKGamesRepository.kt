package org.cxct.sportlottery.net.games

import org.cxct.sportlottery.net.RetrofitHolder
import org.cxct.sportlottery.net.games.api.OKGamesApi

object OKGamesRepository {

    val okGamesApi by lazy { RetrofitHolder.createApiService(OKGamesApi::class.java) }


}