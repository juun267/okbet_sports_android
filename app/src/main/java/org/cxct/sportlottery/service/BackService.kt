package org.cxct.sportlottery.service

import android.annotation.SuppressLint
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
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.util.HTTPsUtil
import timber.log.Timber
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompCommand
import ua.naiksoftware.stomp.dto.StompHeader
import ua.naiksoftware.stomp.dto.StompMessage
import java.util.concurrent.TimeUnit

const val SERVICE_SEND_DATA = "SERVICE_SEND_DATA"

class BackService : BaseService() {

    companion object {
        const val URL_SOCKET_HOST_AND_PORT = "http://sports.cxct.org/api/ws/app/im" //app连接端点,无sockjs
        const val URL_ALL = "/ws/notify/all" //全体公共频道
        const val URL_PING = "/ws/ping" //心跳检测通道 （pong消息将发往用户私人频道）
        var URL_PRIVATE = "/ws/notify/user/${201}"
        var URL_EVENT = "/ws/notify/event/sr:match:25305234" //.apply { replace(":", "\\") }
        var URL_HALL = "/ws/notify/hall/FT/HDP&OU/sr:simple_tournament:96787/sr:match:25305514"

        private const val HEART_BEAT_RATE = 10 * 1000 //每隔10秒進行一次對長連線的心跳檢測
    }

    private val mBinder: IBinder = MyBinder()
    //    private lateinit var mBinder: IBinder

    inner class MyBinder : Binder() {
        val service: BackService
            get() = this@BackService
    }

    enum class EventType {
        ODDS_CHANGE, //赔率变更
        ORDER_SETTLEMENT, //注单结算通知
        USER_MONEY, //余额变更
        USER_NOTICE, //用户消息通知
        NOTICE, //公告
        PING_PONG, //ping-pong心跳
        MATCH_CLOCK, //赛事时刻
        GLOBAL_STOP, //所有赔率禁用，不允许投注
    }

    private var reconnectCount = 0

    val token by lazy {
        LoginRepository(applicationContext).token.value
    }

    //    private val URL_PRIVATE by lazy { "/notify/user/{userId}" } //用户私人频道
    //    private val URL_EVENT by lazy { "/notify/event/{eventId}"} //具体赛事/赛季频道 //(普通玩法：eventId就是matchId，冠军玩法：eventId是赛季Id) //TODO Cheryl 替換變數
    //    private val URL_HALL by lazy { "/notify/hall/{gameType}/{cateMenuCode}/{eventId}" }//大厅赔率频道 //cateMenuCode：HDP&OU=讓球&大小, 1X2=獨贏 //TODO Cheryl 替換變數

