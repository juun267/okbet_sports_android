package org.cxct.sportlottery.network.user.credit


import com.squareup.moshi.Json
import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class Row(
    @Json(name = "balance")
    val balance: Double?,
    @Json(name = "beginTime")
    val beginTime: Long?,
    @Json(name = "creditBalance")
    val creditBalance: Double?,
    @Json(name = "endTime")
    val endTime: Long?,
    @Json(name = "id")
    val id: Int?,
    @Json(name = "platformId")
    val platformId: Int?,
    @Json(name = "remark")
    val remark: String?,
    @Json(name = "reward")
    val reward: Double?,
    @Json(name = "serverTime")
    val serverTime: Long?,
    @Json(name = "statDate")
    val statDate: String?,
    @Json(name = "status")
    val status: Int?,
    @Json(name = "userId")
    val userId: Int?,
    @Json(name = "userName")
    val userName: String?
)