package org.cxct.sportlottery.network.third_game.third_games

import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true)
@KeepMembers
data class GameFirmValues(
    val id: Int?=null, //id主键
    val firmName: String?=null, //厂商名称
    val firmCode: String?=null, //编码
    val firmType: String?=null, //厂商类型 。比如AG.KT,.BY
    val firmShowName: String?=null, //对应中文名
    val playCode: String?=null, //游戏编码
    val sysOpen: Int?=null, //系统开关状态,0-关闭，1-开启）
    val iconUrl: String?=null, //游戏图标地址
    val pageUrl: String?=null, //游戏页面地址
    val enableDemo: Int?=null, //试玩状态，0： 不支持1：支持关闭 2：支持开启
    val sort: Int?=null, //排序
    val open: Int?=null, //平台开关状态,0-关闭，1-开启
    val platformId: Long?=null, //平台ID
    val walletType: Int?=null, //钱包类型 0多钱包,1 单钱包
    val guestOpen:Int?=null  //访客支持状态 0： 不支持 2：支持开启
)