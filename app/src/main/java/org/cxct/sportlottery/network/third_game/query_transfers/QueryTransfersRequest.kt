package org.cxct.sportlottery.network.third_game.query_transfers


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.common.BaseResult
import org.cxct.sportlottery.network.common.PagingParams
import org.cxct.sportlottery.network.common.TimeRangeParams

data class QueryTransfersRequest(
    override val page: Int? = null,
    override val pageSize: Int? = null,
    override val startTime: String? = null,
    override val endTime: String? = null,
    val firmTypeIn: String? = null,
    val firmTypeOut: String? = null,
    val status: Int? = null,
) : PagingParams, TimeRangeParams