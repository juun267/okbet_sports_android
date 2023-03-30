package org.cxct.sportlottery.network.uploadImg

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class UploadVerifyPhotoKYCRequest(
    @Json(name = "identityPhoto")
    val identityPhoto: String?= null,
    @Json(name = "identityType")
    val identityType: Int?= null,
    @Json(name = "identityNumber")
    val identityNumber: String?= null,
    @Json(name = "identityPhotoBackup")
    val identityPhotoBackup: String?= null,
    @Json(name = "identityTypeBackup")
    val identityTypeBackup: Int?= null,
    @Json(name = "identityNumberBackup")
    val identityNumberBackup: String?= null
)