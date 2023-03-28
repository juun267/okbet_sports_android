package org.cxct.sportlottery.network.index.validCode

import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class ValidCodeData(
    val identity: String?, //标识符，登录或再次请求验证码时，需要携带回去
    val img: String? //验证码图片的二进制URL编码字符串
)