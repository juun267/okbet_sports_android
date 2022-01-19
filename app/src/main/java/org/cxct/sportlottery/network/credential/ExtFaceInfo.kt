package org.cxct.sportlottery.network.credential

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class ExtFaceInfo (
    /**
     * 指定人脸验证进程的运行状态。可能的值及其含义如下：
     * Success ：人脸验证过程成功运行。
     * Pending ：人脸验证过程正在等待中。
     * Failure ：人脸验证过程失败。
     */
    @Json(name = "ekycResultFace")
    val ekycResultFace: String?,

    @Json(name = "faceImg")
    val faceImg: String?, //面部自拍图像，采用 base64 编码

    @Json(name = "faceLivenessResult")
    val faceLivenessResult: String?, //面部活力结果
)
