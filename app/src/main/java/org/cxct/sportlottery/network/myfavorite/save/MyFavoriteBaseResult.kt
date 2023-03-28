package org.cxct.sportlottery.network.myfavorite.save

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.common.BaseResult
import org.cxct.sportlottery.network.myfavorite.MyFavorite

@JsonClass(generateAdapter = true) @KeepMembers
data class MyFavoriteBaseResult (
    @Json(name = "code")
    override val code: Int,
    @Json(name = "msg")
    override val msg: String,
    @Json(name = "success")
    override val success: Boolean,
    @Json(name = "t")
    val t: MyFavorite?
    ): BaseResult()