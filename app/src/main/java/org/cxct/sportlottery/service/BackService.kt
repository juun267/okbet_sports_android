package org.cxct.sportlottery.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.reactivex.CompletableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.common.MatchOdd
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.service.ServiceConnectStatus
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
import org.cxct.sportlottery.repository.ChatRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.chat.LiveMsgEntity
import org.cxct.sportlottery.util.EncryptUtil
import org.cxct.sportlottery.util.HTTPsUtil
import org.cxct.sportlottery.util.LanguageManager
import timber.log.Timber
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompCommand
import ua.naiksoftware.stomp.dto.StompHeader
import ua.naiksoftware.stomp.dto.StompMessage
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.util.*
import java.util.concurrent.TimeUnit


class BackService : Service() {
    companion object {

        const val MESSAGE_CATE = "message_cate"

        const val CUSTOM_DATA_MATCH_ODD = "CUSTOM_DATA_MATCH_ODD"
        const val CUSTOM_DATA_MATCH_TYPE = "CUSTOM_DATA_MATCH_TYPE"

        const val USE_SUBSCRIBE_DATA = "USE_SUBSCRIBE_DATA" //是否將第一筆訂閱後取得的資料作為新資料直接顯示, 而非用於變化更新

        const val SERVICE_SEND_DATA = "SERVICE_SEND_DATA"
        const val CHANNEL_KEY = "channel"
        const val SERVER_MESSAGE_KEY = "serverMessage"
        const val CONNECT_STATUS = "connectStatus"

        //        const val WS_END_TYPE = "proto"
        const val WS_END_TYPE = "encrypted"

        private val URL_SOCKET_HOST_AND_PORT: String get() = "${Constants.getSocketUrl()}/api/ws/app/im" //app连接端点,无sockjs
        private val URL_CHAT_SOCKET_HOST: String
            get() = "${
                sConfigData?.chatHost?.replace("https",
                    "wss")
            }"
        const val URL_ALL = "/ws/notify/all/$WS_END_TYPE" //全体公共频道
        //const val URL_ALL = "/ws/notify/all" //全体公共频道

        const val URL_PING = "/ws/ping" //心跳检测通道 （pong消息将发往用户私人频道）

        private const val SPORT_HALL_CHANNEL_LENGTH = 6

        internal var mUserId: Long? = null
        private var mPlatformId: Long? = null

        const val URL_USER = "/user/self/$WS_END_TYPE"
        val URL_USER_PRIVATE: String get() = "/ws/notify/user/$mUserId/$WS_END_TYPE"  //用户私人频道
        val URL_PLATFORM get() = "/ws/notify/platform/$mPlatformId/$WS_END_TYPE" //公共频道  这个通道会通知主站平台维护

        //const val URL_USER = "/user/self"
        //val URL_USER_PRIVATE: String get() = "/ws/notify/user/$mUserId"  //用户私人频道
        //val URL_PLATFORM get() = "/ws/notify/platform/$mPlatformId"
        const val URL_EVENT =
            "/ws/notify/event" //具体赛事/赛季频道 //(普通玩法：eventId就是matchId，冠军玩法：eventId是赛季Id)
        const val URL_HALL = "/ws/notify/hall" //大厅赔率频道 //cateMenuCode：HDP&OU=讓球&大小, 1X2=獨贏
        const val URL_CHAT_ROOM = "/ws/notify/room" //订阅聊天室
        const val URL_CHAT_USER = "/ws/notify/user" //订阅用户
        const val URL_SEND_MESSAGE = "/ws/notify/room/{roomId}/sendMessage"//描述: 传送指定房间聊天讯息

