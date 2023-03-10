package org.cxct.sportlottery.network.index.config

import com.squareup.moshi.JsonClass import org.cxct.sportlottery.proguard.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class Lhc(
    val blue: String?, //蓝
    val green: String?, //绿
    val huo: String?, //火
    val jin: String?, //金（eg。03,04,17,18,25,26,33,34,47,48）
    val mu: String?, //木
    val red: String?, ////红（eg。02,14,26,38）
    val shui: String?, //水
    val sx: String?, //当前生肖（eg。鼠）
    val sxMap: Map<String,String>?, //生肖年份（eg。{"鼠":["02,14,26,38"],"牛":["03,15,27,39"]}）
    val tu: String? //土
)