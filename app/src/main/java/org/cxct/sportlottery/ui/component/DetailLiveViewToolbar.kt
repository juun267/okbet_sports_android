package org.cxct.sportlottery.ui.component

import android.annotation.SuppressLint
import android.content.Context
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
import androidx.core.view.isVisible
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.upstream.HttpDataSource
import kotlinx.android.synthetic.main.view_toolbar_detail_live.view.*
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.util.LiveUtil
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
        LIVE, ANIMATION
    }

    private var mIsLive = false
        set(value) {
            field = value
            checkControlBarVisibility()
        }
    private var mStreamUrl: String? = null
        set(value) {
            if (value.isNullOrEmpty()) return
            field = value
        }
    private var newestUrl: Boolean = false
    private var isLogin: Boolean = false
    var gameType: GameType? = null
    private var mMatchId: String? = null
    private var mEventId: String? = null //動畫Id
        set(value) {
            if (value.isNullOrEmpty()) return
            field = value
            checkControlBarVisibility()
        }
    private var mTrackerUrl: String = ""
        set(value) {
            if (field != value) {
                field = value
                if (iv_animation.isSelected) {
                    if (isLogin) {
                        openWebView()
                    } else {
                        setWebViewHeight()
                        setupNotLogin()
                    }
                }
            }
        }

    private var mLiveShowTag = true

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

    var lastLiveType: LiveType? = null

    private var animationLoadFinish = false


    interface LiveToolBarListener {
        fun getLiveInfo(newestUrl: Boolean = false)
    }

    private var liveToolBarListener: LiveToolBarListener? = null

    init {
        val view =
            LayoutInflater.from(context).inflate(R.layout.view_toolbar_detail_live, this, false)
        addView(view).apply {
            expand_layout.collapse(false)
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

    fun showVideo() {
        iv_play.performClick()
    }

    fun showAnime() {
        iv_animation.performClick()
    }

    private fun initOnclick() {
        iv_play.setOnClickListener {
            lastLiveType = LiveType.LIVE
            if (!iv_play.isSelected) {
                setLiveViewHeight()
                iv_play.isSelected = true
                if (iv_animation.isSelected) {
                    if (isLogin) {
                        hideWebView()
                        switchLiveView(true)
                    } else {
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
                    if (isLogin) {
                        switchLiveView(true)
                    } else {
                        checkExpandLayoutStatus()
                    }
                }
            }
        }

        iv_animation.setOnClickListener {
            if (!iv_animation.isSelected) {
                setWebViewHeight()
                if (isLogin) {
                    openWebView()
                    if (iv_play.isSelected) switchLiveView(false)
                } else {
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
                    when (lastLiveType) {
                        LiveType.LIVE -> {
                            switchLiveView(true)
                        }
                        LiveType.ANIMATION -> {
                            iv_animation.isSelected = true
                            if (isLogin) {
                                openWebView()
                            } else {
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
                mLiveShowTag = true
                iv_play.isSelected = true
                lastLiveType = LiveType.LIVE
                checkExpandLayoutStatus()
                liveToolBarListener?.getLiveInfo()
                if (!mStreamUrl.isNullOrEmpty()) {
                    startPlayer(mMatchId, mEventId, mStreamUrl, isLogin)
                }
            }
            false -> {
                mLiveShowTag = false
                stopPlayer()
                iv_play.isSelected = false
                checkExpandLayoutStatus()
            }
        }
    }

    private fun ImageView.setLiveImxg() {
        when (gameType) {
            GameType.FT -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_soccer_unselected)
            GameType.BK -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_basketball_unselected)
            GameType.TN -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_tennis_unselected)
            GameType.VB -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_volleyball_unselected)
            GameType.BM -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_badminton_unselected)
            GameType.TT -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_pingpong_unselected)
            GameType.IH -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_icehockey_unselected)
            GameType.BX -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_boxing_unselected)
            GameType.CB -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_billiards_unselected)
            GameType.CK -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_cricket_unselected)
            GameType.BB -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_baseball_unselected)
            GameType.RB -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_rugby_unselected)
            GameType.AFT -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_football_unselected)
            GameType.MR -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_racing_unselected)
            GameType.GF -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_golf_unselected)
            GameType.ES -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_esport_unselected)
        }
    }

    private fun setAnimationImgIcon(isOn: Boolean) {
        if (isOn) {
            when (gameType) {
                GameType.FT -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_soccer_selected)
                GameType.BK -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_basketball_selected)
                GameType.TN -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_tennis_selected)
                GameType.VB -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_volleyball_selected)
                GameType.BM -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_badminton_selected)
                GameType.TT -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_pingpong_selected)
                GameType.IH -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_icehockey_selected)
                GameType.BX -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_boxing_selected)
                GameType.CB -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_billiards_selected)
                GameType.CK -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_cricket_selected)
                GameType.BB -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_baseball_selected)
                GameType.RB -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_rugby_selected)
                GameType.AFT -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_football_selected)
                GameType.MR -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_racing_selected)
                GameType.GF -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_golf_selected)
                GameType.ES -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_esport_selected)
            }
        } else {
            when (gameType) {
                GameType.FT -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_soccer_unselected)
                GameType.BK -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_basketball_unselected)
                GameType.TN -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_tennis_unselected)
                GameType.VB -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_volleyball_unselected)
                GameType.BM -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_badminton_unselected)
                GameType.TT -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_pingpong_unselected)
                GameType.IH -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_icehockey_unselected)
                GameType.BX -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_boxing_unselected)
                GameType.CB -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_billiards_unselected)
                GameType.CK -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_cricket_unselected)
                GameType.BB -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_baseball_unselected)
                GameType.RB -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_rugby_unselected)
                GameType.AFT -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_football_unselected)
                GameType.MR -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_racing_unselected)
                GameType.GF -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_golf_unselected)
                GameType.ES -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_esport_unselected)
            }
        }
    }

    /**
     * 設置為直播高度
     */
    private fun setLiveViewHeight() {
        web_view_layout.layoutParams = FrameLayout.LayoutParams(
            MetricsUtil.getScreenWidth(),
            resources.getDimensionPixelSize(R.dimen.live_player_height)
        )
        iv_live_status.scaleType = ImageView.ScaleType.FIT_XY
    }

    /**
     * 檢查當前是否需要展開賽事直播、動畫Layout
     */
    private fun checkExpandLayoutStatus() {
        if (iv_play.isSelected || iv_animation.isSelected) {
            expand_layout.expand()
        } else {
            expand_layout.collapse()
        }
    }

    fun setupPlayerControl(show: Boolean) {
        mIsLive = show
        if (mLiveShowTag && lastLiveType == LiveType.LIVE) {
            setLiveViewHeight()
            switchLiveView(show)
        }

        checkControlBarVisibility()
    }

    fun setupNotLogin() {
        checkExpandLayoutStatus()
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

    fun showLiveView(showLive: Boolean) {
        player_view.isVisible = showLive
        iv_live_status.isVisible = !showLive
        iv_live_status.setImageResource(R.drawable.bg_no_play)
        tvStatus.text = context.getString(R.string.text_cant_play)
        tvStatus.isVisible = !showLive
    }

    fun setupToolBarListener(listener: LiveToolBarListener) {
        liveToolBarListener = listener
    }


    fun getExoPlayer(): SimpleExoPlayer? {
        return exoPlayer
    }

    private fun initializePlayer(streamUrl: String?) {
        streamUrl?.let {
            iv_play.isSelected = true
            iv_animation.isSelected = false
            iv_animation.setLiveImxg()
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

    fun startPlayer(matchId: String?, eventId: String?, streamUrl: String?, isLogin: Boolean) {
        mMatchId = matchId
        if (MultiLanguagesApplication.getInstance()?.getGameDetailAnimationNeedShow() == true) {
            mEventId = eventId
        } else {
            mEventId = null
        }
        mStreamUrl = streamUrl
        if (isLogin) {
            if (lastLiveType == LiveType.LIVE) {
                initializePlayer(streamUrl)
            }
        } else {
            setupNotLogin()
        }
    }

    fun setupTrackerUrl(trackerUrl: String?) {
        mTrackerUrl = trackerUrl ?: ""
    }

    fun stopPlayer() {
        releasePlayer()
    }

    fun setUnLiveState() {
        expand_layout.visibility = View.GONE
        iv_play.visibility = View.GONE
        iv_animation.visibility = View.GONE
        iv_arrow.visibility = View.GONE

        checkControlBarVisibility()
    }

    fun setLiveState() {
        expand_layout.visibility = View.VISIBLE
        iv_play.visibility = View.VISIBLE
        iv_animation.visibility = View.VISIBLE
        iv_arrow.visibility = View.VISIBLE

        checkControlBarVisibility()
    }

    //region 賽事動畫
    private fun openWebView() {
        iv_animation.isSelected = true
        lastLiveType = LiveType.ANIMATION
//        setLivePlayImg()
        web_view.isVisible = true
        setAnimationImgIcon(true)
        player_view.isVisible = false

        web_view.settings.apply {
            javaScriptEnabled = true
            useWideViewPort = true
            displayZoomControls = false
            textZoom = 100
        }
        web_view.setInitialScale(25)
        web_view.setWebViewCommonBackgroundColor()
        web_view.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?,
            ): Boolean {
                view?.loadUrl(mTrackerUrl)
                return true
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                setWebViewHeight()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                animationLoadFinish = true
            }
        }
        if (!animationLoadFinish) {
            web_view.loadUrl(mTrackerUrl)
        } else {
            web_view.onResume()
            setWebViewHeight()
        }
        checkExpandLayoutStatus()
    }

    private fun hideWebView() {
        iv_animation.isSelected = false
        web_view.isVisible = false
        web_view.onPause()
        checkExpandLayoutStatus()
        setAnimationImgIcon(false)
    }

    /**
     * 設置WebView高度
     */
    private fun setWebViewHeight() {
        val screenWidth = MetricsUtil.getScreenWidth()
        web_view_layout.layoutParams = FrameLayout.LayoutParams(
            screenWidth,
            LiveUtil.getAnimationHeightFromWidth(screenWidth).toInt()
        )
        iv_live_status.scaleType = ImageView.ScaleType.CENTER_CROP
    }
    //endregion


    fun initLiveType(hasStream: Boolean, hasAnimation: Boolean) {
        if (lastLiveType != null) return
        when {
            hasStream -> {
                lastLiveType = LiveType.LIVE
            }
            hasAnimation -> {
                lastLiveType = LiveType.ANIMATION
                iv_animation.performClick()
            }

        }
    }

    /**
     * 檢查直播、動畫、數據統計列是否需要顯示
     * 若該列沒有任何圖示顯示則隱藏該列
     */
    private fun checkControlBarVisibility() {
        iv_play.visibility = if (!mIsLive) View.GONE else View.VISIBLE
        iv_animation.visibility = if (mEventId.isNullOrEmpty()) View.GONE else View.VISIBLE
        iv_arrow.visibility =
            if (!iv_play.isVisible && !iv_animation.isVisible) View.GONE else View.VISIBLE
        cl_control.visibility =
            if (!iv_play.isVisible && !iv_animation.isVisible) View.GONE else View.VISIBLE
    }
}