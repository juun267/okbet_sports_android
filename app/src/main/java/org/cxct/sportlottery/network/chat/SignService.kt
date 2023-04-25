package org.cxct.sportlottery.network.chat

import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.chat.getSign.GetSignResult
import retrofit2.Response
import retrofit2.http.GET

/**
 * @author Kevin
 * @create 2023/3/9
 * @description
 */
interface SignService {
    @GET(Constants.CHAT_GET_SIGN)
    suspend fun getSign(): Response<GetSignResult>
}