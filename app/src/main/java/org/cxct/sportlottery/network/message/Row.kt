package org.cxct.sportlottery.network.message

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguard.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class Row(
    @Json(name = "id")
    val id: Long,
    @Json(name = "message")
    val message: String, //内容
    @Json(name = "type")
    val type: Long, //展现类型：1: 投注区底部公告 ,2: 登录弹窗公告 , 3: 未登录弹窗 , 4: 紧急全站公告
    @Json(name = "msgType")
    val msgType: Long, //消息类型：1：游戏公告，2：会员福利，3：转账须知，4：劲爆推荐，5：导航网，6：其他
    @Json(name = "addTime")
    val addTime: Long, //添加时间
    @Json(name = "title")
    val title: String, //标题
    @Json(name = "updateTime")
    val updateTime: Long, //更新时间
    @Json(name = "rechLevels")
    val rechLevels: String, //用户充值层级
    @Json(name = "sort")
    val sort: Long, //排序
    @Json(name = "platformId")
    val platformId: Long //平台id
)