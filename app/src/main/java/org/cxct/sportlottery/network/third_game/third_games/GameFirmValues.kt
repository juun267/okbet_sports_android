package org.cxct.sportlottery.network.third_game.third_games

import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true)
@KeepMembers
data class GameFirmValues(
    val id: Int?, //id主键
    val firmName: String?, //厂商名称
    val firmCode: String?, //编码
    val firmType: String?, //厂商类型 。比如AG.KT,.BY
    val firmShowName: String?, //对应中文名
    val playCode: String?, //游戏编码
    val sysOpen: Int?, //系统开关状态,0-关闭，1-开启）
    val iconUrl: String?, //游戏图标地址
    val pageUrl: String?, //游戏页面地址
    val enableDemo: Int?, //试玩状态，0： 不支持1：支持关闭 2：支持开启
    val sort: Double?, //排序
    val open: Int?, //平台开关状态,0-关闭，1-开启
    val platformId: Long?, //平台ID
    val walletType: Int? //钱包类型 0多钱包,1 单钱包
)