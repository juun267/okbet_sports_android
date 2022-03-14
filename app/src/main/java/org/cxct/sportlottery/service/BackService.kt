package org.cxct.sportlottery.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.service.ServiceConnectStatus
import org.cxct.sportlottery.util.HTTPsUtil
import timber.log.Timber
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompHeader
import java.io.IOException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit


class BackService : Service() {
    companion object {
        const val SERVICE_SEND_DATA = "SERVICE_SEND_DATA"
        const val CHANNEL_KEY = "channel"
        const val SERVER_MESSAGE_KEY = "serverMessage"
        const val CONNECT_STATUS = "connectStatus"

        private val URL_SOCKET_HOST_AND_PORT: String get() = "${Constants.getBaseUrl()}/api/ws/app/im" //app连接端点,无sockjs
        const val URL_ALL = "/ws/notify/all" //全体公共频道
        const val URL_PING = "/ws/ping" //心跳检测通道 （pong消息将发往用户私人频道）

        private const val SPORT_HALL_CHANNEL_LENGTH = 6

        internal var mUserId: Long? = null
        private var mPlatformId: Long? = null

        const val URL_USER = "/user/self"
        val URL_USER_PRIVATE: String get() = "/ws/notify/user/$mUserId"  //用户私人频道
        val URL_PLATFORM get() = "/ws/notify/platform/$mPlatformId" //公共频道  这个通道会通知主站平台维护
        const val URL_EVENT = "/ws/notify/event" //具体赛事/赛季频道 //(普通玩法：eventId就是matchId，冠军玩法：eventId是赛季Id)
        const val URL_HALL = "/ws/notify/hall" //大厅赔率频道 //cateMenuCode：HDP&OU=讓球&大小, 1X2=獨贏

        private const val HEART_BEAT_RATE = 10 * 1000 //每隔10秒進行一次對長連線的心跳檢測
        private const val RECONNECT_LIMIT = 10 //斷線後重連次數限制
        private const val LOADING_TIME_INTERVAL: Long = 500
    }

    private var mToken = ""
    private val mBinder: IBinder = MyBinder()
    private var mStompClient: StompClient? = null
    private var mCompositeDisposable: CompositeDisposable? = null //訊息接收通道 數組
    private val mHeader: List<StompHeader> get() = listOf(StompHeader("token", mToken))
    private val mSubscribedMap = mutableMapOf<String, Disposable?>() //Map<url, channel>
    private val mOriginalSubscribedMap = mutableMapOf<String, Disposable?>() //投注單頁面邏輯, 紀錄進入投注單前以訂閱的頻道, 離開投注單頁面時, 解除訂閱不解除此map中的頻道
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
            }
        }
    }

    override fun onDestroy() {
        Timber.i("onDestroy()")
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

        } catch (e: IOException) {
            e.printStackTrace()
            when (e) {
                is SocketTimeoutException -> {
                    Timber.e("連線超時，正在重連");
                    reconnect()
                }
                is NoRouteToHostException -> {
                    Timber.e("該地址不存在，請檢查");
                    stopSelf()
                }
                is ConnectException -> {
                    Timber.e("連線異常或被拒絕，請檢查");
                    stopSelf()
                }
                else -> {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun reconnect() {
        disconnect()
        connect()
    }

    fun doReconnect() {
        delay = true
        errorFlag = false
        reconnectionNum = 0
        reconnect()
    }

    //關閉所有連線通道，釋放資源
    private fun disconnect() {
        mCompositeDisposable?.dispose()
        mCompositeDisposable = null
        mSubscribedMap.clear()

        mStompClient?.disconnect()
        mStompClient = null
    }

    private fun sendMessageToActivity(channel: String, message: String) {
        val bundle = Bundle()
        bundle.putString(CHANNEL_KEY, channel)
        bundle.putString(SERVER_MESSAGE_KEY, setJObjToJArray(message))
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
     * */
    private fun subscribeChannel(url: String) {
        if (mSubscribedMap.containsKey(url)) return

        if (mStompClient?.isConnected != true) {
            if (!mSubscribeChannelPending.contains(url)) {
                mSubscribeChannelPending.add(url)
            }
        }

        Timber.i(">>> subscribe channel: $url")

        mStompClient?.run {
            this.topic(url, mHeader)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ topicMessage ->
                    Timber.v("[$url] 訂閱接收訊息: ${topicMessage.payload}")
                    sendMessageToActivity(url, topicMessage.payload)
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
            if (mOriginalSubscribedMap.containsValue(it)) {
                mOriginalSubscribedMap.remove(url)
            } else {
                Timber.i("<<< unsubscribe channel: $url")
                mCompositeDisposable?.remove(it)
                mSubscribedMap.remove(url)
            }
        }
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

    fun subscribeSportChannelHall(gameType: String?) {
        val url = "$URL_HALL" //推送频道从原本的/notify/hall/{platformId}/{gameType}调整为/notify/hall,移除平台id与gameType,
        subscribeChannel(url)
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

}

