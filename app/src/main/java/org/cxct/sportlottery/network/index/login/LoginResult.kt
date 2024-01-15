package org.cxct.sportlottery.network.index.login

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.common.BaseResult

@Parcelize
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
    val t: LoginData? = null,  // google登陆会返回这个字段
    @Json(name = "rows")
    val rows: List<LoginData>? = null
) : BaseResult(),Parcelable