package org.cxct.sportlottery.network.index.playquotacom.t


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class T(
    @Json(name = "OUTRIGHT@BK")
    val oUTRIGHTBK: OUTRIGHTBK,
    @Json(name = "OUTRIGHT@FT")
    val oUTRIGHTFT: OUTRIGHTFT,
    @Json(name = "OUTRIGHT@TN")
    val oUTRIGHTTN: OUTRIGHTTN,
    @Json(name = "OUTRIGHT@VB")
    val oUTRIGHTVB: OUTRIGHTVB,
    @Json(name = "PARLAY@BK")
    val pARLAYBK: PARLAYBK,
    @Json(name = "PARLAY@BM")
    val pARLAYBM: PARLAYBM,
    @Json(name = "PARLAY@FT")
    val pARLAYFT: PARLAYFT,
    @Json(name = "PARLAY@TN")
    val pARLAYTN: PARLAYTN,
    @Json(name = "PARLAY@VB")
    val pARLAYVB: PARLAYVB,
    @Json(name = "SINGLE@BK")
    val sINGLEBK: SINGLEBK,
    @Json(name = "SINGLE@BM")
    val sINGLEBM: SINGLEBM,
    @Json(name = "SINGLE@FT")
    val sINGLEFT: SINGLEFT,
    @Json(name = "SINGLE@TN")
    val sINGLETN: SINGLETN,
    @Json(name = "SINGLE@VB")
    val sINGLEVB: SINGLEVB
)