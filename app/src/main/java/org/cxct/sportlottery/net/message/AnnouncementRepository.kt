package org.cxct.sportlottery.net.message

import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.RetrofitHolder
import org.cxct.sportlottery.net.message.api.AnnouncementService
import org.cxct.sportlottery.network.message.Row

object AnnouncementRepository {

    private val announcementApi by lazy { RetrofitHolder.createApiService(AnnouncementService::class.java) }

    /**
     * typeList：1: 投注区底部公告, 2: 登录弹窗公告, 3: 未登录弹窗
     * msgType 1：游戏公告，2：会员福利，3：转账须知，4：劲爆推荐，5：存取款，6：其他
     */
    suspend fun getWithdrawAnnouncement(): ApiResult<Array<Row>> {
            return announcementApi.getPromoteNotice(arrayOf(3),5)
    }

}