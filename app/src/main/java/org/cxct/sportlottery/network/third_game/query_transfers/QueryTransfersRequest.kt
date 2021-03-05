package org.cxct.sportlottery.network.third_game.query_transfers


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.common.BaseResult
import org.cxct.sportlottery.network.common.PagingParams

data class QueryTransfersRequest(
    override val page: Int? = null,
    override val pageSize: Int? = null,
) : PagingParams