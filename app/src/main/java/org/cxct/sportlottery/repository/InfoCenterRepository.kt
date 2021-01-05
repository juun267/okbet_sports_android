package org.cxct.sportlottery.repository

import org.cxct.sportlottery.network.InfoCenter.InfoCenterRequest
import org.cxct.sportlottery.network.InfoCenter.InfoCenterResult
import org.cxct.sportlottery.network.OneBoSportApi
import retrofit2.Response

class InfoCenterRepository {

    suspend fun getUserNoticeList(page: Int, pageSize: Int): Response<InfoCenterResult> {
        return OneBoSportApi.infoCenterService.getInfoList(InfoCenterRequest(page, pageSize))
    }

    suspend fun setMsgReaded(msgId: String): Response<InfoCenterResult> {
        return OneBoSportApi.infoCenterService.setMsgReaded(msgId)
    }

}