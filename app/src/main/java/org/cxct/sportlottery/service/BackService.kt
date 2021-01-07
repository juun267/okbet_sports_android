package org.cxct.sportlottery.service

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import io.reactivex.CompletableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import org.cxct.sportlottery.repository.sLoginData
import org.cxct.sportlottery.util.HTTPsUtil
import timber.log.Timber
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompHeader
import java.util.concurrent.TimeUnit

class BackService : BaseService(){
    companion object {
        private const val URL_BASE = "https://sports.cxct.org/api"
        private const val URL_SOCKET_HOST_AND_PORT = "$URL_BASE/ws/app/im" //app连接端点,无sockjs
        private val URL_PRIVATE by lazy { "$URL_BASE/ws/notify/user/${sLoginData?.userId}" } //用户私人频道
        private val URL_EVENT by lazy { "$URL_BASE/ws/notify/event/{eventId}"} //具体赛事/赛季频道 //(普通玩法：eventId就是matchId，冠军玩法：eventId是赛季Id) //TODO Cheryl 替換變數
        private val URL_HALL by lazy { "$URL_BASE/ws/notify/hall/{gameType}/{cateMenuCode}/{eventId}" }//大厅赔率频道 //TODO Cheryl 替換變數
        private const val URL_ALL = "$URL_BASE/ws/notify/all" //全体公共频道
        private const val URL_PING = "$URL_BASE/ws/ping" //心跳检测通道 （pong消息将发往用户私人频道）


        private const val HEART_BEAT_RATE = 10 * 1000 //每隔10秒進行一次對長連線的心跳檢測
    }


    private var mStompClient: StompClient? = null
    private var mCompositeDisposable: CompositeDisposable? = null //訊息接收通道 數組
    /*
    val token by lazy {
        LoginRepository(applicationContext).token //TODO Cheryl: can i get token this way?
    }
    */
//    val loginData: LoginData = sLoginData ?: LoginData()

//    private var mCompositeDisposable: CompositeDisposable? = null //訊息接收通道 數組

    override fun onCreate() {
        super.onCreate()
//        InitSocketThread().start()

    }

    override fun onDestroy() {
        super.onDestroy()
        disconnect()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (mStompClient?.isConnected != true) {
            Timber.d("==尚未建立連線，連線開始==")
            connect()
        } else {
            Timber.d("==已建立連線，傳遞資料==")
        }

        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("CheckResult")
    fun connect() {
        try {
            val header: List<StompHeader> = listOf(StompHeader("token", sLoginData?.token))
            val headerMap = header.map { it.key to it.value }.toMap()
            val chatUrl = URL_BASE.replace("https", "wss")
            //        val url = "$chat/webchat/websocket?sid=${mChatConfigOutput?.sid ?: ""}"
            //        var socket = new SockJS(host+'/ws/web/im');

            val httpClient = HTTPsUtil.trustAllSslClient(OkHttpClient())
            mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP,
                                      chatUrl,
                                      headerMap,
                                      httpClient.newBuilder().pingInterval(40, TimeUnit.SECONDS).retryOnConnectionFailure(true).build())

            mStompClient?.let { stompClient ->
                stompClient.withServerHeartbeat(HEART_BEAT_RATE)

                stompClient.lifecycle().subscribe { lifecycleEvent: LifecycleEvent ->
                    when (lifecycleEvent.type) {
                        LifecycleEvent.Type.OPENED -> Timber.d("Stomp connection opened")
                        LifecycleEvent.Type.ERROR -> Timber.e(lifecycleEvent.exception)
                        LifecycleEvent.Type.CLOSED -> Timber.d("Stomp connection closed")
                        else -> Timber.e("Stomp connection failed")
                    }
                }


                val allDisposable: Disposable? = stompClient.topic("/notify/all", header)?.subscribe { topicMessage ->
                    Log.e(">>>", "notify/all, topicMessage.payload = ${topicMessage.payload}")
                    Timber.d(topicMessage.payload)
                }
                /*
                mStompClient?.let {
                    it.connect()

                    it.topic("/notify/all", header)?.subscribe { topicMessage ->
                        Timber.d(topicMessage.payload);
                    }
*/

                mCompositeDisposable?.dispose()
                mCompositeDisposable = CompositeDisposable()
                mCompositeDisposable?.add(allDisposable!!)

            }

            mStompClient?.connect(header)
            Timber.e("mStompClient?.isConnected = ${mStompClient?.isConnected}")

            /*
    stompClient.connect({token:token}, function(frame) {
        setConnected(true);
        console.log('Connected:' + frame);
        //监听的路径以及回调
        stompClient.subscribe('/notify/all', function(response) {
            showResponse(response.body);
        });
        ​
        //监听的路径以及回调
        stompClient.subscribe('/notify/hall/1X2/sr:match:3254', function(response) {
            console.log("room event");
            showResponse(response.body);
        });


        //监听的路径以及回调
        stompClient.subscribe('/notify/event/sr:match:3435676', function(response) {
            console.log("room event");
            showResponse(response.body);
        });
        ​
        //监听的路径以及回调
        stompClient.subscribe('/notify/user/'+userId, function(response) {
            console.log("user event");
            showResponse(response.body);
        });
    });
    */

        }catch (e: Exception) {
            e.printStackTrace()
            //TODO Reconnect
        }
    }

    //關閉所有連線通道，釋放資源
    private fun disconnect() {
//        mCompositeDisposable?.dispose()
//        mCompositeDisposable = null

        mStompClient?.disconnect()
        mStompClient = null

//        mRoomInfoList = mutableMapOf()
    }

    private fun applySchedulers(): CompletableTransformer {
        return CompletableTransformer { upstream ->
            upstream.unsubscribeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }
    }

}

