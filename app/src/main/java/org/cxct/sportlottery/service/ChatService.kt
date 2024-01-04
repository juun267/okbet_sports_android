package org.cxct.sportlottery.service

import android.annotation.SuppressLint
import android.content.Context
import io.reactivex.CompletableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.repository.ChatRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.chat.LiveMsgEntity
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
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.TimeUnit

/**
 * @author kevin
 * @create 2023/3/23
 * @description
 */
object ChatService {

    private val URL_CHAT_ROOM = "/ws/notify/room" //订阅聊天室
    private val URL_CHAT_USER = "/ws/notify/user" //订阅用户

    private val cxt: Context get() = MultiLanguagesApplication.appContext
    private val URL_CHAT_SOCKET_HOST: String get() = "${ sConfigData?.chatHost?.replace("https", "wss")}"
    private val chatHost: String get() = "${sConfigData?.chatHost?.replace("https", "wss")}"
    private var timestamp: Long = Date(System.currentTimeMillis()).time

    private var mChatStompClient: StompClient? = null
    private var mCompositeDisposable: CompositeDisposable? = null
    private val mSubscribedMap = mutableMapOf<String, Disposable?>()
    private val mSubscribeChannelPending = mutableListOf<String>()
    private val connectHeaders = mapOf(Pair("sec-websocket-protocol", "v12.stomp"))

    private const val RECONNECT_LIMIT = 3 //斷線後重連次數限制
    private var errorFlag = false // Stomp connect錯誤
    private var reconnectionNum = 0//重新連接次數

    private var chatRoomID: Int = -99
    private var userID: Int = -99

    @Volatile
    private var stoped = true

