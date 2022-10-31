package org.cxct.sportlottery.ui.component

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.pili.pldroid.player.*
import com.pili.pldroid.player.widget.PLVideoView
import kotlinx.android.synthetic.main.activity_detail_sport.view.*
import kotlinx.android.synthetic.main.view_toolbar_detail_live.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.util.*


@SuppressLint("SetJavaScriptEnabled")
class DetailLiveViewToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) :
    LinearLayout(context, attrs, defStyle), PLOnPreparedListener, PLOnInfoListener,
    PLOnCompletionListener, PLOnVideoSizeChangedListener, PLOnErrorListener {

    enum class LiveType {
        LIVE, VIDEO, ANIMATION
    }

    open var curType: LiveType? = null
    private var isLogin: Boolean = false
    var liveUrl: String? = null
    var videoUrl: String? = null
    var animeUrl: String? = null

    var isFullScreen = false

    private var animationLoadFinish = false

    interface LiveToolBarListener {
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
        setWebViewHeight()
        switchPlayView(true)
        startPlayer(isLogin)
    }

    fun showVideo() {
        curType = LiveType.VIDEO
        showPlayView()
        setWebViewHeight()
        iv_fullscreen.isVisible = true
        openWebView()
        switchPlayView(false)
    }

    fun showAnime() {
        curType = LiveType.ANIMATION
        showPlayView()
        setWebViewHeight()
        iv_fullscreen.isVisible = true
        openWebView()
        switchPlayView(false)
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
        web_view.setOnTouchListener { v, event ->
            onTouchScreenListener?.let {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> it.onTouchScreen()
                    MotionEvent.ACTION_UP -> it.onReleaseScreen()
                }
            }
            super.onTouchEvent(event)
        }
        player_view.setOnTouchListener { v, event ->
            onTouchScreenListener?.let {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        it.onTouchScreen()
                        it.onReleaseScreen()
                    }
                }
            }
            super.onTouchEvent(event)
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
                startPlayer(isLogin)
            }
            false -> {
                stopPlayer()
            }
        }
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
                web_view.isVisible = false
                iv_live_status.isVisible = false
                iv_live_status.setImageResource(R.drawable.bg_no_play)
                tvStatus.isVisible = false
                iv_live.isVisible = false
                LogUtil.d(videoUrl)
                iv_video.isVisible = !TextUtils.isEmpty(videoUrl)
                iv_animation.isVisible = !TextUtils.isEmpty(animeUrl)
            }
            LiveType.VIDEO -> {
                player_view.isVisible = false
                web_view.isVisible = true
                iv_live_status.isVisible = false
                iv_live_status.setImageResource(R.drawable.bg_no_play)
                tvStatus.isVisible = false
                iv_live.isVisible = !liveUrl.isNullOrEmpty()
                iv_video.isVisible = false
                iv_animation.isVisible = !animeUrl.isNullOrEmpty()
            }
            LiveType.ANIMATION -> {
                player_view.isVisible = false
                web_view.isVisible = true
                iv_live_status.isVisible = false
                iv_live_status.setImageResource(R.drawable.bg_no_play)
                tvStatus.isVisible = false
                iv_live.isVisible = !liveUrl.isNullOrEmpty()
                iv_video.isVisible = !videoUrl.isNullOrEmpty()
                iv_animation.isVisible = false
            }
        }
    }

    fun setupToolBarListener(listener: LiveToolBarListener) {
        liveToolBarListener = listener
    }


    private fun initializePlayer(streamUrl: String?) {
        streamUrl?.let {
            LogUtil.d("streamUrl=" + it)
            // 1 -> hw codec enable, 0 -> disable [recommended]
            val options = AVOptions()
            options.setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 10 * 1000)
            options.setInteger(AVOptions.KEY_SEEK_MODE, 1)
            options.setInteger(AVOptions.KEY_MEDIACODEC, AVOptions.MEDIA_CODEC_HW_DECODE)
            options.setInteger(AVOptions.KEY_LIVE_STREAMING, 1)
            options.setInteger(AVOptions.KEY_CACHE_BUFFER_DURATION, 200)
            options.setInteger(AVOptions.KEY_CACHE_BUFFER_DURATION_SPEED_ADJUST, 0)

            player_view.setAVOptions(options)
            player_view.setOnPreparedListener(this);
            player_view.setOnInfoListener(this);
            player_view.setOnCompletionListener(this);
            player_view.setOnVideoSizeChangedListener(this);
            player_view.setOnErrorListener(this);
            player_view.setDisplayAspectRatio(PLVideoView.ASPECT_RATIO_FIT_PARENT)
            player_view.setVideoPath(it)
//            mMediaController = MediaController(this, false, true)
//            mMediaController.setOnClickSpeedAdjustListener(mOnClickSpeedAdjustListener)
//            player_view.setMediaController(mMediaController)

            player_view.start();
        }
    }

    private fun releasePlayer() {
        if (player_view.isVisible) {
            player_view.stopPlayback()
        }
    }

    fun startPlayer(isLogin: Boolean) {
        if (player_view.isVisible) {
            initializePlayer(liveUrl)
        }
    }

    fun stopPlayer() {
        if (player_view.isVisible) {
            player_view.stop()
        }
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
        if (isVisible)
            setWebViewHeight()
    }

    fun release() {
        if (player_view.isVisible) {
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
            LiveType.LIVE -> {
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    val screenWidth = MetricsUtil.getScreenWidth()
                    player_view.layoutParams.apply {
                        //视频播放器比例，56.25%，来自H5
                        height = resources.getDimensionPixelSize(R.dimen.live_player_height)
                    }
                } else {
                    player_view.layoutParams.apply {
                        height = ViewGroup.LayoutParams.MATCH_PARENT
                    }
                }
            }
            LiveType.VIDEO -> {
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    val screenWidth = MetricsUtil.getScreenWidth()
                    web_view.layoutParams.apply {
                        //视频播放器比例，56.25%，来自H5
                        height = (screenWidth * 0.5625f).toInt()
                    }
                } else {
                    web_view.layoutParams.apply {
                        height = ViewGroup.LayoutParams.MATCH_PARENT
                    }
                }
            }
            LiveType.ANIMATION -> {
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    val screenWidth = MetricsUtil.getScreenWidth()
                    web_view.layoutParams.apply {
                        height = LiveUtil.getAnimationHeightFromWidth(screenWidth).toInt()
                    }
                } else {
                    web_view.layoutParams.apply {
                        height = ViewGroup.LayoutParams.MATCH_PARENT
                    }
                }
            }
        }
    }

    override fun onPrepared(p0: Int) {
        LogUtil.d("onPrepared" + p0)
    }

    override fun onInfo(p0: Int, p1: Int, p2: Any?) {

    }

    override fun onCompletion() {
        LogUtil.d("onCompletion")
    }

    override fun onVideoSizeChanged(p0: Int, p1: Int) {
        LogUtil.d("onVideoSizeChanged=" + p0 + "," + p1)
        player_view.layoutParams.apply {
            this.height = player_view.width * p1 / p0
        }
    }

    override fun onError(p0: Int, p1: Any?): Boolean {
        LogUtil.e(p0.toString() + "," + p1.toString())
//        ToastUtil.showToast(context, p0.toString() + "," + p1.toString())
        return false
    }

    private var onTouchScreenListener: OnTouchScreenListener? = null
    fun setOnTouchScreenListener(onTouchScreenListener: OnTouchScreenListener) {
        this.onTouchScreenListener = onTouchScreenListener
    }

    interface OnTouchScreenListener {
        fun onTouchScreen()
        fun onReleaseScreen()
    }
}