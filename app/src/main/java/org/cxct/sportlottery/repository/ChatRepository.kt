package org.cxct.sportlottery.repository

import com.google.gson.JsonElement
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.chat.UserLevelConfigVO
import org.cxct.sportlottery.network.chat.checktoken.TokenResult
import org.cxct.sportlottery.network.chat.getUnPacket.GetUnPacketResult
import org.cxct.sportlottery.network.chat.getUnPacket.Row
import org.cxct.sportlottery.network.chat.getUnPacket.UnPacketRequest
import org.cxct.sportlottery.network.chat.guestInit.GuestInitResult
import org.cxct.sportlottery.network.chat.init.InitResult
import org.cxct.sportlottery.network.chat.joinRoom.JoinRoomResult
import org.cxct.sportlottery.network.chat.leaveRoom.LeaveRoomResult
import org.cxct.sportlottery.network.chat.luckyBag.LuckyBagRequest
import org.cxct.sportlottery.network.chat.luckyBag.LuckyBagResult
import org.cxct.sportlottery.network.chat.queryList.QueryListResult
import org.cxct.sportlottery.network.chat.removeMessage.RemoveMessageResult
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.ChatReceiveContent
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.subscribeSuccess.SubscribeSuccessResult
import retrofit2.Response
import timber.log.Timber

/**
 * @author kevin
 * @create 2023/3/9
 * @description
 */
object ChatRepository {

    var uniqueChatMessageList = mutableListOf<ChatReceiveContent<*>>()
    var chatToken: String? = ""
    var chatRoomID: Int = -1
    var userId: Int? = null
    var userCurrency: String? = ""
    var userLevelConfigVO: UserLevelConfigVO? = null //用户权限
    var unPacketList: MutableList<Row>? = mutableListOf()

    suspend fun queryList(): Response<QueryListResult> = OneBoSportApi.chatService.queryList()
    suspend fun luckyBag(luckyBagRequest: LuckyBagRequest): Response<LuckyBagResult> =
        OneBoSportApi.chatService.luckyBag(chatToken, luckyBagRequest)

    suspend fun getUnPacket(
        roomId: Int,
        unPacketRequest: UnPacketRequest,
    ): Response<GetUnPacketResult> =
        OneBoSportApi.chatService.getUnPacket(chatToken, roomId, unPacketRequest)

    suspend fun joinRoom(roomId: Int): Response<JoinRoomResult> =
        OneBoSportApi.chatService.joinRoom(chatToken, roomId)

    suspend fun leaveRoom(roomId: Int): Response<LeaveRoomResult> =
        OneBoSportApi.chatService.leaveRoom(chatToken, roomId)

    suspend fun removeMessage(roomId: Int, messageId: String): Response<RemoveMessageResult> =
        OneBoSportApi.chatService.removeMessage(chatToken, roomId, messageId)

    suspend fun checkToken(token: String): Response<TokenResult> =
        OneBoSportApi.chatService.checkToken(token)

    suspend fun chatInit(t: JsonElement?): Response<InitResult> =
        OneBoSportApi.chatService.chatInit(t).apply {
            if (isSuccessful && body()?.success == true) {
                chatToken = body()?.t?.token
                userId = body()?.t?.userId
                userCurrency = body()?.t?.currency
                userLevelConfigVO = body()?.t?.userLevelConfigVO
                Timber.v("[Chat] chatToken:${chatToken}")
            }
        }

    suspend fun chatGuestInit(): Response<GuestInitResult> =
        OneBoSportApi.chatService.chatGuestInit().apply {
            if (isSuccessful && body()?.success == true) {
                chatToken = body()?.t?.token
                userId = body()?.t?.userId
                userCurrency = body()?.t?.currency
                userLevelConfigVO = body()?.t?.userLevelConfigVO
                Timber.v("[Chat] chatToken:${chatToken}")
            }
        }

    private val _subscribeSuccessResult = MutableSharedFlow<SubscribeSuccessResult?>()
    val subscribeSuccessResult = _subscribeSuccessResult.asSharedFlow()

    suspend fun emitSubscribeSuccessResult(subscribeSuccessResult: SubscribeSuccessResult?) {
        _subscribeSuccessResult.emit(subscribeSuccessResult)
    }

    private val _chatMessage = MutableSharedFlow<ChatReceiveContent<*>?>()
    val chatMessage = _chatMessage.asSharedFlow()

    suspend fun emitChatMessage(chatMessage: ChatReceiveContent<*>?) {
        _chatMessage.emit(chatMessage)
    }

    fun clear() {
        uniqueChatMessageList.clear()
    }
}