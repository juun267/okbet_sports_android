package org.cxct.sportlottery.ui.component

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebViewClient
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.dialog_bottom_sheet_webview.*
import kotlinx.android.synthetic.main.dialog_bottom_sheet_webview.view.*
import kotlinx.android.synthetic.main.view_toolbar_live.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.detail.MatchOdd
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.util.LanguageManager

@SuppressLint("SetJavaScriptEnabled")
class LiveViewToolbar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : LinearLayout(context, attrs, defStyle) {

    private val typedArray by lazy { context.theme.obtainStyledAttributes(attrs, R.styleable.CalendarBottomSheetStyle, 0, 0) }
    private val bottomSheetLayout by lazy { typedArray.getResourceId(R.styleable.CalendarBottomSheetStyle_calendarLayout, R.layout.dialog_bottom_sheet_webview) }
    private val bottomSheetView by lazy { LayoutInflater.from(context).inflate(bottomSheetLayout, null) }
    private val webBottomSheet: BottomSheetDialog by lazy { BottomSheetDialog(context) }

    private var nodeMediaManager: NodeMediaManager? = null
    private var mStreamUrl: String = ""

    lateinit var matchOdd: MatchOdd

    interface LiveToolBarListener {
        fun onExpand()
    }

    private var liveToolBarListener: LiveToolBarListener? = null

    private var nodeMediaListener: NodeMediaManager.NodeMediaListener = object : NodeMediaManager.NodeMediaListener {
        override fun streamLoading() {
            liveLoading()
        }

        override fun isLiveShowing(isShowing: Boolean) {
            showLiveView(isShowing)
        }

    }

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
                    nodeMediaManager?.nodeMediaReload()
                }
                false -> {
                    switchLiveView(true)
                }
            }
        }

        iv_statistics.setOnClickListener {
            setTitle(context.getString(R.string.statistics_title))
            loadBottomSheetUrl(matchOdd)
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
                liveToolBarListener?.onExpand()
                if (mStreamUrl.isNotEmpty()) nodeMediaManager?.nodeMediaStart()
            }
            false -> {
                nodeMediaManager?.nodeMediaStop()

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

    fun setupNodeMediaPlayer(eventListener: NodeMediaManager.LiveEventListener) {
        nodeMediaManager = NodeMediaManager(eventListener, nodeMediaListener)
    }

    fun setupPlayerControl(show: Boolean) {
        switchLiveView(show)
        iv_play.isVisible = show
        iv_arrow.isVisible = show
    }

    fun liveLoading() {
        node_player.visibility = View.GONE
        iv_live_status.visibility = View.VISIBLE
        iv_live_status.setImageResource(R.drawable.img_stream_loading)
    }

    fun showLiveView(showLive: Boolean) {
        node_player.isVisible = showLive
        iv_live_status.isVisible = !showLive
        iv_live_status.setImageResource(R.drawable.img_no_live)
    }

    fun setupLiveUrl(streamUrl: String) {
        mStreamUrl = streamUrl
        nodeMediaManager?.initNodeMediaPlayer(context, node_player, streamUrl)
        nodeMediaManager?.nodeMediaStart()
    }

    fun loadBottomSheetUrl(matchOdd: MatchOdd) {
        bottomSheetView.bottom_sheet_web_view.loadUrl(
            sConfigData?.analysisUrl?.replace("{lang}", LanguageManager.getSelectLanguage(context).key)
                ?.replace("{eventId}", matchOdd.matchInfo.id)
        )
    }

    fun setTitle(title: String) {
        bottomSheetView.sheet_tv_title.text = title
    }

    fun setupToolBarListener(listener: LiveToolBarListener) {
        liveToolBarListener = listener
    }

    private fun setupBottomSheetBehaviour() {
        val root: View? = webBottomSheet.delegate.findViewById(R.id.design_bottom_sheet)
        root?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
        BottomSheetBehavior.from(root as View).isDraggable = false
    }

    fun startNodeMediaPlayer() {
        nodeMediaManager?.nodeMediaStart()
    }

    fun stopNodeMediaPlayer() {
        nodeMediaManager?.nodeMediaStop()
    }

    fun releaseNodeMediaPlayer() {
        nodeMediaManager?.nodeMediaRelease()
    }

}