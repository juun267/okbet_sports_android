package org.cxct.sportlottery.ui.chat

import android.app.Application
import androidx.lifecycle.*
import com.google.gson.JsonElement
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.collectWith
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.net.chat.data.ChatStickerRow
import org.cxct.sportlottery.net.chat.data.Row
import org.cxct.sportlottery.net.chat.data.UnPacketRow
import org.cxct.sportlottery.network.chat.UserLevelConfigVO
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.*
import org.cxct.sportlottery.network.uploadImg.UploadImgRequest
import org.cxct.sportlottery.network.user.iconUrl.IconUrlResult
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.repository.ChatRepository.chatToken
import org.cxct.sportlottery.repository.ChatRepository.userCurrency
import org.cxct.sportlottery.repository.ChatRepository.userId
import org.cxct.sportlottery.repository.ChatRepository.userLevelConfigVO
import org.cxct.sportlottery.service.ChatService
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.ui.chat.bean.*
import org.cxct.sportlottery.util.*
import timber.log.Timber
import java.util.*

/**
 * @author kevin
 * @create 2023/3/14
 * @description
 * @property uniqueChatMessageList 聊天室訊息內容
 */
class ChatViewModel(
    androidContext: Application
) : BaseSocketViewModel(
    androidContext) {

    var isFirstInit = true

    var tagPairList = mutableListOf<Pair<String, String>>() //用來比對點選過的tag

    private val maxInitRetry = 3
    private var userIsSpeak = false //用戶是否可以發言
    private var roomIsSpeak = false //聊天室是否可以發言

    private val _chatEvent = MutableSharedFlow<ChatEvent>(replay = 0)
    val chatEvent = _chatEvent.asSharedFlow()

    val editIconUrlResult: LiveData<Event<IconUrlResult?>> = AvatarRepository.editIconUrlResult

    val connStatus = ChatRepository.chatConnStatus

    private var chatSign: JsonElement? = null



    object ChatErrorCode {
        const val NOT_ENOUGH_BET_AND_RECH_MONEY = 10019
        const val PW_ERROR = 10018                  //口令錯誤特殊處理
        const val BET_NOT_ENOUGH_ERROR = 10019      //打碼量不足
        const val GOT_ALREADY = 10010
        const val NET_ERROR = 2008
    }


    fun subscribeChatRoom(roomId: String) {
        ChatService.subscribeChatRoom(roomId)
    }

    fun unSubscribeChatRoom(roomId: String) {
        ChatService.unSubscribeChatRoom(roomId)
    }

    fun subscribeChatUser(userId: String) {
        ChatService.subscribeChatUser(userId)
    }

    fun unSubscribeChatUser(userId: String) {
        ChatService.unSubscribeChatUser(userId)
    }

    fun chatSendMessage(liveMsgEntity: LiveMsgEntity) {
        ChatService.sendMessage(liveMsgEntity)
    }


    fun chatSendPictureMessage(imagePath:String){
        val msgEvent = LiveMsgEntity()
        msgEvent.content = imagePath
        msgEvent.type = ChatType.CHAT_SEND_PIC_MSG.code.toString()
        chatSendMessage(msgEvent)
    }

    private inline fun convertRoomMsg(roomMsg: ChatMessageResult): ChatRoomMsg<*, *> {
        if (roomMsg.userId == userId) {
            return ChatMineMsg(roomMsg)
        } else {
            return ChatUserMsg(roomMsg)
        }
    }

    init {

        ChatRepository.subscribeSuccessResult.collectWith(viewModelScope) {

            val messageList = it?.messageList ?: return@collectWith
            var chatMessageList = mutableListOf<ChatRoomMsg<*, *>>()

            messageList.forEach { chatMessageResult ->
                if (chatMessageResult.type == ChatMsgReceiveType.CHAT_MSG_RED_ENVELOPE) {
                    chatMessageResult.chatRedEnvelopeMessageResult = chatMessageResult.content?.fromJson()
                }
                chatMessageList.add(convertRoomMsg(chatMessageResult))
            }

            _chatEvent.emit(ChatEvent.UpdateList(chatMessageList))
            if (isFirstInit) {
                isFirstInit = false
            }
            emitChatRoomIsReady(true)

            val strList = mutableListOf<String>().apply {
                it?.bulletinList?.forEach { bulletin ->
                    add(bulletin.content)
                }
            }
            _chatEvent.emit(ChatEvent.UpdateMarquee(strList))
        }

        ChatRepository.chatMessage.collectWith(viewModelScope) { chatReceiveContent ->
            if (chatReceiveContent == null) {
                return@collectWith
            }
            when (chatReceiveContent.type) {
                //房间聊天訊息
                ChatMsgReceiveType.CHAT_MSG -> {
                    val chatMsg = chatReceiveContent.getThisContent<ChatMessageResult>() ?: return@collectWith
                    _chatEvent.emit(ChatEvent.ChatMessage(convertRoomMsg(chatMsg)))
                    _chatEvent.emit(ChatEvent.InsertMessage(chatMsg.userId == userId))
                }

                //用户发送图片讯息
                ChatMsgReceiveType.CHAT_SEND_PIC,
                ChatMsgReceiveType.CHAT_SEND_PIC_AND_TEXT -> {
                    val chatMsg = chatReceiveContent.getThisContent<ChatMessageResult>() ?: return@collectWith
                    _chatEvent.emit(ChatEvent.ChatMessage(convertRoomMsg(chatMsg)))
                    _chatEvent.emit(ChatEvent.InsertPic(chatMsg.userId == userId))
                }

                //ChatType 1001 发送红包
                ChatMsgReceiveType.CHAT_SEND_RED_ENVELOPE -> {

                    val chatContent = chatReceiveContent.content ?: return@collectWith
                    if ((chatContent as ChatRedEnvelopeResult).currency == userCurrency) {
                        //更新未領取紅包列表
                        if (ChatRepository.unPacketList?.any { it.id.toString() == chatContent.id.toString() } == false) {
                            val newUnPacket =
                                UnPacketRow(
                                    id = chatContent.id.toInt(),
                                    roomId = ChatRepository.chatRoomID,
                                    currency = chatContent.currency,
                                    rechMoney = 0,
                                    betMoney = 0,
                                    createBy = chatContent.nickName ?: "",
                                    createDate = 0,
                                    status = 0,
                                    packetType = chatContent.packetType,
                                    platformId = 0
                                )
                            ChatRepository.unPacketList?.add(0, newUnPacket)
                            _chatEvent.emit(ChatEvent.UpdateUnPacketList(chatContent.id.toString()))
                        }

                        _chatEvent.emit(ChatEvent.ChatMessage(
                            ChatRedEnvelopeMsg.ChatRedEnvelopeMsg1001(
                            chatReceiveContent as ChatReceiveContent<ChatRedEnvelopeResult>
                        )))
                        _chatEvent.emit(
                            ChatEvent.RedEnvelope(
                                chatContent.id.toString(),
                                chatContent.packetType,
                                checkIsAdminType()
                            )
                        )
                        isShowChatRedEnpView()
                    }
                }

                //ChatType 1002 用户进入房间
                ChatMsgReceiveType.CHAT_USER_ENTER -> {
                    chatReceiveContent?.getThisContent<ChatUserResult>()?.let {
                        _chatEvent.emit(ChatEvent.UpdateUserEnterList(it))
                    }
                }

                //ChatType 1003 用户离开房间
                ChatMsgReceiveType.CHAT_USER_LEAVE -> {
                    chatReceiveContent?.getThisContent<ChatUserResult>()?.let {
                        _chatEvent.emit(ChatEvent.UserLeave(it))
                    }
                }

                //ChatType 1006 推送平台聊天室是否禁言
                ChatMsgReceiveType.CHAT_SILENCE_ROOM -> {
                    chatReceiveContent?.getThisContent<ChatSilenceRoomResult>()?.let {
                        roomIsSpeak = it.isSpeak == "1" //ws推送聊天室是否禁言
                        emitIsSpeakStatus()
                    }
                }

                //ChatType 1005 推送中奖红包金额
                ChatMsgReceiveType.CHAT_WIN_RED_ENVELOPE_ROOM_NOTIFY -> {
                    val chatContent = chatReceiveContent.content ?: return@collectWith
                    if ((chatContent as ChatWinRedEnvelopeResult).currency == userCurrency) {
                        _chatEvent.emit(ChatEvent.ChatMessage(ChatWinRedEnvelopeMsg(chatReceiveContent as ChatReceiveContent<ChatWinRedEnvelopeResult>)))
                        _chatEvent.emit(ChatEvent.WinRedEnvelope)
                    }
                }

                //ChatType 1007 推送来自红包雨中奖红包通知
                ChatMsgReceiveType.CHAT_WIN_RED_ENVELOPE_RAIN_NOTIFY -> {
                    _chatEvent.emit(ChatEvent.ChatMessage(ChatWinRedEnvelopeMsg(chatReceiveContent as ChatReceiveContent<ChatWinRedEnvelopeResult>)))
                    _chatEvent.emit(ChatEvent.WinRedEnvelope)
                }

                //ChatType 1009 推送用户层级设定修改
                ChatMsgReceiveType.CHAT_UPDATE_USER_LEVEL_CONFIG -> {
                    chatReceiveContent.getThisContent<UserLevelConfigVO>()?.let {
                        if (it.code == userLevelConfigVO?.code) {
                            userLevelConfigVO = it
                            emitIsSpeakStatus()
                        }
                    }
                }

                //ChatType 2002 房间用户禁言
                ChatMsgReceiveType.CHAT_SILENCE -> {
                    userIsSpeak = false //禁言
                    _chatEvent.emit(ChatEvent.Silence)
                    emitIsSpeakStatus()
                }

                //ChatType 2003 房间用户解除禁言
                ChatMsgReceiveType.CHAT_RELIEVE_SILENCE -> {
                    userIsSpeak = true //解除禁言
                    if (checkIsSpeak()) _chatEvent.emit(ChatEvent.UnSilence) //真正能發言狀態才ChatEvent.UnSilence
                    //收到用戶解禁言event，應檢查是否可以發言&發圖
                    emitIsSpeakStatus()
                }

                //ChatType 2004 踢出房间
                ChatMsgReceiveType.CHAT_KICK_OUT -> {
                    unSubscribeChatRoomAndUser() //收到被踢出房間的event就不再更新聊天室訊息
                    _chatEvent.emit(ChatEvent.KickOut)
                }

                //ChatType 2005 发送用户个人红包
                ChatMsgReceiveType.CHAT_SEND_PERSONAL_RED_ENVELOPE -> {
                    val chatContent = chatReceiveContent.content as ChatPersonalRedEnvelopeResult? ?: return@collectWith
                    if (chatContent.currency == userCurrency) {

                        _chatEvent.emit(ChatEvent.ChatMessage(ChatRedEnvelopeMsg.ChatRedEnvelopeMsg2005(
                            chatReceiveContent as ChatReceiveContent<ChatPersonalRedEnvelopeResult>
                        )))

                        if (LoginRepository.isLogin.value == true) getUnPacket(ChatRepository.chatRoomID)

                        _chatEvent.emit(
                            ChatEvent.PersonalRedEnvelope(
                                chatContent.id.toString(),
                                chatContent.packetType ?: -1,
                                checkIsAdminType()
                            )
                        )
                        isShowChatRedEnpView()
                    }
                }

                //ChatType 2006 发送用户系统提示讯息
                ChatMsgReceiveType.CHAT_USER_PROMPT -> {
                    _chatEvent.emit(ChatEvent.ChatMessage(ChatSystemMsg(chatReceiveContent as ChatReceiveContent<ChatMessageResult>)))
                    _chatEvent.emit(ChatEvent.UserSystemPrompt)
                }

                //ChatType 2007 删除消息
                ChatMsgReceiveType.CHAT_MSG_REMOVE -> {
                    val result = chatReceiveContent.getThisContent<ChatRemoveMsgResult>() ?: return@collectWith
                    if (result.messageId.isEmptyStr()) {
                        return@collectWith
                    }
                    _chatEvent.emit(ChatEvent.RemoveMsg(result.messageId!!))
                }

                //ChatType 2008 房间用户红包消息
                ChatMsgReceiveType.CHAT_MSG_RED_ENVELOPE -> {
                    val chatContent = chatReceiveContent.getThisContent<ChatPersonalRedEnvelopeResult>() ?: return@collectWith
                    if (chatContent.currency == userCurrency) {
                        _chatEvent.emit(ChatEvent.ChatMessage(ChatRedEnvelopeMsg.ChatRedEnvelopeMsg2008(
                            chatReceiveContent as ChatReceiveContent<ChatMessageResult>
                        )))
                        _chatEvent.emit(ChatEvent.RedEnvelopeMsg)
                    }
                }

                //ChatType 2009 推送会员用户层级变更
                ChatMsgReceiveType.CHAT_UPDATE_MEMBER -> {
                    updateUserLevelConfigFromMemberChange()
                }

                //异常信息
                ChatMsgReceiveType.CHAT_ERROR -> {

                }
            }

        }

    }


    override fun checkLoginStatus(): Boolean {
        viewModelScope.launch {
           queryList().join()
            if (LoginRepository.isLogin.value == true) {
                Timber.i("[Chat] 已登入(一般用户,游客) 執行chatInit")
                chatInit()
            } else {
                Timber.i("[Chat] 未登入(訪客) 執行guestChatInit")
                guestChatInit()
            }
        }
        return super.checkLoginStatus()
    }

    private fun updateUserLevelConfigFromMemberChange() {
        val sign = chatSign ?: return
        launch {
            ChatRepository.chatInit(sign).let {
                if (it.succeeded()) {
                    userLevelConfigVO = it.getData()?.userLevelConfigVO
                    _chatEvent.emit(ChatEvent.IsAdminType(checkIsAdminType()))
                    isShowChatRedEnpView()
                    emitIsSpeakStatus()
                } else {
                    _chatEvent.emit(ChatEvent.InitFail(it.msg))
                }
            }
        }
    }

    fun checkChatTokenIsAlive() = launch {
        val token = chatToken.orEmpty()
        if (token.isEmptyStr()) {
            return@launch
        }

        val tokenResult = ChatRepository.checkToken(token)
        if (!tokenResult.succeeded()) {
            Timber.e("[Chat] tokenResult: $tokenResult")
            checkLoginStatus()
        }
    }

    private fun getInitErrorText(code: Int) = androidContext.getString(R.string.text_cant_play) + "($code)"
    //标记表情包是否初始化
    private var isInitEmoji=false
    /* 游客、一般用户 */
    private fun chatInit() = viewModelScope.launch {
        var sign = chatSign
        repeat(maxInitRetry) {
            Timber.i("[Chat] init, 次數 -> $it")

            var errorMsg: String? = null
            var errorCode = 0

            if (sign == null) {
                Timber.i("[Chat] 執行 getSign")
                ChatRepository.chatRoom?.let {
                    val signResult = UserInfoRepository.getSign(it.constraintType,it.dataStatisticsRange)
                    sign = signResult.getData()
                    errorMsg = signResult.msg
                }
            }

            if (sign != null) {
                val result = ChatRepository.chatInit(sign!!)
                val chatRoom = ChatRepository.chatRoom
                errorMsg = result.msg
                if (result.succeeded() && chatRoom != null) {
                    Timber.i("[Chat] 初始化成功 用戶遊客獲取房間列表")
                    enterRoom(chatRoom)
                    userIsSpeak = result?.getData()?.state == 0 //state（0正常、1禁言、2禁止登录)
                    if(!isInitEmoji){
                        isInitEmoji=true
                        getChatSticker()
                    }
                    return@launch
                } else {
                    sign = null // 置空下次循环从新获取
                    errorCode = 1
                }
            }

            if (it == maxInitRetry - 1) {
                _chatEvent.emit(ChatEvent.InitFail(errorMsg ?: getInitErrorText(errorCode)))
            }
        }

    }

    /* 访客 */
    private fun guestChatInit() = viewModelScope.launch {
        repeat(maxInitRetry) {
            Timber.i("[Chat] guest init 失敗 重新 init, 次數 -> $it")
            var result = ChatRepository.chatGuestInit()
            val chatRoom = ChatRepository.chatRoom
            if (result.succeeded() && chatRoom != null) {
                Timber.i("[Chat] 初始化成功 訪客獲取房間列表")
                enterRoom(chatRoom)
                return@launch
            }

            if (it == maxInitRetry - 1) {
                _chatEvent.emit(ChatEvent.InitFail(result.msg ?: getInitErrorText(2)))
            }
        }
    }

    /**
     * 获取聊天表情
     */
    val chatStickerEvent:SingleLiveEvent<List<ChatStickerRow>> = SingleLiveEvent()
    private fun getChatSticker()= launch {
        val chatStickerResult=ChatRepository.getChatStickers()
        if(chatStickerResult.succeeded()&&chatStickerResult.getData()!=null){
            chatStickerEvent.postValue(chatStickerResult.getData())
        }else{
            chatStickerEvent.postValue(arrayListOf())
        }
    }
    private suspend fun queryList() = viewModelScope.launch {
        val queryListResult = ChatRepository.queryList()

        if (!queryListResult.succeeded()) {
            _chatEvent.emit(ChatEvent.InitFail(queryListResult.msg))
            return@launch
        }
        val language = LanguageManager.getLanguageString2()
        ChatRepository.chatRoom = queryListResult.getData()?.find { it.language == language && it.isOpen.isStatusOpen() }
        if (ChatRepository.chatRoom == null) {
            _chatEvent.emit(ChatEvent.NoMatchRoom)
        }else {
            _chatEvent.emit(ChatEvent.IsAdminType(checkIsAdminType()))
        }
        return@launch
    }
    private suspend fun enterRoom(chatRoom: Row){
        ChatService.connect() // 链接聊天室
        if (ChatRepository.chatRoomID != chatRoom.id) {
            //背景返回之後，比較既有roomId，如果不同才重新joinRoom
            joinRoom(chatRoom)
        } else {
            subscribeRoomAndUser(chatRoom)
        }
    }

    /**
     * 獲取未領取紅包資訊
     */
    private suspend fun getUnPacket(roomId: Int) = launch {

        val getUnPacketResult = ChatRepository.getUnPacket(roomId)
        Timber.i("[Chat] 獲取未領取紅包資訊 ：\n${getUnPacketResult.getData()}")
        ChatRepository.unPacketList = getUnPacketResult.getData()?.filter { row -> row.currency == userCurrency }?.toMutableList()
        _chatEvent.emit(
            ChatEvent.GetUnPacket(getUnPacketResult, checkIsAdminType()) //未領取紅包，如為管理員則不顯示
        )
        isShowChatRedEnpView()
    }

    /**
     * 判斷懸浮紅包Icon要不要出現
     * 2-1. 【紅包】懸浮圖示僅限「會員 (已登入)」才會顯示，訪客、游客(試玩)、管理員不會顯示。
     * */
    private suspend fun isShowChatRedEnpView() {
        _chatEvent.emit(ChatEvent.ChatRedEnpViewStatus(
            !checkIsAdminType() //非管理員
                    && (ChatRepository.unPacketList?.size ?: 0) > 0 //有未領取的紅包
                    && isLogin.value == true //已登入
                    && !isGuest() //不能為訪客
            )
        )
    }

    fun getLuckyBag(packetId: Int, watchWord: String) = launch {

        val luckyBagResult = ChatRepository.luckyBag(packetId, watchWord)
        Timber.i("[Chat] 紅包結果：${luckyBagResult}")

        val resultCode = luckyBagResult.code
        if(resultCode != ChatErrorCode.PW_ERROR
            && resultCode != ChatErrorCode.NET_ERROR
            && resultCode != ChatErrorCode.BET_NOT_ENOUGH_ERROR) {
            //刪除紅包
            if(ChatRepository.unPacketList?.any { it.id.toString() == packetId.toString() } == true) {
                val newUnPacketList = ChatRepository.unPacketList?.filter { it.id.toString() != packetId.toString() }
                ChatRepository.unPacketList = newUnPacketList?.toMutableList()
                _chatEvent.emit(ChatEvent.UpdateUnPacketList(packetId.toString()))
                isShowChatRedEnpView()
            }
        }

        _chatEvent.emit(ChatEvent.GetLuckyBagResult(luckyBagResult))
    }

    private suspend fun subscribeRoomAndUser(room: Row) {
        ChatRepository.chatRoomID = room.id
        _chatEvent.emit(ChatEvent.SubscribeRoom(room.id))
        Timber.i("[Chat] 進入房間 id -> ${room.id}")
        getUnPacket(room.id)

        userId?.let {
            Timber.i("[Chat] 訂閱個人訊息通知")
            _chatEvent.emit(ChatEvent.SubscribeChatUser(it))
        }
        roomIsSpeak = room.isSpeak == "1" //加入的聊天室，是否可以發言
        emitIsSpeakStatus()
    }

    private suspend fun joinRoom(room: Row) = launch {
        val result = ChatRepository.joinRoom(room.id)
        if (result.succeeded()) {
            subscribeRoomAndUser(room)
        } else {
            _chatEvent.emit(ChatEvent.InitFail(result.msg))
        }
    }

    /**
     * 解訂閱Room和User
     */
    private fun unSubscribeChatRoomAndUser() = launch {
        _chatEvent.emit(ChatEvent.UnSubscribeRoom(ChatRepository.chatRoomID))
        userId?.let {_chatEvent.emit(ChatEvent.UnSubscribeChatUser(it)) }
    }

    private fun leaveRoom() = launch {
        clearRemoveRedEnvelope()
        ChatRepository.chatRoomID = -1
        ChatRepository.userId = null
        val result = ChatRepository.leaveRoom(ChatRepository.chatRoomID)
        if (result.succeeded()) {
            Timber.i("[Chat] 離開房間 id -> ${ChatRepository.chatRoomID}")
            unSubscribeChatRoomAndUser()
        }
    }

    suspend fun emitChatRoomIsReady(isChatRoomReady: Boolean) {
        _chatEvent.emit(ChatEvent.ChatRoomIsReady(isChatRoomReady))
    }

    //上傳聊天室圖片
    fun uploadImage(uploadImgRequest: UploadImgRequest) = launch {
        doNetwork(androidContext) { AvatarRepository.uploadChatImage(uploadImgRequest) }
    }

    /**
     * 檢查是否為管理員
     */
    private fun checkIsAdminType(): Boolean {
        return userLevelConfigVO?.type == "2" //类型 0游客、1会员、2管理員、3訪客
    }

    /**
     * 檢查是否可發言
     */
    fun checkIsSpeak(): Boolean {
        return roomIsSpeak && userIsSpeak && userLevelConfigVO?.isSpeak == "1"  //需確認room, user & level是否可發言
    }

    /**
     * 檢查是否可以發圖 (note:不可發言也不能發圖)
     */
    private fun checkIsSendImg(): Boolean {
        return if (checkIsSpeak()) userLevelConfigVO?.isSendImg == "1" else false
    }

    /**
     * 取得用戶層級最大輸入長度
     */
    private fun getInputMaxLength(): Int {
        return userLevelConfigVO?.maxLength ?: 0
    }

    /**
     * 發送是否可發言的event
     */
    private suspend fun emitIsSpeakStatus() {
        _chatEvent.emit(ChatEvent.SendMessageStatusEvent(checkIsSpeak(), getInputMaxLength(), checkIsSendImg()))
    }

    fun getHistoryMessageList()= launch {
//        _chatEvent.emit(ChatEvent.UpdateList(ChatRepository.chatRoomMessageList))
//        _chatEvent.emit(ChatEvent.ScrollToBottom)
    }

    private fun clearRemoveRedEnvelope() {

    }

    fun showPhoto(url: String) = launch {
        _chatEvent.emit(ChatEvent.ShowPhoto(url))
    }

    fun initChatClient(owner: LifecycleOwner) {

        owner.lifecycle.addObserver(object : LifecycleEventObserver {

            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {

                when (event) {
                    Lifecycle.Event.ON_START -> startCheckToken() //
                    Lifecycle.Event.ON_RESUME -> checkLoginStatus() //背景返回必須重走checkLoginStatus
                    Lifecycle.Event.ON_STOP -> stopTimer() //
                    Lifecycle.Event.ON_DESTROY -> {  // 退出房间
                        ChatService.stop()
                        leaveRoom()
                    }
                    else -> {

                    }

                }
            }
        })
    }

    private var chatTimer: Timer? = null

    private fun startCheckToken() {
        try {
            if (LoginRepository.isLogin.value == true) {
                if (chatTimer == null) {
                    chatTimer = Timer()
                    chatTimer?.schedule(object : TimerTask() {
                        override fun run() {
                            checkChatTokenIsAlive()
                        }
                    }, 30000, 30000) //延時(delay)在30s後重複執行task, 週期(period)是30s
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopTimer() {
        chatTimer?.cancel()
        chatTimer = null
    }

}