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
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.dialog_bottom_sheet_webview.*
import kotlinx.android.synthetic.main.dialog_bottom_sheet_webview.view.*
import kotlinx.android.synthetic.main.view_toolbar_live.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.detail.MatchOdd
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.LanguageManager
import timber.log.Timber

@SuppressLint("SetJavaScriptEnabled")
class LiveViewToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) :
    LinearLayout(context, attrs, defStyle) {

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

    private fun initOnclick() {
        iv_play.setOnClickListener {
            when (expand_layout.isExpanded) {
                true -> {
                    stopPlayer()
                    startPlayer(mMatchId, mEventId, mStreamUrl)
                }
                false -> {
                    switchLiveView(true)
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

        expand_layout.setOnExpansionUpdateListener { _, state ->
            iv_play.setImageResource(
                when (state) {
                    0 -> R.drawable.ic_live_player
                    else -> R.drawable.ic_live_player_selected
                }
            )
        }
    }

    private fun switchLiveView(open: Boolean) {
        if (!iv_play.isVisible) return
        when (open) {
            true -> {
                iv_arrow.animate().rotation(180f).setDuration(100).start()
                iv_play.isSelected = true
                expand_layout.expand()
                liveToolBarListener?.getLiveInfo()
                if (!mStreamUrl.isNullOrEmpty()) {
                    startPlayer(mMatchId, mEventId, mStreamUrl)
                }
            }
            false -> {
                stopPlayer()
                iv_arrow.animate().rotation(0f).setDuration(100).start()
                iv_play.isSelected = false
                expand_layout.collapse()
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

    fun setupPlayerControl(show: Boolean) {
        switchLiveView(show)
        iv_play.isVisible = show
        iv_arrow.isVisible = show
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
        bottomSheetView.bottom_sheet_web_view.loadUrl(
            mMatchId?.let {
                sConfigData?.analysisUrl?.replace(
                    "{lang}",
                    LanguageManager.getSelectLanguage(context).key
                )
                    ?.replace("{eventId}", it)
            }
        )
        webBottomSheet.show()
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
            if (exoPlayer == null) {
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
            } else {
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
            this@LiveViewToolbar.playWhenReady = this.playWhenReady
            removeListener(playbackStateListener)
            release()
        }
        exoPlayer = null
    }

    fun startPlayer(matchId: String?, eventId: String?, streamUrl: String?) {
        mMatchId = matchId
        mEventId = eventId
        mStreamUrl = streamUrl
        initializePlayer(streamUrl)
    }

    fun stopPlayer() {
        releasePlayer()
    }

}