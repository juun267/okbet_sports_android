package org.cxct.sportlottery.service

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.service.ServiceConnectStatus
import org.cxct.sportlottery.util.EncryptUtil
import org.cxct.sportlottery.util.HTTPsUtil
import timber.log.Timber
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompHeader
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

 object BackService {

     private const val WS_END_TYPE = "proto" //ws proto 路徑
//     private const val WS_END_TYPE = "encrypted" //ws 舊格式路徑

    private val URL_SOCKET_HOST_AND_PORT: String get() = "${Constants.getSocketUrl()}/api/ws/app/im" //app连接端点,无sockjs

     private const val URL_ALL = "/ws/notify/all/$WS_END_TYPE" //全体公共频道

    private const val SPORT_HALL_CHANNEL_LENGTH = 6

    private var mUserId: Long? = null
    private var mPlatformId: Long? = null

    private const val URL_USER = "/user/self/$WS_END_TYPE"
    private val URL_USER_PRIVATE: String get() = "/ws/notify/user/$mUserId/$WS_END_TYPE"  //用户私人频道
    private val URL_PLATFORM get() = "/ws/notify/platform/$mPlatformId/$WS_END_TYPE" //公共频道  这个通道会通知主站平台维护

    //const val URL_USER = "/user/self"
    //val URL_USER_PRIVATE: String get() = "/ws/notify/user/$mUserId"  //用户私人频道
    //val URL_PLATFORM get() = "/ws/notify/platform/$mPlatformId"
    private const val URL_EVENT = "/ws/notify/event" //具体赛事/赛季频道 //(普通玩法：eventId就是matchId，冠军玩法：eventId是赛季Id)
     private const val URL_HALL = "/ws/notify/hall" //大厅赔率频道 //cateMenuCode：HDP&OU=讓球&大小, 1X2=獨贏

    private const val HEART_BEAT_RATE = 10 * 1000 //每隔10秒進行一次對長連線的心跳檢測
    private const val RECONNECT_LIMIT = 1 //斷線後重連次數限制
    private const val LOADING_TIME_INTERVAL: Long = 500


    private var mToken = ""
    private var mStompClient: StompClient? = null
    private val mHeader: List<StompHeader> get() = listOf(StompHeader("token", mToken))
    private var lifecycleDisposable: Disposable? = null
    private val mSubscribedMap = mutableMapOf<String, Disposable>() //Map<url, channel>
    private val mOriginalSubscribedMap = mutableMapOf<String, Disposable?>() //投注單頁面邏輯, 紀錄進入投注單前以訂閱的頻道, 離開投注單頁面時, 解除訂閱不解除此map中的頻道
    private val mSubscribeChannelPending = mutableListOf<String>()
    private var errorFlag = false // Stomp connect錯誤
    private var reconnectionNum = 0//重新連接次數
    private var delay: Boolean = false
     private val connectHeaders = mapOf(Pair("sec-websocket-protocol", "v12.stomp"))

    fun connect(token: String?, userId: Long, platformId: Long) {
        val changed = mToken != token || mUserId != userId || mPlatformId != platformId

        if (!changed && isConnecting) { // 正在连接中
            return
        }

        //未連線 或者 連線參數有變 => 重新建立連線
        if (mStompClient?.isConnected != true || changed) {

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

    private var isConnecting = false
    private fun connect() {
        if (isConnecting || mStompClient?.isConnected == true) {
            return
        }
        isConnecting = true

        try {
            Timber.i(">>>token = ${mToken}, url = $URL_SOCKET_HOST_AND_PORT")
            resetSubscriptions()

            val httpClient = HTTPsUtil.trustAllSslClient(OkHttpClient())
                .newBuilder()
                .pingInterval(40, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build()

            mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, URL_SOCKET_HOST_AND_PORT, connectHeaders, httpClient)
            if (mStompClient == null) {
                isConnecting = false
                return
            }

            val stompClient = mStompClient!!
            stompClient.withClientHeartbeat(HEART_BEAT_RATE).withServerHeartbeat(HEART_BEAT_RATE)
            sendConnectStatusToActivity(ServiceConnectStatus.CONNECTING)
            lifecycleDisposable = stompClient.lifecycle()
                    .subscribeOn(Schedulers.io())
                    .delay(if (delay) LOADING_TIME_INTERVAL else 0, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { lifecycleEvent: LifecycleEvent ->
                        isConnecting = false
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

            stompClient.connect(mHeader) //建立連線
            subscribeSystemChannel()

        } catch (e: Exception) {
            e.printStackTrace()
            isConnecting = false
            when (e) {
                is SocketTimeoutException -> {
                    Timber.e("連線超時，正在重連")
                    reconnect()
                }
                is NoRouteToHostException -> {
                    Timber.e("該地址不存在，請檢查")
                    disconnect()
                }
                is ConnectException -> {
                    Timber.e("連線異常或被拒絕，請檢查")
                    disconnect()
                }
                else -> {
                    e.printStackTrace()
                    disconnect()
                }
            }
        }
    }

    private fun subscribeSystemChannel() {
        //訂閱用戶私人頻道
        val userChannel = if (mToken.isEmpty()) URL_USER else URL_USER_PRIVATE
        if(isNewChannel(userChannel)) {
            subscribeChannel(userChannel)
        }
        //訂閱全體公共頻道
        if(isNewChannel(URL_ALL)) {
            subscribeChannel(URL_ALL)
        }
        if(isNewChannel(URL_PLATFORM)) {
            subscribeChannel(URL_PLATFORM)
        }
    }

    private fun isNewChannel(url: String): Boolean {
        return !mSubscribedMap.contains(url) && !mSubscribeChannelPending.contains(url)
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

     fun cleanUserChannel(){
         unsubscribeChannel(URL_USER)
         unsubscribeChannel(URL_USER_PRIVATE)
         mToken = ""
         mUserId = null
     }
    //關閉所有連線通道，釋放資源
    private fun disconnect() {
        resetSubscriptions()
        mStompClient?.disconnect()
        mStompClient = null
        isConnecting = false
    }

    private fun sendMessageToActivity(channel: String, message: String?) {
        message?.let { ServiceBroadcastReceiver.onReceiveMessage(channel, message) }
    }

    private fun sendConnectStatusToActivity(connectStatus: ServiceConnectStatus) {
        ServiceBroadcastReceiver.onConnectStatus(connectStatus)
    }

    private fun resetSubscriptions() {
        lifecycleDisposable?.dispose()
        lifecycleDisposable = null
        mSubscribedMap.values.forEach { it.dispose() }
        mSubscribedMap.clear()
    }

    /**
     * @Date 2021/10/20
     * mStompClient?.isConnected 可能實際上不代表Client連線成功
     * @param firstSubscribeData 是否使用訂閱後第一筆資料作為新資料直接顯示, 而非更新
     * */
    private fun subscribeChannel(url: String) {

        if (mSubscribedMap.containsKey(url)) return

        Timber.i(">>> subscribe channel: $url")
        if (mStompClient?.isConnected != true) {

            if (!mSubscribeChannelPending.contains(url)) {
                mSubscribeChannelPending.add(url)
            }
        }

        if (mStompClient == null) {
            connect()
            return
        }

        mStompClient!!.topic(url, mHeader)
            .subscribeOn(Schedulers.io())
            .subscribe({ topicMessage ->
                if (BuildConfig.DEBUG) { // 仅开发模式执行，该段代码会进行数据解密会对性能有所影响
                    Timber.v("[$url] 訂閱接收訊息: ${EncryptUtil.uncompressProto(topicMessage.payload)}")
                }
                sendMessageToActivity(url, topicMessage.payload)
            }, { throwable ->
                Timber.e("[$url] 訂閱通道失敗: $throwable")
            })
            .let { newDisposable ->

                mSubscribedMap[url] = newDisposable

                //訂閱完成後檢查是否有訂閱失敗的頻道
                mSubscribeChannelPending.remove(url)
                if (!mSubscribeChannelPending.isNullOrEmpty()) {
                    subscribeChannel(mSubscribeChannelPending.first())
                }
            }
    }

    private fun unsubscribeChannel(url: String) {
        mSubscribedMap[url]?.let {
            it.dispose()
            if (!mOriginalSubscribedMap.containsValue(it)) {
                Timber.i("<<< unsubscribe channel: $url")
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

        val url = "$URL_EVENT/$mPlatformId/$eventId/$WS_END_TYPE"
        //val url = "$URL_EVENT/$mPlatformId/$eventId"
        subscribeChannel(url)
    }

    fun unsubscribeEventChannel(eventId: String?) {
        if (eventId == null) return

        val url = "$URL_EVENT/$mPlatformId/$eventId/$WS_END_TYPE"
        //val url = "$URL_EVENT/$mPlatformId/$eventId"
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

    fun subscribeSportChannelHall() {
        val url = "$URL_HALL/$WS_END_TYPE" //推送频道从原本的/notify/hall/{platformId}/{gameType}调整为/notify/hall,移除平台id与gameType,
        //val url = "$URL_HALL"
        subscribeChannel(url)
    }

    fun subscribeHallChannel(gameType: String?, eventId: String?) {
        if (gameType == null || eventId == null) return
        val url = "$URL_HALL/$mPlatformId/$gameType/$eventId/$WS_END_TYPE"
        //val url = "$URL_HALL/$mPlatformId/$gameType/$eventId"

        subscribeChannel(url)
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

}

