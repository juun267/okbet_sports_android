package org.cxct.sportlottery.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import io.reactivex.CompletableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import org.cxct.sportlottery.util.HTTPsUtil
import timber.log.Timber
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompCommand
import ua.naiksoftware.stomp.dto.StompHeader
import ua.naiksoftware.stomp.dto.StompMessage
import java.io.IOException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit


class BackService : Service() {
    companion object {
        const val SERVICE_SEND_DATA = "SERVICE_SEND_DATA"

        private const val URL_SOCKET_HOST_AND_PORT = "http://sports.cxct.org/api/ws/app/im" //app连接端点,无sockjs
        const val URL_ALL = "/ws/notify/all" //全体公共频道
        const val URL_PING = "/ws/ping" //心跳检测通道 （pong消息将发往用户私人频道）

        private var mUserId: Long? = null
        private var mPlatformId: Long? = null

        const val URL_USER = "/user/self"
        val URL_USER_PRIVATE: String get() = "/ws/notify/user/$mUserId"  //用户私人频道
        val URL_PLATFORM get() = "/ws/notify/platform/$mPlatformId" //公共频道  这个通道会通知主站平台维护
        var URL_EVENT = "/ws/notify/event" //具体赛事/赛季频道 //(普通玩法：eventId就是matchId，冠军玩法：eventId是赛季Id)
        var URL_HALL = "/ws/notify/hall" //大厅赔率频道 //cateMenuCode：HDP&OU=讓球&大小, 1X2=獨贏

        private const val HEART_BEAT_RATE = 10 * 1000 //每隔10秒進行一次對長連線的心跳檢測
    }

    private var mToken = ""

    private var mReconnectCount = 0

    private val mBinder: IBinder = MyBinder()

    inner class MyBinder : Binder() {
        val service: BackService
            get() = this@BackService

        fun connect(token: String?, userId: Long, platformId: Long) {
            mToken = token ?: ""
            mUserId = userId
            mPlatformId = platformId

            if (mStompClient?.isConnected != true) {
                Timber.d("==尚未建立連線，連線開始==")
                connect()
            } else {
                Timber.d("==已建立連線，傳遞資料==")
                reconnect()
            }
        }
    }

    private var mStompClient: StompClient? = null
    private var mCompositeDisposable: CompositeDisposable? = null //訊息接收通道 數組
    private val mHeader: List<StompHeader> get() = listOf(StompHeader("token", mToken))

    //Map<url, channel>
    private val subscribedMap = mutableMapOf<String, Disposable?>()

    private fun subscribeChannel(url: String) {
        Timber.i(">>> subscribe channel: $url")
        val newDisposable: Disposable? = mStompClient?.subscribe(url) { topicMessage ->
            Timber.d(">>> socket channel: $url ==> returned msg: ${topicMessage.payload}")
        }
        mCompositeDisposable?.add(newDisposable!!)
        subscribedMap[url] = newDisposable
    }

    private fun unsubscribeChannel(url: String) {
        Timber.i("<<< unsubscribe channel: $url")
        subscribedMap[url]?.let {
            it.dispose()
            mCompositeDisposable?.remove(it)
        }
        subscribedMap.remove(url)
    }

    fun subscribeEventChannel(eventId: String?) {
        if (eventId == null) return

        val url = "$URL_EVENT/$mPlatformId/$eventId"

        subscribeChannel(url)
    }

    fun unsubscribeEventChannel(eventId: String?) {
        if (eventId == null) return

        val url = "$URL_EVENT/$mPlatformId/$eventId"
        unsubscribeChannel(url)
    }

    fun unsubscribeAllEventChannel() {
        //要 clone 一份 list 來處理 url 判斷，避免刪減 map 資料時產生 ConcurrentModificationException
        val urlList = subscribedMap.keys.toList()
        urlList.forEach { url ->
            if (url.contains("$URL_EVENT/"))
                unsubscribeChannel(url)
        }
    }

    fun subscribeHallChannel(gameType: String?, cateMenuCode: String?, eventId: String?) {
        if (gameType == null || eventId == null) return

        val url = "$URL_HALL/$mPlatformId/$gameType/$cateMenuCode/$eventId"

        subscribeChannel(url)
    }

    fun unsubscribeHallChannel(gameType: String?, cateMenuCode: String?, eventId: String?) {
        if (gameType == null || eventId == null) return

        val url = "$URL_HALL/$mPlatformId/$gameType/$cateMenuCode/$eventId"

        unsubscribeChannel(url)
    }

