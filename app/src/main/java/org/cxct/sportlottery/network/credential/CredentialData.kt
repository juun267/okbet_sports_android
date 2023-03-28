package org.cxct.sportlottery.network.credential

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class CredentialData(
    @Json(name = "result")
    val result: CredentialDetailData?,
    @Json(name = "transactionId")
    val transactionId: String?, //事务唯一id
    @Json(name = "clientCfg")
    val clientCfg: String?, //客户端配置信息，包括SDK连接和行为参数
    @Json(name = "rsaPubKey")
    val rsaPubKey: String?, //rsa公钥需要传给sdk端
)