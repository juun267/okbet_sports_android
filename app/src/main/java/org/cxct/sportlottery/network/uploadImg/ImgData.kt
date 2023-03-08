package org.cxct.sportlottery.network.uploadImg

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ImgData(
    val path: String?, //路径
    val thumb: String? //缩略图路径
)