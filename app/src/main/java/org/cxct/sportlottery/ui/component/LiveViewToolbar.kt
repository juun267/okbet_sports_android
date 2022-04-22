package org.cxct.sportlottery.ui.component

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.dialog_bottom_sheet_webview.*
import kotlinx.android.synthetic.main.dialog_bottom_sheet_webview.view.*
import kotlinx.android.synthetic.main.view_toolbar_live.view.*
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.util.DisplayUtil.dp
import timber.log.Timber

@SuppressLint("SetJavaScriptEnabled")
class LiveViewToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) :
    LinearLayout(context, attrs, defStyle) {

    enum class LiveType{
        LIVE, ANIMATION
    }

    private val typedArray by lazy {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CalendarBottomSheetStyle,
            0,
            0
        )
    }
    private val bottomSheetLayout by lazy {
        typedArray.getResourceId(
            R.styleable.CalendarBottomSheetStyle_calendarLayout,
            R.layout.dialog_bottom_sheet_webview
        )
    }
    private val bottomSheetView by lazy {
        LayoutInflater.from(context).inflate(bottomSheetLayout, null)
    }
    private val webBottomSheet: BottomSheetDialog by lazy { BottomSheetDialog(context) }

//    private var mStreamUrl: String? = null
    private var newestUrl: Boolean = false
    private var isLogin:Boolean = false

    private var mMatchId: String? = null
    private var mEventId: String? = null //動畫Id
        set(value) {
            field = value
            iv_animation.visibility = if (field.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
    private var mTrackerUrl: String = ""

    //exoplayer
    private var exoPlayer: SimpleExoPlayer? = null

    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition = 0L

    private val playbackStateListener by lazy {
        object : Player.Listener {
            /**
             * 會有以下四種playback狀態
             * STATE_IDLE: 初始狀態, 播放器停止, playback failed
             * STATE_BUFFERING: 媒體讀取中
             * STATE_READY: 準備好可播放
             * STATE_ENDED: 播放結束
             */
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    ExoPlayer.STATE_IDLE -> {
                        Timber.i("ExoPlayer.STATE_IDLE      -")
                        //獲取過最新的直播地址後仍然無法播放進入暫停狀態
                        if (newestUrl)
                            showLiveView(false)
                    }
                    Player.STATE_BUFFERING -> {
                        // TODO 載入中的圈圈狀態
                        if (!player_view.isVisible) liveLoading()
                        Timber.i("ExoPlayer.STATE_BUFFERING     -")
                    }
                    ExoPlayer.STATE_READY -> {
                        Timber.i("ExoPlayer.STATE_READY -")
                        showLiveView(true)
                    }
                    Player.STATE_ENDED -> {
                        Timber.i("ExoPlayer.STATE_ENDED     -")
                        showLiveView(false)
                    }
                    else -> Timber.e("UNKNOWN_STATE             -")
                }
            }

            @SuppressLint("SwitchIntDef")
            override fun onPlayerError(error: PlaybackException) {
                if (error.cause is HttpDataSource.HttpDataSourceException) {
                    when (val httpError = error.cause) {
                        //直播地址播放連線失敗
                        is HttpDataSource.InvalidResponseCodeException -> {
//                            Timber.e("PlayerError = $httpError")
//                            newestUrl = true
//                            //重新獲取最新的直播地址
//                            liveToolBarListener?.getLiveInfo(true)
                        }
                    }
                }
                Timber.e("PlayerError = $error")
                newestUrl = true
                //重新獲取最新的直播地址
                liveToolBarListener?.getLiveInfo(true)
            }
        }
    }

    private var lastLiveType = LiveType.LIVE

    private var animationLoadFinish = false


    interface LiveToolBarListener {
        fun getLiveInfo(newestUrl: Boolean = false)
        fun showStatistics()
    }

    private var liveToolBarListener: LiveToolBarListener? = null

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.view_toolbar_live, this, false)
        addView(view).apply {
            expand_layout.collapse(false)
        }

        try {
            initOnclick()
            setupBottomSheet()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            typedArray.recycle()
        }

    }

    fun initLoginStatus(login:Boolean){
        this.isLogin = login
    }

    private fun initOnclick() {
        iv_play.setOnClickListener {
            lastLiveType = LiveType.LIVE
            if (!iv_play.isSelected) {
                iv_play.isSelected = true
                if (iv_animation.isSelected){
                    if(isLogin){
                        hideWebView()
                        switchLiveView(true)
                    }else{
                        iv_animation.isSelected = false
                        setupNotLogin()
                    }
                }
            }
            when (expand_layout.isExpanded) {
                true -> {
                    // 暫時不給他重複點擊
//                    stopPlayer()
//                    startPlayer(mMatchId, mEventId, mStreamUrl)
                }
                false -> {
                    if(isLogin){
                        switchLiveView(true)
                    }else{
                        checkExpandLayoutStatus()
                    }
                }
            }
        }

        iv_animation.setOnClickListener {
            if (!iv_animation.isSelected) {
                if(isLogin){
                    openWebView()
                    if (iv_play.isSelected) switchLiveView(false)
                }else{
                    iv_animation.isSelected = true
                    iv_play.isSelected = false
                    setupNotLogin()
                }
            }
            /*when (expand_layout.isExpanded) {
                true -> {
//                    hideWebView()//根據需求，先隱藏賽事動畫
                    switchLiveView(false)
                    isAnimationOpen = false
                }
                false -> {
//                    openWebView()//根據需求，先隱藏賽事動畫
                    switchLiveView(true)
                    isAnimationOpen = true
                }
            }*/
        }

        iv_statistics.setOnClickListener {
            liveToolBarListener?.showStatistics()
        }

        iv_arrow.setOnClickListener {
            when {
                iv_play.isSelected -> {
                    switchLiveView(false)
                }
                iv_animation.isSelected -> {
                    lastLiveType = LiveType.ANIMATION
                    hideWebView()
                }
                else -> {
                    when(lastLiveType) {
                        LiveType.LIVE -> {
                            switchLiveView(true)
                        }
                        LiveType.ANIMATION -> {
                            iv_animation.isSelected = true
                            if(isLogin){
                                openWebView()
                            }else{
                                setupNotLogin()
                            }
                        }
                    }
                }
            }
            checkExpandLayoutStatus()
        }
    }

    private fun switchLiveView(open: Boolean) {
        if (!iv_play.isVisible) return
        when (open) {
            true -> {
                iv_play.isSelected = true
                lastLiveType = LiveType.LIVE
                checkExpandLayoutStatus()
                liveToolBarListener?.getLiveInfo(true)
//                if (!mStreamUrl.isNullOrEmpty()) {
//                    startPlayer(mMatchId, mEventId, mStreamUrl, isLogin)
//                }
            }
            false -> {
                stopPlayer()
                iv_play.isSelected = false
                checkExpandLayoutStatus()
            }
        }
    }

    /**
     * 檢查當前是否需要展開賽事直播、動畫Layout
     */
    private fun checkExpandLayoutStatus() {
        if (iv_play.isSelected || iv_animation.isSelected) {
            iv_arrow.animate().rotation(180f).setDuration(100).start()
            expand_layout.expand()
        } else {
            expand_layout.collapse()
            iv_arrow.animate().rotation(0f).setDuration(100).start()
        }
    }

    private fun setupBottomSheet() {
        webBottomSheet.setContentView(bottomSheetView)
        webBottomSheet.iv_close.setOnClickListener {
            webBottomSheet.dismiss()
        }

        val settings: WebSettings = bottomSheetView.bottom_sheet_web_view.settings
        settings.javaScriptEnabled = true
        bottomSheetView.bottom_sheet_web_view.webViewClient = WebViewClient()

        bottomSheetView.post {
            setupBottomSheetBehaviour()
        }
    }

    fun setupPlayerControl(show: Boolean) {
        iv_play.isVisible = show
        iv_arrow.isVisible = show
        switchLiveView(show)
    }

    fun setupNotLogin(){
        checkExpandLayoutStatus()
        player_view.visibility = View.GONE
        iv_live_status.isVisible = true
        tvStatus.isVisible = true
        iv_live_status.setImageResource(R.drawable.bg_no_play)
        tvStatus.text = context.getString(R.string.login_notify)
    }

    fun liveLoading() {
        player_view.visibility = View.GONE
        iv_live_status.visibility = View.VISIBLE
        tvStatus.visibility = View.GONE
        iv_live_status.setImageResource(R.drawable.img_stream_loading)
    }

    fun showLiveView(showLive: Boolean) {
        player_view.isVisible = showLive
        iv_live_status.isVisible = !showLive
        iv_live_status.setImageResource(R.drawable.bg_no_play)
        tvStatus.text= context.getString(R.string.text_cant_play)
        tvStatus.isVisible = !showLive
    }

    fun setTitle(title: String) {
        bottomSheetView.sheet_tv_title.text = title
    }

    fun setupToolBarListener(listener: LiveToolBarListener) {
        liveToolBarListener = listener
    }

    private fun setupBottomSheetBehaviour() {

        val bottomSheet = webBottomSheet.findViewById<View>(R.id.design_bottom_sheet)
        bottomSheet?.let {
            setBackgroundResource(android.R.color.transparent)
            layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
            val behavior = BottomSheetBehavior.from<View>(bottomSheet)
            behavior.peekHeight = resources.displayMetrics.heightPixels - 50.dp //減去最上方工具列高度

        }

    }

    fun getExoPlayer(): SimpleExoPlayer? {
        return exoPlayer
    }

    private fun initializePlayer(streamUrl: String?) {
        streamUrl?.let {
            iv_play.isSelected = true
            iv_animation.isSelected = false
            if (exoPlayer == null) {
                exoPlayer = SimpleExoPlayer.Builder(context).build().also { exoPlayer ->
                    player_view.player = exoPlayer
                    val mediaItem =
                        MediaItem.Builder().setUri(streamUrl).build()
                    exoPlayer.setMediaItem(mediaItem)
                    exoPlayer.playWhenReady = playWhenReady
                    exoPlayer.seekTo(currentWindow, playbackPosition)
                    exoPlayer.addListener(playbackStateListener)
                    exoPlayer.prepare()
                }
            } else {
                exoPlayer?.let { player ->
                    val mediaItem =
                        MediaItem.Builder().setUri(streamUrl).build()
                    player.setMediaItem(mediaItem)
                    player.playWhenReady = playWhenReady
                    player.seekTo(currentWindow, playbackPosition)
                    player.prepare()
                }
            }
        }
    }

    private fun releasePlayer() {
        exoPlayer?.run {
            playbackPosition = this.currentPosition
            currentWindow = this.currentWindowIndex
            this@LiveViewToolbar.playWhenReady = this.playWhenReady
            removeListener(playbackStateListener)
            release()
        }
        exoPlayer = null
    }

    fun startPlayer(matchId: String?, eventId: String?, streamUrl: String?,isLogin:Boolean) {
        mMatchId = matchId
        if(MultiLanguagesApplication.getInstance()?.getGameDetailAnimationNeedShow() == true){
            mEventId = eventId
        }else{
            mEventId = null
        }
//        mStreamUrl = streamUrl
        if(isLogin){
            if (streamUrl.isNullOrEmpty()) {
                showLiveView(false)
            }
            else {
                initializePlayer(streamUrl)
            }
        }else{
            setupNotLogin()
        }
    }

    fun setupTrackerUrl(trackerUrl: String?) {
        mTrackerUrl = trackerUrl ?: ""
    }

    fun stopPlayer() {
        releasePlayer()
    }

    //region 賽事動畫
    private fun openWebView() {
        iv_animation.isSelected = true
        lastLiveType = LiveType.ANIMATION
//        setLivePlayImg()
        web_view.isVisible = true
//        setAnimationImgIcon(true)
        player_view.isVisible = false

        web_view.settings.apply {
            javaScriptEnabled = true
            useWideViewPort = true
            displayZoomControls = false
        }
        web_view.setInitialScale(25)
        web_view.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                view?.loadUrl(mTrackerUrl)
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                animationLoadFinish = true
            }
        }
        if (!animationLoadFinish)
            web_view.loadUrl(mTrackerUrl)
        else
            web_view.onResume()
        checkExpandLayoutStatus()
    }

    private fun hideWebView() {
        iv_animation.isSelected = false
        web_view.isVisible = false
        web_view.onPause()
        checkExpandLayoutStatus()
//        setAnimationImgIcon(false)
    }
    //endregion

}