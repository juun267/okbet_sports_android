package org.cxct.sportlottery.ui.chat

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.chat.UserLevelConfigVO
import org.cxct.sportlottery.network.chat.getUnPacket.UnPacketRequest
import org.cxct.sportlottery.network.chat.luckyBag.LuckyBagRequest
import org.cxct.sportlottery.network.chat.queryList.Row
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.*
import org.cxct.sportlottery.network.uploadImg.UploadImgRequest
import org.cxct.sportlottery.network.user.iconUrl.IconUrlResult
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.repository.ChatRepository.chatToken
import org.cxct.sportlottery.repository.ChatRepository.uniqueChatMessageList
import org.cxct.sportlottery.repository.ChatRepository.userCurrency
import org.cxct.sportlottery.repository.ChatRepository.userId
import org.cxct.sportlottery.repository.ChatRepository.userLevelConfigVO
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
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
    androidContext: Application,
    val avatarRepository: AvatarRepository,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    favoriteRepository: MyFavoriteRepository,
) : BaseSocketViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository,
    favoriteRepository) {

    companion object {
        private const val MAX_MSG_SIZE = 500
        private const val REMOVE_COUNT = 100
    }

    var isFirstInit = true

    var uniqueChatUserEnterList = mutableListOf<ChatUserResult>()

    var tagPairList = mutableListOf<Pair<String, String>>() //用來比對點選過的tag

    private var initTimes: Int = 0
    private var userIsSpeak = false //用戶是否可以發言
    private var roomIsSpeak = false //聊天室是否可以發言

    private val _chatEvent = MutableSharedFlow<ChatEvent>(replay = 0)
    val chatEvent = _chatEvent.asSharedFlow()

    private val _removeRangeEvent = MutableSharedFlow<ChatEvent>(replay = 0)
    val removeRangeEvent = _removeRangeEvent.asSharedFlow()

    //上傳聊天室圖片
    val editIconUrlResult: LiveData<Event<IconUrlResult?>> = avatarRepository.editIconUrlResult
    var tempChatImgUrl: String? = null

    object ChatErrorCode {
        const val NOT_ENOUGH_BET_AND_RECH_MONEY = 10019
        const val PW_ERROR = 10018                  //口令錯誤特殊處理
        const val BET_NOT_ENOUGH_ERROR = 10019      //打碼量不足
        const val GOT_ALREADY = 10010
        const val NET_ERROR = 2008
    }

    init {
        viewModelScope.launch {
            ChatRepository.subscribeSuccessResult.collect {
                val chatMessageList = mutableListOf<ChatReceiveContent<*>>()
                it?.messageList?.forEach { chatMessageResult ->
                    if (chatMessageResult.type == ChatMsgReceiveType.CHAT_MSG_RED_ENVELOPE) {
                        chatMessageResult.chatRedEnvelopeMessageResult = chatMessageResult.content?.fromJson()
                    }
                    chatMessageList.add(
                        ChatReceiveContent(
                            content = chatMessageResult,
                            msg = null,
                            seq = null,
                            time = if (chatMessageResult.curTime.isNullOrEmpty()) null else chatMessageResult.curTime.toLong(),
                            type = chatMessageResult.type
                        ).apply {
                            isMySelf = content?.userId == userId
                        })

                }
                uniqueChatMessageList = chatMessageList
                setupDateTipsMessage()
                _chatEvent.emit(ChatEvent.UpdateList(uniqueChatMessageList))
                _chatEvent.emit(ChatEvent.NotifyChange)
                if (isFirstInit) {
                    _chatEvent.emit(ChatEvent.ScrollToBottom)
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
        }
        viewModelScope.launch {
            ChatRepository.chatMessage.collect { chatReceiveContent ->
                if (chatReceiveContent == null) {
                    return@collect
                }
                when (chatReceiveContent.type) {
                    //房间聊天訊息
                    ChatMsgReceiveType.CHAT_MSG -> {
                        chatReceiveContent.isMySelf = chatReceiveContent.getThisContent<ChatMessageResult>()?.userId == userId
                        uniqueChatMessageList.add(chatReceiveContent)
                        _chatEvent.emit(ChatEvent.UpdateList(uniqueChatMessageList))
                        _chatEvent.emit(ChatEvent.InsertMessage(chatReceiveContent.isMySelf))
                    }

                    //用户发送图片讯息
                    ChatMsgReceiveType.CHAT_SEND_PIC,
                    ChatMsgReceiveType.CHAT_SEND_PIC_AND_TEXT -> {
                        chatReceiveContent.isMySelf = chatReceiveContent.getThisContent<ChatMessageResult>()?.userId == userId
                        uniqueChatMessageList.add(chatReceiveContent)
                        _chatEvent.emit(ChatEvent.UpdateList(uniqueChatMessageList))
                        _chatEvent.emit(ChatEvent.InsertPic(chatReceiveContent.isMySelf))
                    }

                    //ChatType 1001 发送红包
                    ChatMsgReceiveType.CHAT_SEND_RED_ENVELOPE -> {
                        if ((chatReceiveContent.content as ChatRedEnvelopeResult).currency == userCurrency) {
                            //更新未領取紅包列表
                            if (ChatRepository.unPacketList?.any { it.id.toString() == chatReceiveContent.content.id.toString() } == false) {
                                val newUnPacket =
                                    org.cxct.sportlottery.network.chat.getUnPacket.Row(
                                        id = chatReceiveContent.content.id.toInt(),
                                        roomId = ChatRepository.chatRoomID,
                                        currency = chatReceiveContent.content.currency,
                                        rechMoney = 0,
                                        betMoney = 0,
                                        createBy = chatReceiveContent.content.nickName ?: "",
                                        createDate = 0,
                                        status = 0,
                                        packetType = chatReceiveContent.content.packetType,
                                        platformId = 0
                                    )
                                ChatRepository.unPacketList?.add(0, newUnPacket)
                                _chatEvent.emit(ChatEvent.UpdateUnPacketList(chatReceiveContent.content.id.toString()))
                            }

                            chatReceiveContent.let {
                                uniqueChatMessageList.add(it)
                                _chatEvent.emit(ChatEvent.UpdateList(uniqueChatMessageList))
                            }

                            _chatEvent.emit(
                                ChatEvent.RedEnvelope(
                                    chatReceiveContent.content.id.toString(),
                                    chatReceiveContent.content.packetType ?: -1,
                                    checkIsAdminType()
                                )
                            )
                            isShowChatRedEnpView()
                        }
                    }

                    //ChatType 1002 用户进入房间
                    ChatMsgReceiveType.CHAT_USER_ENTER -> {
                        chatReceiveContent?.getThisContent<ChatUserResult>()?.let {
                            uniqueChatUserEnterList.add(it)
                            _chatEvent.emit(ChatEvent.UpdateUserEnterList(uniqueChatUserEnterList))
                            _chatEvent.emit(ChatEvent.InsertUserEnter)
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
                        if ((chatReceiveContent.content as ChatWinRedEnvelopeResult).currency == userCurrency) {
                            chatReceiveContent.let {
                                uniqueChatMessageList.add(it)
                                _chatEvent.emit(ChatEvent.UpdateList(uniqueChatMessageList))
                                _chatEvent.emit(ChatEvent.WinRedEnvelope)
                            }
                        }
                    }

                    //ChatType 1007 推送来自红包雨中奖红包通知
                    ChatMsgReceiveType.CHAT_WIN_RED_ENVELOPE_RAIN_NOTIFY -> {
                        uniqueChatMessageList.add(chatReceiveContent)
                        _chatEvent.emit(ChatEvent.UpdateList(uniqueChatMessageList))
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
                        if ((chatReceiveContent?.content as ChatPersonalRedEnvelopeResult).currency == userCurrency) {
                            chatReceiveContent.let {
                                uniqueChatMessageList.add(it)
                                _chatEvent.emit(ChatEvent.UpdateList(uniqueChatMessageList))
                            }

                            if (loginRepository.isLogin.value == true) getUnPacket(ChatRepository.chatRoomID)

                            _chatEvent.emit(
                                ChatEvent.PersonalRedEnvelope(
                                    chatReceiveContent.content.id.toString(),
                                    chatReceiveContent.content.packetType ?: -1,
                                    checkIsAdminType()
                                )
                            )
                            isShowChatRedEnpView()
                        }
                    }

                    //ChatType 2006 发送用户系统提示讯息
                    ChatMsgReceiveType.CHAT_USER_PROMPT -> {
                        chatReceiveContent?.let {
                            uniqueChatMessageList.add(it)
                            _chatEvent.emit(ChatEvent.UpdateList(uniqueChatMessageList))
                        }
                        _chatEvent.emit(ChatEvent.UserSystemPrompt)
                    }

                    //ChatType 2007 删除消息
                    ChatMsgReceiveType.CHAT_MSG_REMOVE -> {
                        chatReceiveContent?.getThisContent<ChatRemoveMsgResult>()?.let { result ->
                            val messageItemPosition = uniqueChatMessageList.indexOf(
                                uniqueChatMessageList.find {
                                    it.content is ChatMessageResult && it.content.messageId == result.messageId
                                }
                            )
                            uniqueChatMessageList.removeAt(messageItemPosition)
                            _chatEvent.emit(ChatEvent.UpdateList(uniqueChatMessageList))
                            _chatEvent.emit(ChatEvent.RemoveMsg(messageItemPosition))
                        }
                    }

                    //ChatType 2008 房间用户红包消息
                    ChatMsgReceiveType.CHAT_MSG_RED_ENVELOPE -> {
                        if ((chatReceiveContent?.content as ChatPersonalRedEnvelopeResult).currency == userCurrency) {
                            chatReceiveContent.let {
                                uniqueChatMessageList.add(it)
                                _chatEvent.emit(ChatEvent.UpdateList(uniqueChatMessageList))
                            }
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
                _chatEvent.emit(ChatEvent.CheckMessageCount)
            }
        }

        viewModelScope.launch {
            chatEvent.collect { chatEvent ->
                when (chatEvent) {
                    is ChatEvent.CheckMessageCount -> {
                        if (uniqueChatMessageList.size > MAX_MSG_SIZE){
                            val originalMessageList = uniqueChatMessageList.filter { !it.isCustomMessage }.toMutableList()
                            uniqueChatMessageList = originalMessageList.subList(REMOVE_COUNT, originalMessageList.count())
                            setupDateTipsMessage()
                            _removeRangeEvent.emit(ChatEvent.UpdateList(uniqueChatMessageList))
                            _removeRangeEvent.emit(ChatEvent.RemoveRangeMessageItem(uniqueChatMessageList, REMOVE_COUNT))
                        }
                    }
                }
            }
        }
    }

    /**
     * 將歷史訊息配置時間提示訊息
     */
    private fun setupDateTipsMessage() {
        val originalMessageList = uniqueChatMessageList.filter { !it.isCustomMessage }.toMutableList()
        val groupMessage = originalMessageList.groupBy {
            getChatDateByTimeStamp(getChatMessageTime(it))
        }
        groupMessage.keys.forEach { groupDate ->
            val messageDateContent = when (groupDate) {
                getChatDateByTimeStamp(System.currentTimeMillis()) -> "Today" //需求要求寫死英文Today
                else -> groupDate
            }
            val firstDateMessageIndex =
                originalMessageList.indexOfFirst { getChatDateByTimeStamp(getChatMessageTime(it)) == groupDate }
            originalMessageList.add(
                firstDateMessageIndex,
                ChatReceiveContent(
                    content = messageDateContent,
                    msg = null,
                    seq = null,
                    time = null,
                    type = ChatMsgCustomType.DATE_TIP
                ).apply { isCustomMessage = true },
            )
        }
        uniqueChatMessageList = originalMessageList
    }

    /**
     * 獲取聊天訊息中的時間參數(時間戳 Long)
     */
    private fun getChatMessageTime(chatReceiveContent: ChatReceiveContent<*>): Long? {
        return when (val messageContent = chatReceiveContent.content) {
            is ChatMessageResult -> {
                messageContent.curTime?.toLong()
            }
            else -> {
                chatReceiveContent.time
            }
        }
    }

    /**
     * 時間戳轉聊天室日期格式
     */
    private fun getChatDateByTimeStamp(timeStamp: Long?): String? {
        return TimeUtil.timeStampToDateString(timeStamp, TimeUtil.D_NARROW_MONTH, Locale.US)
    }

    override fun checkLoginStatus(): Boolean {
        when (loginRepository.isLogin.value) {
            true -> {
                Timber.i("[Chat] 已登入(一般用户,游客) 執行chatInit")
                chatInit()
            }
            else -> {
                Timber.i("[Chat] 未登入(訪客) 執行guestChatInit")
                guestChatInit()
            }
        }
        return super.checkLoginStatus()
    }

    private fun updateUserLevelConfigFromMemberChange() {
        viewModelScope.launch {
            val sign = userInfoRepository.chatSign
            doNetwork {
                ChatRepository.chatInit(sign)
            }?.let {
                if (it.success) {
                    userLevelConfigVO = it.t?.userLevelConfigVO
                    _chatEvent.emit(ChatEvent.IsAdminType(checkIsAdminType()))
                    _chatEvent.emit(ChatEvent.NotifyChange)
                    isShowChatRedEnpView()
                    emitIsSpeakStatus()
                } else {
                    _chatEvent.emit(ChatEvent.InitFail(it.msg))
                }
            }
        }
    }

    fun checkChatTokenIsAlive() {
        viewModelScope.launch {
            val token = chatToken.orEmpty()
            if (token.isNotEmpty()) {
                doNetwork(androidContext) {
                    ChatRepository.checkToken(token)
                }.let { tokenResult ->
                    if (tokenResult?.success == false) {
                        Timber.e("[Chat] tokenResult: $tokenResult")
                        checkLoginStatus()
                    }
                }
            }
        }
    }

    /* 游客、一般用户 */
    private fun chatInit() {
        viewModelScope.launch {
            val sign = userInfoRepository.chatSign
            doNetwork(androidContext) {
                ChatRepository.chatInit(sign)
            }?.let {
                if (it.success) {
                    Timber.i("[Chat] 初始化成功 用戶遊客獲取房間列表")
                    initTimes = 0
                    userIsSpeak = it.t?.state == 0 //state（0正常、1禁言、2禁止登录)
                    queryList()
                } else {
                    when {
                        initTimes < 2 -> {
                            initTimes++
                            getSign()
                            Timber.i("[Chat] init 失敗 重新 getSign, 次數 -> $initTimes")
                        }
                        else -> {
                            _chatEvent.emit(ChatEvent.InitFail(it.msg))
                        }
                    }
                }
            }
        }
    }

    /* 访客 */
    private fun guestChatInit() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                ChatRepository.chatGuestInit()
            }?.let {
                if (it.success) {
                    Timber.i("[Chat] 初始化成功 訪客獲取房間列表")
                    initTimes = 0
                    queryList()
                } else {
                    when {
                        initTimes < 2 -> {
                            initTimes++
                            guestChatInit()
                            Timber.i("[Chat] guest init 失敗 重新 init, 次數 -> $initTimes")
                        }
                        else -> {
                            _chatEvent.emit(ChatEvent.InitFail(it.msg))
                        }
                    }
                }
            }
        }
    }

    private suspend fun getSign() {
        doNetwork(androidContext) {
            userInfoRepository.getSign()
        }?.let {
            if (it.success) {
                Timber.i("[Chat] 執行 getSign")
                chatInit()
            } else {
                _chatEvent.emit(ChatEvent.InitFail(it.msg))
            }
        }
    }

    private suspend fun queryList() {
        doNetwork(androidContext) {
            ChatRepository.queryList()
        }?.let { queryListResult ->
            if (queryListResult.success) {
                queryListResult.rows?.find { it.language == LanguageManager.getLanguageString(androidContext) && it.isOpen == "1" }?.let {
                    _chatEvent.emit(ChatEvent.IsAdminType(checkIsAdminType()))
                    if (ChatRepository.chatRoomID != it.id) {
                        //背景返回之後，比較既有roomId，如果不同才重新joinRoom
                        joinRoom(it)
                    } else {
                        subscribeRoomAndUser(it)
                    }
                } ?: _chatEvent.emit(ChatEvent.NoMatchRoom)
            } else {
                _chatEvent.emit(ChatEvent.InitFail(queryListResult.msg))
            }
        }
    }

    /**
     * 獲取未領取紅包資訊
     */
    private suspend fun getUnPacket(roomId: Int) {
        doNetwork(androidContext) {
            ChatRepository.getUnPacket(roomId, UnPacketRequest(roomId))
        }?.let { getUnPacketResult ->
            Timber.i("[Chat] 獲取未領取紅包資訊 ：\n${getUnPacketResult.rows}")
            ChatRepository.unPacketList = getUnPacketResult.rows?.filter { row -> row.currency == userCurrency }?.toMutableList()
            _chatEvent.emit(
                ChatEvent.GetUnPacket(getUnPacketResult, checkIsAdminType()) //未領取紅包，如為管理員則不顯示
            )
            isShowChatRedEnpView()
        }
    }

    /**
     * 判斷懸浮紅包Icon要不要出現
     * 2-1. 【紅包】懸浮圖示僅限「會員 (已登入)」才會顯示，訪客、游客(試玩)、管理員不會顯示。
     * */
    private suspend fun isShowChatRedEnpView() {
        _chatEvent.emit(
            ChatEvent.ChatRedEnpViewStatus(
                !checkIsAdminType() //非管理員
                        && (ChatRepository.unPacketList?.size ?: 0) > 0 //有未領取的紅包
                        && isLogin.value == true //已登入
                        && !isGuest() //不能為訪客
            )
        )
    }

    fun getLuckyBag(luckyBagRequest: LuckyBagRequest) {
        viewModelScope.launch {
            doNetwork(androidContext) {
                ChatRepository.luckyBag(luckyBagRequest)
            }?.let { luckyBagResult ->
                Timber.i("[Chat] 紅包結果：${luckyBagResult}")
                when(luckyBagResult.code){
                    ChatErrorCode.PW_ERROR, ChatErrorCode.NET_ERROR, ChatErrorCode.BET_NOT_ENOUGH_ERROR -> {}
                    else -> {
                        //刪除紅包
                        if(ChatRepository.unPacketList?.any { it.id.toString() == luckyBagRequest.packetId.toString() } == true){
                            val newUnPacketList = ChatRepository.unPacketList?.filter { it.id.toString() != luckyBagRequest.packetId.toString() }
                            ChatRepository.unPacketList = newUnPacketList?.toMutableList()
                            _chatEvent.emit(ChatEvent.UpdateUnPacketList(luckyBagRequest.packetId.toString()))
                            isShowChatRedEnpView()
                        }
                    }
                }
                _chatEvent.emit(ChatEvent.GetLuckyBagResult(luckyBagResult))
            }
        }
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

    private suspend fun joinRoom(room: Row) {
        doNetwork(androidContext) {
            ChatRepository.joinRoom(room.id)
        }?.let {
            if (it.success) {
                subscribeRoomAndUser(room)
            } else {
                _chatEvent.emit(ChatEvent.InitFail(it.msg))
            }
        }
    }

    /**
     * 解訂閱Room和User
     */
    private fun unSubscribeChatRoomAndUser() {
        viewModelScope.launch {
            _chatEvent.emit(ChatEvent.UnSubscribeRoom(ChatRepository.chatRoomID))
            userId?.let {
                _chatEvent.emit(ChatEvent.UnSubscribeChatUser(it))
            }
        }
    }

    fun leaveRoom() {
        viewModelScope.launch {
            clearRemoveRedEnvelope()
            doNetwork(androidContext) {
                ChatRepository.leaveRoom(ChatRepository.chatRoomID)
            }?.let {
                if (it.success) {
                    Timber.i("[Chat] 離開房間 id -> ${ChatRepository.chatRoomID}")
                    ChatRepository.chatRoomID = -1
                    unSubscribeChatRoomAndUser()
                }
            }
        }
    }

    suspend fun emitChatRoomIsReady(isChatRoomReady: Boolean) {
        _chatEvent.emit(ChatEvent.ChatRoomIsReady(isChatRoomReady))
    }

    //上傳聊天室圖片
    fun uploadImage(uploadImgRequest: UploadImgRequest) {
        viewModelScope.launch {
            doNetwork(androidContext) {
                avatarRepository.uploadChatImage(uploadImgRequest)
            }
        }
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
        _chatEvent.emit(
            ChatEvent.ActionInputSendStatusAndMaxLength(checkIsSpeak(), getInputMaxLength())
        )
        _chatEvent.emit(ChatEvent.ActionUploadImageStatus(checkIsSendImg()))
    }

    fun getHistoryMessageList() {
        viewModelScope.launch {
            _chatEvent.emit(ChatEvent.UpdateList(uniqueChatMessageList))
            _chatEvent.emit(ChatEvent.NotifyChange)
            _chatEvent.emit(ChatEvent.ScrollToBottom)
        }
    }

    private fun clearRemoveRedEnvelope() {
        uniqueChatMessageList.retainAll {
            it.type == ChatMsgReceiveType.CHAT_MSG || it.type == ChatMsgReceiveType.CHAT_SEND_PIC
        }
    }

    fun showPhoto(url: String){
        viewModelScope.launch {
            _chatEvent.emit(ChatEvent.ShowPhoto(url))
        }
    }
}