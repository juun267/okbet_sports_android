package org.cxct.sportlottery.repository

import com.google.gson.JsonElement
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.cxct.sportlottery.common.extentions.safeApi
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.RetrofitHolder
import org.cxct.sportlottery.net.chat.api.ChatService
import org.cxct.sportlottery.network.chat.UserLevelConfigVO
import org.cxct.sportlottery.net.chat.data.UnPacketRow
import org.cxct.sportlottery.net.chat.data.GuestInitResponse
import org.cxct.sportlottery.net.chat.data.ChatInitResponse
import org.cxct.sportlottery.net.chat.data.ChatStickerRow
import org.cxct.sportlottery.net.chat.data.JoinRoomResonse
import org.cxct.sportlottery.net.chat.data.Row
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.ChatReceiveContent
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.subscribeSuccess.SubscribeSuccessResult
import org.json.JSONObject
import timber.log.Timber

/**
 * @author kevin
 * @create 2023/3/9
 * @description
 */
object ChatRepository {

    private val chatApi  by lazy { RetrofitHolder.createChatApiService(ChatService::class.java) }

    var chatToken: String? = ""
    var chatRoomID: Int = -1
    var userId: Int? = null
    var userCurrency: String? = ""
    var userLevelConfigVO: UserLevelConfigVO? = null //用户权限
    var unPacketList: MutableList<UnPacketRow>? = mutableListOf()
    var chatRoom : Row?=null

    suspend fun queryList(): ApiResult<List<Row>> = safeApi {
        chatApi.queryList()
    }

    suspend fun getChatStickers():ApiResult<List<ChatStickerRow>> = safeApi {
        //List<ChatSticker>
        chatApi.getChatStickers()
    }

    suspend fun luckyBag(packetId: Int, watchWord: String): ApiResult<String> = safeApi {
        val params = JSONObject()
        params.put("packetId", packetId)
        params.put("watchWord", watchWord)
        chatApi.luckyBag(params)
    }

    suspend fun getUnPacket(
        roomId: Int,
    ): ApiResult<List<UnPacketRow>> = safeApi {
        chatApi.getUnPacket(roomId, mapOf("roomId" to roomId))
    }

    suspend fun joinRoom(roomId: Int): ApiResult<JoinRoomResonse> = safeApi {
        chatApi.joinRoom(roomId)
    }

    suspend fun leaveRoom(roomId: Int): ApiResult<String> = safeApi {
        chatApi.leaveRoom(roomId)
    }

    suspend fun removeMessage(roomId: Int, messageId: String): ApiResult<String> = safeApi {
        chatApi.removeMessage(roomId, messageId)
    }

    suspend fun checkToken(token: String): ApiResult<String> = safeApi { chatApi.checkToken(token) }


    suspend fun chatInit(t: JsonElement): ApiResult<ChatInitResponse> = safeApi {
        chatApi.chatInit(t).apply {
            if (succeeded()) {
                getData()?.let {
                    chatToken = it.token
                    userId = it.userId
                    userCurrency = it.currency
                    userLevelConfigVO = it.userLevelConfigVO
                }
                Timber.v("[Chat] chatToken:${chatToken}")
            }
        }
    }


    suspend fun chatGuestInit(): ApiResult<GuestInitResponse> = safeApi {
        chatApi.chatGuestInit().apply {
            if (succeeded()) {
                getData()?.let {
                    chatToken = it.token
                    userId = it.userId
                    userCurrency = it.currency
                    userLevelConfigVO = it.userLevelConfigVO
                    Timber.v("[Chat] chatToken:${chatToken}")
                }
            }

        }
    }

    private val _subscribeSuccessResult = MutableSharedFlow<SubscribeSuccessResult?>()
    val subscribeSuccessResult = _subscribeSuccessResult.asSharedFlow()

    suspend fun emitSubscribeSuccessResult(subscribeSuccessResult: SubscribeSuccessResult?) {
        _subscribeSuccessResult.emit(subscribeSuccessResult)
    }

    private val _chatMessage = MutableSharedFlow<ChatReceiveContent<*>?>()
    val chatMessage = _chatMessage.asSharedFlow()

    private val _chatConnStatus = MutableSharedFlow<Boolean>(0)
    val chatConnStatus = _chatConnStatus.asSharedFlow()


    suspend fun emitChatMessage(chatMessage: ChatReceiveContent<*>?) {
        _chatMessage.emit(chatMessage)
    }

    suspend fun emitConnStatus(enable: Boolean) {
        _chatConnStatus.emit(enable)
    }

}