package org.cxct.sportlottery.network.chat


import com.google.gson.JsonElement
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.chat.checktoken.TokenResult
import org.cxct.sportlottery.network.chat.getUnPacket.GetUnPacketResult
import org.cxct.sportlottery.network.chat.getUnPacket.UnPacketRequest
import org.cxct.sportlottery.network.chat.guestInit.GuestInitResult
import org.cxct.sportlottery.network.chat.init.InitResult
import org.cxct.sportlottery.network.chat.joinRoom.JoinRoomResult
import org.cxct.sportlottery.network.chat.leaveRoom.LeaveRoomResult
import org.cxct.sportlottery.network.chat.luckyBag.LuckyBagRequest
import org.cxct.sportlottery.network.chat.luckyBag.LuckyBagResult
import org.cxct.sportlottery.network.chat.queryList.QueryListResult
import org.cxct.sportlottery.network.chat.removeMessage.RemoveMessageResult
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * @author kevin
 * @create 2023/3/9
 * @description
 */
interface ChatService {
    @POST(Constants.ROOM_QUERY_LIST)
    suspend fun queryList(): Response<QueryListResult>

    @POST(Constants.PACKET_LUCKY_BAG)
    suspend fun luckyBag(
        @Header("x-session-token") token: String?,
        @Body luckyBagRequest: LuckyBagRequest,
    ): Response<LuckyBagResult>

    @POST(Constants.PACKET_GET_UNPACKET)
    suspend fun getUnPacket(
        @Header("x-session-token") token: String?,
        @Path("roomId") roomId: Int,
        @Body unPacketRequest: UnPacketRequest,
    ): Response<GetUnPacketResult>

    @POST(Constants.CHAT_INIT)
    suspend fun chatInit(
        @Body t: JsonElement?,
    ): Response<InitResult>

    @POST(Constants.CHAT_GUEST_INIT)
    suspend fun chatGuestInit(): Response<GuestInitResult>

    @POST(Constants.CHAT_JOIN_ROOM)
    suspend fun joinRoom(
        @Header("x-session-token") token: String?,
        @Path("roomId") roomId: Int,
    ): Response<JoinRoomResult>

    @POST(Constants.CHAT_LEAVE_ROOM)
    suspend fun leaveRoom(
        @Header("x-session-token") token: String?,
        @Path("roomId") roomId: Int,
    ): Response<LeaveRoomResult>

    @POST(Constants.CHAT_REMOVE_MESSAGE)
    suspend fun removeMessage(
        @Header("x-session-token") token: String?,
        @Path("roomId") roomId: Int,
        @Path("messageId") messageId: String,
    ): Response<RemoveMessageResult>

    @POST(Constants.CHAT_CHECK_TOKEN)
    suspend fun checkToken(
        @Header("x-session-token") token: String?,
    ): Response<TokenResult>
}
