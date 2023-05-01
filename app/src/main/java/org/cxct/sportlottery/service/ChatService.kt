package org.cxct.sportlottery.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import org.cxct.sportlottery.repository.ChatRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.util.HTTPsUtil
import org.cxct.sportlottery.util.LanguageManager
import timber.log.Timber
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompHeader
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @author kevin
 * @create 2023/3/23
 * @description
 */
class ChatService : Service() {

    companion object {
        const val MESSAGE_CATE = "message_cate"
        const val SERVER_MESSAGE_KEY = "serverMessage"
        const val SERVICE_SEND_DATA = "SERVICE_SEND_DATA"
        const val URL_CHAT_ROOM = "/ws/notify/room" //订阅聊天室
        const val URL_CHAT_USER = "/ws/notify/user" //订阅用户
        const val MESSAGECATE_CHAT = "1"
    }

    private val chatHost: String get() = "${sConfigData?.chatHost?.replace("https", "wss")}"
    private var timestamp: Long = Date(System.currentTimeMillis()).time

    private var mChatStompClient: StompClient? = null
    private var mCompositeDisposable: CompositeDisposable? = null
    private val mSubscribedMap = mutableMapOf<String, Disposable?>()
    private val mSubscribeChannelPending = mutableListOf<String>()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
        connectChat()
    }

    private fun connectChat() {
        try {
            resetSubscriptions()
            val httpClient = HTTPsUtil.trustAllSslClient(OkHttpClient())
                .newBuilder()
                .pingInterval(40, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build()

            mChatStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP,
                "${chatHost}ws/chat/app",
                null,
                httpClient)
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
                                }
                                LifecycleEvent.Type.CLOSED -> {
                                    Timber.d("[Chat] ===>\"Stomp connection closed\"")
                                    Timber.d("[Chat] Stomp connection closed")
                                    disconnect()
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

    private fun subscribeChatChannel(url: String) {
        if (url.isEmpty()) {
            Timber.d("[Chat] subscribeChannel: url isEmpty")
            return
        }

        if (mSubscribedMap.containsKey(url)) return

        if (mChatStompClient?.isConnected != true) {
            if (!mSubscribeChannelPending.contains(url)) {
                mSubscribeChannelPending.add(url)
            }
        }

        Timber.d("[Chat] >> subscribe channel: $url")

        val header = getDefaultChatHeader()
        header.add(StompHeader(StompHeader.ID, "sub-${timestamp}"))
        header.add(StompHeader(StompHeader.DESTINATION, url))
        header.add(StompHeader("lang", LanguageManager.getSelectLanguage(applicationContext).key))
        header.add(StompHeader("roomId", ChatRepository.chatRoomID.toString()))
        header.add(StompHeader("userId", ChatRepository.userId.toString()))

        Timber.d("[Chat] subscribeChannel: timestamp: $timestamp")

        mChatStompClient?.run {
            this.topic(url, header)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ topicMessage ->
                    Timber.d("[Chat] ===>\"[$url] 訂閱接收訊息: ${topicMessage.payload}\"")
                    topicMessage.payload?.let { sendChatMessageToViewModule(topicMessage.payload) }
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

}