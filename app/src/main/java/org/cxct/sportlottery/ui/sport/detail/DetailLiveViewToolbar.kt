package org.cxct.sportlottery.ui.sport.detail

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.shuyu.gsyvideoplayer.GSYVideoManager
import kotlinx.android.synthetic.main.activity_detail_sport.view.*
import kotlinx.android.synthetic.main.view_toolbar_detail_live.view.*
import kotlinx.android.synthetic.main.view_video_ok.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.setViewGone
import org.cxct.sportlottery.common.extentions.setViewVisible
import org.cxct.sportlottery.databinding.ViewToolbarDetailLiveBinding
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.OKVideoPlayer
import org.cxct.sportlottery.view.webView.OkWebChromeClient
import org.cxct.sportlottery.view.webView.OkWebViewClient
import org.cxct.sportlottery.view.webView.WebViewCallBack


class DetailLiveViewToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : LinearLayout(context, attrs, defStyle) {

    enum class LiveType {
        LIVE, VIDEO, ANIMATION
    }

    private var isLogin: Boolean = false
    var curType: LiveType? = null
    var liveUrl: String? = null
    var videoUrl: String? = null
    var animeUrl: String? = null
    var isFullScreen = false

    private var animationLoadFinish = false
    private lateinit var viewBinding: ViewToolbarDetailLiveBinding

    interface LiveToolBarListener {
        fun onFullScreen(fullScreen: Boolean)
        fun onTabClick(position: Int)
    }

    private var liveToolBarListener: LiveToolBarListener? = null

    init {
        viewBinding = ViewToolbarDetailLiveBinding.inflate(LayoutInflater.from(context))
        addView(viewBinding.root)
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
        iv_fullscreen.isVisible = false
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
        iv_fullscreen.isVisible = false
        openWebView()
        liveToolBarListener?.onTabClick(1)
    }

    fun showAnime() {
        curType = LiveType.ANIMATION
        switchPlayView(false)
        showPlayView()
        setWebViewHeight()
        iv_fullscreen.isVisible = false
        openWebView()
        liveToolBarListener?.onTabClick(2)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initOnclick() {
//        iv_live.setOnClickListener {
//            liveUrl?.let {
//                showLive()
//            }
//        }
//        iv_video.setOnClickListener {
//            videoUrl?.let {
//                showVideo()
//            }
//        }
//
//        iv_animation.setOnClickListener {
//            animeUrl?.let {
//                showAnime()
//            }
//        }
        iv_fullscreen.setOnClickListener {
            showFullScreen(!isFullScreen)
            if (isFullScreen) {//全屏
                liveToolBarListener?.onFullScreen(true)
            } else {
                liveToolBarListener?.onFullScreen(false)
            }
        }
        viewBinding.webView.setOnTouchListener { v, event ->
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
        viewBinding.webView.visibility = View.GONE
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
                setViewVisible(player_view, iv_live_sound)
                setViewGone(viewBinding.webView, iv_live_status, tvStatus)
                liveUrl?.let { LogUtil.d(it) }
                iv_live_status.setImageResource(R.drawable.bg_no_play)
//                iv_video.isVisible = !TextUtils.isEmpty(videoUrl)
//                iv_animation.isVisible = !TextUtils.isEmpty(animeUrl)
            }

            LiveType.VIDEO -> {
                setViewVisible(viewBinding.webView)
                setViewGone(player_view, iv_live_status, tvStatus, iv_live_sound)
//                iv_live.isVisible = !liveUrl.isNullOrEmpty()
//                iv_live_status.setImageResource(R.drawable.bg_no_play)
//                iv_animation.isVisible = !animeUrl.isNullOrEmpty()
            }

            LiveType.ANIMATION -> {
                setViewVisible(viewBinding.webView)
                setViewGone(player_view, iv_live_status, iv_live_sound, tvStatus)
                iv_live_status.setImageResource(R.drawable.bg_no_play)
//                iv_live.isVisible = !liveUrl.isNullOrEmpty()
//                iv_video.isVisible = !videoUrl.isNullOrEmpty()
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
                            height =
                                player_view.width * player_view.currentVideoHeight / player_view.currentVideoWidth
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
//        iv_animation.isSelected = true
        viewBinding.webView.isVisible = true
        player_view.isVisible = false

        viewBinding.webView.okWebChromeClient = object: OkWebChromeClient(){



            override fun onHideCustomView() {
                super.onHideCustomView()
            }

            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                super.onShowCustomView(view, callback)
            }
        }


        viewBinding.webView.okWebViewClient = object : OkWebViewClient(object : WebViewCallBack {
            override fun pageStarted(view: View?, url: String?) {}
            override fun pageFinished(view: View?, url: String?) {
                animationLoadFinish = true
                view?.post {
                    view.measure(0, 0)
                }
            }
            override fun onError() {}
        }) {
            override fun shouldOverrideUrlLoading(
                view: WebView?, request: WebResourceRequest?
            ): Boolean {
                if (curType == LiveType.VIDEO) {
                    viewBinding.webView.loadUrl(videoUrl!!)
                } else if (curType == LiveType.ANIMATION) {
                    viewBinding.webView.loadUrl(animeUrl!!)
                }
                return false
            }
        }


        if (curType == LiveType.VIDEO) {
            viewBinding.webView.loadUrl(videoUrl!!)
        } else if (curType == LiveType.ANIMATION) {
            viewBinding.webView.loadUrl(animeUrl!!)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        if (isVisible) setWebViewHeight()
    }

    fun release() {
        releasePlayer()
        viewBinding.webView.stopLoading()
        viewBinding.webView.clearCache(false)
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
                    viewBinding.webView.layoutParams.apply {
                        //视频播放器比例，56.25%，来自H5
                        height = (screenWidth * 0.5625f).toInt()
                    }
                } else {
                    viewBinding.webView.layoutParams.apply {
                        height = ViewGroup.LayoutParams.MATCH_PARENT
                    }
                }
            }

            LiveType.ANIMATION -> {
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    val screenWidth = MetricsUtil.getScreenWidth()
                    viewBinding.webView.layoutParams.apply {
                        height = LiveUtil.getAnimationHeightFromWidth(screenWidth).toInt()
                    }
                } else {
                    viewBinding.webView.layoutParams.apply {
                        height = ViewGroup.LayoutParams.MATCH_PARENT
                    }
                }
            }

            else -> {}
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