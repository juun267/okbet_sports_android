package org.cxct.sportlottery.network.index.playquotacom.t


interface PlayQuota {
    val code: String
    val gameType: String
    val id: Int
    val max: Int
    val min: Int
    val name: String
    val platformId: Int
}