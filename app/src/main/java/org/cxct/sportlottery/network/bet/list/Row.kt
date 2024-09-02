package org.cxct.sportlottery.network.bet.list


import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.bet.MatchOdd
import org.cxct.sportlottery.network.bet.settledDetailList.ParlayComsDetailVO

@Parcelize
@JsonClass(generateAdapter = true) @KeepMembers
data class Row (
    @Json(name = "addTime")
    val addTime: Long,
    @Json(name = "cancelReason")
    val cancelReason: String?,
    @Json(name = "gameType")
    val gameType: String,
    @Json(name = "grossWin")
    val grossWin: Double?,
    @Json(name = "matchOdds")
    val matchOdds: List<MatchOdd>,
    @Json(name = "parlayComsDetailVOs")
    val parlayComsDetailVOs: List<ParlayComsDetailVO>?,
    @Json(name = "matchType")
    val matchType: String?,
    @Json(name = "netWin")
    val netWin: Double?,
    @Json(name = "num")
    val num: Int,
    @Json(name = "orderNo")
    val orderNo: String,
    @Json(name = "parlayType")
    val parlayType: String,
    @Json(name = "rebate")
    val rebate: Double,
    @Json(name = "rebateAmount")
    val rebateAmount: Double?,
    @Json(name = "settleTime")
    val settleTime: Long?,
    @Json(name = "updateTime")
    val updateTime: Long?,
    @Json(name = "betConfirmTime")
    val betConfirmTime: Long? = 0,
    @Json(name = "stake")
    val stake: Double,
    @Json(name = "status")
    var status: Int,
    @Json(name = "totalAmount")
    val totalAmount: Double,
    @Json(name = "userId")
    val userId: Int,
    @Json(name = "userName")
    val userName: String,
    @Json(name = "win")
    val win: Double?,
    @Json(name = "winnable")
    val winnable: Double,
    @Json(name = "userPlayAmount")
    val userPlayAmount: Double?,
    @Json(name = "cancelledBy")
    val cancelledBy: String?, //备注栏位："mts" (风控) => "因系统审核不成立","own"、"source" (后台) => "因机制审核不成立"
    @Json(name = "cashoutStatus")
    val orginCashoutStatus: Int = 0,//cashout 狀態 0:不可 ,1:可 ,
    @Json(name = "cashoutMsg")
    var cashoutMsg: String?=null,//不能cashout 原因
    @Json(name = "cashoutAmount")
    var cashoutAmount: String?,//cashout 金额
    @Json(name = "uniqNo")
    val uniqNo: String, //新字段，订单号
): Parcelable{
    var cashoutStatusShow: Int = orginCashoutStatus
    set(value) {if (orginCashoutStatus==1) field=value}
    //cashout 操作按钮的状态 1确认中,2下注中,
   var cashoutOperationStatus: Int=0
}