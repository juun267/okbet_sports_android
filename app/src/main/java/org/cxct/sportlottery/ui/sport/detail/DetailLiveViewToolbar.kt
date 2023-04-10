package org.cxct.sportlottery.ui.sport.detail

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.shuyu.gsyvideoplayer.GSYVideoManager
import kotlinx.android.synthetic.main.activity_detail_sport.view.*
import kotlinx.android.synthetic.main.view_toolbar_detail_live.view.*
import kotlinx.android.synthetic.main.view_video_ok.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.OKVideoPlayer


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
    private var isLogin: Boolean = false
    var liveUrl: String? = null
    var videoUrl: String? = null
    var animeUrl: String? = null

    var isFullScreen = false

    private var animationLoadFinish = false

    interface LiveToolBarListener {
        fun onFullScreen(fullScreen: Boolean)
        fun onTabClick(position: Int)
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
        setWebViewHeight()
        liveToolBarListener?.onTabClick(0)

    }

    fun showVideo() {
        curType = LiveType.VIDEO
        switchPlayView(false)
        showPlayView()
        setWebViewHeight()
        iv_fullscreen.isVisible = true
        openWebView()
        liveToolBarListener?.onTabClick(1)
    }

    fun showAnime() {
        curType = LiveType.ANIMATION
        switchPlayView(false)
        showPlayView()
        setWebViewHeight()
        iv_fullscreen.isVisible = true
        openWebView()
        liveToolBarListener?.onTabClick(2)
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
        player_view.surface_container.setOnTouchListener { v, event ->
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
        player_view.rl_tran_cover.setOnTouchListener { v, event ->
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
                startPlayer()
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
                iv_live_sound.isVisible = true
                LogUtil.d(liveUrl)
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
                iv_live_sound.isVisible = false
                iv_animation.isVisible = !animeUrl.isNullOrEmpty()
            }
            LiveType.ANIMATION -> {
                player_view.isVisible = false
                web_view.isVisible = true
                iv_live_status.isVisible = false
                iv_live_sound.isVisible = false
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
            iv_live_sound.isSelected = true
            GSYVideoManager.instance().isNeedMute = !iv_live_sound.isSelected
            iv_live_sound.setOnClickListener {
                it.isSelected = !it.isSelected
                GSYVideoManager.instance().isNeedMute = !iv_live_sound.isSelected
            }
            player_view.showTranBar(true)
            player_view.setOnOkListener(object : OKVideoPlayer.OnOkListener {
                override fun onStartPrepared() {

                }

                override fun onPrepared() {
                    player_view.layoutParams.apply {
                        LogUtil.d(player_view.currentVideoWidth.toString() + "," + player_view.currentVideoHeight)
                        if (player_view.currentVideoWidth > 0) {
                            height = player_view.width * player_view.currentVideoHeight / player_view.currentVideoWidth
                        }
                        player_view.layoutParams = this
                    }
                }

                override fun onError() {
                }
            })
            player_view.setUp(it, true, "");
            player_view.startPlayLogic();
        }
    }

    private fun releasePlayer() {
        player_view.release()
    }

    fun startPlayer() {
        if (player_view.isVisible) {
            initializePlayer(liveUrl)
        }
    }

    fun stopPlayer() {
        if (player_view.isVisible) {
            player_view.onVideoPause()
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

        web_view.webChromeClient = object : WebChromeClient() {
            override fun getDefaultVideoPoster(): Bitmap? {
                return if (super.getDefaultVideoPoster() == null) {
                    BitmapFactory.decodeResource(
                        resources,
                        R.drawable.ic_video)
                } else {
                    super.getDefaultVideoPoster()
                }
            }
        }
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
                return false
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
        releasePlayer()
        web_view.stopLoading()
        web_view.clearCache(false)
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
                        height = screenWidth * 720 / 1280
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
    private var onTouchScreenListener: OnTouchScreenListener? = null
    fun setOnTouchScreenListener(onTouchScreenListener: OnTouchScreenListener) {
        this.onTouchScreenListener = onTouchScreenListener
    }

    interface OnTouchScreenListener {
        fun onTouchScreen()
        fun onReleaseScreen()
    }

}