package org.cxct.sportlottery.network.myfavorite.query

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.proguard.KeepMembers
import org.cxct.sportlottery.network.common.BaseResult
import org.cxct.sportlottery.network.myfavorite.MyFavorite

@JsonClass(generateAdapter = true) @KeepMembers
data class SportMenuFavoriteResult(
    @Json(name = "code")
    override val code: Int,
    @Json(name = "msg")
    override val msg: String,
    @Json(name = "success")
    override val success: Boolean,
    @Json(name = "t")
    val t: MyFavorite?
) : BaseResult()