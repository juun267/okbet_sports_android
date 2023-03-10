package org.cxct.sportlottery.network.credential

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.proguard.KeepMembers
import kotlinx.android.parcel.Parcelize


@Parcelize
@JsonClass(generateAdapter = true) @KeepMembers
data class ExtIdInfo(
    /**
     * @param ekycResultDoc
     * Success : 文档验证过程成功运行。
     * Pending : 文件验证过程待处理。
     * Failure : 文件验证过程失败。
     */
    @Json(name = "ekycResultDoc")
    val ekycResultDoc: String?,

    @Json(name = "docEdition")
    val docEdition: Int?, //该字段的值返回为 1，而对于新的 HKID，该字段的值返回为 2。

    @Json(name = "frontPageImg")
    val frontPageImg: String?, //身份证件的正面图像

    @Json(name = "backPageImg")
    val backPageImg: String?, //身份证件的反面图像

    /**
     * @param docErrorDetails
     * NO_REQUIRED_ID : 上传图片识别的身份证件与指定的身份证件类型不匹配。
     * BLUR ：上传的身份证件图像模糊。
     * NO_FACE_DETECTED ：本应从指定的身份证件中识别出来的人脸，在上传的图像中没有按预期检测到。
     * NOT_REAL_DOC ：上传的身份证件图片被检测为假的。
     * EXPOSURE ：上传的身份证件图像曝光过度。
     * UNKNOWN : 所有其他识别错误。
     */
    @Json(name = "docErrorDetails")
    val docErrorDetails: String?,

    @Json(name = "ocrResult")
    val ocrResult: OcrResult?, //证件的详情信息

) : Parcelable

