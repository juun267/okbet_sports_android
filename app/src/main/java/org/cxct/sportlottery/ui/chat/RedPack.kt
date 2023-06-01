package org.cxct.sportlottery.ui.chat

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class RedPack : Serializable {
    @SerializedName("id")
    var id: Int? = null

    @SerializedName("totalMoney")
    var totalMoney: Double? = null

    @SerializedName("totalNum")
    var totalNum: Int? = null

    @SerializedName("surplusNum")
    var surplusNum: Int? = null

    @SerializedName("surplusMoney")
    var surplusMoney: Double? = null

    @SerializedName("rechMoney")
    var rechMoney: Double? = null

    @SerializedName("betMoney")
    var betMoney: Double? = null

    @SerializedName("status")
    var status //initial, opening, award, missed, condition, dismiss, timeout
            : String? = null

    @SerializedName("packetType")
    var packetType: String? = null

    @SerializedName("currency")
    var currency: String? = null

    @SerializedName("award")
    var award: Double? = null
}
