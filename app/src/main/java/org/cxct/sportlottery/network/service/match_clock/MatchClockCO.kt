package org.cxct.sportlottery.network.service.match_clock


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.proguard.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class MatchClockCO(
    /*@Json(name = "eventDate")
    val eventDate: Long?,*/
    @Json(name = "gameType")
    val gameType: String?,
    @Json(name = "matchId")
    val matchId: String?,
    @Json(name = "matchTime")
    var matchTime: Long?, //赛事进行到的时刻，秒数,可能为空 //足球時間
    /*@Json(name = "remainingTime")
    val remainingTime: Int?,*/
    @Json(name = "remainingTimeInPeriod")
    var remainingTimeInPeriod: Long?, //赛事该阶段/该节(篮球)剩余时间，秒数,可能为空 //籃球時間
    /*@Json(name = "scheduledTime")
    val scheduledTime: Long?,*/
    /*@Json(name = "stoppageTime")
    val stoppageTime: Int?,*/
    /*@Json(name = "stoppageTimeAnnounced")
    val stoppageTimeAnnounced: Int?,*/
    @Json(name = "stopped")
    val stopped: Int? //是否计时停止 1:是 ，0：否
)