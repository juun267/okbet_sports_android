package org.cxct.sportlottery.network.uploadImg

class UploadImgRequest(
    val projectCode: String,
    val platformCode: String,
    val userId: String,
    val expireAfterDays: String,
    val thumb: String,
    val size: String
)