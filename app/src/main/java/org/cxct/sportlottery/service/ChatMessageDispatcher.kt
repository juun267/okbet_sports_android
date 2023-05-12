package org.cxct.sportlottery.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.chat.UserLevelConfigVO
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.*
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.subscribeSuccess.SubscribeSuccessResult
import org.cxct.sportlottery.repository.ChatRepository
import org.cxct.sportlottery.ui.chat.ChatMsgReceiveType
import org.cxct.sportlottery.util.JsonUtil
import org.json.JSONObject
import org.json.JSONTokener

object ChatMessageDispatcher {

    private val contentClazz = mapOf<Int, Class<*>>(
        ChatMsgReceiveType.CHAT_MSG to ChatMessageResult::class.java,
        ChatMsgReceiveType.CHAT_SEND_PIC to ChatMessageResult::class.java,
        ChatMsgReceiveType.CHAT_SEND_PIC_AND_TEXT to ChatMessageResult::class.java,
//        ChatMsgReceiveType.CHAT_SEND_RED_ENVELOPE to ChatRedEnvelopeResult::class.java,    //  红包相关功能展示屏蔽
        ChatMsgReceiveType.CHAT_USER_ENTER to ChatUserResult::class.java,
        ChatMsgReceiveType.CHAT_SILENCE_ROOM to ChatSilenceRoomResult::class.java,
//        ChatMsgReceiveType.CHAT_WIN_RED_ENVELOPE_ROOM_NOTIFY to ChatWinRedEnvelopeResult::class.java, //  红包相关功能展示屏蔽
//        ChatMsgReceiveType.CHAT_WIN_RED_ENVELOPE_RAIN_NOTIFY to ChatWinRedEnvelopeResult::class.java, //  红包相关功能展示屏蔽
        ChatMsgReceiveType.CHAT_SILENCE to ChatPersonalMsgResult::class.java,
        ChatMsgReceiveType.CHAT_RELIEVE_SILENCE to ChatPersonalMsgResult::class.java,
        ChatMsgReceiveType.CHAT_KICK_OUT to ChatPersonalMsgResult::class.java,
//        ChatMsgReceiveType.CHAT_SEND_PERSONAL_RED_ENVELOPE to ChatPersonalRedEnvelopeResult::class.java, //  红包相关功能展示屏蔽
        ChatMsgReceiveType.CHAT_USER_PROMPT to ChatMessageResult::class.java,
        ChatMsgReceiveType.CHAT_MSG_REMOVE to ChatRemoveMsgResult::class.java,
//        ChatMsgReceiveType.CHAT_MSG_RED_ENVELOPE to ChatRedEnvelopeMessageResult::class.java, //  红包相关功能展示屏蔽
        ChatMsgReceiveType.CHAT_UPDATE_USER_LEVEL_CONFIG to UserLevelConfigVO::class.java,
        ChatMsgReceiveType.CHAT_UPDATE_MEMBER to UserLevelConfigVO::class.java,
    )

