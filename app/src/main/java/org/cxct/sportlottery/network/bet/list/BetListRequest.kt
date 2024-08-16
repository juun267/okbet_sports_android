package org.cxct.sportlottery.network.bet.list

import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.common.IdParams
import org.cxct.sportlottery.network.common.PagingParams
import org.cxct.sportlottery.network.common.TimeRangeParams


@KeepMembers
data class BetListRequest(
    val championOnly: Int,
    val statusList: List<Int>? = listOf(),
    val gameType: String? = null,
    val uniqNo:String? = null,
    val remark:String? = null,
    val queryTimeType:String? = "addTime",
    var playCateCode: String? = null,
    var cashoutStatusList: Int? = null,//cashout狀態 0:不可 ,1:可 ,2按鈕不可按
    override val userId: Int? = null,
    override val platformId: Int? = null,
    override val startTime: String? = null,
    override val endTime: String? = null,
    override val page: Int? = null,
    override val pageSize: Int? = null,
) : TimeRangeParams, PagingParams, IdParams
