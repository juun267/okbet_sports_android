package org.cxct.sportlottery.network.credential

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
@JsonClass(generateAdapter = true)
data class CredentialCompleteData(
    @Json(name = "result")
    val result: CredentialDetailData?,

    /**
     * 指定整个身份证明过程的运行状态
     * Success ：身份证明过程成功运行。
     * Pending :身份证明过程正在等待中。
     * Failure ：身份证明过程失败，表示证件验证、人脸验证或风控处理中至少有一个失败。
     * InProcess :身份证明过程正在进行中。
     * VoidCancelled :身份证明过程被取消。
     * VoidTimeout :身份证明过程超时。
*/
    @Json(name = "ekycResult")
    val ekycResult: String?,

    @Json(name = "extBasicInfo")
    val extBasicInfo: ExtBasicInfo?, //证件相关信息

    @Json(name = "extFaceInfo")
    val extFaceInfo: ExtFaceInfo?, //面部相关信息

    @Json(name = "extIdInfo")
    val extIdInfo: ExtIdInfo?, //面部相关信息
): Parcelable {

    private val SUCCESS = "Success"
    fun isSuccessed(): Boolean {
        return SUCCESS == ekycResult && SUCCESS == result?.resultStatus
    }
}

//enum class EkycResultType(val value: String) {
//    SUCCESS("Success"),
//    PENDING("Pending"),
//    FAILURE("Failure"),
//    IN_PROCESS("InProcess"),
//    VOID_CANCELLED("VoidCancelled"),
//    VOID_TIMEOUT("VoidTimeout")
//}