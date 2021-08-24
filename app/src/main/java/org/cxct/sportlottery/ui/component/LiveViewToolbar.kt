package org.cxct.sportlottery.ui.component

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.os.Message
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.LinearLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.dialog_bottom_sheet_webview.*
import kotlinx.android.synthetic.main.dialog_bottom_sheet_webview.view.*
import kotlinx.android.synthetic.main.view_toolbar_live.view.*
import org.cxct.sportlottery.BuildConfig
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

    lateinit var matchOdd: MatchOdd

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.view_toolbar_live, this, false)
        addView(view)

        try {
            initOnclick()
            setupWebView(web_view)
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
                iv_arrow.animate().rotation(0f).setDuration(100).start()
                expand_layout.collapse()
            } else {
                iv_arrow.animate().rotation(180f).setDuration(100).start()
                expand_layout.expand()
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
                        setTitle(context.getString(R.string.bottom_sheet_statistics))
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

    private fun setupWebView(webView: WebView) {
        //是否需要載入滾球動畫
        if (sConfigData?.sportAnimation.isNullOrBlank()) return

        if (BuildConfig.DEBUG) WebView.setWebContentsDebuggingEnabled(true)

        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.blockNetworkImage = false
        settings.domStorageEnabled = true //对H5支持
        settings.useWideViewPort = true //将图片调整到适合webview的大小
        settings.loadWithOverviewMode = true // 缩放至屏幕的大小
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.defaultTextEncodingName = "utf-8"
        settings.cacheMode = WebSettings.LOAD_NO_CACHE
        settings.databaseEnabled = false
        settings.setAppCacheEnabled(false)
        settings.setSupportMultipleWindows(true) //20191120 記錄問題： target=_black 允許跳轉新窗口處理

        webView.webChromeClient = object : WebChromeClient() {
            override fun onCreateWindow(
                view: WebView,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message,
            ): Boolean {
                val newWebView = WebView(view.context)
                newWebView.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                        //20191120 記錄問題： target=_black 允許跳轉新窗口處理
                        //在此处进行跳转URL的处理, 一般情况下_black需要重新打开一个页面
                        try {
                            //使用系統默認外部瀏覽器跳轉
                            val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            context.startActivity(i)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        return true
                    }
                }
                val transport = resultMsg.obj as WebView.WebViewTransport
                transport.webView = newWebView
                resultMsg.sendToTarget()
                return true
            }

            // For Android 5.0+
            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams,
            ): Boolean {
                return true
            }
        }

        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (!url.startsWith("http")) {
                    try {
                        val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        i.flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        context.startActivity(i)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    return true
                }

                view.loadUrl(url)
                return true
            }

            override fun onReceivedSslError(
                view: WebView,
                handler: SslErrorHandler,
                error: SslError,
            ) {
                //此方法是为了处理在5.0以上Https的问题，必须加上
                handler.proceed()
            }
        }

    }

    fun setWebViewUrl(matchOdd: MatchOdd) {
        if (matchOdd.matchInfo.liveVideo == 1) {
            expand_layout.expand(false)
            web_view.loadUrl(sConfigData?.liveUrl?.replace("{eventId}", matchOdd.matchInfo.id))
        } else {
            expand_layout.collapse(false)
        }
    }

    fun loadBottomSheetUrl(matchOdd: MatchOdd) {
        bottomSheetView.bottom_sheet_web_view.loadUrl(
            sConfigData?.analysisUrl?.replace("{lang}", LanguageManager.getSelectLanguage(context).key)?.replace("{eventId}", matchOdd.matchInfo.id)
        )
    }

    fun setTitle(title: String) {
        bottomSheetView.sheet_tv_title.text = title
    }

    private fun setupBottomSheetBehaviour() {
        val root: View? = webBottomSheet.delegate.findViewById(R.id.design_bottom_sheet)
        root?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
        BottomSheetBehavior.from(root as View).isDraggable = false
    }

}