    private val okHttpClient by lazy {
        HTTPsUtil.trustAllSslClient(OkHttpClient())
            .newBuilder()
            .pingInterval(40, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
        }

    private fun connectChat() {
        try {

            if (stoped) {
                disconnect()
                return
            }

            Timber.i("[Chat] >>>, url = ${URL_CHAT_SOCKET_HOST}")
            resetSubscriptions()

            val url = "${URL_CHAT_SOCKET_HOST}ws/chat/app"
            mChatStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, url, connectHeaders, okHttpClient)
            mChatStompClient?.let { chatStompClient ->

                chatStompClient.withClientHeartbeat(10 * 1000).withServerHeartbeat(10 * 1000)
                val disposable = chatStompClient.lifecycle()
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
                                    Timber.d("[Chat] Stomp connection closed ")
                                    reconnectionNum++
                                    if (errorFlag && reconnectionNum < RECONNECT_LIMIT) {
                                        Timber.e("[Chat] Stomp connection closed, start reconnect: $reconnectionNum  ")
                                        reconnectChat()
                                    } else {
                                        disconnect()
                                        onChatConnStaus(false)
                                    }
                                }
                                LifecycleEvent.Type.ERROR -> {
                                    errorFlag = true
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
                    disconnect()
                }
                is ConnectException -> {
                    Timber.e("[Chat] 連線異常或被拒絕，請檢查")
                    disconnect()
                }
                else -> {
                    e.printStackTrace()
                    disconnect()
                }
            }
        }
    }

    private fun unsubscribeChannel(url: String) {
        val disposable = mSubscribedMap[url] ?: return
        Timber.i("<<< unsubscribe channel: $url")
        mCompositeDisposable?.remove(disposable)
        mSubscribedMap.remove(url)
    }

    //Chat聊天室
    /**
     * 傳送聊天室訊息
     */
    @SuppressLint("CheckResult")
    private fun chatSendMessage(liveMsgEntity: LiveMsgEntity) {

        val chatClient =  mChatStompClient ?: return
        val header = getDefaultChatHeader()
        header.add(StompHeader(StompHeader.DESTINATION, "/ws/notify/room/${ChatRepository.chatRoomID}/sendMessage"))
        header.add(StompHeader("lang", LanguageManager.getSelectLanguage(cxt).key))
        header.add(StompHeader("roomId", ChatRepository.chatRoomID.toString()))

        chatClient.send(StompMessage(StompCommand.SEND, header, liveMsgEntity.toJSONString()))
            ?.compose(applySchedulers())
            ?.subscribe({
                Timber.d("[Chat] 傳送聊天室訊息 傳送成功!!!! JSONString: ${liveMsgEntity.toJSONString()}")
            }, { throwable ->
                Timber.e(throwable, "[Chat] 傳送聊天室訊息 傳送失敗 : ")
            })
    }

    private fun applySchedulers(): CompletableTransformer {
        return CompletableTransformer { upstream ->
            upstream.unsubscribeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }
    }

    private fun subscribeChatChannel(url: String) {
        if (url.isEmpty()) {
            Timber.d("[Chat] subscribeChannel: url isEmpty")
            return
        }

        if (mSubscribedMap.containsKey(url)) return

        if (mChatStompClient?.isConnected != true && !mSubscribeChannelPending.contains(url)) {
            mSubscribeChannelPending.add(url)
        }


        if (mChatStompClient == null) {
            reconnectChat()//背景中喚醒APP會有mStompClient=null的情況 導致停止訂閱賽事、
            return
        }

        Timber.d("[Chat] >> subscribe channel: $url")

        val header = getDefaultChatHeader()
        header.add(StompHeader(StompHeader.ID, "sub-${timestamp}"))
        header.add(StompHeader(StompHeader.DESTINATION, url))
        header.add(StompHeader("lang", LanguageManager.getSelectLanguage(cxt).key))
        header.add(StompHeader("roomId", ChatRepository.chatRoomID.toString()))
        header.add(StompHeader("userId", ChatRepository.userId.toString()))

        Timber.d("[Chat] subscribeChannel: timestamp: $timestamp")

        mChatStompClient!!.topic(url, header)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ topicMessage ->
                Timber.d("[Chat] ===>\"[$url] 訂閱接收訊息: ${topicMessage.payload}\"")
                topicMessage.payload?.let { sendChatMessageToViewModule(topicMessage.payload) }
//                    getSocketMsg(type, topicMessage.payload)
            }, { throwable ->
                Timber.e("[Chat] ===>\"[$url] 訂閱通道失敗: $throwable")
            }).let { newDisposable ->
                mCompositeDisposable?.add(newDisposable)
                mSubscribedMap[url] = newDisposable

                //訂閱完成後檢查是否有訂閱失敗的頻道
                mSubscribeChannelPending.remove(url)
                if (mSubscribeChannelPending.isNotEmpty()) {
//                        subscribeChannel(mSubscribeChannelPending.first(), JoinType.OTHER)
                }
            }
    }

    private fun getDefaultChatHeader(): MutableList<StompHeader> {
        val header: MutableList<StompHeader> = mutableListOf()
        header.add(StompHeader("Content-Type", "application/json")) //加上Content-Type，JSON才能正確解析
        header.add(StompHeader("x-session-token", ChatRepository.chatToken))
        header.add(StompHeader("device", "2")) //WEB (0), MOBILE_BROWSER(1), ANDROID(2), IOS(3)
        return header
    }

    private fun sendChatMessageToViewModule(message: String) {
        ChatMessageDispatcher.onChatMessage(message)
    }

    private fun onChatConnStaus(enable: Boolean) {
        ChatMessageDispatcher.onConnectStatusChanged(enable)
    }

    private fun doReconnectChat() {
        errorFlag = false
        reconnectionNum = 0
        reconnectChat()
    }

    private fun reconnectChat() {
        disconnect()
        connectChat()
    }

    private fun resetSubscriptions() {
        mCompositeDisposable?.dispose()
        mCompositeDisposable = CompositeDisposable()
        mSubscribedMap.clear()
    }

    private fun disconnect() {
        mCompositeDisposable?.dispose()
        mCompositeDisposable = null
        mSubscribedMap.clear()
        mChatStompClient?.disconnect()
        mChatStompClient = null
    }

    fun subscribeChatRoom(roomId: String) {
        if (roomId.isEmptyStr()) {
            subscribeChatChannel("${URL_CHAT_ROOM}/$roomId")
        }
    }

    fun unSubscribeChatRoom(roomId: String) {
        if (!roomId.isEmptyStr()) {
            unsubscribeChannel("${URL_CHAT_ROOM}/$roomId")
        }
    }

    fun subscribeChatUser(userId: String) {
        if (!userId.isEmptyStr()) {
            subscribeChatChannel("${URL_CHAT_USER}/$userId")
        }
    }

    fun unSubscribeChatUser(userId: String) {
        if (userId.isEmptyStr()) return
        val url = "${URL_CHAT_USER}/$userId"
        unsubscribeChannel(url)
    }

    fun sendMessage(liveMsgEntity: LiveMsgEntity) {
        chatSendMessage(liveMsgEntity)
    }

    fun connect() {

        stoped = false
        if (mChatStompClient?.isConnected != true
            || ChatRepository.chatRoomID != chatRoomID
            || ChatRepository.userId != userID) {

            chatRoomID = ChatRepository.chatRoomID
            userID = ChatRepository.userId ?: -99
            doReconnectChat()
            return
        }

        onChatConnStaus(true)
    }

    fun stop() {
        stoped = true
        chatRoomID = -99
        userID = -99
        reconnectionNum = 0
        disconnect()
    }

}