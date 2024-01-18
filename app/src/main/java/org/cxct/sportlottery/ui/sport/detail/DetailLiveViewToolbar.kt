package org.cxct.sportlottery.ui.sport.detail

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
import org.cxct.sportlottery.common.extentions.clickDelay
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.setViewGone
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.common.loading.Gloading
import org.cxct.sportlottery.databinding.ViewToolbarDetailLiveBinding
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.OKVideoPlayer
import org.cxct.sportlottery.view.webView.OkWebChromeClient
import org.cxct.sportlottery.view.webView.OkWebView
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

    private val viewBinding: ViewToolbarDetailLiveBinding
    private var videoWebView: OkWebView? = null
    private lateinit var livePlayer: OKVideoPlayer
    private val loading: Gloading.Holder

    interface LiveToolBarListener {
        fun onFullScreen(fullScreen: Boolean)
        fun onClose()
    }

    private var liveToolBarListener: LiveToolBarListener? = null

    init {
        setBackgroundResource(R.color.color_000000)
        viewBinding = ViewToolbarDetailLiveBinding.inflate(LayoutInflater.from(context), this)
        loading = Gloading.wrapView(viewBinding.ivLiveStatus)
        initOnclick()
    }

    private fun setLivePlayerEnable(url: String?) {
        if (::livePlayer.isInitialized) {
            livePlayer.visible()
        } else {
            livePlayer = OKVideoPlayer(context)
            addView(livePlayer, 0, LayoutParams(-1, -1))
        }
        initializePlayer(url, livePlayer)
    }
    private fun disableLivePlayer() {
        if (::livePlayer.isInitialized) {
            livePlayer.onVideoPause()
            livePlayer.gone()
        }
    }

    private fun releasePlayer() {
        if (::livePlayer.isInitialized) {
            livePlayer.release()
        }
    }

    private fun setVideWebViewEnable(url: String): OkWebView {
        disableLivePlayer()
        loading.showLoading()
        viewBinding.run { setViewGone(tvStatus, ivLiveSound, ivFullscreen) }

        if (videoWebView == null) {
            videoWebView = OkWebView(context)
            videoWebView!!.setBackgroundResource(R.color.color_025BE8)
            addView(videoWebView, 0, LayoutParams(-1, -1))
        } else {
            videoWebView!!.visible()
        }
        openWebView(videoWebView!!, url)
        return videoWebView!!
    }

    private fun disableVideoWebView() {
        videoWebView?.gone()
    }

    private fun resetPlayHeight() {
        if (!::livePlayer.isInitialized) {
            return
        }
        var height = ViewGroup.LayoutParams.MATCH_PARENT
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            //视频播放器比例，56.25%，来自H5
            livePlayer.layoutParams.height = MetricsUtil.getScreenWidth() * 720 / 1280
        }
        setPlayViewHeight(livePlayer, height)
    }

    private fun resetVideoWebViewHeight(webView: OkWebView) {
        var height = ViewGroup.LayoutParams.MATCH_PARENT
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            //视频播放器比例，56.25%，来自H5
            height = (MetricsUtil.getScreenWidth() * 0.5625f).toInt()
        }
        setPlayViewHeight(webView, height)
    }

    private fun resetAnimWebViewHeight(webView: OkWebView) {
        var height = ViewGroup.LayoutParams.MATCH_PARENT
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            height = LiveUtil.getAnimationHeightFromWidth(MetricsUtil.getScreenWidth()).toInt()
        }
        setPlayViewHeight(webView, height)
    }

    private inline fun setPlayViewHeight(view: View, height: Int) {
        view.layoutParams.height = height
        loading.wrapper.layoutParams.height = height
        loading.wrapper.layoutParams = loading.wrapper.layoutParams
    }

    fun showLive() {
        curType = LIVE
        setLivePlayerEnable(liveUrl)
        disableVideoWebView()
        resetPlayHeight()

        loading.showLoadSuccess()
        viewBinding.ivLiveSound.visible()
        viewBinding.run { setViewGone(tvStatus, ivFullscreen) }
        liveUrl?.let { LogUtil.d(it) }
    }

    fun showVideo() {
        curType = VIDEO
        resetVideoWebViewHeight(setVideWebViewEnable(videoUrl!!))
    }

    fun showAnime() {
        curType = ANIMATION
        resetAnimWebViewHeight(setVideWebViewEnable(animeUrl!!))
    }

    private fun initOnclick() = viewBinding.run {
        ivLiveClose.clickDelay {
            stopPlay()
            liveToolBarListener?.let {
                it.onClose()
            }
        }
        ivFullscreen.clickDelay {
            showFullScreen(!isFullScreen)
            if (isFullScreen) {//全屏
                liveToolBarListener?.onFullScreen(true)
            } else {
                liveToolBarListener?.onFullScreen(false)
            }
        }
    }


    fun setupToolBarListener(listener: LiveToolBarListener) {
        liveToolBarListener = listener
    }


    private fun initializePlayer(streamUrl: String?, playerView: OKVideoPlayer) {
        if (streamUrl == null) {
            return
        }
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



    private var playRUL: String? = null
    //region 賽事動畫
    private fun openWebView(webView: OkWebView, videoURL: String) {
        playRUL = videoURL
        webView.okWebChromeClient = OkWebChromeClient()
        webView.okWebViewClient = object : OkWebViewClient(object : WebViewCallBack {
            override fun onError() { }
            override fun pageStarted(view: View?, url: String?) {}

            override fun pageFinished(view: View?, url: String?) {
                if (url == playRUL) {
                    webView.visible()
                    loading.showLoadSuccess()
                }
                view?.measure(0, 0)

            }

        }) {
            override fun shouldOverrideUrlLoading( view: WebView?, request: WebResourceRequest?): Boolean {
                if (curType == VIDEO) {
                    webView.loadUrl(videoUrl!!)
                } else if (curType == ANIMATION) {
                    webView.loadUrl(animeUrl!!)
                }
                return false
            }
        }

        webView.loadUrl(videoURL)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        if (isVisible) setWebViewHeight()
    }


    fun stopPlay() {
        playRUL = null
        videoWebView?.loadData("x", null, null)
        if (::livePlayer.isInitialized && livePlayer.isVisible) {
            livePlayer.onVideoPause()
        }
    }

    fun release() {
        stopPlay()
        releasePlayer()
        videoWebView?.let {
            it.stopLoading()
            it.clearCache(true)
        }
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
            LIVE -> resetPlayHeight()
            VIDEO -> videoWebView?.let { resetVideoWebViewHeight(it) }
            ANIMATION -> videoWebView?.let { resetAnimWebViewHeight(it) }

            else -> {}
        }
    }


}