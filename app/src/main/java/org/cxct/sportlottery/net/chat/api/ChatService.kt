package org.cxct.sportlottery.net.chat.api

import com.google.gson.JsonElement
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.net.chat.data.UnPacketRow
import org.cxct.sportlottery.net.chat.data.GuestInitResponse
import org.cxct.sportlottery.net.chat.data.ChatInitResponse
import org.cxct.sportlottery.net.chat.data.JoinRoomResonse
import org.cxct.sportlottery.net.chat.data.Row
import org.json.JSONObject
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ChatService {

    @POST(Constants.ROOM_QUERY_LIST)
    suspend fun queryList(): ApiResult<List<Row>>

    @POST(Constants.PACKET_LUCKY_BAG)
    suspend fun luckyBag(
        @Body luckyBagRequest: JSONObject,
    ): ApiResult<String>

    @POST(Constants.PACKET_GET_UNPACKET)
    suspend fun getUnPacket(
        @Path("roomId") roomId: Int,
        @Body unPacketRequest: Map<String, Int>,
    ): ApiResult<List<UnPacketRow>>

    @POST(Constants.CHAT_INIT)
    suspend fun chatInit(
        @Body t: JsonElement,
    ): ApiResult<ChatInitResponse>

    @POST(Constants.CHAT_GUEST_INIT)
    suspend fun chatGuestInit(): ApiResult<GuestInitResponse>

    @POST(Constants.CHAT_JOIN_ROOM)
    suspend fun joinRoom(
        @Path("roomId") roomId: Int,
    ): ApiResult<JoinRoomResonse>

    @POST(Constants.CHAT_LEAVE_ROOM)
    suspend fun leaveRoom(
        @Path("roomId") roomId: Int,
    ): ApiResult<String>

    @POST(Constants.CHAT_REMOVE_MESSAGE)
    suspend fun removeMessage(
        @Path("roomId") roomId: Int,
        @Path("messageId") messageId: String,
    ): ApiResult<String>

    @POST(Constants.CHAT_CHECK_TOKEN)
    suspend fun checkToken(
        @Header("x-session-token") token: String?,
    ): ApiResult<String>

}