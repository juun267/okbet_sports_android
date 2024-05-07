package org.cxct.sportlottery.net.message

import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.RetrofitHolder
import org.cxct.sportlottery.net.message.api.AnnouncementService
import org.cxct.sportlottery.network.message.Row

object AnnouncementRepository {

    private val announcementApi by lazy { RetrofitHolder.createApiService(AnnouncementService::class.java) }

    // 提款公告
    suspend fun getWithdrawAnnouncement(): ApiResult<Array<Row>> {
        return announcementApi.getPromoteNotice(arrayOf(1, 2, 3))
    }

}