package org.cxct.sportlottery.network.infoCenter

import org.cxct.sportlottery.network.Constants.USER_NOTICE_LIST
import org.cxct.sportlottery.network.Constants.USER_NOTICE_READED
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface InfoCenterService {

    @POST(USER_NOTICE_LIST)
    suspend fun getInfoList(
        @Body infoCenterRequest: InfoCenterRequest
    ): Response<InfoCenterResult>

    @POST(USER_NOTICE_READED)
    suspend fun setMsgReaded(
        @Path("id") msgId: String
    ): Response<SetReadResult>
}