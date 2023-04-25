package org.cxct.sportlottery.ui.chat

import org.cxct.sportlottery.network.chat.getUnPacket.GetUnPacketResult
import org.cxct.sportlottery.network.chat.luckyBag.LuckyBagResult
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.ChatReceiveContent
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.ChatUserResult

sealed class ChatEvent {
    data class ChatRoomIsReady(val isReady: Boolean) : ChatEvent()
    data class UpdateList(val chatMessageList: MutableList<ChatReceiveContent<*>>) : ChatEvent()
    data class RemoveMsg(val position: Int) : ChatEvent()
    data class UserEnter(val chatUserResult: ChatUserResult) : ChatEvent()
    data class UserLeave(val chatUserResult: ChatUserResult) : ChatEvent()
    data class SubscribeRoom(val roomId: Int) : ChatEvent()
    data class UnSubscribeRoom(val roomId: Int) : ChatEvent()
    data class SubscribeChatUser(val userId: Int) : ChatEvent()
    data class UnSubscribeChatUser(val userId: Int) : ChatEvent()
    data class InitFail(val message: String) : ChatEvent()
    data class ActionInputSendStatusAndMaxLength(val isEnable: Boolean, val maxLength: Int) :
        ChatEvent()

    data class ActionUploadImageStatus(val isEnable: Boolean) : ChatEvent()
    data class UpdateMarquee(val marqueeList: MutableList<String>) : ChatEvent()
    data class GetLuckyBagResult(val luckyBagResult: LuckyBagResult) : ChatEvent()
    data class InsertMessage(val isMe: Boolean) : ChatEvent()
    data class InsertPic(val isMe: Boolean) : ChatEvent()
    data class UpdateUserEnterList(val userEnterList: MutableList<ChatUserResult>) : ChatEvent()
    data class GetUnPacket(val getUnPacketResult: GetUnPacketResult, val isAdmin: Boolean) :
        ChatEvent()

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
    data class RemoveRangeMessageItem(
        val chatMessageList: MutableList<ChatReceiveContent<*>>,
        val count: Int,
    ) : ChatEvent()

    data class ShowPhoto(val photoUrl: String) : ChatEvent()
    object OpenLuckyBag : ChatEvent()
    object NoMatchRoom : ChatEvent()
    object Silence : ChatEvent()
    object UnSilence : ChatEvent()
    object KickOut : ChatEvent()
    object UserSystemPrompt : ChatEvent()
    object RedEnvelopeMsg : ChatEvent()
    object InsertUserEnter : ChatEvent()
    object WinRedEnvelope : ChatEvent()
    object ScrollToBottom : ChatEvent()
    object NotifyChange : ChatEvent()
    object CheckMessageCount : ChatEvent()
}
