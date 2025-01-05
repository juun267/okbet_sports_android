package org.cxct.sportlottery.network.money.config

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class RechCfg(
    @Json(name = "banks")
    val banks: List<RechBank>?,
    @Json(name = "exchangeList")
    val exchangeList: List<Exchange>?,
    @Json(name = "exchangeRate")
    val exchangeRate: Double?,
    @Json(name = "id")
    val id: Int,
    @Json(name = "maxMoney")
    val maxMoney: Double?,
    @Json(name = "minMoney")
    val minMoney: Double?,
    @Json(name = "onlineType")
    val onlineType: Int?,
    @Json(name = "onlineTypeId")
    val onlineTypeId: Int?,
    @Json(name = "pageDesc")
    val pageDesc: String?,
    @Json(name = "para1")
    val para1: String?,
    @Json(name = "payUrl")
    val payUrl: String?,
    @Json(name = "payee")
    val payee: String?,
    @Json(name = "payeeName")
    val payeeName: String?,
    @Json(name = "pcMobile")
    val pcMobile: Int?,
    @Json(name = "prodName")
    val prodName: String?,
    @Json(name = "qrCode")
    val qrCode: String?,
    @Json(name = "rebateFee")
    val rebateFee: Double?,
    @Json(name = "rechName")
    val rechName: String?,
    @Json(name = "rechType")
    val rechType: String?,
    @Json(name = "remark")
    val remark: String?,
    @Json(name = "open")
    var open: Int?,
    @Json(name = "frontDeskRemark")
    val frontDeskRemark: String?,
    @Json(name = "isAccount")
    val isAccount: Int,//是否需要账号
    @Json(name = "isEmail")
    val isEmail: Int,//是否需要邮箱
    @Json(name = "rebateFeeNew")
    val rebateFeeNew: Double?,//新返利
)
