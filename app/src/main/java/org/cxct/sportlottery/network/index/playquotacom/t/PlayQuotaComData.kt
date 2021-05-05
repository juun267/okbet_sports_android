package org.cxct.sportlottery.network.index.playquotacom.t

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlayQuotaComData(
    @Json(name = "OUTRIGHT@BK")
    val oUTRIGHTBK: OutrightBK,
    @Json(name = "OUTRIGHT@BM")
    val oUTRIGHTBM: OutrightBM,
    @Json(name = "OUTRIGHT@FT")
    val oUTRIGHTFT: OutrightFT,
    @Json(name = "OUTRIGHT@TN")
    val oUTRIGHTTN: OutrightTN,
    @Json(name = "OUTRIGHT@VB")
    val oUTRIGHTVB: OutrightVB,
    @Json(name = "PARLAY@BK")
    val pARLAYBK: ParlayBK,
    @Json(name = "PARLAY@BM")
    val pARLAYBM: ParlayBM,
    @Json(name = "PARLAY@FT")
    val pARLAYFT: ParlayFT,
    @Json(name = "PARLAY@TN")
    val pARLAYTN: ParlayTN,
    @Json(name = "PARLAY@VB")
    val pARLAYVB: ParlayVB,
    @Json(name = "SINGLE@BK")
    val sINGLEBK: SingleBK,
    @Json(name = "SINGLE@BM")
    val sINGLEBM: SingleBM,
    @Json(name = "SINGLE@FT")
    val sINGLEFT: SingleFT,
    @Json(name = "SINGLE@TN")
    val sINGLETN: SingleTN,
    @Json(name = "SINGLE@VB")
    val sINGLEVB: SingleVB
)
