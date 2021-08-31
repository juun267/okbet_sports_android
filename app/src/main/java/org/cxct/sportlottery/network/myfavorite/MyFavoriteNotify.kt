package org.cxct.sportlottery.network.myfavorite

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.common.FavoriteType

@JsonClass(generateAdapter = true)
data class MyFavoriteNotify(
    @Json(name = "type")
    val type: FavoriteType?,
    @Json(name = "code")
    val isFavorite: Boolean?
)