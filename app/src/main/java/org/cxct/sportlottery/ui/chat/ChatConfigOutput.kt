package org.cxct.sportlottery.ui.chat

import com.google.gson.annotations.SerializedName
import java.io.Serializable

open class ChatConfigOutput : BaseOutput(), Serializable {

    @SerializedName("role")
    var role: Role? = null

    @SerializedName("fk")
    var fk: String? = null

    @SerializedName("nickName")
    var nickName: String? = null

    @SerializedName("likeCount")
    var likeCount: Int? = null

    @SerializedName("roleList")
    var roleList: MutableList<Role>? = null

    @SerializedName("packet")
    var packetList: MutableList<RedPack>? = null

    @SerializedName("room")
    var room: Room? = null

    @SerializedName("token")
    var token: String? = null

    @SerializedName("sid")
    var sid: String? = null

    @SerializedName("pushBet")
    var pushBet: String? = null

    @SerializedName("setted")
    var setted: String? = null // "1": 已修改過暱稱, "0" or null: 未修改

    @SerializedName("balance")
    var balance: Double? = null

    @SerializedName("serviceGroupChatOpen")
    var serviceGroupChatOpen: Int? = null

    @SerializedName("chatUserId")
    var chatUserId: Long? = null //聊天室 userId

    @SerializedName("iconUrl")
    var iconUrl: String? = null

    @SerializedName("followerCount")
    var followerCount: Int? = null

    @SerializedName("status")
    var status: String? = null // 後台設定的禁言狀態(1:禁言, 0:非禁言)

    @SerializedName("chatSpeak")
    var chatSpeak: Boolean? = null // 是否為聊天時段

    class Role : Serializable {

        @SerializedName("id")
        var id: Int? = null

        @SerializedName("name")
        var name: String? = null

        @SerializedName("growthThreshold")
        var growthThreshold: Int? = null

        @SerializedName("remark")
        var remark: String? = null

        @SerializedName("icon")
        var icon: String? = null

        @SerializedName("level")
        var level: Int? = null

        @SerializedName("bgColor")
        var bgColor: String? = null

        @SerializedName("textColor")
        var textColor: String? = null

        @SerializedName("betBgColor")
        var betBgColor: String? = null

        @SerializedName("nickTextColor")
        var nickTextColor: String? = null

        @SerializedName("createBy")
        var createBy: String? = null

        @SerializedName("createDate")
        var createDate: String? = null

        @SerializedName("updateBy")
        var updateBy: String? = null

        @SerializedName("updateDate")
        var updateDate: String? = null

        @SerializedName("platCode")
        var platCode: String? = null

        @SerializedName("isDefault")
        var isDefault: String? = null

        @SerializedName("isKick")
        var isKick: String? = null

        @SerializedName("isGag")
        var isGag: String? = null

        @SerializedName("isSpeak")
        var isSpeak: String? = null

        @SerializedName("isChat")
        var isChat: String? = null

        @SerializedName("isSendImg")
        var isSendImg: String? = null

        @SerializedName("isSendPacket")
        var isSendPacket: String? = null

        @SerializedName("isSendAudio")
        var isSendAudio: String? = null

        @SerializedName("type")
        var type: String? = null // 遊客="0", 會員="1", 管理員="2"

        @SerializedName("platName")
        var platName: String? = null

        @SerializedName("maxLength")
        var maxLength: Int? = null

    }

//    class Packet: Serializable {
//
//        @SerializedName("packetType")
//        var packetType: String? = null
//
//        @SerializedName("id")
//        var id: Int? = null
//
//        //== APP 端添加參數 ==//
//        var status: String? = null //紅包狀態, 初始接收："init", 已經彈窗過："popped", 已經開啟過："opened"
//        //==================//
//    }

    class Room : Serializable {

        @SerializedName("id")
        var id: Int? = null

        @SerializedName("name")
        var name: String? = null

        @SerializedName("platCode")
        var platCode: String? = null

        @SerializedName("roomType")
        var roomType: String? = null

        @SerializedName("platName")
        var platName: String? = null

        @SerializedName("rechMoney")
        var rechMoney: String? = null

        @SerializedName("betMoney")
        var betMoney: String? = null

        @SerializedName("createDate")
        var createDate: String? = null

        @SerializedName("remark")
        var remark: String? = null

        @SerializedName("isOpen")
        var isOpen: String? = null

        @SerializedName("isSpeak")
        var isSpeak: String? = null

        @SerializedName("isShowCount")
        var isShowCount: String? = null

        @SerializedName("basicAmount")
        var basicAmount: Int? = null

        @SerializedName("onlineCount")
        var onlineCount: Int? = null

    }
}