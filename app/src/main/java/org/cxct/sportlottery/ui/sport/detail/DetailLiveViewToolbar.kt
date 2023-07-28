package org.cxct.sportlottery.ui.sport.detail

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.shuyu.gsyvideoplayer.GSYVideoManager
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
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
) : FrameLayout(context, attrs, defStyle) {

    private val LIVE = 100
    private val VIDEO = 200
    private val ANIMATION = 300

    var curType: Int = 0
    var liveUrl: String? = null
    var videoUrl: String? = null
    var animeUrl: String? = null
    var isFullScreen = false

    private var animationLoadFinish = false
    private val viewBinding: ViewToolbarDetailLiveBinding

    interface LiveToolBarListener {
        fun onFullScreen(fullScreen: Boolean)
        fun onTabClick(position: Int)
        fun onClose()
    }

    private var liveToolBarListener: LiveToolBarListener? = null

    init {
        setBackgroundResource(R.color.color_000000)
        viewBinding = ViewToolbarDetailLiveBinding.inflate(LayoutInflater.from(context), this)
        initOnclick()
    }

    fun showLive() {
        curType = LIVE
        viewBinding.ivFullscreen.gone()
        showPlayView()
        switchPlayView(true)
        setWebViewHeight()
        liveToolBarListener?.onTabClick(0)

    }

    fun showVideo() {
        curType = VIDEO
        switchPlayView(false)
        showPlayView()
        setWebViewHeight()
        viewBinding.ivFullscreen.gone()
        openWebView()
        liveToolBarListener?.onTabClick(1)
    }

    fun showAnime() {
        curType = ANIMATION
        switchPlayView(false)
        showPlayView()
        setWebViewHeight()
        viewBinding.ivFullscreen.gone()
        openWebView()
        liveToolBarListener?.onTabClick(2)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initOnclick() = viewBinding.run {
        ivLiveClose.setOnClickListener {
            release()
            liveToolBarListener?.let {
                it.onClose()
            }
        }
        ivFullscreen.setOnClickListener {
            showFullScreen(!isFullScreen)
            if (isFullScreen) {//全屏
                liveToolBarListener?.onFullScreen(true)
            } else {
                liveToolBarListener?.onFullScreen(false)
            }
        }
    }


    private fun switchPlayView(open: Boolean) {
        if (open) {
            startPlayer()
        } else {
            stopPlayer()
        }
    }


    private fun showPlayView() = viewBinding.run {
        when (curType) {
            LIVE -> {
                setViewVisible(playerView, ivLiveSound)
                setViewGone(webView, ivLiveStatus, tvStatus)
                liveUrl?.let { LogUtil.d(it) }
                ivLiveStatus.setImageResource(R.drawable.bg_no_play)
            }

            VIDEO -> {
                setViewVisible(viewBinding.webView)
                setViewGone(playerView, ivLiveStatus, tvStatus, ivLiveSound)
//                iv_live.isVisible = !liveUrl.isNullOrEmpty()
//                iv_live_status.setImageResource(R.drawable.bg_no_play)
//                iv_animation.isVisible = !animeUrl.isNullOrEmpty()
            }

            ANIMATION -> {
                setViewVisible(viewBinding.webView)
                setViewGone(playerView, ivLiveStatus, ivLiveSound, tvStatus)
                ivLiveStatus.setImageResource(R.drawable.bg_no_play)
//                iv_live.isVisible = !liveUrl.isNullOrEmpty()
//                iv_video.isVisible = !videoUrl.isNullOrEmpty()
            }
        }
    }

    fun setupToolBarListener(listener: LiveToolBarListener) {
        liveToolBarListener = listener
    }


    private fun initializePlayer(streamUrl: String?) {
        if (streamUrl == null) {
            return
        }
        val playerView = viewBinding.playerView
        val ivLiveSound = viewBinding.ivLiveSound
        LogUtil.d("streamUrl=$streamUrl")
        ivLiveSound.isSelected = true
        GSYVideoManager.instance().isNeedMute = !ivLiveSound.isSelected
        ivLiveSound.setOnClickListener {
            it.isSelected = !it.isSelected
            GSYVideoManager.instance().isNeedMute = !ivLiveSound.isSelected
        }
        playerView.showTranBar(true)
        playerView.setOnOkListener(object : OKVideoPlayer.OnOkListener {
            override fun onStartPrepared() {

            }

            override fun onPrepared() {
                playerView.layoutParams.apply {
                    LogUtil.d(playerView.currentVideoWidth.toString() + "," + playerView.currentVideoHeight)
                    if (playerView.currentVideoWidth > 0) {
                        height = playerView.width * playerView.currentVideoHeight / playerView.currentVideoWidth
                    }
                    playerView.layoutParams = this
                }
            }

            override fun onError() {
            }
        })
        playerView.setUp(streamUrl, true, "")
        playerView.startPlayLogic()
    }

    private fun releasePlayer() {
        viewBinding.playerView.release()
    }

    private fun startPlayer() {
        if (viewBinding.playerView.isVisible) {
            initializePlayer(liveUrl)
        }
    }

    fun stopPlayer() {
        if (viewBinding.playerView.isVisible) {
            viewBinding.playerView.onVideoPause()
        }
    }

    //region 賽事動畫
    private fun openWebView() {
//        iv_animation.isSelected = true
        viewBinding.webView.isVisible = true
        viewBinding.playerView.gone()
        viewBinding.webView.okWebChromeClient = OkWebChromeClient()
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
                if (curType == VIDEO) {
                    viewBinding.webView.loadUrl(videoUrl!!)
                } else if (curType == ANIMATION) {
                    viewBinding.webView.loadUrl(animeUrl!!)
                }
                return false
            }
        }


        if (curType == VIDEO) {
            viewBinding.webView.loadUrl(videoUrl!!)
        } else if (curType == ANIMATION) {
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
        viewBinding.webView.clearCache(true)
    }

    fun showFullScreen(fullScreen: Boolean) {
        isFullScreen = fullScreen
        viewBinding.ivFullscreen.isSelected = isFullScreen
    }

    /**
     * 設置WebView高度
     */
    private fun setWebViewHeight() = viewBinding.run {
        when (curType) {
            LIVE -> {
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    val screenWidth = MetricsUtil.getScreenWidth()
                    //视频播放器比例，56.25%，来自H5
                    playerView.layoutParams.height = screenWidth * 720 / 1280
                } else {
                    playerView.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                }
            }

            VIDEO -> {
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    val screenWidth = MetricsUtil.getScreenWidth()
                    //视频播放器比例，56.25%，来自H5
                    webView.layoutParams.height = (screenWidth * 0.5625f).toInt()
                } else {
                    webView.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                }
            }

            ANIMATION -> {
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    val screenWidth = MetricsUtil.getScreenWidth()
                    webView.layoutParams.height = LiveUtil.getAnimationHeightFromWidth(screenWidth).toInt()
                } else {
                    webView.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                }
            }

            else -> {}
        }
    }


}