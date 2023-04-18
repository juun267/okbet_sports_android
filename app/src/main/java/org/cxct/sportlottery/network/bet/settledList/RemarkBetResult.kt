package org.cxct.sportlottery.network.bet.settledList

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.bet.settledDetailList.ParlayComsDetailVO
import org.cxct.sportlottery.network.common.BaseResult

@JsonClass(generateAdapter = true) @KeepMembers
data class RemarkBetResult(
    override val code: Int,
    override val msg: String,
    override val success: Boolean,
    @Json(name="t")
    val remarkBetResult: RemarkBetResultList
) : BaseResult()

data class RemarkBetResultList(
    val addTime: Long,
    val betConfirmTime: Long,
    val bettingStationId: Int?,
    val cancelReason: Any?,
    val cancelledBy: Any?,
    val currency: String?,
    val gameType: String?,
    val grossWin: Any?,
    val matchOdds: List<MatchOdd>,
    val matchType: Any?,
    val netWin: Any?,
    val num: Int?,
    val orderNo: String?,
    val parlayComsDetailVOs: Any?,
    val parlayType: String?,
    val reMark: String?,
    val rebate: Double,
    val rebateAmount: Any?,
    val settleTime: Any?,
    val stake: Int?,
    val stationCode: String?,
    val status: Int?,
    val totalAmount: Int?,
    val uniqNo: String??,
    val updateTime: Long?,
    val userId: Int?,
    val userName: String??,
    val userPlayAmount: Int?,
    val validBetAmount: Int?,
    val win: Any??,
    val winnable: Double?
)