    fun onChatMessage(jString: String) = GlobalScope.launch(Dispatchers.IO) {
        val jsonObj = JSONTokener(jString).nextValue()
        if (jsonObj !is JSONObject) {
            return@launch
        }

        val jsonObject = JSONObject(jString)
        if (jsonObject.has("bulletinList") && jsonObject.has("messageList")) {
            JsonUtil.fromJson(jString, SubscribeSuccessResult::class.java)?.let {
                ChatRepository.emitSubscribeSuccessResult(it)
            }
            return@launch
        }

        val type = jsonObject.optInt("type", -1)
        if (type == -1) {
            return@launch
        }

        val clazz = contentClazz[type] ?: return@launch
        JsonUtil.fromJson(jString, ChatReceiveContent::class.java, clazz)?.let {
            ChatRepository.emitChatMessage(it)
        }

//        when (type) {
//            ChatMsgReceiveType.CHAT_MSG,
//            ChatMsgReceiveType.CHAT_SEND_PIC,
//            ChatMsgReceiveType.CHAT_SEND_PIC_AND_TEXT -> {
//                val chatMessage = jString.fromJson<ChatReceiveContent<ChatMessageResult>>()
//                ChatRepository.emitChatMessage(chatMessage)
//            }
//
//            ChatMsgReceiveType.CHAT_SEND_RED_ENVELOPE -> {
//                val chatMessage = jString.fromJson<ChatReceiveContent<ChatRedEnvelopeResult>>()
//                ChatRepository.emitChatMessage(chatMessage)
//            }
//
////                                ChatMsgReceiveType.CHAT_USER_LEAVE.code,離開房間不需要顯示
//            ChatMsgReceiveType.CHAT_USER_ENTER,
//            -> {
//                val chatMessage = jString.fromJson<ChatReceiveContent<ChatUserResult>>()
//                ChatRepository.emitChatMessage(chatMessage)
//            }
//
//            ChatMsgReceiveType.CHAT_SILENCE_ROOM -> {
//                val chatMessage = jString.fromJson<ChatReceiveContent<ChatSilenceRoomResult>>()
//                ChatRepository.emitChatMessage(chatMessage)
//            }
//
//            ChatMsgReceiveType.CHAT_WIN_RED_ENVELOPE_ROOM_NOTIFY,
//            ChatMsgReceiveType.CHAT_WIN_RED_ENVELOPE_RAIN_NOTIFY -> {
//                val chatMessage = jString.fromJson<ChatReceiveContent<ChatWinRedEnvelopeResult>>()
//                ChatRepository.emitChatMessage(chatMessage)
//            }
//
//            ChatMsgReceiveType.CHAT_SILENCE,
//            ChatMsgReceiveType.CHAT_RELIEVE_SILENCE,
//            ChatMsgReceiveType.CHAT_KICK_OUT -> {
//                val chatMessage = jString.fromJson<ChatReceiveContent<ChatPersonalMsgResult>>()
//                ChatRepository.emitChatMessage(chatMessage)
//            }
//
//            ChatMsgReceiveType.CHAT_SEND_PERSONAL_RED_ENVELOPE -> {
//                val chatMessage = jString.fromJson<ChatReceiveContent<ChatPersonalRedEnvelopeResult>>()
//                ChatRepository.emitChatMessage(chatMessage)
//            }
//
//            ChatMsgReceiveType.CHAT_USER_PROMPT -> {
//                val chatMessage = jString.fromJson<ChatReceiveContent<ChatMessageResult>>()
//                ChatRepository.emitChatMessage(chatMessage)
//            }
//
//            ChatMsgReceiveType.CHAT_MSG_REMOVE -> {
//                val chatMessage = jString.fromJson<ChatReceiveContent<ChatRemoveMsgResult>>()
//                ChatRepository.emitChatMessage(chatMessage)
//            }
//
//            ChatMsgReceiveType.CHAT_MSG_RED_ENVELOPE -> {
//                val chatMessage = jString.fromJson<ChatReceiveContent<ChatRedEnvelopeMessageResult>>()
//                ChatRepository.emitChatMessage(chatMessage)
//            }
//
//            ChatMsgReceiveType.CHAT_UPDATE_USER_LEVEL_CONFIG -> {
//                val chatMessage = jString.fromJson<ChatReceiveContent<UserLevelConfigVO>>()
//                ChatRepository.emitChatMessage(chatMessage)
//            }
//
//            ChatMsgReceiveType.CHAT_UPDATE_MEMBER -> {
//                val chatMessage = jString.fromJson<ChatReceiveContent<UserLevelConfigVO>>()
//                ChatRepository.emitChatMessage(chatMessage)
//            }
//
//            ChatMsgReceiveType.CHAT_ERROR -> {
//
//            }
//
//            else -> {}
//        }
    }

}