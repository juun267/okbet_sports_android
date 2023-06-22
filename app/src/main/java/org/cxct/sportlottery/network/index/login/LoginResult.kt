package org.cxct.sportlottery.network.index.login

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.common.BaseResult

@JsonClass(generateAdapter = true)
@KeepMembers
data class LoginResult(
    @Json(name = "code")
    override val code: Int,
    @Json(name = "msg")
    override val msg: String,
    @Json(name = "success")
    override val success: Boolean,
    @Json(name = "t")
    val t: LoginData? = null,
    @Json(name = "rows")
    val rows: List<LoginData>? = null
) : BaseResult(),java.io.Serializable