    private var mStompClient: StompClient? = null
    private var mCompositeDisposable: CompositeDisposable? = null //訊息接收通道 數組
    private val mPingDisposable: Disposable? = null
    private val mHeader: List<StompHeader> by lazy { listOf(StompHeader("token", token)) }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnect()
    }

    override fun onBind(intent: Intent): IBinder {

        if (token.isNullOrEmpty()) return mBinder

        if (mStompClient?.isConnected != true) {
            Timber.d("==尚未建立連線，連線開始==")
            connect()
        } else {
            Timber.d("==已建立連線，傳遞資料==")
        }

        return mBinder
    }

    fun getStr(): String {
        //        return BackService::class.java.name
        return "test"
    }

    @SuppressLint("CheckResult") fun connect() {
        //        try {
        val headerMap = mHeader.map { it.key to it.value }.toMap()
        Timber.e(">>>token = ${token}, url = $URL_SOCKET_HOST_AND_PORT")

        val httpClient = HTTPsUtil.trustAllSslClient(OkHttpClient())
        mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP,
                                  URL_SOCKET_HOST_AND_PORT,
                                  headerMap,
                                  httpClient.newBuilder().pingInterval(40,
                                                                       TimeUnit.SECONDS).retryOnConnectionFailure(
                                      true).build())

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
                stompClient.subscribe(URL_PRIVATE) { topicMessage ->
                    //                    Timber.e("$URL_PRIVATE, msg = ${topicMessage.payload}")
                }
            //全体公共频道
            val allDisposable: Disposable? = stompClient.subscribe(URL_ALL) { topicMessage ->
                Timber.e("$URL_ALL, msg = ${topicMessage.payload}")
            }

            //具体赛事/赛季频道
            val eventDisposable: Disposable? = stompClient.subscribe(URL_EVENT) { topicMessage ->
                Timber.e("$URL_EVENT, msg = ${topicMessage.payload}")
            }
            //大厅赔率频道
            val hallDisposable: Disposable? = stompClient.subscribe(URL_HALL) { topicMessage ->
                Timber.e("$URL_HALL, msg = ${topicMessage.payload}")
            }
            mCompositeDisposable?.add(lifecycleDisposable)
            mCompositeDisposable?.add(privateDisposable!!)
            mCompositeDisposable?.add(eventDisposable!!)
            mCompositeDisposable?.add(allDisposable!!)
            mCompositeDisposable?.add(hallDisposable!!)
            /*
                            stompClient.setPathMatcher { path, msg ->

                                Timber.e("path = $path")
                            }
                            */
            stompClient.connect(mHeader)
        }

        //        }catch (e: Exception) {
        //            e.printStackTrace()
        //            reconnect()
        //        }
    }

    private fun sendMessageToActivity(channel: String, message: String) {
        val bundle = Bundle()
        bundle.putString("channel", channel)
        bundle.putString("serverMessage", message)
        val intent = Intent(SERVICE_SEND_DATA)
        intent.putExtras(bundle)
        sendBroadcast(intent)
    }

    private fun reconnect() {
        reconnectCount++
        connect()
    }

    //關閉所有連線通道，釋放資源
    private fun disconnect() {
        mCompositeDisposable?.dispose()
        mCompositeDisposable = null

        mStompClient?.disconnect()
        mStompClient = null

        //        mRoomInfoList = mutableMapOf()
    }

    @SuppressLint("CheckResult")
    private fun StompClient.subscribe(url: String, respond: (StompMessage) -> Unit): Disposable? {

        return this.topic(url, mHeader).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(
            { topicMessage ->
                Timber.e(">>> $url 訂閱成功")
                respond(topicMessage) //TODO Cheryl: 如果沒有要在service處理的事, 可以刪掉這行
                sendMessageToActivity(url, topicMessage.payload)
            },
            { throwable ->
                Timber.e("訂閱失敗 : $throwable")
            })
    }

    @SuppressLint("CheckResult") fun sendMessage(url: String, content: String) {
        Timber.e("start sending message to server")
        val sendHeader = mHeader.toMutableList().apply {
            this.add(StompHeader(StompHeader.DESTINATION, url))
        }

        mStompClient?.send(StompMessage(StompCommand.SEND, sendHeader, content))?.compose(
            applySchedulers())?.subscribe({
                                                      Timber.e("sending message to server succeed!!!")
                                                  }, { throwable ->
                                                      Timber.e("傳送訊息失敗 ==> $throwable")
                                                      reconnect()
                                                  })
    }

    fun subScribeMatch() {
        Timber.e("subScribeMatch")
        //        val eventDisposable: Disposable? = mStompClient?.subscribe(URL_EVENT) { topicMessage ->
        //            Timber.e(">>> msg = ${topicMessage.payload}")
        //        }
        //        mCompositeDisposable?.add(eventDisposable!!)

    }

    private fun applySchedulers(): CompletableTransformer {
        return CompletableTransformer { upstream ->
            upstream.unsubscribeOn(Schedulers.newThread()).subscribeOn(Schedulers.io()).observeOn(
                AndroidSchedulers.mainThread())
        }
    }

    private fun resetSubscriptions() {
        mCompositeDisposable?.dispose()
        mCompositeDisposable = CompositeDisposable()
    }

    private fun sendPing() {

    }
}

