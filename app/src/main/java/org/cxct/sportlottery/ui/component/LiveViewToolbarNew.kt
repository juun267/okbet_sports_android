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
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.dialog_bottom_sheet_webview.*
import kotlinx.android.synthetic.main.dialog_bottom_sheet_webview.view.*
import kotlinx.android.synthetic.main.view_toolbar_live.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.setWebViewCommonBackgroundColor
import timber.log.Timber


@SuppressLint("SetJavaScriptEnabled")
class LiveViewToolbarNew @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) :
    LinearLayout(context, attrs, defStyle) {

    var defaultAnimationUrl = ""

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

    private var mStreamUrl: String? = null
    private var newestUrl: Boolean = false

    private var mMatchId: String? = null
    private var mEventId: String? = null

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
                        liveLoading()
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
                            Timber.e("PlayerError = $httpError")
                            newestUrl = true
                            //重新獲取最新的直播地址
                            liveToolBarListener?.getLiveInfo(true)
                        }
                    }
                }
            }
        }
    }


    interface LiveToolBarListener {
        fun getLiveInfo(newestUrl: Boolean = false)
    }

    private var liveToolBarListener: LiveToolBarListener? = null

    var gameType: GameType? = null

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.view_toolbar_live, this, false)
        addView(view).apply {
            expand_layout.expand(false)
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


    private fun setAnimationUrl() {
        defaultAnimationUrl = "https://sports.cxsport.net/animation/?" +
                "matchId=${mMatchId}" +
                "&mode=widget" +
                "&lang=${LanguageManager.getLanguageString(context)}" +
                "&eventId=${mEventId}"
    }

    private var isPlayOpen = false
    private var isAnimationOpen = false

    private fun initOnclick() {
        iv_play.setOnClickListener {
            hideWebView()
            when (expand_layout.isExpanded && !isAnimationOpen) {
                true -> {
                    switchLiveView(false)
                    stopPlayer()
                    startPlayer(mMatchId, mEventId, mStreamUrl)
                    iv_play.setImageResource(R.drawable.ic_live_player)
                    isPlayOpen = false
                }
                false -> {
                    switchLiveView(true)
                    iv_play.setImageResource(R.drawable.ic_live_player_selected)
                    isPlayOpen = true
                }
            }
        }

        iv_animation.setOnClickListener {
            stopPlayer()
            iv_play.setImageResource(R.drawable.ic_live_player)
            when (expand_layout.isExpanded && !isPlayOpen) {
                true -> {
                    hideWebView()
                    switchLiveView(false)
                    isAnimationOpen = false
                }
                false -> {
                    openWebView()
                    switchLiveView(true)
                    isAnimationOpen = true
                }
            }

        }

        iv_statistics.setOnClickListener {
            setTitle(context.getString(R.string.statistics_title))
            loadBottomSheetUrl()
        }

        iv_arrow.setOnClickListener {
            if (expand_layout.isExpanded) {
                switchLiveView(false)
            } else {
                switchLiveView(true)
            }
        }
    }

    private fun switchLiveView(open: Boolean) {
        when (open) {
            true -> {
                expand_layout.expand()
                iv_arrow.animate().rotation(180f).setDuration(100).start()
                iv_play.isSelected = true
                liveToolBarListener?.getLiveInfo()
                if (isPlayOpen && !mStreamUrl.isNullOrEmpty()) {
                    startPlayer(mMatchId, mEventId, mStreamUrl)
                }
            }
            false -> {
                expand_layout.collapse()
                stopPlayer()
                iv_arrow.animate().rotation(0f).setDuration(100).start()
                iv_play.isSelected = false
            }
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

    fun setupPlayerControl(isPlayShow: Boolean) {
        iv_play.isVisible = isPlayShow

        isPlayOpen = isPlayShow
        isAnimationOpen = !isPlayShow

        if (isPlayShow) {
            iv_play.setImageResource(R.drawable.ic_live_player_selected)
            setAnimationImgIcon(false)
            hideWebView() //根據需求，先隱藏賽事動畫
        } else {
            setAnimationImgIcon(true)
            openWebView() //根據需求，先隱藏賽事動畫
        }
    }


    fun liveLoading() {
        player_view.visibility = View.GONE
        iv_live_status.visibility = View.VISIBLE
        iv_live_status.setImageResource(R.drawable.img_stream_loading)
    }

    fun showLiveView(showLive: Boolean) {
        player_view.isVisible = showLive
        iv_live_status.isVisible = !showLive
        iv_live_status.setImageResource(R.drawable.img_no_live)
    }

    private fun loadBottomSheetUrl() {
        mMatchId?.let {
            sConfigData?.analysisUrl?.replace(
                "{lang}",
                LanguageManager.getSelectLanguage(context).key
            )
                ?.replace("{eventId}", it)
        }?.let {
            bottomSheetView.bottom_sheet_web_view.loadUrl(
                it
            )
        }
        webBottomSheet.show()
    }

    fun setTitle(title: String) {
        bottomSheetView.sheet_tv_title.text = title
    }

    fun setupToolBarListener(listener: LiveToolBarListener) {
        liveToolBarListener = listener
    }

    private fun setupBottomSheetBehaviour() {
        val bottomSheet: FrameLayout =
            webBottomSheet.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        val layoutParams = bottomSheet.layoutParams
        if (layoutParams != null) {
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
        bottomSheet.layoutParams = layoutParams
        bottomSheetBehavior.peekHeight = resources.displayMetrics.heightPixels - 50.dp //減去最上方工具列高度
    }

    private fun openWebView() {
//        setLivePlayImg() //先留著，確定之後不用可刪
        web_view.isVisible = true
        setAnimationImgIcon(true)
        player_view.isVisible = false

        web_view.settings.apply {
            javaScriptEnabled = true
            useWideViewPort = true
            displayZoomControls = false
        }
        web_view.setInitialScale(25)
        web_view.setWebViewCommonBackgroundColor()
        web_view.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                view?.loadUrl(defaultAnimationUrl)
                return true
            }
        }
        web_view.loadUrl(defaultAnimationUrl)
    }
 //先留著，確定之後不用可刪
/*    private fun setLivePlayImg() {
        when (gameType) {
            GameType.FT -> iv_animation.setImageResource(R.drawable.ic_live_football_small)
            GameType.BK -> iv_animation.setImageResource(R.drawable.ic_live_basketball_small)
            GameType.TN -> iv_animation.setImageResource(R.drawable.ic_live_tennis_small)
            GameType.VB -> iv_animation.setImageResource(R.drawable.ic_live_volleyball_small)
            GameType.BM -> iv_animation.setImageResource(R.drawable.ic_live_badminton_small)
            GameType.TT -> iv_animation.setImageResource(R.drawable.ic_live_pingpong_small)
            GameType.IH -> iv_animation.setImageResource(R.drawable.ic_live_icehockey_small)
            GameType.BX -> iv_animation.setImageResource(R.drawable.ic_live_boxing_small)
            GameType.CB -> iv_animation.setImageResource(R.drawable.ic_live_billiards_small)
            GameType.CK -> iv_animation.setImageResource(R.drawable.ic_live_cricket_small)
            GameType.BB -> iv_animation.setImageResource(R.drawable.ic_live_baseball_small)
            GameType.RB -> iv_animation.setImageResource(R.drawable.ic_live_rugby_small)
            GameType.AFT -> iv_animation.setImageResource(R.drawable.ic_live_soccer_small)
            GameType.MR -> iv_animation.setImageResource(R.drawable.ic_live_racing_small)
            GameType.GF -> iv_animation.setImageResource(R.drawable.ic_live_golf_small)
        }
    }*/
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
                GameType.FT -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_football_unselected)
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
                GameType.AFT -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_soccer_unselected)
                GameType.MR -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_racing_unselected)
                GameType.GF -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_golf_unselected)
                GameType.ES -> iv_animation.setImageResource(R.drawable.ic_icon_game_live_esport_unselected)
            }
        }
    }


