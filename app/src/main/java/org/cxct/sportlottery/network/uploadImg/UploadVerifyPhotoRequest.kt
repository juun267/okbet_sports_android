package org.cxct.sportlottery.network.uploadImg


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UploadVerifyPhotoRequest(
    @Json(name = "verifyPhoto1")
    val verifyPhoto1: String,
    @Json(name = "verifyPhoto2")
    val verifyPhoto2: String
)