    fun unsubscribeAllHallChannel() {
        //要 clone 一份 list 來處理 url 判斷，避免刪減 map 資料時產生 ConcurrentModificationException
        val urlList = subscribedMap.keys.toList()
        urlList.forEach { url ->
            if (url.contains("$URL_HALL/"))
                unsubscribeChannel(url)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseSocket()
    }

    override fun onBind(intent: Intent?): IBinder {
        return mBinder
    }

    private fun connect() {
        try {
            Timber.i(">>>token = ${mToken}, url = $URL_SOCKET_HOST_AND_PORT")

            val httpClient = HTTPsUtil.trustAllSslClient(OkHttpClient())
            mStompClient = Stomp.over(
                Stomp.ConnectionProvider.OKHTTP,
                URL_SOCKET_HOST_AND_PORT,
                null,
                httpClient.newBuilder().pingInterval(
                    40,
                    TimeUnit.SECONDS
                ).retryOnConnectionFailure(
                    true
                ).build()
            )

            mStompClient?.let { stompClient ->

                stompClient.withServerHeartbeat(HEART_BEAT_RATE).withServerHeartbeat(HEART_BEAT_RATE)

                resetSubscriptions()

                val lifecycleDisposable =
                    stompClient.lifecycle().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe { lifecycleEvent: LifecycleEvent ->
                        when (lifecycleEvent.type) {
                            LifecycleEvent.Type.OPENED -> Timber.d("Stomp connection opened")
                            LifecycleEvent.Type.CLOSED -> {
                                Timber.d("Stomp connection closed")
                                resetSubscriptions()
                            }
                            LifecycleEvent.Type.ERROR -> {
                                Timber.e("Stomp connection error ==> ${lifecycleEvent.exception}")
                                reconnect()
                            }
                            LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT -> {
                                Timber.d("Stomp connection failed server heartbeat")
                                reconnect()
                            }
                            else -> Timber.e("Stomp connection failed")
                        }
                    }

                //用户私人频道
                val privateDisposable: Disposable? =
                    if (mToken.isEmpty()) {
                        stompClient.subscribe(URL_USER)
                    } else {
                        stompClient.subscribe(URL_USER_PRIVATE)
                    }

                //全体公共频道
                val allDisposable: Disposable? = stompClient.subscribe(URL_ALL)

                val platformDisposable: Disposable? = stompClient.subscribe(URL_PLATFORM)

                mCompositeDisposable?.add(lifecycleDisposable)
                mCompositeDisposable?.add(privateDisposable!!)
                mCompositeDisposable?.add(allDisposable!!)
                mCompositeDisposable?.add(platformDisposable!!)

                stompClient.connect(mHeader)
            }

        } catch (e: IOException) {
            e.printStackTrace()
            when (e) {
                is SocketTimeoutException -> {
                    Timber.e("連線超時，正在重連");
                    releaseSocket()
                    reconnect()
                }
                is NoRouteToHostException -> {
                    Timber.e("該地址不存在，請檢查");
                    stopSelf();
                }
                is ConnectException -> {
                    Timber.e("連線異常或被拒絕，請檢查");
                    stopSelf();
                }
                else -> {
                    e.printStackTrace()
                }
            }

        }
    }

    private fun sendMessageToActivity(channel: String, message: String) {
        val bundle = Bundle()
        bundle.putString("channel", channel)
        bundle.putString("serverMessage", setJObjToJArray(message))
        val intent = Intent(SERVICE_SEND_DATA)
        intent.putExtras(bundle)
        sendBroadcast(intent)
    }

    private fun setJObjToJArray(message: String): String {
        var newStr = message
        if (message.startsWith("{") && message.endsWith("}")) {
            newStr = "[$message]"
        }
        return newStr
    }

    private fun reconnect() {
        mReconnectCount++
        releaseSocket()
        connect()
    }

    //關閉所有連線通道，釋放資源
    private fun releaseSocket() {
        mCompositeDisposable?.dispose()
        mCompositeDisposable = null

        mStompClient?.disconnect()
        mStompClient = null
    }

    private fun StompClient.subscribe(url: String, respond: (StompMessage) -> Unit = { }): Disposable? {
        return this.topic(url, mHeader)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ topicMessage ->
                Timber.i("[$url] 訂閱接收訊息: ${topicMessage.payload}")
                respond(topicMessage) //TODO Cheryl: 如果沒有要在service處理的事, 可以刪掉這行
                sendMessageToActivity(url, topicMessage.payload)
            }, { throwable ->
                Timber.e("[$url] 訂閱通道失敗: $throwable")
            })
    }

    @SuppressLint("CheckResult")
    fun sendMessage(url: String, content: String) {
        Timber.i("start sending message to server")
        val sendHeader = mHeader.toMutableList().apply {
            this.add(StompHeader(StompHeader.DESTINATION, url))
        }

        mStompClient?.send(StompMessage(StompCommand.SEND, sendHeader, content))?.compose(
            applySchedulers()
        )?.subscribe({
            Timber.i("傳送訊息成功!!!")
        }, { throwable ->
            Timber.e("傳送訊息失敗 ==> $throwable")
            reconnect()
        })
    }

    private fun applySchedulers(): CompletableTransformer {
        return CompletableTransformer { upstream ->
            upstream.unsubscribeOn(Schedulers.newThread()).subscribeOn(Schedulers.io()).observeOn(
                AndroidSchedulers.mainThread()
            )
        }
    }

    private fun resetSubscriptions() {
        mCompositeDisposable?.dispose()
        mCompositeDisposable = CompositeDisposable()
    }

}

