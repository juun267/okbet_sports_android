//package org.cxct.sportlottery.network.odds.list
//
//import com.squareup.moshi.Json
//import com.squareup.moshi.JsonClass
//import org.cxct.sportlottery.enum.BetStatus
//import org.cxct.sportlottery.enum.OddState
//
//@JsonClass(generateAdapter = true)
//data class Odd(
//    @Json(name = "id")
//    val id: String? = "",
//    @Json(name = "odds")
//    var odds: Double? = null,
//    @Json(name = "hkOdds")
//    var hkOdds: Double? = null,
//    @Json(name = "producerId")
//    var producerId: Int? = null,
//    @Json(name = "spread")
//    val spread: String? = null,
//    @Json(name = "status")
//    var status: Int = BetStatus.ACTIVATED.code
//) : OddStateParams {
//    var isSelected: Boolean? = false
//    override var oddState: Int = OddState.SAME.state
//    @Transient
//    override var runnable: Runnable? = null
//    var outrightCateKey: String? = null
//}
//
//
