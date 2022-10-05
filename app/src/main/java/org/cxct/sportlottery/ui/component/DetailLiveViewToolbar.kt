package org.cxct.sportlottery.ui.component

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.view.isVisible
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.upstream.HttpDataSource
import kotlinx.android.synthetic.main.activity_detail_sport.view.*
import kotlinx.android.synthetic.main.view_toolbar_detail_live.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.util.LiveUtil
import org.cxct.sportlottery.util.LogUtil
import org.cxct.sportlottery.util.MetricsUtil
import org.cxct.sportlottery.util.setWebViewCommonBackgroundColor
import timber.log.Timber


@SuppressLint("SetJavaScriptEnabled")
class DetailLiveViewToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) :
    LinearLayout(context, attrs, defStyle) {

    enum class LiveType {
        LIVE, VIDEO, ANIMATION
    }

    open var curType: LiveType? = null
    private var mStreamUrl: String? = null
        set(value) {
            if (value.isNullOrEmpty()) return
            field = value
        }
    private var isLogin: Boolean = false
    var liveUrl: String? = null
    var videoUrl: String? = null
    var animeUrl: String? = null

    var isFullScreen = false

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
                        showPlayView()
                    }
                    Player.STATE_BUFFERING -> {
                        // TODO 載入中的圈圈狀態
                        if (!player_view.isVisible) liveLoading()
                        Timber.i("ExoPlayer.STATE_BUFFERING     -")
                    }
                    ExoPlayer.STATE_READY -> {
                        Timber.i("ExoPlayer.STATE_READY -")
                        showPlayView()
                    }
                    Player.STATE_ENDED -> {
                        Timber.i("ExoPlayer.STATE_ENDED     -")
                        showPlayView()
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
                //重新獲取最新的直播地址
                liveToolBarListener?.getLiveInfo(true)
            }
        }
    }

    private var animationLoadFinish = false

    interface LiveToolBarListener {
        fun getLiveInfo(newestUrl: Boolean = false)
        fun onFullScreen(fullScreen: Boolean)
    }

    private var liveToolBarListener: LiveToolBarListener? = null

    init {
        val view =
            LayoutInflater.from(context).inflate(R.layout.view_toolbar_detail_live, this, false)
        addView(view).apply {
        }

        try {
            initOnclick()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun initLoginStatus(login: Boolean) {
        this.isLogin = login
    }

    fun showLive() {
        curType = LiveType.LIVE
        iv_fullscreen.isVisible = true
        showPlayView()
        switchPlayView(true)
        startPlayer(liveUrl, isLogin)
    }

    fun showVideo() {
        curType = LiveType.VIDEO
        showPlayView()
        setWebViewHeight()
//        if (isLogin) {
        iv_fullscreen.isVisible = true
        openWebView()
        switchPlayView(false)
//        } else {
//            setupNotLogin()
//        }
    }

    fun showAnime() {
        curType = LiveType.ANIMATION
        showPlayView()
        setWebViewHeight()
//        if (isLogin) {
        iv_fullscreen.isVisible = false
        openWebView()
        switchPlayView(false)
//        } else {
//            setupNotLogin()
//        }
    }

    private fun initOnclick() {
        iv_live.setOnClickListener {
            liveUrl?.let {
                showLive()
            }
        }
        iv_video.setOnClickListener {
            videoUrl?.let {
                showVideo()
            }
        }

        iv_animation.setOnClickListener {
            animeUrl?.let {
                showAnime()
            }
        }
        iv_fullscreen.setOnClickListener {
            showFullScreen(!isFullScreen)
            if (isFullScreen) {//全屏
                liveToolBarListener?.onFullScreen(true)
            } else {
                liveToolBarListener?.onFullScreen(false)
            }
        }
    }

    fun onBackPressed() {
        if (live_view_tool_bar.isFullScreen) {
            showFullScreen(false)
        } else {
            onBackPressed()
        }
    }

    private fun switchPlayView(open: Boolean) {
        when (open) {
            true -> {
                if (!mStreamUrl.isNullOrEmpty()) {
                    startPlayer(mStreamUrl, isLogin)
                }
            }
            false -> {
                stopPlayer()
            }
        }
    }

    /**
     * 設置為直播高度
     */
    private fun setLiveViewHeight() {
        web_view_layout.layoutParams = FrameLayout.LayoutParams(MetricsUtil.getScreenWidth(),
            resources.getDimensionPixelSize(R.dimen.live_player_height))
        iv_live_status.scaleType = ImageView.ScaleType.FIT_XY
    }

    fun setupNotLogin() {
        player_view.visibility = View.GONE
        web_view.visibility = View.GONE
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

    private fun showPlayView() {
        when (curType) {
            LiveType.LIVE -> {
                player_view.isVisible = true
                iv_live_status.isVisible = false
                iv_live_status.setImageResource(R.drawable.bg_no_play)
                tvStatus.isVisible = false
                iv_live.isVisible = false
                iv_video.isVisible = !videoUrl.isNullOrEmpty()
                iv_animation.isVisible = !animeUrl.isNullOrEmpty()
            }
            LiveType.VIDEO -> {
                player_view.isVisible = false
                iv_live_status.isVisible = false
                iv_live_status.setImageResource(R.drawable.bg_no_play)
                tvStatus.isVisible = false
                web_view.isVisible = true
                iv_live.isVisible = !liveUrl.isNullOrEmpty()
                iv_video.isVisible = false
                iv_animation.isVisible = !animeUrl.isNullOrEmpty()
            }
            LiveType.ANIMATION -> {
                player_view.isVisible = false
                iv_live_status.isVisible = false
                iv_live_status.setImageResource(R.drawable.bg_no_play)
                tvStatus.isVisible = false
                web_view.isVisible = true
                iv_live.isVisible = !liveUrl.isNullOrEmpty()
                iv_video.isVisible = !videoUrl.isNullOrEmpty()
                iv_animation.isVisible = false
            }
        }
    }

    fun setupToolBarListener(listener: LiveToolBarListener) {
        liveToolBarListener = listener
    }


    fun getExoPlayer(): SimpleExoPlayer? {
        return exoPlayer
    }

    private fun initializePlayer(streamUrl: String?) {
        streamUrl?.let {
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
            this@DetailLiveViewToolbar.playWhenReady = this.playWhenReady
            removeListener(playbackStateListener)
            release()
        }
        exoPlayer = null
    }

    fun startPlayer(streamUrl: String?, isLogin: Boolean) {
        mStreamUrl = streamUrl
        if (isLogin) {
            initializePlayer(streamUrl)
        } else {
            setupNotLogin()
        }
    }

    fun stopPlayer() {
        releasePlayer()
    }


    //region 賽事動畫
    private fun openWebView() {
        iv_animation.isSelected = true
        web_view.isVisible = true
        player_view.isVisible = false

        web_view.settings.apply {
            javaScriptEnabled = true
            useWideViewPort = true
            displayZoomControls = false
            textZoom = 100
            loadWithOverviewMode = true
        }
        web_view.setInitialScale(25)
        web_view.setWebViewCommonBackgroundColor()
        web_view.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?,
            ): Boolean {
                if (curType == LiveType.VIDEO) {
                    web_view.loadUrl(videoUrl!!)
                } else if (curType == LiveType.ANIMATION) {
                    web_view.loadUrl(animeUrl!!)
                }
                return true
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
//                setWebViewHeight()
            }

            override fun onPageFinished(view: WebView, url: String?) {
                super.onPageFinished(view, url)
                animationLoadFinish = true
                view.post {
                    view.measure(0, 0)
                    val measuredHeight = view.measuredHeight
                    LogUtil.d("measuredHeight=" + measuredHeight)
//                    web_view.minimumHeight=view.measuredHeight
                }
            }
        }
        if (curType == LiveType.VIDEO) {
            web_view.loadUrl(videoUrl!!)
        } else if (curType == LiveType.ANIMATION) {
            web_view.loadUrl(animeUrl!!)
        }
    }

    private fun hideWebView() {
        iv_animation.isSelected = false
        web_view.isVisible = false
        web_view.onPause()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)

    }

    fun release() {
        if (exoPlayer != null) {
            releasePlayer()
        }
        if (web_view.isVisible) {
            web_view.stopLoading()
            web_view.clearCache(false)
        }
    }

    fun showFullScreen(fullScreen: Boolean) {
        isFullScreen = fullScreen
        iv_fullscreen.isSelected = isFullScreen
    }

    /**
     * 設置WebView高度
     */
    private fun setWebViewHeight() {
        when (curType) {
            LiveType.LIVE, LiveType.VIDEO -> {
                //视频播放器比例，56.25%，来自H5
                val screenWidth = MetricsUtil.getScreenWidth()
                web_view.layoutParams =
                    RelativeLayout.LayoutParams(screenWidth, (screenWidth * 0.5625f).toInt())
            }
            LiveType.ANIMATION -> {
                val screenWidth = MetricsUtil.getScreenWidth()
                web_view.layoutParams = RelativeLayout.LayoutParams(screenWidth,
                    LiveUtil.getAnimationHeightFromWidth(screenWidth).toInt())
            }
        }

    }
}