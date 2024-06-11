package org.cxct.sportlottery.ui.maintab.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.net.games.data.OKGameBean

@Parcelize
class EnterThirdGameResult(
    val resultType: ResultType,
    val url: String?,
    val errorMsg: String? = null,
    val okGameBean: OKGameBean? = null,
    val guestLogin: Boolean = false,
):Parcelable {
    //20200302 記錄問題：新增一個 NONE type，來清除狀態，避免 fragment 畫面重啟馬上就會觸發 observe，重複開啟第三方遊戲
    enum class ResultType { SUCCESS, FAIL, NEED_REGISTER, GUEST, NONE }
}