        private const val HEART_BEAT_RATE = 10 * 1000 //每隔10秒進行一次對長連線的心跳檢測
        private const val RECONNECT_LIMIT = 10 //斷線後重連次數限制
        private const val LOADING_TIME_INTERVAL: Long = 500
        private var timesTamp: Long = Date(System.currentTimeMillis()).time
    }


    private var mToken = ""
    private val mBinder: IBinder = MyBinder()
    private var mStompClient: StompClient? = null
    private var mChatStompClient: StompClient? = null
    private var mCompositeDisposable: CompositeDisposable? = null //訊息接收通道 數組
    private val mHeader: List<StompHeader> get() = listOf(StompHeader("token", mToken))
    private val mSubscribedMap = mutableMapOf<String, Disposable?>() //Map<url, channel>
    private val mOriginalSubscribedMap =
        mutableMapOf<String, Disposable?>() //投注單頁面邏輯, 紀錄進入投注單前以訂閱的頻道, 離開投注單頁面時, 解除訂閱不解除此map中的頻道
    private var mFastBetSubscribed: String? = null
    private val mSubscribeChannelPending = mutableListOf<String>()
    private var errorFlag = false // Stomp connect錯誤
    private var reconnectionNum = 0//重新連接次數
    private var delay: Boolean = false

    inner class MyBinder : Binder() {
        val service: BackService
            get() = this@BackService

        fun connect(token: String?, userId: Long, platformId: Long) {
            //未連線 或者 連線參數有變 => 重新建立連線
            if (mStompClient?.isConnected != true
                || mToken != token || mUserId != userId || mPlatformId != platformId
            ) {
                mToken = token ?: ""
                mUserId = userId
                mPlatformId = platformId

                Timber.d("==建立新連線==")
                reconnect()
            } else {
                Timber.d("==已建立連線==")
                //connect()
                sendConnectStatusToActivity(ServiceConnectStatus.CONNECTED) // TODO(測試) 不直接重新連線，改發送已連線訊號
            }
        }
    }

    override fun onDestroy() {
        Timber.d("==已斷線==")
        disconnect()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder {
        return mBinder
    }

    private fun connect() {
        try {
            Timber.i(">>>token = ${mToken}, url = $URL_SOCKET_HOST_AND_PORT")
            resetSubscriptions()

            val httpClient = HTTPsUtil.trustAllSslClient(OkHttpClient())
                .newBuilder()
                .pingInterval(40, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build()
            mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, URL_SOCKET_HOST_AND_PORT, null, httpClient)
            mStompClient?.let { stompClient ->
                stompClient.withClientHeartbeat(HEART_BEAT_RATE).withServerHeartbeat(HEART_BEAT_RATE)

                sendConnectStatusToActivity(ServiceConnectStatus.CONNECTING)

                val lifecycleDisposable =
                    stompClient.lifecycle()
                        .subscribeOn(Schedulers.io())
                        .delay(if (delay) LOADING_TIME_INTERVAL else 0, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { lifecycleEvent: LifecycleEvent ->
                            when (lifecycleEvent.type) {
                                LifecycleEvent.Type.OPENED -> {
                                    Timber.d("Stomp connection opened")
                                    sendConnectStatusToActivity(ServiceConnectStatus.CONNECTED)
                                    delay = false
                                }
                                LifecycleEvent.Type.CLOSED -> {
                                    Timber.d("Stomp connection closed")
                                    reconnectionNum++
                                    if (errorFlag && reconnectionNum < RECONNECT_LIMIT) {
                                        Timber.e("Stomp connection broken, the $reconnectionNum time reconnect.")
                                        reconnect()
                                    } else {
                                        sendConnectStatusToActivity(ServiceConnectStatus.RECONNECT_FREQUENCY_LIMIT)
                                        delay = false
                                    }
                                }
                                LifecycleEvent.Type.ERROR -> {
                                    errorFlag = true
                                    Timber.e("Stomp connection error ==> ${lifecycleEvent.exception}")
//                                    reconnect()
                                }
                                LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT -> {
                                    Timber.d("Stomp connection failed server heartbeat")
//                                    reconnect()
                                }
                                null -> Timber.e("Stomp connection failed")
                            }
                        }
                mCompositeDisposable?.add(lifecycleDisposable)

                //訂閱用戶私人頻道
                subscribeChannel(if (mToken.isEmpty()) URL_USER else URL_USER_PRIVATE)

                //訂閱全體公共頻道
                subscribeChannel(URL_ALL)
                subscribeChannel(URL_PLATFORM)

                //建立連線
                stompClient.connect(mHeader)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            when (e) {
                is SocketTimeoutException -> {
                    Timber.e("連線超時，正在重連")
                    reconnect()
                }
                is NoRouteToHostException -> {
                    Timber.e("該地址不存在，請檢查")
                    stopSelf()
                }
                is ConnectException -> {
                    Timber.e("連線異常或被拒絕，請檢查")
                    stopSelf()
                }
                else -> {
                    e.printStackTrace()
                    stopSelf()
                }
            }
        }
    }

    private fun connectChat() {
        try {
            Timber.i("[Chat] >>>token = ${mToken}, url = $URL_CHAT_SOCKET_HOST")
            resetSubscriptions()

            val httpClient = HTTPsUtil.trustAllSslClient(OkHttpClient())
                .newBuilder()
                .pingInterval(40, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build()
            val url = "${URL_CHAT_SOCKET_HOST}ws/chat/app"
            mChatStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, url, null, httpClient)
            mChatStompClient?.let { chatStompClient ->
                chatStompClient.withClientHeartbeat(10 * 1000).withServerHeartbeat(10 * 1000)

                val disposable =
                    chatStompClient.lifecycle()
                        .doOnError { throwable ->
                            Timber.d("[Chat] connect doOnError: ${throwable.message}")
                        }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            Timber.d("[Chat] connect: type: ${it.type}")
                            Timber.d("[Chat] connect: message: ${it.message}")
                            Timber.d("[Chat] connect: exception: ${it.exception}")
                            Timber.d("[Chat] connect: handshakeResponseHeaders: ${it.handshakeResponseHeaders}")
                            when (it.type) {
                                LifecycleEvent.Type.OPENED -> {
                                    Timber.d("[Chat] ===>\"Stomp connection opened\"")
                                    Timber.d("[Chat] Stomp connection opened")
                                    subscribeChatChannel("${URL_CHAT_ROOM}/${ChatRepository.chatRoomID}")
                                    subscribeChatChannel("${URL_CHAT_USER}/${ChatRepository.userId}")
                                    onChatConnStaus(true)
                                }
                                LifecycleEvent.Type.CLOSED -> {
                                    Timber.d("[Chat] ===>\"Stomp connection closed\"")
                                    Timber.d("[Chat] Stomp connection closed")
                                    disconnect()
                                    onChatConnStaus(false)
                                }
                                LifecycleEvent.Type.ERROR -> {
                                    Timber.d("[Chat] ===>\"Stomp connection error\"")
                                    Timber.d("[Chat] Stomp connection error ==> ${it.exception}")
//                                    doChatStep(ChatStateEvent.ChatStep.CHAT_RECONNECT)
                                }
                                null -> {
                                    Timber.d("[Chat] ===>\"Stomp connection failed\"")
                                    Timber.d("[Chat] Stomp connection failed")
                                    disconnect()
                                }
                                else -> {}
                            }
                        }

                mCompositeDisposable?.add(disposable)

                //建立連線
                val header = getDefaultChatHeader()
                chatStompClient.connect(header)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            when (e) {
                is SocketTimeoutException -> {
                    Timber.e("[Chat] 連線超時，正在重連")
                    reconnectChat()
                }
                is NoRouteToHostException -> {
                    Timber.e("[Chat] 該地址不存在，請檢查")
                    stopSelf()
                }
                is ConnectException -> {
                    Timber.e("[Chat] 連線異常或被拒絕，請檢查")
                    stopSelf()
                }
                else -> {
                    e.printStackTrace()
                    stopSelf()
                }
            }
        }
    }

    private fun reconnect() {
        disconnect()
        connect()
    }

    private fun reconnectChat() {
        disconnect()
        connectChat()
    }

    fun doReconnect() {
        delay = true
        errorFlag = false
        reconnectionNum = 0
        reconnect()
    }

    fun doReconnectChat() {
        delay = true
        errorFlag = false
        reconnectionNum = 0
        reconnectChat()
    }

    //關閉所有連線通道，釋放資源
    private fun disconnect() {
        mCompositeDisposable?.dispose()
        mCompositeDisposable = null
        mSubscribedMap.clear()

        mStompClient?.disconnect()
        mStompClient = null
        mChatStompClient?.disconnect()
        mChatStompClient = null
    }

    private fun sendChatMessageToViewModule(message: String) {
        ChatMessageDispatcher.onChatMessage(message)
//        val bundle = Bundle()
//        bundle.putString(MESSAGE_CATE, MessageCate.Chat.cate)
//        bundle.putString(SERVER_MESSAGE_KEY, message)
//        val intent = Intent(SERVICE_SEND_DATA)
//        intent.putExtras(bundle)
//        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun onChatConnStaus(enable: Boolean) {
        ChatMessageDispatcher.onConnectStatusChanged(enable)
    }

    private fun sendMessageToActivity(
        channel: String,
        message: String,
        matchOdd: MatchOdd?,
        matchType: MatchType?,
        isSubscribeData: Boolean,
    ) {
        val bundle = Bundle()
        bundle.putString(CHANNEL_KEY, channel)
        bundle.putString(SERVER_MESSAGE_KEY, setJObjToJArray(message))
        when (matchOdd) {
            //首頁使用的資料結構
            is Recommend -> {
                matchOdd
            }
            //大廳使用的資料結構
            is org.cxct.sportlottery.network.odds.list.MatchOdd -> {
                matchOdd
            }
            //詳細頁使用的資料結構
            is org.cxct.sportlottery.network.odds.detail.MatchOdd -> {
                matchOdd
            }
            is org.cxct.sportlottery.network.bet.info.MatchOdd -> {
                matchOdd
            }
            else -> {
                null
            }
        }?.let {
            bundle.putParcelable(CUSTOM_DATA_MATCH_ODD, it)
        }
        bundle.putSerializable(CUSTOM_DATA_MATCH_TYPE, matchType)
        bundle.putBoolean(USE_SUBSCRIBE_DATA, isSubscribeData)
        val intent = Intent(SERVICE_SEND_DATA)
        intent.putExtras(bundle)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun sendConnectStatusToActivity(connectStatus: ServiceConnectStatus) {
        val bundle = Bundle().apply {
            putSerializable(CONNECT_STATUS, connectStatus)
        }
        val intent = Intent(SERVICE_SEND_DATA).apply {
            putExtras(bundle)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun setJObjToJArray(message: String): String {
        var newStr = message
        if (message.startsWith("{") && message.endsWith("}")) {
            newStr = "[$message]"
        }
        return newStr
    }

    private fun resetSubscriptions() {
        mCompositeDisposable?.dispose()
        mCompositeDisposable = CompositeDisposable()
        mSubscribedMap.clear()
    }

    /**
     * @Date 2021/10/20
     * mStompClient?.isConnected 可能實際上不代表Client連線成功
     * @param firstSubscribeData 是否使用訂閱後第一筆資料作為新資料直接顯示, 而非更新
     * */
    private fun subscribeChannel(
        url: String,
        matchOdd: MatchOdd? = null,
        matchType: MatchType? = null,
        firstSubscribeData: Boolean = false,
    ) {
        if (mSubscribedMap.containsKey(url)) return

        if (mStompClient?.isConnected != true) {
            if (!mSubscribeChannelPending.contains(url)) {
                mSubscribeChannelPending.add(url)
            }
        }

        Timber.i(">>> subscribe channel: $url")

        //紀錄是否為訂閱後第一筆
        var useSubscribeData = firstSubscribeData

        mStompClient?.run {
            this.topic(url, mHeader)
                .subscribeOn(Schedulers.io())
                .subscribe({ topicMessage ->
                    Timber.v("[$url] 訂閱接收訊息: ${EncryptUtil.uncompress(topicMessage.payload)}")
                    sendMessageToActivity(url,
                        topicMessage.payload,
                        matchOdd,
                        matchType,
                        useSubscribeData)
                    useSubscribeData = false //第一筆過後將flag設為false
                }, { throwable ->
                    Timber.e("[$url] 訂閱通道失敗: $throwable")
                })
                .let { newDisposable ->
                    mCompositeDisposable?.add(newDisposable)
                    mSubscribedMap[url] = newDisposable

                    //訂閱完成後檢查是否有訂閱失敗的頻道
                    mSubscribeChannelPending.remove(url)
                    if (!mSubscribeChannelPending.isNullOrEmpty()) {
                        subscribeChannel(mSubscribeChannelPending.first())
                    }
                }
        } ?: reconnect()//背景中喚醒APP會有mStompClient=null的情況 導致停止訂閱賽事
    }

    private fun unsubscribeChannel(url: String) {
        mSubscribedMap[url]?.let {
            if (!mOriginalSubscribedMap.containsValue(it)) {
                Timber.i("<<< unsubscribe channel: $url")
                mCompositeDisposable?.remove(it)
                mSubscribedMap.remove(url)
            }
        }
    }

    private fun subscribeChatChannel(url: String) {
        if (url.isEmpty()) {
            Timber.d("[Chat] subscribeChannel: url isEmpty")
            return
        }

        if (mSubscribedMap.containsKey(url)) return

        Timber.d("[Chat] subscribeChannel: isConnected ${mStompClient?.isConnected}")

        if (mChatStompClient?.isConnected != true) {
            if (!mSubscribeChannelPending.contains(url)) {
                mSubscribeChannelPending.add(url)
            }
        }

        Timber.d("[Chat] >> subscribe channel: $url")

        val header = getDefaultChatHeader()
        header.add(StompHeader(StompHeader.ID, "sub-$timesTamp"))
        header.add(StompHeader(StompHeader.DESTINATION, url))
        header.add(StompHeader("lang", LanguageManager.getSelectLanguage(applicationContext).key))
        header.add(StompHeader("roomId", ChatRepository.chatRoomID.toString()))
        header.add(StompHeader("userId", ChatRepository.userId.toString()))

        Timber.d("[Chat] subscribeChannel: timesTamp: $timesTamp")

        mChatStompClient?.run {
            this.topic(url, header)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ topicMessage ->
                    Timber.d("[Chat] ===>\"[$url] 訂閱接收訊息: ${topicMessage.payload}\"")
                    sendChatMessageToViewModule(topicMessage.payload)
//                    getSocketMsg(type, topicMessage.payload)
                }, { throwable ->
                    Timber.e("[Chat] ===>\"[$url] 訂閱通道失敗: $throwable")
                })
                .let { newDisposable ->
                    mCompositeDisposable?.add(newDisposable)
                    mSubscribedMap[url] = newDisposable

                    //訂閱完成後檢查是否有訂閱失敗的頻道
                    mSubscribeChannelPending.remove(url)
                    if (mSubscribeChannelPending.isNotEmpty()) {
//                        subscribeChannel(mSubscribeChannelPending.first(), JoinType.OTHER)
                    }
                }
        } ?: reconnectChat()//背景中喚醒APP會有mStompClient=null的情況 導致停止訂閱賽事
    }

    /**
     * 為了避免投注單關閉時解除訂閱到當前頁面的頻道, 所以先將當前的訂閱記錄起來
     */
    fun betListPageSubscribeEvent() {
        mOriginalSubscribedMap.putAll(mSubscribedMap)
    }

    /**
     * 投注單頁面解除訂閱完畢, 清除暫存的訂閱頻道
     */
    fun betListPageUnSubScribeEvent() {
        mOriginalSubscribedMap.clear()
    }

    fun fastBetPageSubscribeHallEvent(gameType: String?, eventId: String?) {
        if (gameType == null || eventId == null) return
        val url = "$URL_HALL/$mPlatformId/$gameType/$eventId/$WS_END_TYPE"

        subscribeChannel(url)
        mFastBetSubscribed = url
    }

    fun fastBetPageSubscribeEvent(eventId: String?) {
        if (eventId == null) return

        val url = "$URL_EVENT/$mPlatformId/$eventId/$WS_END_TYPE"

        subscribeChannel(url)
        mFastBetSubscribed = url
    }

    fun fastBetPageUnSubscribeEvent() {
        mFastBetSubscribed?.let { fastBetSubscribeUrl ->
            unsubscribeChannel(fastBetSubscribeUrl)
            mFastBetSubscribed = null
        }
    }

    fun subscribeEventChannel(eventId: String?, matchOdd: MatchOdd? = null) {
        if (eventId == null) return

        val url = "$URL_EVENT/$mPlatformId/$eventId/$WS_END_TYPE"
        //val url = "$URL_EVENT/$mPlatformId/$eventId"
        subscribeChannel(url, matchOdd)
    }

    fun unsubscribeEventChannel(eventId: String?) {
        if (eventId == null) return

        val url = "$URL_EVENT/$mPlatformId/$eventId/$WS_END_TYPE"
        //val url = "$URL_EVENT/$mPlatformId/$eventId"
        unsubscribeChannel(url)
    }


    fun subscribeChatRoom(roomId: String?) {
        if (roomId == null) return
        val url = "$URL_CHAT_ROOM/$roomId"
        subscribeChatChannel(url)
    }

    fun unSubscribeChatRoom(roomId: String?) {
        if (roomId == null) return
        val url = "$URL_CHAT_ROOM/$roomId"
        unsubscribeChannel(url)
    }

    fun subscribeChatUser(userId: String?) {
        if (userId == null) return
        val url = "$URL_CHAT_USER/$userId"
        subscribeChatChannel(url)
    }

    fun unSubscribeChatUser(userId: String?) {
        if (userId == null) return
        val url = "$URL_CHAT_USER/$userId"
        unsubscribeChannel(url)
    }

    fun sendMessage(liveMsgEntity: LiveMsgEntity?) {
        chatSendMessage(liveMsgEntity)
    }

    fun unsubscribeAllEventChannel() {
        //要 clone 一份 list 來處理 url 判斷，避免刪減 map 資料時產生 ConcurrentModificationException
        val urlList = mSubscribedMap.keys.toList()
        urlList.forEach { url ->
            if (url.contains("$URL_EVENT/"))
                unsubscribeChannel(url)
        }

        //解除所有訂閱時, 清除pending中公共頻道以外的
        val pendingUrlList = mSubscribeChannelPending.toList()
        pendingUrlList.forEach { pendingUrl ->
            if (pendingUrl.contains("$URL_EVENT/"))
                mSubscribeChannelPending.remove(pendingUrl)
        }
    }

    fun subscribeSportChannelHall() {
        val url =
            "$URL_HALL/$WS_END_TYPE" //推送频道从原本的/notify/hall/{platformId}/{gameType}调整为/notify/hall,移除平台id与gameType,
        //val url = "$URL_HALL"
        subscribeChannel(url)
    }

    fun subscribeHallChannel(
        gameType: String?,
        eventId: String?,
        matchOdd: MatchOdd? = null,
        matchType: MatchType? = null,
        isSubscribeData: Boolean = false,
    ) {
        if (gameType == null || eventId == null) return
        val url = "$URL_HALL/$mPlatformId/$gameType/$eventId/$WS_END_TYPE"
        //val url = "$URL_HALL/$mPlatformId/$gameType/$eventId"

        subscribeChannel(url, matchOdd, matchType, isSubscribeData)
    }

    fun unsubscribeHallChannel(gameType: String?, eventId: String?) {
        if (gameType == null || eventId == null) return

        val url = "$URL_HALL/$mPlatformId/$gameType/$eventId/$WS_END_TYPE"
        //val url = "$URL_HALL/$mPlatformId/$gameType/$eventId"
        unsubscribeChannel(url)
    }

    fun unsubscribeHallChannel(eventId: String?) {
        if (eventId == null) return

        //要 clone 一份 list 來處理 url 判斷，避免刪減 map 資料時產生 ConcurrentModificationException
        val urlList = mSubscribedMap.keys.toList()
        urlList.forEach { url ->
            // 解除球種頻道以外的訂閱, 球種頻道格式:/ws/notify/hall/1/FT
            if (url.contains("$URL_HALL/") && url.contains("/$eventId"))
                unsubscribeChannel(url)
        }
    }

    fun unsubscribeAllHallChannel() {
        //要 clone 一份 list 來處理 url 判斷，避免刪減 map 資料時產生 ConcurrentModificationException
        val urlList = mSubscribedMap.keys.toList()
        urlList.forEach { url ->
            // 解除球種頻道以外的訂閱, 球種頻道格式:/ws/notify/hall/1/FT
            if (url.contains("$URL_HALL/") && url.split("/").size > SPORT_HALL_CHANNEL_LENGTH)
                unsubscribeChannel(url)
        }
    }

    fun unsubscribeSportHallChannel() {
        //要 clone 一份 list 來處理 url 判斷，避免刪減 map 資料時產生 ConcurrentModificationException
        val urlList = mSubscribedMap.keys.toList()
        urlList.forEach { url ->
            // 解除球種頻道的訂閱, 球種頻道格式:/ws/notify/hall/1/FT
            if (url.contains("$URL_HALL/") && url.split("/").size == SPORT_HALL_CHANNEL_LENGTH)
                unsubscribeChannel(url)
        }
    }

    //Chat聊天室
    /**
     * 傳送聊天室訊息
     */
    private fun chatSendMessage(
        liveMsgEntity: LiveMsgEntity?,
    ) {
        liveMsgEntity?.let {
            val header = getDefaultChatHeader()
            header.add(
                StompHeader(
                    StompHeader.DESTINATION,
                    "/ws/notify/room/${ChatRepository.chatRoomID}/sendMessage"
                )
            )
            header.add(StompHeader("lang",
                LanguageManager.getSelectLanguage(applicationContext).key))
            header.add(StompHeader("roomId", ChatRepository.chatRoomID.toString()))

            mChatStompClient?.send(
                StompMessage(
                    StompCommand.SEND,
                    header,
                    liveMsgEntity.toJSONString()
                )
            )
                ?.compose(applySchedulers())
                ?.subscribe({
                    Timber.d("[Chat] 傳送聊天室訊息 傳送成功!!!! JSONString: ${liveMsgEntity.toJSONString()}")
                }, { throwable ->
                    Timber.e(throwable, "[Chat] 傳送聊天室訊息 傳送失敗 : ")
                })
        }
    }

    private fun applySchedulers(): CompletableTransformer {
        return CompletableTransformer { upstream ->
            upstream.unsubscribeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }
    }

    private fun getDefaultChatHeader(): MutableList<StompHeader> {
        val header: MutableList<StompHeader> = mutableListOf()
        header.add(StompHeader("Content-Type", "application/json")) //加上Content-Type，JSON才能正確解析
        header.add(StompHeader("x-session-token", ChatRepository.chatToken))
        header.add(StompHeader("device", "2")) //WEB (0), MOBILE_BROWSER(1), ANDROID(2), IOS(3)
        return header
    }
}

