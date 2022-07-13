package org.cxct.sportlottery.network.index.playquotacom.t

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlayQuotaComData(
    @Json(name = "OUTRIGHT@BK")
    val oUTRIGHTBK: OutrightBK?,
    @Json(name = "OUTRIGHT@FT")
    val oUTRIGHTFT: OutrightFT?,
    @Json(name = "OUTRIGHT@TN")
    val oUTRIGHTTN: OutrightTN?,
    @Json(name = "OUTRIGHT@VB")
    val oUTRIGHTVB: OutrightVB?,
    @Json(name = "OUTRIGHT@BM")
    val oUTRIGHTBM: OutrightGame?,
    @Json(name = "OUTRIGHT@TT")
    val oUTRIGHTTT: OutrightGame?,
    @Json(name = "OUTRIGHT@IH")
    val oUTRIGHTIH: OutrightGame?,
    @Json(name = "OUTRIGHT@BX")
    val oUTRIGHTBX: OutrightGame?,
    @Json(name = "OUTRIGHT@CB")
    val oUTRIGHTCB: OutrightGame?,
    @Json(name = "OUTRIGHT@CK")
    val oUTRIGHTCK: OutrightGame?,
    @Json(name = "OUTRIGHT@BB")
    val oUTRIGHTBB: OutrightGame?,
    @Json(name = "OUTRIGHT@RB")
    val oUTRIGHTRB: OutrightGame?,
    @Json(name = "OUTRIGHT@AFT")
    val oUTRIGHTAFT: OutrightGame?,
    @Json(name = "OUTRIGHT@MR")
    val oUTRIGHTMR: OutrightGame?,
    @Json(name = "OUTRIGHT@GF")
    val oUTRIGHTGF: OutrightGame?,
    @Json(name = "PARLAY@BK")
    val pARLAYBK: ParlayBK?,
    @Json(name = "PARLAY@FT")
    val pARLAYFT: ParlayFT?,
    @Json(name = "PARLAY@TN")
    val pARLAYTN: ParlayTN?,
    @Json(name = "PARLAY@VB")
    val pARLAYVB: ParlayVB?,
    @Json(name = "PARLAY@BM")
    val pARLAYBM: ParlayGame?,
    @Json(name = "PARLAY@TT")
    val pARLAYTT: ParlayGame?,
    @Json(name = "PARLAY@IH")
    val pARLAYIH: ParlayGame?,
    @Json(name = "PARLAY@BX")
    val pARLAYBX: ParlayGame?,
    @Json(name = "PARLAY@CB")
    val pARLAYCB: ParlayGame?,
    @Json(name = "PARLAY@CK")
    val pARLAYCK: ParlayGame?,
    @Json(name = "PARLAY@BB")
    val pARLAYBB: ParlayGame?,
    @Json(name = "PARLAY@RB")
    val pARLAYRB: ParlayGame?,
    @Json(name = "PARLAY@AFT")
    val pARLAYAFT: ParlayGame?,
    @Json(name = "PARLAY@MR")
    val pARLAYMR: ParlayGame?,
    @Json(name = "PARLAY@GF")
    val pARLAYGF: ParlayGame?,
    @Json(name = "SINGLE@BK")
    val sINGLEBK: SingleBK?,
    @Json(name = "SINGLE@FT")
    val sINGLEFT: SingleFT?,
    @Json(name = "SINGLE@TN")
    val sINGLETN: SingleTN?,
    @Json(name = "SINGLE@VB")
    val sINGLEVB: SingleVB?,
    @Json(name = "SINGLE@BM")
    val sINGLEBM: SingleGame?,
    @Json(name = "SINGLE@TT")
    val sINGLETT: SingleGame?,
    @Json(name = "SINGLE@IH")
    val sINGLEIH: SingleGame?,
    @Json(name = "SINGLE@BX")
    val sINGLEBX: SingleGame?,
    @Json(name = "SINGLE@CB")
    val sINGLECB: SingleGame?,
    @Json(name = "SINGLE@CK")
    val sINGLECK: SingleGame?,
    @Json(name = "SINGLE@BB")
    val sINGLEBB: SingleGame?,
    @Json(name = "SINGLE@RB")
    val sINGLERB: SingleGame?,
    @Json(name = "SINGLE@AFT")
    val sINGLEAFT: SingleGame?,
    @Json(name = "SINGLE@MR")
    val sINGLEMR: SingleGame?,
    @Json(name = "SINGLE@GF")
    val sINGLEGF: SingleGame?

)
