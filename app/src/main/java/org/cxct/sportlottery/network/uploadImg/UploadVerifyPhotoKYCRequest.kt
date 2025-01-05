package org.cxct.sportlottery.network.uploadImg

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class UploadVerifyPhotoKYCRequest(
    val identityPhoto: String?= null,
    val identityType: Int?= null,
    val identityNumber: String?= null,
    val identityPhotoBackup: String?= null,
    val identityTypeBackup: Int?= null,
    val identityNumberBackup: String?= null,
    val verifyPhoto1: String?= null,
    val verifyPhoto2: String?= null,//后台的预留字段 暂时不用
    val firstName: String?,
    val middleName: String?,
    val lastName: String?,
    val birthday: String?,
    val version: String = "v2",
)