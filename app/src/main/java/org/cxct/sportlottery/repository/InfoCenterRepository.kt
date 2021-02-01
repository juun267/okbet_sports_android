package org.cxct.sportlottery.repository

import org.cxct.sportlottery.network.infoCenter.InfoCenterRequest
import org.cxct.sportlottery.network.infoCenter.InfoCenterResult
import org.cxct.sportlottery.network.OneBoSportApi
import retrofit2.Response

class InfoCenterRepository {

    suspend fun getUserNoticeList(infoCenterRequest: InfoCenterRequest): Response<InfoCenterResult> {
        return OneBoSportApi.infoCenterService.getInfoList(infoCenterRequest)
    }

    suspend fun setMsgReaded(msgId: String): Response<InfoCenterResult> {
        return OneBoSportApi.infoCenterService.setMsgReaded(msgId)
    }

}