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
import com.google.android.material.tabs.TabLayout
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

    lateinit var matchOdd: MatchOdd

    interface LiveToolBarListener{
        fun onExpand(expanded: Boolean)
    }

    private var liveToolBarListener: LiveToolBarListener? = null

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.view_toolbar_live, this, false)
        addView(view)

        try {
            initOnclick()
            setupBottomSheet()
            setupTab()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            typedArray.recycle()
        }

    }


    private fun initOnclick() {
        iv_arrow.setOnClickListener {
            if (expand_layout.isExpanded) {
                nodeMediaManager?.nodeMediaStop()
                iv_arrow.animate().rotation(0f).setDuration(100).start()
                expand_layout.collapse()
                liveToolBarListener?.onExpand(false)
            } else {
                iv_arrow.animate().rotation(180f).setDuration(100).start()
                expand_layout.expand()
                liveToolBarListener?.onExpand(true)
            }
        }

        expand_layout.setOnExpansionUpdateListener { _, state ->
            tab_layout.getTabAt(0)?.setIcon(
                when (state) {
                    0 -> R.drawable.ic_icon_game_schedule
                    else -> R.drawable.ic_icon_game_schedule_ec
                }
            )
        }
    }

    private fun setupTab() {
        tab_layout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {

                if (tab?.position != 0) {
                    if (!webBottomSheet.isShowing) {
                        webBottomSheet.show()
                    } else webBottomSheet.dismiss()
                }

                when (tab?.position) {
                    1 -> {
                        setTitle(context.getString(R.string.statistics_title))
                        loadBottomSheetUrl(matchOdd)
                    }
                }


            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })
    }

    private fun setupBottomSheet() {
        webBottomSheet.setContentView(bottomSheetView)
        webBottomSheet.iv_close.setOnClickListener {
            webBottomSheet.dismiss()
        }
        webBottomSheet.setOnDismissListener {
            tab_layout.getTabAt(0)?.select()
        }
        val settings: WebSettings = bottomSheetView.bottom_sheet_web_view.settings
        settings.javaScriptEnabled = true
        bottomSheetView.bottom_sheet_web_view.webViewClient = WebViewClient()

        bottomSheetView.post {
            setupBottomSheetBehaviour()
        }
    }

    fun setupNodeMediaPlayer(eventListener: NodeMediaManager.LiveEventListener){
        nodeMediaManager = NodeMediaManager(eventListener)
    }

    fun showLiveView(showLive: Boolean) {
        node_player.isVisible = showLive
        default_img.isVisible = !showLive
    }

    fun setupLiveUrl(streamUrl: String) {
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