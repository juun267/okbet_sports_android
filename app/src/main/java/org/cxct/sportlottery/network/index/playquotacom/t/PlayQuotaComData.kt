package org.cxct.sportlottery.network.index.playquotacom.t

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlayQuotaComData(
    @Json(name = "OUTRIGHT@BK")
    val oUTRIGHTBK: OutrightBK,
    @Json(name = "OUTRIGHT@FT")
    val oUTRIGHTFT: OutrightFT,
    @Json(name = "OUTRIGHT@TN")
    val oUTRIGHTTN: OutrightTN,
    @Json(name = "OUTRIGHT@VB")
    val oUTRIGHTVB: OutrightVB,
    @Json(name = "OUTRIGHT@TT")
    val oUTRIGHTTT: OutrightGame,
    @Json(name = "OUTRIGHT@IH")
    val oUTRIGHTIH: OutrightGame,
    @Json(name = "OUTRIGHT@BX")
    val oUTRIGHTBX: OutrightGame,
    @Json(name = "OUTRIGHT@CB")
    val oUTRIGHTCB: OutrightGame,
    @Json(name = "OUTRIGHT@CK")
    val oUTRIGHTCK: OutrightGame,
    @Json(name = "OUTRIGHT@BB")
    val oUTRIGHTBB: OutrightGame,
    @Json(name = "OUTRIGHT@RB")
    val oUTRIGHTRB: OutrightGame,
    @Json(name = "OUTRIGHT@AFT")
    val oUTRIGHTAFT: OutrightGame,
    @Json(name = "OUTRIGHT@MR")
    val oUTRIGHTMR: OutrightGame,
    @Json(name = "OUTRIGHT@GF")
    val oUTRIGHTGF: OutrightGame,
    @Json(name = "PARLAY@BK")
    val pARLAYBK: ParlayBK,
    @Json(name = "PARLAY@FT")
    val pARLAYFT: ParlayFT,
    @Json(name = "PARLAY@TN")
    val pARLAYTN: ParlayTN,
    @Json(name = "PARLAY@VB")
    val pARLAYVB: ParlayVB,
    @Json(name = "SINGLE@BK")
    val sINGLEBK: SingleBK,
    @Json(name = "SINGLE@FT")
    val sINGLEFT: SingleFT,
    @Json(name = "SINGLE@TN")
    val sINGLETN: SingleTN,
    @Json(name = "SINGLE@VB")
    val sINGLEVB: SingleVB
)
