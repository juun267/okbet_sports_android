package org.cxct.sportlottery.ui.chat

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.chat.data.UnPacketRow
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.ChatRoomMsg
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.ChatUserResult

sealed class ChatEvent {
    data class ChatRoomIsReady(val isReady: Boolean) : ChatEvent()
    data class UpdateList(val chatMessageList: MutableList<ChatRoomMsg<*, *>>) : ChatEvent()
    data class ChatMessage(val chatMessage: ChatRoomMsg<*, *>) : ChatEvent()
    data class RemoveMsg(val msgId: String) : ChatEvent()
    data class UserEnter(val chatUserResult: ChatUserResult) : ChatEvent()
    data class UserLeave(val chatUserResult: ChatUserResult) : ChatEvent()
    data class SubscribeRoom(val roomId: Int) : ChatEvent()
    data class UnSubscribeRoom(val roomId: Int) : ChatEvent()
    data class SubscribeChatUser(val userId: Int) : ChatEvent()
    data class UnSubscribeChatUser(val userId: Int) : ChatEvent()
    data class InitFail(val message: String) : ChatEvent()

    data class SendMessageStatusEvent(val sendTextEnabled: Boolean,
                                      val textMaxLength: Int,
                                      val uploadImgEnable: Boolean): ChatEvent()
    data class UpdateMarquee(val marqueeList: MutableList<String>) : ChatEvent()
    data class GetLuckyBagResult(val luckyBagResult: ApiResult<String>) : ChatEvent()
    data class InsertMessage(val isMe: Boolean) : ChatEvent()
    data class InsertPic(val isMe: Boolean) : ChatEvent()
    data class UpdateUserEnterList(val chatUser: ChatUserResult) : ChatEvent()
    @Parcelize
    data class GetUnPacket(val getUnPacketResult: ApiResult<List<UnPacketRow>>, val isAdmin: Boolean) :
        ChatEvent(),Parcelable

    data class RedEnvelope(val packetId: String, val packetType: Int, val isAdmin: Boolean) :
        ChatEvent()

    data class PersonalRedEnvelope(
        val packetId: String,
        val packetType: Int,
        val isAdmin: Boolean,
    ) : ChatEvent()

    data class IsAdminType(val isAdmin: Boolean) : ChatEvent()
    data class UpdateUnPacketList(val packetId: String) : ChatEvent()
    data class ChatRedEnpViewStatus(val isShow: Boolean) : ChatEvent()
    data class ShowPhoto(val photoUrl: String) : ChatEvent()
    object OpenLuckyBag : ChatEvent()
    object NoMatchRoom : ChatEvent()
    object Silence : ChatEvent()
    object UnSilence : ChatEvent()
    object KickOut : ChatEvent()
    object UserSystemPrompt : ChatEvent()
    object RedEnvelopeMsg : ChatEvent()
    object WinRedEnvelope : ChatEvent()
    object ScrollToBottom : ChatEvent()

}