//    private fun setAnimationImgIcon(isOn: Boolean) {
//        if (isOn) iv_animation.setImageResource(R.drawable.ic_icon_game_live_soccer_selected)
//        else iv_animation.setImageResource(R.drawable.ic_icon_game_live_soccer_unselected)
//    }

    private fun hideWebView() {
        web_view.isVisible = false
        setAnimationImgIcon(false)
    }

    fun getExoPlayer(): SimpleExoPlayer? {
        return exoPlayer
    }

    private fun initializePlayer(streamUrl: String?) {
        when {
            (exoPlayer != null && !streamUrl.isNullOrEmpty()) -> {
                exoPlayer = SimpleExoPlayer.Builder(context).build().also { exoPlayer ->
                    player_view.player = exoPlayer
                    val mediaItem =
                        MediaItem.Builder().setUri(streamUrl)
                            .setMimeType(MimeTypes.APPLICATION_M3U8).build()
                    exoPlayer.setMediaItem(mediaItem)

                    exoPlayer.playWhenReady = playWhenReady
                    exoPlayer.seekTo(currentWindow, playbackPosition)
                    exoPlayer.addListener(playbackStateListener)
                    exoPlayer.prepare()
                }
            }
            else -> {
                exoPlayer?.let { player ->
                    val mediaItem =
                        MediaItem.Builder().setUri(streamUrl)
                            .setMimeType(MimeTypes.APPLICATION_M3U8).build()
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
            this@LiveViewToolbarNew.playWhenReady = this.playWhenReady
            removeListener(playbackStateListener)
            release()
        }
        exoPlayer = null
    }

    fun startPlayer(matchId: String?, eventId: String?, streamUrl: String?) {
        mMatchId = matchId
        mEventId = eventId
        mStreamUrl = streamUrl
        setAnimationUrl()
        initializePlayer(streamUrl)
    }

    fun stopPlayer() {
        releasePlayer()
    }

}