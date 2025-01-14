package org.cxct.sportlottery.network.uploadImg

import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class ImgData(
    val path: String?, //路径
    val thumb: String? //缩略图路径
)