package org.cxct.sportlottery.network.money.list


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.common.BaseResult

@JsonClass(generateAdapter = true) @KeepMembers
data class SportBillResult(
    @Json(name = "code")
    override val code: Int = 0,
    @Json(name = "msg")
    override val msg: String = "",
    @Json(name = "rows")
    val rows: List<Row> = listOf(),
    @Json(name = "success")
    override val success: Boolean = false,
    @Json(name = "total")
    val total: Int = 0
): BaseResult() {
    @JsonClass(generateAdapter = true) @KeepMembers
    data class Row(
        @Json(name = "addTime")
        var addTime: String = "",
        @Json(name = "balance")
        val balance: Double = 0.0,
        @Json(name = "money")
        val money: Double = 0.0,
        @Json(name = "orderNo")
        val orderNo: String = "",
        @Json(name = "tranTypeName")
        val tranTypeName: String = "",
        @Json(name = "userName")
        val userName: String = ""
    ) {
        var rechTimeStr: String = ""
        var rechDateStr: String =""
    }
}