package org.cxct.sportlottery.network.InfoCenter

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class InfoCenterData(
    @Json(name = "id")
    val id: Int,
    @Json(name = "title")
    val title: String?,
    @Json(name = "content")
    val content: String,
    @Json(name = "messageType")//消息类型 1:系统公告 2:赛事公告
    val messageType: Int,
    @Json(name = "matchId")//关联赛事id
    val matchId: String,
    @Json(name = "url")
    val url: String,//TODO BIll 文件寫是Int要再確認
    @Json(name = "platformId")//平台id,platformId = 0时，代表全部平台
    val platformId: Int,
    @Json(name = "addTime")//添加时间
    val addTime: String,
    @Json(name = "updateTime")//更新时间
    val updateTime: String,
    @Json(name = "startTime")
    val startTime: String,
    @Json(name = "endTime")
    val endTime: String
)
